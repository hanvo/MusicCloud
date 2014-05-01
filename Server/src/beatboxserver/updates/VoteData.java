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
public class VoteData {
    
    /**
     * Construct a new {@link VoteData} object
     * @param songID ID for the song referred to
     * @param votes Number of votes for this song
     */
    public VoteData(long songID, long votes) {
        id = songID;
        this.votes = votes;
    }
    
    @Expose
    private final long id;
    
    @Expose
    private final long votes;
}
