/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.Session.SessionType;
import beatboxserver.Song.SongStatus;

import java.util.concurrent.locks.ReentrantLock;

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
     * Construct a new {@link DatabaseManager}
     * @param songDatabasePath {@link String} path to the song database file
     * @throws SQLException 
     */
    public DatabaseManager(String songDatabasePath) throws SQLException {
        
        // Load the sqlite-JDBC driver
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
        
        connection = DriverManager.getConnection("jdbc:sqlite:");
        
        Connection db = DriverManager.getConnection("jdbc:sqlite:" + songDatabasePath);
        
        // Create the tables
        createTables(connection);
        
        // Transfer the contents over
        transferData(db, connection);
        
        transactionLock = new ReentrantLock();
    }
    
    /**
     * Create a new {@link PreparedStatment}
     * @param query {@link String} query for the {@link PreparedStatement}
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
     * Create a new {@link Statement}
     * @return
     * @throws SQLException 
     */
    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }
    
    /**
     * Initiate a transaction operations
     * Must call stopTransaction or deadlock will likely occur
     * Nesting of calls is expressly prohibited
     * @throws SQLException 
     */
    public void startTransaction() throws SQLException {
        transactionLock.lock();
        connection.setAutoCommit(false);
    }
    
    /**
     * Stop and commit a previously completed transaction
     * @throws SQLException 
     */
    public void stopTransaction() throws SQLException {
        if (transactionLock.isHeldByCurrentThread()) {
            try {
                connection.commit();
                connection.setAutoCommit(true);
            } catch (Exception e) {
                transactionLock.unlock();
                throw e;
            }
        } else {
            throw new IllegalStateException();
        }
    }
    
    /**
     * Rollback a previously started transaction
     * @throws SQLException 
     */
    public void rollbackTransaction()  throws SQLException {
        if (transactionLock.isHeldByCurrentThread()) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (Exception e) {
                transactionLock.unlock();
                throw e;
            }
        } else {
            throw new IllegalStateException();
        }
    }
    
    
    /**
     * Utility method for printing SQL exceptions for debugging
     * @param exception {@link SQLException} describing the error
     */
    public static void printSQLException(SQLException exception) {

        for (Throwable e : exception) {
            if (e instanceof SQLException) {

                e.printStackTrace(System.err);
                logger.warn("SQLState: %s\nError Code: %d\nMessage: %s\n",
                        ((SQLException)e).getSQLState(),
                        ((SQLException)e).getErrorCode(),
                        e.getMessage());

                Throwable t = exception.getCause();
                while(t != null) {
                    logger.warn("Cause: " + t);
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
        stmt.executeUpdate("DROP TABLE IF EXISTS session");
        stmt.executeUpdate("DROP TABLE IF EXISTS song");
        stmt.executeUpdate("DROP TABLE IF EXISTS client_session");
        stmt.executeUpdate("DROP TABLE IF EXISTS speaker_session");
        stmt.executeUpdate("DROP TABLE IF EXISTS votes");
        stmt.executeUpdate("DROP TABLE IF EXISTS likes");
        
        // Create sessions table
        stmt.executeUpdate("CREATE TABLE sessions (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                                                + "ip_address STRING NOT NULL, "
                                                + "session_type INTEGER REFERENCES session_types(id) ON DELETE CASCADE NOT NULL, "
                                                + "time_started INTEGER NOT NULL, "
                                                + "CONSTRAINT unique_session UNIQUE(ip_address,session_type))");
        // Create client_sessions table
        stmt.executeUpdate("CREATE TABLE user_sessions (id INTEGER REFERENCES sessions(id) ON DELETE CASCADE)");
        
        // Create speaker_sessions table
        stmt.executeUpdate("CREATE TABLE speaker_sessions (id INTEGER REFERENCES sessions(id) ON DELETE CASCADE, "
                                                        + "current_song INTEGER REFERENCES songs(id), "
                                                        + "current_status INTEGER NOT NULL DEFAULT '" + SongStatus.Inactive + "')");
        
        // Create songs table
        stmt.executeUpdate("CREATE TABLE songs (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                                            + "name STRING NOT NULL, "
                                            + "path STRING NOT NULL, "
                                            + "artist STRING NOT NULL, "
                                            + "album STRING NOT NULL, "
                                            + "length REAL NOT NULL, "
                                            + "image_type STRING, "
                                            + "image BLOB, "
                                            + "status INTEGER NOT NULL DEFAULT '" + SongStatus.Inactive + "')");
        
        // Create votes table
        stmt.executeUpdate("CREATE TABLE votes (session_id INTEGER REFERENCES sessions(id) ON DELETE CASCADE, "
                                            + "song_id INTEGER REFERENCES songs(id) ON DELETE CASCADE, "
                                            + "PRIMARY KEY(session_id))");
        
        // Create likes table
        stmt.executeUpdate("CREATE TABLE likes (session_id INTEGER REFERENCES sessions(id) ON DELETE CASCADE, "
                                            + "song_id INTEGER REFERENCES songs(id), "
                                            + "value BYTE NOT NULL, "
                                            + "PRIMARY KEY(session_id, song_id))");
        
        // Create session types table
        stmt.executeUpdate("CREATE TABLE session_types (id INTEGER UNIQUE, type STRING NOT NULL)");
        
        // Insert the session types into the DB
        int count;
        count = stmt.executeUpdate("INSERT INTO session_types (id, type) VALUES ('" + SessionType.User.ordinal() + "', '" + SessionType.User.toString() + "')");
        count *= stmt.executeUpdate("INSERT INTO session_types (id, type) VALUES ('" + SessionType.Speaker.ordinal() + "', '" + SessionType.Speaker.toString() + "')");
        if (count != 1) {
            throw new SQLException("Failed to insert session types");
        }
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
        
        logger.info("Loading data from file");
        
        // "(name STRING, path STRING, artist STRING, album STRING, length REAL, image_type STRING, image BLOB, status INTEGER)"
        String query = "INSERT INTO songs (name, path, artist, album, length, image_type, image, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement insertStmt = newDatabase.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        
        Statement getStatement = oldDatabase.createStatement();
        ResultSet results = getStatement.executeQuery("SELECT id, song, path, artist, album, lengthOfSong, art, artType FROM music");
        
        // Iterate through the result set to transfer the contents over
        while (results.next()) {
            
            logger.info("Inserting song: %s, %s, %s, %s, %f, %s",
                        results.getString("song"),
                        results.getString("path"),
                        results.getString("artist"),
                        results.getString("album"),
                        results.getDouble("lengthOfSong"),
                        results.getString("artType")
            );
            
            insertStmt.setString(1, results.getString("song"));
            insertStmt.setString(2, results.getString("path"));
            insertStmt.setString(3, results.getString("artist"));
            insertStmt.setString(4, results.getString("album"));
            insertStmt.setDouble(5, results.getDouble("lengthOfSong"));
            insertStmt.setString(6, results.getString("artType"));
            insertStmt.setBytes(7, results.getBytes("art"));
            insertStmt.setLong(8, SongStatus.Inactive.ordinal());
            
            if (insertStmt.executeUpdate() != 1) {
                logger.warn("Failed to add song to the internal DB");
            }
        }
    }
    
    private final ReentrantLock transactionLock;
    private final Connection connection;
    
    private final static Logger logger = LogManager.getFormatterLogger(DatabaseManager.class.getName());
}
