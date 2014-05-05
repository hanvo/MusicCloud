/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.updates;


/**
 *
 * @author rahmanj
 */
public class LikeData {
    
    /**
     * Construct a new {@link LikeData} 
     * @param songID Song ID for this {@link LikeData}
     * @param songLikes Likes for the song
     * @param songDislikes Dislikes for the song
     * @param balance Balance between likes and dislikes, for display purposes
     */
    public LikeData(long songID, long songLikes, long songDislikes, double balance) {
        if (songID < 0 || songLikes < 0 || songDislikes < 0 || balance < -1 || balance > 1) {
            throw new IllegalArgumentException();
        }
        id = songID;
        likes = songLikes;
        dislikes = songDislikes;
        this.balance = balance;
    }
    
    public long getID() {
        return id;
    }
    
    /**
     * Get the number of likes for the given song
     * @return 
     */
    public long getLikes() {
        return likes;
    }
    
    public long getDislikes() {
        return dislikes;
    }
    
    public double getBalance() {
        return balance;
    }
    
    private long id;
            
    private long likes;
    
    private long dislikes;
    
    private double balance;
}
