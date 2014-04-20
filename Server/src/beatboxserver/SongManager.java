/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import io.netty.buffer.ByteBuf;

import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author rahmanj
 */
public class SongManager {
    
    public SongManager() {
        songMap = new HashMap<>();
        statsMap = new HashMap<>();
    }
    
    
    public void vote(String songID, UserClient client) {
        synchronized(this) {
        }
    }
    
    public void like(String songID, UserClient client) {
        synchronized(this) {
            synchronized(client) {
                if (client.doesDislikeSong(songID)) {
                    statsMap.get(songID).removeDislike();
                }
                statsMap.get(songID).addLike();
            }
        }
    }
    
    public void dislike(String songID, UserClient client) {
        synchronized(this) {
            synchronized(client) {
                if (client.doesLikeSong(songID)) {
                    statsMap.get(songID).removeLike();
                }
                statsMap.get(songID).addDislike();
            }
        }
    }
    
    
    private Map<String, Song> songMap;
    private Map<String, SongStats> statsMap;
    private Map<String, ByteBuf> photoMap;
}
