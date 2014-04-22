/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import io.netty.buffer.ByteBuf;

import com.google.gson.annotations.Expose;

/**
 *
 * @author rahmanj
 */
public class Song {
    
    /**
     * 
     * @param songID
     * @param songName
     * @param songArtist
     * @param songAlbum
     * @param songPath
     * @param songLength 
     */
    public Song(long songID, String songName, String songArtist, String songAlbum, String songPath, long songLength) {
        this(songID, songName, songArtist, songAlbum, songPath, songLength, null, null);
    }
    
    /**
     * 
     * @param songID
     * @param songName
     * @param songArtist
     * @param songAlbum
     * @param songPath
     * @param songLength
     * @param image
     * @param imageType 
     */
    public Song(long songID, String songName, String songArtist, String songAlbum, String songPath, long songLength, ByteBuf image, String imageType) {
        songID = songID;
        name = songName;
        artist = songArtist;
        album = songAlbum;
        path = songPath;
        length = songLength;
        if (image != null) {
            imageData = image.duplicate();
        } else {
            imageData = null;
        }
        imageMimeType = imageType;
    }
    
    
    
    /**
     * 
     * @return 
     */
    public long getID() {
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
    public long getLength() {
        return length;
    }
    
    public ByteBuf getImageBuffer() {
        return imageData;
    }
    
    /**
     * 
     * @return a string containing the mime type for the image, or null if no image was included
     */
    public String getImageMimeType() {
        return imageMimeType;
    }
    
    @Expose
    private long id;
    
    @Expose
    private String name;
    
    @Expose
    private String artist;
    
    @Expose
    private String album;
    
    
    private String path;
    
    private String imageMimeType;
    private ByteBuf imageData;
    
    @Expose
    private long length;
}
