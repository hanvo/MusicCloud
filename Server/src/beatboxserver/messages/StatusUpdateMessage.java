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
public class StatusUpdateMessage extends Message {
    
    public enum Status {Playing, Paused};
    
    public StatusUpdateMessage() {
        super(MessageType.StatusUpdateMessage);
    }
    
    @Expose
    public String id;
    
    @Expose
    public Status status;
    
    @Expose
    public int position;
    
}
