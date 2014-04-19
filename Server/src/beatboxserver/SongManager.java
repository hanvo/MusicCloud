/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author rahmanj
 */
public class SongManager {
    
    public SongManager() {
        songMap = new HashMap<>();
    }
    
    
    public void vote(Song song, UserClient client) {
        synchronized(this) {
        }
    }
    
    public void like(Song song, UserClient client) {
        synchronized(this) {
            
        }
    }
    
    public void dislike(Song song, UserClient client) {
        synchronized(this) {
            
        }
    }
    
    
    private Map<String, Song> songMap;
}
