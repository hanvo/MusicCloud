/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author rahmanj
 */
public class DatabaseManager {
    
    /**
     * 
     * @param songDatabasePath
     * @throws SQLException 
     */
    public DatabaseManager(String songDatabasePath) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        
        
        Connection db = DriverManager.getConnection("jdbc:sqlite:" + songDatabasePath);
        
        // Create the tables
        createTables(connection);
        
        // Transfer the contents over
        transferData(db, connection);
    }
    
    /**
     * 
     * @param query
     * @return
     * @throws SQLException 
     */
    public PreparedStatement createPreparedStatement(String query) throws SQLException {
        if (query == null) {
            throw new IllegalArgumentException();
        }
        return connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    }
    
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }
    
    
    /**
     * Utility method for printing SQL exceptions for debugging
     * @param ex 
     */
    public static void printSQLException(SQLException ex) {

        for (Throwable e : ex) {
            if (e instanceof SQLException) {

                e.printStackTrace(System.err);
                System.err.println("SQLState: " +
                    ((SQLException)e).getSQLState());

                System.err.println("Error Code: " +
                    ((SQLException)e).getErrorCode());

                System.err.println("Message: " + e.getMessage());

                Throwable t = ex.getCause();
                while(t != null) {
                    System.out.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }
    
    
    /**
     * Initialize the database with the given {@link Connection}
     * @param db {@link Connection} to the database we are the initialize for use
     * @throws SQLException 
     */
    private static void createTables(Connection db) throws SQLException {
        Statement stmt = db.createStatement();
        stmt.setQueryTimeout(10);
        
        // Clear out any old tables
        stmt.executeUpdate("DROP TABLE session");
        stmt.executeUpdate("DROP TABLE song");
        stmt.executeUpdate("DROP TABLE client_session");
        stmt.executeUpdate("DROP TABLE speaker_session");
        stmt.executeUpdate("DROP TABLE votes");
        stmt.executeUpdate("DROP TABLE likes");
        
        // Create sessions table
        stmt.executeUpdate("CREATE TABLE sessions (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                                + "ip_address STRING,"
                                                + "session_type INTEGER,"
                                                + "time_started INTEGER)");
        // Create client_sessions table
        stmt.executeUpdate("CREATE TABLE user_sessions (id INTEGER REFERENCES sessions(id) ON DELETE CASCADE)");
        
        // Create speaker_sessions table
        stmt.executeUpdate("CREATE TABLE speaker_sessions (id INTEGER REFERENCES sessions(id) ON DELETE CASCADE,"
                                                        + "current_song INTEGER REFERENCES songs(id),"
                                                        + "current_status INTEGER,"
                                                        + "playback_position INTEGER)");
        
        // Create songs table
        stmt.executeUpdate("CREATE TABLE songs (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                            + "name STRING,"
                                            + "path STRING,"
                                            + "artist STRING,"
                                            + "album REAL,"
                                            + "image_type STRING,"
                                            + "image BLOB)");
        
        // Create votes table
        stmt.executeUpdate("CREATE TABLE votes (session_id INTEGER REFERENCES sessions(id) ON DELETE CASCADE,"
                                            + "song_id INTEGER REFERENCES songs(id) ON DELETE CASCADE,"
                                            + "PRIMARY KEY(session_id, song_id))");
        
        // Create likes table
        stmt.executeUpdate("CREATE TABLE likes (session_id INTEGER REFERENCES sessions(id) ON DELETE CASCADE,"
                                             + "song_id INTEGER REFERENCES songs(id),"
                                             + "PRIMARY KEY(session_id, song_id))");
        
    }
    
    /**
     * Transfer data from the song_db over to the in-memory database
     * @param oldDatabase
     * @param newDatabase
     * @throws SQLException 
     */
    private static void transferData(Connection oldDatabase, Connection newDatabase) throws SQLException {
        if (oldDatabase == null || newDatabase == null) {
            throw new IllegalArgumentException();
        }
        
        // "(name STRING, path STRING, artist STRING, album STRING, length REAL, image_type STRING, image BLOB)"
        PreparedStatement insertStmt = newDatabase.prepareStatement("INSERT INTO songs VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        
        Statement getStatement = oldDatabase.createStatement();
        ResultSet results = getStatement.executeQuery("SELECT id, song, path, artist, album, lengthOfSong, art, artType FROM music");
        
        // Iterate through the result set to transfer the contents over
        while (results.next()) {
            insertStmt.setString(1, results.getString("song"));
            insertStmt.setString(2, results.getString("path"));
            insertStmt.setString(3, results.getString("artist"));
            insertStmt.setString(4, results.getString("album"));
            insertStmt.setDouble(5, results.getDouble("lengthOfSong"));
            insertStmt.setString(6, results.getString("artType"));
            insertStmt.setBlob(7, results.getBlob("art"));
            insertStmt.executeUpdate();
        }
    }
    
    private Connection connection;
    
    private final static Logger logger = LogManager.getFormatterLogger(DatabaseManager.class.getName());
}
