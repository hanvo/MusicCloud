/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.messages;

import beatboxserver.Message;
/**
 *
 * @author rahmanj
 */
public class LikeUpdateMessage extends Message {
    
    public LikeUpdateMessage(int messageID, String clientID) {
        super(messageName, messageID, clientID);
    }
    
    public final static String messageName = "LIKE_UPDATE";
}
