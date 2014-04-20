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
     * @param likeBalance 
     */
    public LikeData(String songID, int songLikes, int songDislikes, double likeBalance) {
        id = songID;
        likes = songLikes;
        dislikes = songDislikes;
        balance = likeBalance;
    }
    
    @Expose
    private String id;
            
    @Expose
    private int likes;
    
    @Expose
    private int dislikes;
    
    @Expose
    private double balance;
}
