/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import com.google.gson.annotations.Expose;

/**
 *
 * @author rahmanj
 */
public class SongStats {
    
    public SongStats(String songID) {
        id = songID;
        likes = 0;
        dislikes = 0;
        balance = computeBalance();
    }
    
    public String getID() {
        return id;
    }
    
    public int getLikes() {
        return likes;
    }
    
    public void addLike() {
        likes++;
    }
    
    public void removeLike() {
        if (likes > 0) {
            likes--;
        }
    }
    
    public int getDislikes() {
        return dislikes;
    }
    
    public void addDislike() {
        dislikes++;
    }
    
    public void removeDislike() {
        if (dislikes > 0) {
            dislikes--;
        }
    }
    
    public double getBalance() {
        return computeBalance();
    }
    
    @Expose
    private String id;
    
    @Expose
    private int likes;
    
    @Expose
    private int dislikes;
    
    @Expose
    private double balance;
    
    
    private double computeBalance() {
        int difference = likes - dislikes;
        int sum = likes + dislikes;
        
        if (sum != 0) {
            return difference / (double)sum;
        } else {
            return 0;
        }
    }
}
