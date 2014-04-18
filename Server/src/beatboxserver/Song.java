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
public class Song {
    
    public Song(String songName, String songArtist, String songAlbum, String songPath, String songCoverPath, int songLength) {
        name = new String(songName);
        artist = new String(songArtist);
        album = new String(songAlbum);
        path = new String(songPath);
        coverPath = new String(songCoverPath);
        length = songLength;
    }
    
    
    
    @Expose
    private String name;
    
    @Expose
    private String artist;
    
    @Expose
    private String album;
    
    private String path;
    private String coverPath;
    
    @Expose
    private int length;
}
