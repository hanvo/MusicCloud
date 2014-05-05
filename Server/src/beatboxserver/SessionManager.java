/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.updates.SessionUpdate;
import beatboxserver.Session.SessionType;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 *
 * @author rahmanj
 */
public final class SessionManager {
    
    /**
     * Create new {@link SessionManager} instance
     * @param databaseManager {@link DatabaseManager} for the {@link SessionManager} to use
     * @param authenticationManager {@link AuthenticatationManager} for the {@link SessionManager} to use
     */
    public SessionManager(DatabaseManager databaseManager, AuthenticationManager authenticationManager) {
        databaseMgr = databaseManager;
        authManager = authenticationManager;
        sessionMap = new HashMap<>();
    }
    
    /**
     * 
     * @param pin {@link String} pin sent by client
     * @param ipAddress {@link String} IP address of connecting client
     * @param type {@link SessionType} to create
     * @return
     * @throws SQLException
     * @throws InvocationTargetException
     */
    public Session createSession(String pin, String ipAddress, SessionType type) throws SQLException, InvocationTargetException {
        if (pin == null || type == null) {
            throw new IllegalArgumentException();
        }
        
        boolean authenticated;
        
        long sessionID;
        String tableName;
        Session session;
        
        authenticated = this.authManager.authenticate(pin);
        
        if (authenticated) {
            
            int unixTimestamp = (int)(System.currentTimeMillis() / 1000L);
            String query = "INSERT INTO sessions (ip_address, session_type, time_started, session_type) VALUES ( ?, ?, ?, ?)";
            try (PreparedStatement stmt = databaseMgr.createPreparedStatement(query)) {
                stmt.setString(1, ipAddress);
                stmt.setLong(2, type.ordinal());
                stmt.setLong(3, unixTimestamp);
                stmt.setLong(4, type.ordinal());
                
                try {
                    
                    // Begin transaction to atomically create a new session
                    databaseMgr.startTransaction();
                    
                    if (stmt.executeUpdate() != 1) {
                        throw new IllegalStateException();
                    }

                    // Get the newly created ID of the session
                    // Statment.getGeneratedKeys() doesn't work for SQLite :(
                    try (Statement statement = databaseMgr.createStatement()) {
                        ResultSet rs = statement.executeQuery("SELECT LAST_INSERT_ROWID() as id");
                        
                        if (rs.next()) {
                            sessionID = rs.getLong("id");
                        } else {
                            throw new SQLException();
                        }
                        
                        // Insert into child table
                        tableName = type.toString().toLowerCase() + "_sessions";
                        statement.executeUpdate("INSERT INTO " + tableName + " (id) VALUES ('" + sessionID + "')");
                    }
                    
                    // TODO, should rollback in event of failure

                } finally {
                    
                    // Commit the transaction
                    databaseMgr.stopTransaction();
                }

            }
        } else { 
            throw new SecurityException("Invalid pin");
        }
        
        // Create instance of session class
        String typeName = this.getClass().getPackage().getName() + "." + type.toString() + "Session";
        try {
            Class cls = Class.forName(typeName);
            Constructor ctor = cls.getDeclaredConstructor(long.class, String.class);
            if (!Session.class.isAssignableFrom(cls)) {
                throw new InvocationTargetException(new ClassCastException());
            }
            session = (Session)ctor.newInstance(sessionID, ipAddress);
            
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
        
        synchronized(this) {
            
            logger.trace("Adding session %d to the map", sessionID);
            sessionMap.put(sessionID, session);
        }
        
        return session;
    }
    
    /**
     * 
     * @param sessionID 
     * @throws SQLException
     */
    public void destroySession(long sessionID) throws SQLException {
        if (sessionID < 0) {
            throw new IllegalArgumentException();
        }
        
        logger.trace("Deleting session %d", sessionID);
        
        synchronized(this) {
            try (PreparedStatement stmt = databaseMgr.createPreparedStatement("DELETE FROM sessions WHERE id = ?")) {
                stmt.setLong(1, sessionID);
                stmt.executeUpdate();
            }
            
            if (sessionMap.containsKey(sessionID)) {
                sessionMap.remove(sessionID);
            }
        }
    }
    
    /**
     * Broadcast a given update to all the sessions of a given type
     * @param update The {@link SessionUpdate} to broadcast to the sessions
     * @param sessionType {@link long} as the database ID (ordinal) for the session type
     * @throws SQLException
     */
    public void broadcastUpdate(SessionUpdate update, long sessionType) throws SQLException {
        if (update == null) {
            throw new IllegalArgumentException();
        }
        
        List<Long> sessions = new ArrayList<>();
        
        // Get the list of sessions with the given type from the DB
        String query = "SELECT id FROM sessions WHERE sessions.session_type = ?";
        try (PreparedStatement stmt = databaseMgr.createPreparedStatement(query)) {
            stmt.setLong(1, sessionType);
            ResultSet rs = stmt.executeQuery();
            long id;
            while (rs.next()) {
                id = rs.getLong("id");
                logger.trace("Adding %d to session ID list", id);
                sessions.add(id);
            }
        }
        
        synchronized(this) {
            for (long id : sessions) {
                if (sessionMap.containsKey(id)) {
                    
                    logger.debug("Sending update to session %d", id);
                    sessionMap.get(id).sendUpdate(update);
                }
            }
        }
    }
    
    /**
     * 
     * @param update
     * @param sessionID
     */
    public void sendUpdate(SessionUpdate update, long sessionID) {
        if (update == null) {
            throw new IllegalArgumentException();
        }
        
        synchronized(this) {
            if (sessionMap.containsKey(sessionID)) {
                sessionMap.get(sessionID).sendUpdate(update);
            } else {
                logger.warn("Tried sending update to non-existant session");
            }
        }
    }
    
    /**
     * Register an update request for a given session
     * @param sessionID
     * @param chan
     */
    public void registerRequest(long sessionID, Channel chan) {
        if (chan == null) {
            throw new IllegalArgumentException();
        }
        
        if (!sessionMap.containsKey(sessionID)) {
            throw new IllegalArgumentException("No session with given ID");
        }
        
        synchronized(this) {
            Session session = sessionMap.get(sessionID);
            session.assignRequest(chan);
        }
    }
    
    
    /**
     * 
     * @return
     * @throws SQLException
     */
    public long getSessionCount() throws SQLException {
        String query = "SELECT COUNT(*) as sessions FROM sessions WHERE session_type = '" + SessionType.User.ordinal() + "'";
        try (Statement stmt = databaseMgr.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            if (rs.next()) {
                return rs.getLong("sessions");
            } else {
                throw new NoSuchElementException();
            }
        }
    }
    
    /**
     * Check if the given session is valid
     * @param sessionID {@link long} session ID to validate
     * @param ipAddress {@link String} IP address
     * @return 
     * @throws IllegalArgumentException
     */
    public boolean validSession(long sessionID, String ipAddress) {
        if (sessionID < 0 || ipAddress == null) {
            throw new IllegalArgumentException();
        }
        
        try {
            String query = "SELECT ip_address, time_started FROM sessions WHERE id = ?";
            try (PreparedStatement stmt = databaseMgr.createPreparedStatement(query)) {
                stmt.setLong(1, sessionID);

                // Check to ensure we selected (1) row
                ResultSet rs = stmt.executeQuery();
                int size = 0;
                while (rs.next()) {
                    if (!rs.getString("ip_address").equals(ipAddress)) {
                        logger.warn("Session IP address mismatch");
                        return false;
                    }
                    size++;
                }

                if (size != 1) {
                    logger.warn("Invalid number of client IDs found");
                    return false;
                }
                
                // TODO check timestamp
                
            }
        } catch (SQLException e) {
            
            logger.warn("Failed to authenticate session", e);
            return false;
        }
        
        return true;
    }
    
    private final Map<Long, Session> sessionMap;
    private final DatabaseManager databaseMgr;
    private final AuthenticationManager authManager;
    
    private final static Logger logger = LogManager.getFormatterLogger(SessionManager.class.getName());
}
