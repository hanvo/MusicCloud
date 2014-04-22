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
    
    public enum StatusType {Playing, Paused};
    
    public ActiveSong(long songID,
                      String songName,
                      String songArtist,
                      String songAlbum,
                      String songPath,
                      int songLength,
                      int playbackPosition) {
        super(songID, songName, songArtist, songAlbum, songPath, songLength);
        playbackPosition = 0; // Start at playback position 0
        status = StatusType.Paused;
    }
    
    /**
     * 
     * @param pos 
     */
    public void setPlaybackPosition(int pos) {
        position = pos;
    }
    
    /**
     * 
     * @param s 
     */
    public void setPlaybackStatus(StatusType s) {
        status = s;
    }
    
    @Expose
    protected int position;
    
    @Expose
    protected StatusType status;
}
