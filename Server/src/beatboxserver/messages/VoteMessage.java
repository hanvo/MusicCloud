/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.messages;

import com.google.gson.annotations.Expose;

/**
 *
 * @author rahmanj
 */
public class VoteMessage extends Message {
    
    public VoteMessage() {
        super(MessageType.VoteMessage);
    }
    
    @Expose
    public long id;
}
