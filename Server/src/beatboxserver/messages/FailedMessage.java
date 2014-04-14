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
public class FailedMessage extends Message {
 
    public FailedMessage(int messageID, String clientID) {
        super(messageName, messageID, clientID);
    }
    
    public final static String messageName = "FAILED";
}
