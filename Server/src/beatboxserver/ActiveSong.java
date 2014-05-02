/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 * @author rahmanj
 */
public class ActiveSong extends Song {
    
    /**
     * Construct a new instance of the active song
     * @param songID
     * @param songName
     * @param songArtist
     * @param songAlbum
     * @param songPath
     * @param songLength
     * @param status
     * @param songVotes
     */
    public ActiveSong(long songID,
                      String songName,
                      String songArtist,
                      String songAlbum,
                      String songPath,
                      long songLength,
                      long songVotes,
                      SongStatus status) {
        super(songID, songName, songArtist, songAlbum, songPath, songLength, songVotes);
        this.status = status;
    }
    
    /**
     * Construct a new instance of the active song
     * @param songID
     * @param songName
     * @param songArtist
     * @param songAlbum
     * @param songPath
     * @param songLength
     * @param songVotes
     */
    public ActiveSong(long songID,
                      String songName,
                      String songArtist,
                      String songAlbum,
                      String songPath,
                      long songLength,
                      long songVotes) {
        super(songID, songName, songArtist, songAlbum, songPath, songLength, songVotes);
        this.status = SongStatus.Stopped;
    }
    
    
    /**
     * Set the current playback status of the active song
     * @param s {@link SongStatus} for the {@link ActiveSong}
     */
    public void setPlaybackStatus(SongStatus s) {
        status = s;
    }
    
    protected SongStatus status;
}
