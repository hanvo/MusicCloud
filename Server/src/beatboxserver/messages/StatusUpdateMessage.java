/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.messages;

/**
 *
 * @author rahmanj
 */
public class StatusUpdateMessage extends Message {
    
    public StatusUpdateMessage() {
        super(MessageType.StatusUpdateMessage);
    }
}
