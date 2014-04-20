/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import io.netty.channel.Channel;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author rahmanj
 */
public class UserClient extends Client {
    
    /**
     * 
     * @param id 
     */
    public UserClient(String id) {
        super(id, ClientType.User);
        
        likedSongs = new HashMap<>();
        dislikedSongs = new HashMap<>();
        votedSong = null;
    }
    
    /**
     * 
     * @param songID 
     */
    public void likeSong(String songID, SongManager songManager) {
        if (doesDislikeSong(songID)) {
            dislikedSongs.remove(songID);
        }
        likedSongs.put(songID, Boolean.TRUE);
    }
    
    /**
     * 
     * @param songID 
     */
    public void dislikeSong(String songID, SongManager songManager) {
        if (doesLikeSong(songID)) {
            likedSongs.remove(songID);
        }
        dislikedSongs.put(songID, Boolean.TRUE);
    }
    
    /**
     * 
     * @param songID
     * @return 
     */
    public boolean doesLikeSong(String songID) {
        return likedSongs.containsKey(songID);
    }
    
    /**
     * 
     * @param songID
     * @return 
     */
    public boolean doesDislikeSong(String songID) {
        return dislikedSongs.containsKey(songID);
    }
    
    /**
     * 
     * @param song
     * @return 
     */
    public boolean votedOn(Song song) {
        return song.getID().equals(votedSong);
    }
    
    
    private Map<String, Boolean> likedSongs;
    private Map<String, Boolean> dislikedSongs;
    private String votedSong;
}
