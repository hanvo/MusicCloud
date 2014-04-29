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
public class UserSession extends Session {
    
    /**
     * 
     * @param id
     * @param ipAddress
     */
    public UserSession(long id, String ipAddress) {
        super(id, ipAddress, SessionType.User);
    }
    
    
    /**
     * 
     * @param songID
     * @param songManager
     */
    public void likeSong(String songID, SongManager songManager) {
        
    }
    
    /**
     * 
     * @param songID 
     */
    public void dislikeSong(String songID, SongManager songManager) {
        
    }
    
    /**
     * 
     * @param songID
     * @return 
     */
    public boolean doesLikeSong(String songID) {
        return false;
    }
    
    /**
     * 
     * @param songID
     * @return 
     */
    public boolean doesDislikeSong(String songID) {
        return false;
    }
    
    /**
     * 
     * @param song
     * @return 
     */
    public boolean votedOn(Song song) {
        return false;
    }
    
}
