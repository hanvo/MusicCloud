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
public class LikeMessage extends Message {
    
    public LikeMessage() {
        super(MessageType.LikeMessage);
    }
    
    @Expose
    public String id;
}
