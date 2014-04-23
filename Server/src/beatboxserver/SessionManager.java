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
public class SessionManager {
    
    /**
     * 
     * @param databaseManager
     * @param authenticationManager 
     */
    public SessionManager(DatabaseManager databaseManager, AuthenticationManager authenticationManager) {
        dbManager = databaseManager;
        authManager = authenticationManager;
        sessionMap = new HashMap<>();
    }
    
    /**
     * 
     * @param pin
     * @param ipAddress
     * @param type
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
            try (Statement stmt = dbManager.createStatement()) {

                String query = "INSERT INTO session (ip_address, session_type, time_started) VALUES ( '" + ipAddress + "', '" + type.ordinal() + "', )";
                if (stmt.executeUpdate(query) != 1) {
                    stmt.close();
                    throw new IllegalStateException();
                }

                sessionID = stmt.getGeneratedKeys().getLong("id");

                // Insert into child table
                tableName = type.toString().toLowerCase() + "_sessions";
                stmt.executeUpdate("INSERT INTO " + tableName + " (id) VALUES ('" + sessionID + "')");

            }
        } else { 
            throw new SecurityException("Invalid pin");
        }
        
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
        
        synchronized(this) {
            try (PreparedStatement stmt = dbManager.createPreparedStatement("DELETE FROM sessions WHERE id = ?")) {
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
        String query = "SELECT id FROM sessions WHERE sessions.session_type = '?'";
        try (PreparedStatement stmt = dbManager.createPreparedStatement(query)) {
            stmt.setLong(1, sessionType);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sessions.add(rs.getLong("id"));
            }
        }
        
        synchronized(this) {
            for (long id : sessions) {
                if (sessionMap.containsKey(id)) {
                    sessionMap.get(id).sendUpdate(update);
                }
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
     * @param id
     * @retur 
     * @throws java.sql.SQLExceptionn 
     */
    /*public Session getSession(long id) throws SQLException {
    if (id < 0) {
    throw new IllegalArgumentException();
    }
    
    synchronized(this) {
    Statement stmt = dbManager.createPreparedStatement("SELECT ");
    ResultSet rs = stmt.executeQuery("SELECT sessions.id as session_id, sessions.ip_address as ip_address, session_types.type as type "
    + "FROM sessions INNER JOIN session_types ON sessions.session_type = session_types.id "
    + "WHERE sessions.id = " + id);
    
    if (rs.next()) {
    //switch (rs.getString())
    }
    
    }
    return null;
    }*/
    
    /**
     * Check if the given session is valid
     * @param sessionID
     * @param ipAddress
     * @return 
     */
    public boolean validSession(long sessionID, String ipAddress) {
        if (sessionID < 0 || ipAddress == null) {
            throw new IllegalArgumentException();
        }
        
        try {
            String query = "SELECT ip_address, time_started FROM sessions WHERE id = '?'";
            try (PreparedStatement stmt = dbManager.createPreparedStatement(query)) {
                stmt.setLong(1, sessionID);

                // Check to ensure we selected (1) row
                ResultSet rs = stmt.executeQuery();
                int size = 0;
                while (rs.next()) {
                    if (!rs.getString("ip_address").equals(ipAddress)) {
                        stmt.close();
                        return false;
                    }
                    size++;
                }

                if (size != 1) {
                    stmt.close();
                    return false;
                }
            }
        } catch (SQLException e) {
            logger.warn("Failed to authenticate session", e);
            return false;
        }
        
        return true;
    }
    
    private final Map<Long, Session> sessionMap;
    private final DatabaseManager dbManager;
    private final AuthenticationManager authManager;
    
    private final static Logger logger = LogManager.getFormatterLogger(SessionManager.class.getName());
}
