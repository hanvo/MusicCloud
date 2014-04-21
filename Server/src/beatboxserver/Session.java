/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.updates.ClientUpdate;
import io.netty.channel.Channel;

import com.google.gson.annotations.Expose;

/**
 * 
 * @author rahmanj
 */
public abstract class Session {
    
    /**
     * Enumeration used to track different session types
     * Associated classes are named EnumValueSession
     */
    public enum SessionType {User, Speaker};
    
    /**
     * 
     * @param id
     * @param type 
     */
    public Session(long id, String ipAddress, SessionType type) {
        this.id = id;
        this.clientType = type;
        this.ipAddresss = ipAddress;
    }
    
    
    /**
     * 
     * @return 
     */
    public long getID() {
        return id;
    }
    
    /**
     * Destroy the client and close pending requests
     */
    public void destroyClient() {
        updateQueue.closePendingRequests();
    }
    
    /**
     * 
     * @param update 
     */
    public void sendUpdate(ClientUpdate update) {
        if (update == null) {
            throw new IllegalArgumentException();
        }
        updateQueue.queueUpdate(update);
    }
    
    
    /**
     * 
     * @param ch 
     */
    public void assignRequest(Channel ch) {
        updateQueue.queueRequest(ch);
    }
    
    @Expose
    private long id;
    
    private String ipAddresss;
    
    private SessionType clientType;
    private ClientUpdateQueue updateQueue;
}
