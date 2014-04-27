/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.updates.VoteData;
import beatboxserver.updates.VoteUpdate;
import beatboxserver.updates.LikeData;
import beatboxserver.updates.LikeUpdate;
import beatboxserver.updates.UpcomingSongUpdate;
import beatboxserver.updates.PlaybackCommand;
import beatboxserver.updates.PlaybackCommandUpdate;

import beatboxserver.Song.SongStatus;

import beatboxserver.Session.SessionType;

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
        
        nextSong = null;
        activeSong = null;
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
        
        try (PreparedStatement stmt = databaseMgr.createPreparedStatement("INSERT OR REPLACE likes (session_id, song_id) VALUES (?, ?, '1')")) {
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
        ResultSet rs = stmt.executeQuery("SELECT id, name, path, artist, album, length, counts.vote_count as votes FROM songs "
                                       + "LEFT OUTER JOIN (SELECT song_id, COUNT(*) as vote_count FROM votes GROUP BY song_id) counts "
                                       + "ON counts.song_id = id ORDER BY counts.vote_count IS NOT NULL, counts.vote_count DESC");
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
        
        ActiveSong active;
        
        String query = "SELECT id, name, path, artist, album, length, counts.vote_count AS votes FROM songs "
                     + "LEFT OUTER JOIN (SELECT song_id, COUNT(*) as vote_count FROM votes GROUP BY song_id) counts "
                     + "ON counts.song_id = id WHERE status != '" + SongStatus.Inactive.ordinal() + "' "
                     + "ORDER BY counts.vote_count IS NOT NULL, counts.vote_count DESC LIMIT 1";
        try (Statement stmt = databaseMgr.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            
            // Check if we even have an active song
            if (rs.next()) {
                active = new ActiveSong(rs.getLong("id"),
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
        
        return active;
    }
    
    /**
     * Get the next song from the database to be played based on votes
     * @return 
     */
    public Song getNextSong() throws SQLException {
        Song song;
        
        String query = "SELECT id, name, path, artist, album, length, counts.vote_count AS votes FROM songs "
                     + "LEFT OUTER JOIN (SELECT song_id, COUNT(*) as vote_count FROM votes GROUP BY song_id) counts "
                     + "ON counts.song_id = id WHERE status = '" + SongStatus.Inactive.ordinal() + "' "
                     + "ORDER BY counts.vote_count IS NOT NULL, counts.vote_count DESC LIMIT 1";
        
        try (Statement stmt = databaseMgr.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            
            // Check if we even have a next song
            if (rs.next()) {
                song = new Song(rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("artist"),
                    rs.getString("album"),
                    rs.getString("path"),
                    rs.getLong("length"),
                    rs.getLong("votes"));
            } else {
                throw new NoSuchElementException();
            }
            
            // Check for duplicate playing songs, this should never happen
            if (rs.next()) {
                throw new IllegalStateException("Duplicate active songs");
            }
        }
        
        return song;
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
    public void speakerStatusUpdate(long sessionID, StatusUpdateMessage update) throws SQLException {
        if (sessionID < 0 || update == null) {
            throw new IllegalArgumentException();
        }
        
        synchronized(this) {
            switch (update.status) {
                case Playing:
                    // TODO
                    // Mark the current song as Playing
                    if (update.id == activeSong.getID()) {
                        // TODO Update the database with the next status for this song
                    } else {
                        
                        // Inform the speaker it's confused
                        sessionMgr.sendUpdate(new PlaybackCommandUpdate(
                                new PlaybackCommand(PlaybackCommand.Command.Play, activeSong.getID())),
                                sessionID);
                    }
                    
                    break;
                case Stopped:
                    
                    // TODO Mark the current song as Stopped in the DB
                    
                    // The speaker should ask us for the next song, but politely remind it who's boss around here
                    if (nextSong != null) {
                        sessionMgr.sendUpdate(new UpcomingSongUpdate(nextSong), sessionID);
                    } else {
                        logger.warn("Received STOP message, but no next song selected");
                    }
                    
                    break;
                case Ready:
                    
                    if (nextSong != null) {
                        // Pick a new song
                        nextSong = getNextSong();
                    }
                    
                    // Send the playback command if the song ID matches the active song
                    if (update.id == nextSong.getID()) {
                        
                        try {
                            databaseMgr.startTransaction();
                            
                            // Wipe likes for the previous song from the DB
                            String query = "DELETE FROM likes where song_id = ?";
                            try (PreparedStatement stmt = databaseMgr.createPreparedStatement(query)) {
                                stmt.setLong(1, activeSong.getID());
                                stmt.executeUpdate();
                            }
                            
                            // Wipe votes for the new song from the DB
                            query = "DELETE FROM votes WHERE song_id = ?";
                            try (PreparedStatement stmt = databaseMgr.createPreparedStatement(query)) {
                                stmt.setLong(1, nextSong.getID());
                                stmt.executeUpdate();
                            }

                            // Mark the active song as inactive now that a new song is starting
                            query = "UPDATE songs SET status = " + SongStatus.Inactive.ordinal() + " WHERE song_id = ?";
                            try (PreparedStatement stmt = databaseMgr.createPreparedStatement(query)) {
                                stmt.setLong(1, activeSong.getID());
                                stmt.executeUpdate();
                            }
                            
                            // Mark the next song as active
                            query = "UPDATE songs SET status = " + SongStatus.Playing.ordinal() + " WHERE song_id = ?";
                            try (PreparedStatement stmt = databaseMgr.createPreparedStatement(query)) {
                                stmt.setLong(1, nextSong.getID());
                            }
                                
                        } catch (Exception e) {
                            
                            // TODO, what if startTransaction() threw the exception??? Answer: Bad things!
                            databaseMgr.rollbackTransaction();
                            throw e;
                        }
                        
                        // Commit our changes
                        databaseMgr.stopTransaction();
                        
                        // Send message to start playback
                        sessionMgr.sendUpdate(new PlaybackCommandUpdate(
                                new PlaybackCommand(PlaybackCommand.Command.Play, activeSong.getID())),
                                sessionID);
                        
                        // TODO, this is a problem for multiple speakers

                        activeSong = new ActiveSong(nextSong.getID(),
                                                    nextSong.getName(),
                                                    nextSong.getArtist(),
                                                    nextSong.getAlbum(),
                                                    nextSong.getPath(),
                                                    nextSong.getLength(),
                                                    nextSong.getVotes());
                        nextSong = null;
                    } else {
                        
                        // Inform the speaker it's confused
                        sessionMgr.sendUpdate(new UpcomingSongUpdate(nextSong), sessionID);
                    }
                    break;
            }
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
    public boolean skipToNext() throws SQLException {
        
        // TODO Finish this logic
        ActiveSong song = null;
        
        try {
            song = getActiveSong();
        } catch (NoSuchElementException e) {
            // TODO, no active song
        }
        
        //LikeData data
        synchronized (this) {
            
            if (nextSong == null || nextSong.getID() == activeSong.getID()) {
                nextSong = getNextSong();
            }
            

            // Need to remove votes for this song, then retrieve the next most voted song
            UpcomingSongUpdate message = new UpcomingSongUpdate(nextSong);
            
            // Then broadcast a message the speaker and clients about the new song
            sessionMgr.broadcastUpdate(message, SessionType.Speaker.ordinal());
            

            // Wait for speakers to request the song

            // Wait for READY from the speakers

            // Then send play

        }
        
        return true;
    }
    
    //<editor-fold defaultstate="collapsed" desc="Fields">
    private Song nextSong;
    private ActiveSong activeSong;
    
    
    private final DatabaseManager databaseMgr;
    private final SessionManager sessionMgr;
    private final static Logger logger = LogManager.getFormatterLogger(SongManager.class.getName());
//</editor-fold>
}
