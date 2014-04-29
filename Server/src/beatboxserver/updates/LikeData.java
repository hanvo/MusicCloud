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
    public LikeData(long songID, long songLikes, long songDislikes, double balance) {
        if (songID < 0 || songLikes < 0 || songDislikes < 0 || balance < -1 || balance > 1) {
            throw new IllegalArgumentException();
        }
        id = songID;
        likes = 0;
        dislikes = 0;
        this.balance = balance;
    }
    
    @Expose
    private long id;
            
    @Expose
    private int likes;
    
    @Expose
    private int dislikes;
    
    @Expose
    private double balance;
    
}
