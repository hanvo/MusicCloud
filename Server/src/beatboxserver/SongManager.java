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
import beatboxserver.updates.SongUpdate;
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

import java.util.Timer;
import java.util.TimerTask;
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
public final class SongManager {
    
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
        
        songTimer = new Timer();
        songTimerTask = null;
    }
    
    /**
     * Order playback to start for the next song once the previous song finishes
     * @throws SQLException
     */
    public void playNextSong() throws SQLException {

        
        if (nextSong == null) {
            nextSong = getNextSong();
            
            logger.info("Playing next song");
            
            // Send message to start playback
            sessionMgr.broadcastUpdate(new UpcomingSongUpdate(nextSong), SessionType.Speaker.ordinal());
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="Client Actions">
    
    /**
     * Vote for a given song
     * @param songID {@link long} {@link Song} ID
     * @param sessionID {@link long} {@link Session} ID
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    public void vote(long songID, long sessionID) throws SQLException, IllegalArgumentException {
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
        
        // Update the clients with the new like information
        sessionMgr.broadcastUpdate(new VoteUpdate(getVotes()), SessionType.User.ordinal());
    }
    
    /**
     * Like a song
     * @param songID {@link long} {@link Song} ID
     * @param sessionID {@link long} {@link Session} ID
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    public void like(long songID, long sessionID) throws SQLException, IllegalArgumentException {
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
        
        // Update the clients with the new like information
        sessionMgr.broadcastUpdate(new LikeUpdate(getStats()), SessionType.User.ordinal());
    }
    
    /**
     * Dislike a song
     * @param songID {@link long} {@link Song} ID
     * @param sessionID {@link long} {@link Session} ID
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    public void dislike(long songID, long sessionID) throws SQLException, IllegalArgumentException {
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
        
        synchronized (this) {
            // Update the clients with the new like information
            LikeData stats = getStats();
            sessionMgr.broadcastUpdate(new LikeUpdate(stats), SessionType.User.ordinal());
        
            long sessions = sessionMgr.getSessionCount();
            
            // Check if enough people hate the current song
            if ((double)(stats.getDislikes() - stats.getLikes()) / sessions > 0.3) {
                nextSong = getNextSong(); // Setting nextSong to non-null indicates we've made a choice
                
                // TODO send message
            }
            
        }
        // TODO Include logic for removing the current song if dislikes reach certain level
    }
    
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Client Get Methods">
    
    /**
     * Get {@link LikeData} for the {@link ActiveSong}
     * @return
     * @throws SQLException
     * @throws NoSuchElementException
     */
    public LikeData getStats() throws SQLException, NoSuchElementException {
        
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
     * Get the set of songs
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
     * @throws SQLException
     * @throws IllegalStateException
     * @throws NoSuchElementException 
     */
    public ActiveSong getActiveSong() throws SQLException, IllegalStateException, NoSuchElementException {
        synchronized (this) {
            if (activeSong != null) {
                activeSong = getActiveSongFromDB();
            }
            return activeSong;
        }
    }
    
    /**
     * Gets the current {@link ActiveSong}
     * @return
     * @throws SQLException
     * @throws IllegalStateException
     * @throws NoSuchElementException
     */
    private ActiveSong getActiveSongFromDB() throws SQLException, IllegalStateException, NoSuchElementException {
        
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
     * Get the next {@link Song} from the database to be played based on votes
     * @return
     * @throws SQLException
     * @throws NoSuchElementException
     * @throws IllegalStateException
     */
    private Song getNextSong() throws SQLException, NoSuchElementException, IllegalStateException {
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
     * Retrieve the photo associated with the given {@link Song}
     * @param songID {@link long} containing the ID for the requested song photo
     * @return
     * @throws SQLException
     * @throws IOException
     * @throws NoSuchElementException
     */
    public SongPhoto getSongPhoto(long songID) throws SQLException, IOException, NoSuchElementException {
        if (songID < 0) {
            throw new IllegalArgumentException();
        }
        
        SongPhoto photo;
        
        logger.trace("Requesting photo for song: %d", songID);
        
        try (PreparedStatement stmt = databaseMgr.createPreparedStatement("SELECT image, image_type FROM songs WHERE id = ?")) {
            stmt.setLong(1, songID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                
                byte[] data = rs.getBytes("image");

                ByteBuf buf = Unpooled.copiedBuffer(data);
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
     * @param sessionID {@link long} {@link Session} ID for the speaker update
     * @param update {@link StatusUpdateMessage}
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    public void speakerStatusUpdate(long sessionID, StatusUpdateMessage update) throws SQLException, IllegalArgumentException {
        if (sessionID < 0 || update == null) {
            throw new IllegalArgumentException();
        }
        
        synchronized(this) {
            switch (update.status) {
                case Playing:
                    // Mark the current song as Playing
                    if (update.id == nextSong.getID()) {
                        
                        // Update the database with information about the next song
                        updateNextSong(activeSong, nextSong);
                        
                        if (songTimerTask != null) {
                            songTimerTask.cancel();
                        }
                        
                        final SongManager manager = this;
                        
                        // Create a task to switch to the next song once this song finishes playing
                        songTimerTask = new TimerTask() {
                            @Override
                            public void run() {
                                synchronized(manager) {
                                    try {
                                        manager.playNextSong();
                                    } catch (Exception e) {
                                        // TODO, handle this later
                                    }
                                }
                            }
                        };
                        
                        // Schedule to run approx 5 seconds before the end of the song
                        songTimer.schedule(songTimerTask, (nextSong.getLength() - 5) * 1000);
                        
                        
                        // Update object state based on the transition
                        activeSong = new ActiveSong(nextSong.getID(),
                                                    nextSong.getName(),
                                                    nextSong.getArtist(),
                                                    nextSong.getAlbum(),
                                                    nextSong.getPath(),
                                                    nextSong.getLength(),
                                                    nextSong.getVotes());
                        nextSong = null;
                        
                        // Tell the phone clients that something happened
                        sessionMgr.broadcastUpdate(new SongUpdate(activeSong), SessionType.User.ordinal());
                    } else if (update.id != activeSong.getID()){
                        
                        // Inform the speaker it's confused
                        sessionMgr.sendUpdate(new PlaybackCommandUpdate(
                                new PlaybackCommand(PlaybackCommand.Command.Play, activeSong.getID())),
                                sessionID);
                    }
                    
                    break;
                case Stopped: // Previous song finished playing
                    
                    // TODO, what are we doing with speaker updates in the DB??
                    // Mark the current song as Stopped in the DB
                    String query = "UPDATE songs SET status = " + SongStatus.Stopped.ordinal() + " WHERE id = ?";
                    try (PreparedStatement stmt = databaseMgr.createPreparedStatement(query)) {
                        stmt.setLong(1, activeSong.getID());
                        stmt.executeQuery();
                    }
                    
                    if (nextSong != null) {
                        // Pick a new song
                        nextSong = getNextSong();
                    }
                    
                    // The speaker should ask us for the next song, but politely remind it who's boss around here
                    //sessionMgr.sendUpdate(new UpcomingSongUpdate(nextSong), sessionID);
                    //sessionMgr.sendUpdate(new PlaybackCommandUpdate(new PlaybackCommand(PlaybackCommand.Command.Play, nextSong.getID())), sessionID);
                    
                    
                    break;
                case Ready:
                    
                    // Send the playback command if the song ID matches the active song
                    if (update.id == nextSong.getID()) {
                        
                        // Send message to start playback
                        sessionMgr.sendUpdate(new PlaybackCommandUpdate(
                                new PlaybackCommand(PlaybackCommand.Command.Play, nextSong.getID())),
                                sessionID);
                        
                        // TODO, this is a problem for multiple speakers, need a quorum, and then tell the others to get in line
                        // Hmmm, may try using Paxos later as an experiement
                       
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
     * Get the {@link SongData} for the {@link Song} identified by the ID
     * @param songID {@link long} Song ID to request {@link SongData} for
     * @return
     * @throws SQLException
     * @throws IOException
     * @throws IllegalArguementException
     */
    public SongData getSongData(long songID) throws SQLException, IOException, IllegalArgumentException {
        if (songID < 0) {
            throw new IllegalArgumentException();
        }
        
        String path;
        try (PreparedStatement stmt = databaseMgr.createPreparedStatement("SELECT path FROM songs WHERE id = ?")) {
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
    
    //<editor-fold defaultstate="collapsed" desc="Utility functionality">
    
    
    /**
     * Compute the balance between likes and dislikes
     * @param likes Number of likes for the song
     * @param dislikes Number of dislikes for the song
     * @return
     */
    private double computeLikeBalance(long likes, long dislikes) {
        if (likes < 0 || dislikes < 0) {
            throw new IllegalArgumentException();
        }
        
        long difference = likes - dislikes;
        long sum = likes + dislikes;
        
        return (sum == 0) ? 0.0 : (double)difference / (double)sum;
    }
    
    /**
     * Stops playback of the current song, and demands playback of the next song
     * @return
     */
    private void skipToNext() throws SQLException {
        
        ActiveSong song = null;
        song = getActiveSongFromDB();
        
        //LikeData data
        synchronized (this) {
            
            if (nextSong == null) {
                nextSong = getNextSong();
            }
            
            // Cancel
            if (songTimerTask != null) {
                songTimerTask.cancel();
            }
            
            // Inform the speakers of our choice for the next song
            UpcomingSongUpdate update = new UpcomingSongUpdate(nextSong);
            
            // Broadcast to the client
            sessionMgr.broadcastUpdate(update, SessionType.Speaker.ordinal());
            
            // Send message asking the speakers to stop playing the current song
            PlaybackCommandUpdate message = new PlaybackCommandUpdate(new PlaybackCommand(PlaybackCommand.Command.Stop, activeSong.getID()));
            
            // Then broadcast a message the speaker and clients about the new song
            sessionMgr.broadcastUpdate(message, SessionType.Speaker.ordinal());
            
            // Speakers will respond by stopping playback of the current song
            // then requesting the song data for the next song,
            // and finally sending ready before starting playback
        }
    }
    
    /**
     * Update the database with new state based on changing the song
     * @param currentSong {@link ActiveSong} currently being played being ended
     * @param nextSong {@link Song} that is about to be played
     * @throws SQLException 
     */
    private void updateNextSong(ActiveSong currentSong, Song nextSong) throws SQLException {
        try {
            databaseMgr.startTransaction();
            
            String query;
            
            // Wipe likes for the previous song from the DB
            if (nextSong != null) {
                query = "DELETE FROM likes where song_id = ?";
                try (PreparedStatement stmt = databaseMgr.createPreparedStatement(query)) {
                    stmt.setLong(1, currentSong.getID());
                    stmt.executeUpdate();
                }
                
                // Mark the active song as inactive now that a new song is starting
                query = "UPDATE songs SET status = " + SongStatus.Inactive.ordinal() + " WHERE song_id = ?";
                try (PreparedStatement stmt = databaseMgr.createPreparedStatement(query)) {
                    stmt.setLong(1, currentSong.getID());
                    stmt.executeUpdate();
                }
            }
            
            // Wipe votes for the new song from the DB
            query = "DELETE FROM votes WHERE song_id = ?";
            try (PreparedStatement stmt = databaseMgr.createPreparedStatement(query)) {
                stmt.setLong(1, nextSong.getID());
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
    }
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Fields">
    private Song nextSong;
    private ActiveSong activeSong;
    
    private final Timer songTimer;
    private TimerTask songTimerTask;
    
    private final DatabaseManager databaseMgr;
    private final SessionManager sessionMgr;
    private final static Logger logger = LogManager.getFormatterLogger(SongManager.class.getName());
//</editor-fold>
}
