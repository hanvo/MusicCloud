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
                      long position,
                      SongStatus status) {
        super(songID, songName, songArtist, songAlbum, songPath, songLength, songVotes);
        this.status = status;
        this.position = position;
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
                      long songVotes,
                      long position) {
        super(songID, songName, songArtist, songAlbum, songPath, songLength, songVotes);
        this.status = SongStatus.Stopped;
        this.position = position;
    }
    
    
    /**
     * Set the current playback status of the active song
     * @param s {@link SongStatus} for the {@link ActiveSong}
     */
    public void setPlaybackStatus(SongStatus s) {
        status = s;
    }
    
    public long getPosition() {
        return position;
    }
    
    protected long position;
    protected SongStatus status;
}
