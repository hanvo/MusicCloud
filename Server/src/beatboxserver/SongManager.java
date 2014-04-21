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
    
    public SongManager(DatabaseManager databaseManager) {
        songMap = new HashMap<>();
        statsMap = new HashMap<>();
        dbManager = databaseManager;
    }
    
    
    /**
     * 
     * @param song 
     */
    public void addSong(Song song) {
        synchronized(this) {
            if (songMap.containsKey(song.getID())) {
                throw new IllegalArgumentException("Duplicate song IDs");
            } else {
                songMap.put(song.getID(), song);
                statsMap.put(song.getID(), new SongStats(song.getID()));
            }
        }
    }
    
    public void vote(String songID, UserSession client) {
        synchronized(this) {
        }
    }
    
    public void like(String songID, UserSession client) {
        synchronized(this) {
            synchronized(client) {
                if (client.doesDislikeSong(songID)) {
                    statsMap.get(songID).removeDislike();
                }
                statsMap.get(songID).addLike();
            }
        }
    }
    
    public void dislike(String songID, UserSession client) {
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
    
    private DatabaseManager dbManager;
}
