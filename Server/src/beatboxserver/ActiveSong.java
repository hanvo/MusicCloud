/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import com.google.gson.annotations.Expose;

/**
 *
 * @author rahmanj
 */
public class ActiveSong extends Song {
    
    /**
     * 
     * @param songID
     * @param songName
     * @param songArtist
     * @param songAlbum
     * @param songPath
     * @param songLength
     * @param status 
     */
    public ActiveSong(long songID,
                      String songName,
                      String songArtist,
                      String songAlbum,
                      String songPath,
                      long songLength,
                      SongStatus status) {
        super(songID, songName, songArtist, songAlbum, songPath, songLength);
        this.status = status;
    }
    
    /**
     * 
     * @param songID
     * @param songName
     * @param songArtist
     * @param songAlbum
     * @param songPath
     * @param songLength 
     */
    public ActiveSong(long songID,
                      String songName,
                      String songArtist,
                      String songAlbum,
                      String songPath,
                      long songLength) {
        super(songID, songName, songArtist, songAlbum, songPath, songLength);
        this.status = SongStatus.Stopped;
    }
    
    
    /**
     * 
     * @param s 
     */
    public void setPlaybackStatus(SongStatus s) {
        status = s;
    }
    
    @Expose
    protected SongStatus status;
}
