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
    
    public VoteData(long songID, long votes) {
        id = songID;
        this.votes = votes;
    }
    
    @Expose
    private long id;
    
    @Expose
    private long votes;
}
