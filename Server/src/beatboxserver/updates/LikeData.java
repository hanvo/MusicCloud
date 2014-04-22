/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.updates;

import com.google.gson.annotations.Expose;

/**
 *
 * @author rahmanj
 */
public class LikeData {
    
    /**
     * 
     * @param songID
     * @param songLikes
     * @param songDislikes
     */
    public LikeData(long songID, long songLikes, long songDislikes) {
        id = songID;
        likes = 0;
        dislikes = 0;
        balance = computeBalance();
    }
    
    @Expose
    private long id;
            
    @Expose
    private int likes;
    
    @Expose
    private int dislikes;
    
    @Expose
    private double balance;
    
    /**
     * 
     * @return 
     */
    private double computeBalance() {
        int difference = likes - dislikes;
        int sum = likes + dislikes;
        
        return balance = (sum == 0) ? 0.0 : (double)difference / (double)sum;
    }
}
