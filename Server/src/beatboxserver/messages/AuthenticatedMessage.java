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
public class AuthenticatedMessage extends Message {
    
    public AuthenticatedMessage(int messageID, String clientID) {
        super(messageName, messageID, clientID);
    }
    
    public static final String messageName = "AUTHENTICATED";
}
