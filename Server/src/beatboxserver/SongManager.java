/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author rahmanj
 */
public class SongManager {
    
    public SongManager(DatabaseManager databaseManager) {
        dbManager = databaseManager;
    }
    

    
    public void vote(long songID, long sessionID) throws SQLException {
        if (songID < 0 || sessionID < 0) {
            throw new IllegalArgumentException();
        }
        
        try (PreparedStatement stmt = dbManager.createPreparedStatement("INSERT OR REPLACE votes(session_id, song_id) VALUES (?, ?)")) {
            stmt.setLong(1, sessionID);
            stmt.setLong(2, songID);
            if (stmt.executeUpdate() != 1) {
                throw new SQLException();
            }
        }
    }
    
    public void like(long songID, long sessionID) throws SQLException {
        if (songID < 0 || sessionID < 0) {
            throw new IllegalArgumentException();
        }
        
        try (PreparedStatement stmt = dbManager.createPreparedStatement("INSERT OR REPLACE likes() VALUES (?, ?, 1)")) {
            stmt.setLong(1, sessionID);
            stmt.setLong(2, songID);
            if (stmt.executeUpdate() != 1) {
                throw new SQLException();
            }
        }
    }
    
    public void dislike(long songID, long sessionID) throws SQLException {
        if (songID < 0 || sessionID < 0) {
            throw new IllegalArgumentException();
        }
        
        try (PreparedStatement stmt = dbManager.createPreparedStatement("INSERT OR REPLACE likes() VALUES (?, ?, -1)")) {
            stmt.setLong(1, sessionID);
            stmt.setLong(2, songID);
            if (stmt.executeUpdate() != 1) {
                throw new SQLException();
            }
        }
    }
    
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public SongStats getStats() throws SQLException {
        
        long songID;
        long likes, dislikes;
        try (Statement stmt = dbManager.createStatement()) {
            
            // Use the active field to filter out likes for non-active songs
            String query = "SELECT songs.id AS id, value, COUNT(*) AS likes FROM likes "
                         + "INNER JOIN songs ON songs.id = song_id "
                         + "WHERE active = TRUE "
                         + "GROUP BY songs.id, value";
            ResultSet rs = stmt.executeQuery(query);
            
            long value, count;

            likes = dislikes = 0;

            // Iterate through the ResultSet
            while (rs.next()) {
                value = rs.getLong("value");
                count = rs.getLong("likes");
                songID = rs.getLong("id");
                
                // Dislikes are stored as -1, likes are stored as 1
                if (songID == activeSongID) {
                    if (value < 0) {
                        dislikes = count;
                    } else if (value > 0) {
                        likes = count;
                    }
                }
            }
        }
        
        return new SongStats(activeSongID, likes, dislikes);
    }
    
    /**
     * 
     * @return
     * @throws SQLException 
     */
    public List<Song> getSongList() throws SQLException {
        List<Song> songs = new ArrayList<>();
        Statement stmt = dbManager.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT id, name, path, artist, album, length FROM songs");
        while (rs.next()) {
            songs.add(new Song(rs.getLong("id"),
                                rs.getString("name"),
                                rs.getString("artist"),
                                rs.getString("album"),
                                rs.getString("path"),
                                rs.getLong("length")));
        }
        
        return songs;
    }
    

    /**
     * 
     * @return 
     */
    public ActiveSong getActiveSong() {
        
        // TODO 
        return null;
    }
    
    private long activeSongID;
    private final DatabaseManager dbManager;
    
    private final static Logger logger = LogManager.getFormatterLogger(SongManager.class.getName());
}
