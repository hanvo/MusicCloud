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
    
    public enum Status {Playing, Paused, Ready, Stopped};
    
    public StatusUpdateMessage() {
        super(MessageType.StatusUpdateMessage);
    }
    
    /**
     * Song ID of currently playing song
     */
    @Expose
    public long id;
    
    @Expose
    public Status status;
    
    /**
     * Playback position in milliseconds
     */
    @Expose
    public long position;
    
}
