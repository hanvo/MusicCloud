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
public class ReadyMessage extends Message {
    
    public ReadyMessage() {
        super(MessageType.ReadyMessage);
    }
    
    
    /**
     * Song ID that is ready to play
     */
    public long id;
}
