/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.updates.VoteData;
import beatboxserver.updates.LikeData;
import beatboxserver.Song.SongStatus;

import beatboxserver.messages.*;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Blob;

import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author rahmanj
 */
public class SongManager {
    
    /**
     * Construct a new instance of the {@link SongManager}
     * @param databaseManager The {@link DatabaseManager} instance to use with this {@link SongManager}
     * @param sessionManager The {@link SessionManager} instance to use with this {@link SongManager}
     */
    public SongManager(DatabaseManager databaseManager, SessionManager sessionManager) {
        databaseMgr = databaseManager;
        sessionMgr = sessionManager;
    }
    
    /**
     * Order playback to start for the next song
     */
    public void playNextSong() {
        
    }
    
    //<editor-fold defaultstate="collapsed" desc="Client Actions">
    
    /**
     * 
     * @param songID
     * @param sessionID
     * @throws SQLException 
     */
    public void vote(long songID, long sessionID) throws SQLException {
        if (songID < 0 || sessionID < 0) {
            throw new IllegalArgumentException();
        }
        
        try (PreparedStatement stmt = databaseMgr.createPreparedStatement("INSERT OR REPLACE votes(session_id, song_id) VALUES (?, ?)")) {
            stmt.setLong(1, sessionID);
            stmt.setLong(2, songID);
            if (stmt.executeUpdate() != 1) {
                throw new SQLException();
            }
        }
    }
    
    /**
     * 
     * @param songID
     * @param sessionID
     * @throws SQLException 
     */
    public void like(long songID, long sessionID) throws SQLException {
        if (songID < 0 || sessionID < 0) {
            throw new IllegalArgumentException();
        }
        
        try (PreparedStatement stmt = databaseMgr.createPreparedStatement("INSERT OR REPLACE likes (session_id, song_id) VALUES (?, ?, 1)")) {
            stmt.setLong(1, sessionID);
            stmt.setLong(2, songID);
            if (stmt.executeUpdate() != 1) {
                throw new SQLException();
            }
        }
    }
    
    /**
     * 
     * @param songID
     * @param sessionID
     * @throws SQLException 
     */
    public void dislike(long songID, long sessionID) throws SQLException {
        if (songID < 0 || sessionID < 0) {
            throw new IllegalArgumentException();
        }
        
        try (PreparedStatement stmt = databaseMgr.createPreparedStatement("INSERT OR REPLACE likes (session_id, song_id, value) VALUES (?, ?, '-1')")) {
            stmt.setLong(1, sessionID);
            stmt.setLong(2, songID);
            if (stmt.executeUpdate() != 1) {
                throw new SQLException();
            }
        }
        
        // TODO Include logic for removing the current song if dislikes reach certain level
    }
    
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Client Get Methods">
    
    /**
     *
     * @return
     * @throws SQLException
     */
    public LikeData getStats() throws SQLException {
        
        long songID = -1;
        long likes, dislikes;
        try (Statement stmt = databaseMgr.createStatement()) {
            
            // Use the status field to filter out likes for non-active songs
            String query = "SELECT songs.id AS id, value, COUNT(*) AS likes FROM likes "
                    + "INNER JOIN songs ON songs.id = song_id "
                    + "WHERE status != '" + SongStatus.Inactive.ordinal() + "' "
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
                if (value < 0) {
                    dislikes = count;
                } else if (value > 0) {
                    likes = count;
                }
            }
        }
        
        if (songID == -1) {
            throw new NoSuchElementException();
        }
        
        return new LikeData(songID, likes, dislikes, computeLikeBalance(likes, dislikes));
    }
    
    /**
     *
     * @return
     * @throws SQLException
     */
    public List<Song> getSongList() throws SQLException {
        List<Song> songs = new ArrayList<>();
        Statement stmt = databaseMgr.createStatement();
        
        // Search for songs, while sorting by votes (Sub query to get the vote count)
        ResultSet rs = stmt.executeQuery("SELECT id, name, path, artist, album, length, votes FROM songs "
                                       + "LEFT OUTER JOIN (SELECT song_id, COUNT(*) as vote_count FROM votes GROUP BY song_id) counts "
                                       + "ON counts.song_id = id ORDER BY counts.vote_count DESC NULLS LAST");
        while (rs.next()) {
            songs.add(new Song(rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("artist"),
                    rs.getString("album"),
                    rs.getString("path"),
                    rs.getLong("length"),
                    rs.getLong("votes")));
        }
        
        return songs;
    }
    
    /**
     * Get a list of current votes for the songs in the database
     * @return
     * @throws SQLException
     */
    public List<VoteData> getVotes() throws SQLException {
        List<VoteData> votes = new ArrayList<>();
        Statement stmt = databaseMgr.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT song_id AS id, COUNT(*) as votes FROM votes GROUP BY song_id");
        while (rs.next()) {
            votes.add(new VoteData(rs.getLong("id"), rs.getLong("votes")));
        }
        return votes;
    }
    
    
    /**
     *
     * @return
     */
    public ActiveSong getActiveSong() throws SQLException {
        
        ActiveSong activeSong;
        
        String query = "SELECT id, name, path, artist, album, length, votes FROM songs "
                     + "LEFT OUTER JOIN (SELECT song_id, COUNT(*) as vote_count FROM votes GROUP BY song_id) counts "
                     + "ON counts.song_id = id WHERE status = '" + SongStatus.Playing.ordinal() + "' LIMIT 1";
        //query = "SELECT id, name, path, artist, album, length FROM songs WHERE status = '" + SongStatus.Playing.ordinal() + "' LIMIT 1";
        try (Statement stmt = databaseMgr.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            
            
            if (rs.next()) {
                activeSong = new ActiveSong(rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("artist"),
                    rs.getString("album"),
                    rs.getString("path"),
                    rs.getLong("length"),
                    rs.getLong("votes"),
                    SongStatus.values()[rs.getInt("status")]);
            } else {
                throw new NoSuchElementException();
            }
            
            // Check for duplicate playing songs, this should never happen
            if (rs.next()) {
                throw new IllegalStateException("Duplicate active songs");
            }
        }
        
        return activeSong;
    }
    
    /**
     * Retrieve the photo associated with the given song
     * @param songID {@link long} containing the ID for the requested song photo
     * @return
     * @throws SQLException
     */
    public SongPhoto getSongPhoto(long songID) throws SQLException {
        if (songID < 0) {
            throw new IllegalArgumentException();
        }
        
        SongPhoto photo;
        Blob blob;
        
        try (PreparedStatement stmt = databaseMgr.createPreparedStatement("SELECT image, image_type FROM songs WHERE id = '?'")) {
            stmt.setLong(1, songID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                
                blob = rs.getBlob("image");
                ByteBuf buf = Unpooled.copiedBuffer(blob.getBytes(1, (int)blob.length()));
                photo = new SongPhoto(buf, rs.getString("image_type"));
            } else {
                throw new NoSuchElementException();
            }
        }
        
        return photo;
    }
    
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Speaker Actions">
    
    /**
     * 
     * @param sessionID
     * @param update 
     */
    public void speakerStatusUpdate(long sessionID, StatusUpdateMessage update) {
        if (sessionID < 0 || update == null) {
            throw new IllegalArgumentException();
        }
        
        switch (update.status) {
            case Playing:
                // TODO
                // Mark the current song as Playing
                break;
            case Stopped:
                // TODO
                // Mark the current song as Stopped
                break;
            case Ready:
                // TODO
                // Send the playback command if the song ID matches the active song
                break;
        }
    }

//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Speaker Get Methods">
    
    /**
     * 
     * @param songID
     * @return
     * @throws SQLException
     * @throws IOException 
     */
    public SongData getSongData(long songID) throws SQLException, IOException {
        if (songID < 0) {
            throw new IllegalArgumentException();
        }
        
        String path;
        try (PreparedStatement stmt = databaseMgr.createPreparedStatement("SELECT path FROM songs WHERE id = '?'")) {
            stmt.setLong(1, songID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                path = rs.getString("name");
            } else {
                throw new NoSuchElementException();
            }
        }
        
        // Read mp3 file from disk into RAM and send off to the client
        // This could *probably* be optimized quite a bit
        File file = new File(path);
        int length = (int)file.length();
        FileInputStream fs = new FileInputStream(file);
        
        byte[] data = new byte[length];
        int bytesRead = 0;
        int bytesLeft = length;
        int lastRead;
        
        do {
            lastRead = fs.read(data, bytesRead, bytesLeft);
            bytesRead = bytesRead + lastRead;
            bytesLeft = length - bytesRead;
        } while (lastRead > 0 && bytesLeft > 0);
        
        ByteBuf buf = Unpooled.copiedBuffer(data);
        SongData songData = new SongData(buf, "audio/mp3");
        
        return songData;
    }
    
//</editor-fold>
    
    
    /**
     * 
     * @param likes
     * @param dislikes
     * @return 
     */
    public double computeLikeBalance(long likes, long dislikes) {
        if (likes < 0 || dislikes < 0) {
            throw new IllegalArgumentException();
        }
        
        long difference = likes - dislikes;
        long sum = likes + dislikes;
        
        return (sum == 0) ? 0.0 : (double)difference / (double)sum;
    }
    
    /**
     * 
     * @return 
     */
    public boolean skipToNext() {
        ActiveSong song = getActiveSong();
        //LikeData data 
    }
    
    //<editor-fold defaultstate="collapsed" desc="Fields">
    private long activeSongID;
    private final DatabaseManager databaseMgr;
    private final SessionManager sessionMgr;
    private final static Logger logger = LogManager.getFormatterLogger(SongManager.class.getName());
//</editor-fold>
}
