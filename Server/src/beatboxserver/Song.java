/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import io.netty.buffer.ByteBuf;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 * @author rahmanj
 */
public class Song {
    
    
    public enum SongStatus {Playing, Stopped, Inactive};
    
    /**
     * 
     * @param songID
     * @param songName
     * @param songArtist
     * @param songAlbum
     * @param songPath
     * @param songLength
     * @param songVotes
     */
    public Song(long songID, String songName, String songArtist, String songAlbum, String songPath, long songLength, long songVotes) {
        this(songID, songName, songArtist, songAlbum, songPath, songLength, songVotes, null, null);
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
    public Song(long songID, String songName, String songArtist, String songAlbum, String songPath, long songLength, long songVotes, ByteBuf image, String imageType) {
        id = songID;
        name = songName;
        artist = songArtist;
        album = songAlbum;
        path = songPath;
        length = songLength;
        votes = songVotes;
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
    
    /**
     * 
     * @return 
     */
    public String getPath() {
        return path;
    }
    
    /**
     * 
     * @return 
     */
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
    
    
    /**
     * 
     * @return 
     */
    public long getVotes() {
        return votes;
    }
    
    private long id;
    
    private String name;
    
    private String artist;
    
    private String album;
    
    @JsonIgnore
    private String path;
    
    @JsonIgnore
    private String imageMimeType;
    
    @JsonIgnore
    private ByteBuf imageData;
    
    private long length;

    private long votes;
}
