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
    public UserClient(String id) {
        super(id, ClientType.User);
        
        likedSongs = new HashMap<>();
        dislikedSongs = new HashMap<>();
        votedSong = null;
    }
    
    
    public boolean likesSong(Song song) {
        return likedSongs.containsKey(song.getID());
    }
    
    public boolean dislikesSong(Song song) {
        return dislikedSongs.containsKey(song.getID());
    }
    
    public boolean votedOn(Song song) {
        return song.getID().equals(votedSong);
    }
    
    
    private Map<String, Boolean> likedSongs;
    private Map<String, Boolean> dislikedSongs;
    private String votedSong;
}
