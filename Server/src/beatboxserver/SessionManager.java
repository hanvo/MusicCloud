/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.updates.ClientUpdate;
import beatboxserver.Session.SessionType;

import java.util.Map;
import java.util.HashMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;


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
        Statement stmt;
        
        long sessionID;
        String tableName;
        Session session;
        
        authenticated = this.authManager.authenticate(pin);
        
        if (authenticated) {
            stmt = dbManager.createStatement();
            
            String query = "INSERT INTO session (ip_address, session_type, time_started) VALUES (" + ipAddress + ", " + type.ordinal() + ", )";
            if (stmt.executeUpdate(query) != 1) {
                stmt.close();
                throw new IllegalStateException();
            }
            
            sessionID = stmt.getGeneratedKeys().getLong("id");
            
            // Insert into child table
            tableName = type.toString().toLowerCase() + "_sessions";
            stmt.executeUpdate("INSERT INTO " + tableName + " (id) VALUES (" + sessionID + ")");
            
            // Close the resource
            stmt.close();
        } else { 
            throw new SecurityException("Invalid pin");
        }
        
        String typeName = this.getClass().getPackage().getName() + "." + type.toString() + "Session";
        try {
            Class cls = Class.forName(typeName);
            Constructor ctor = cls.getDeclaredConstructor(long.class, String.class);
            if (!Session.class.isAssignableFrom(cls)) {
                throw new InvocationTargetException();
            }
            session = (Session)ctor.newInstance(sessionID, ipAddress);
            
        } catch (Exception e) {
            throw new InvocationTargetException();
        }
        
        return session;
    }
    
    /**
     * 
     * @param c 
     */
    public void destroySession(Session c) {
        // TODO Add database code for this
    }
    
    /**
     * 
     * @param update
     * @param type 
     */
    public void broadcastUpdate(ClientUpdate update, Class type) {
        synchronized(this) {
            
        }
    }
    
    /**
     * 
     * @param id
     * @return 
     */
    public Session getSession(long id) {
        Session c;
        
        if (id < -1) {
            throw new IllegalArgumentException();
        }
        
        synchronized(this) {
            if (sessionMap.containsKey(id)) {
                c = sessionMap.get(id);
            } else {
                c = null;
            }
        }
        return c;
    }
    
    /**
     * 
     * @param id
     * @return 
     */
    public boolean validSession(long id, String ipAddress) {
        if (id < 0 || ipAddress == null) {
            throw new IllegalArgumentException();
        }
        
        PreparedStatement stmt;
        try {
            stmt = dbManager.createPreparedStatement("SELECT (ip_address, time_started) FROM sessions WHERE id = '?'");
            stmt.setLong(1, id);
            
            // Check to ensure we selected (1) row
            ResultSet rs = stmt.executeQuery();
            int size = 0;
            while (rs.next()) {
                size++;
            }
            
            if (size != 1) {
                stmt.close();
                return false;
            }
                
            
        } catch (SQLException e) {
            return false;
        }
        
        return true;
    }
    
    private final Map<Long, Session> sessionMap;
    private final DatabaseManager dbManager;
    private final AuthenticationManager authManager;
}
