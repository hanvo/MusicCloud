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
    
    /**
     * 
     * @param songName
     * @param songArtist
     * @param songAlbum
     * @param songPath
     * @param songCoverPath
     * @param songLength 
     */
    public Song(String songName, String songArtist, String songAlbum, String songPath, String songCoverPath, int songLength) {
        name = songName;
        artist = songArtist;
        album = songAlbum;
        path = songPath;
        coverPath = songCoverPath;
        length = songLength;
    }
    
    /**
     * 
     * @return 
     */
    public String getID() {
        return id;
    }
    
    /**
     * 
     * @return 
     */
    public String getName() {
        return name;
    }
    
    /**
     * 
     * @return 
     */
    public String getArtist() {
        return artist;
    }
    
    /**
     * 
     * @return 
     */
    public String getAlbum() {
        return album;
    }
    
    /**
     * 
     * @return 
     */
    public int getLenth() {
        return length;
    }
    
    @Expose
    private String id;
    
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
