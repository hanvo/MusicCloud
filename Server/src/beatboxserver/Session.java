/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.updates.SessionUpdate;
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
     * Create a new {@link Session}
     * @param id {@link long} ID for the new {@link Session}
     * @param ipAddress {@link String} IP address for the session
     * @param type {@SessionType} of the new {@link Session}
     */
    public Session(long id, String ipAddress, SessionType type) {
        this.id = id;
        this.clientType = type;
        this.ipAddresss = ipAddress;
        updateQueue = new SessionUpdateQueue();
    }
    
    
    /**
     * Get the session ID
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
     * Send update to the given session
     * @param update {@link SessionUpdate} to send to the client
     */
    public void sendUpdate(SessionUpdate update) {
        if (update == null) {
            throw new IllegalArgumentException();
        }
        updateQueue.queueUpdate(update);
    }
    
    
    /**
     * Queue a request for the given {@link Session}
     * @param ch {@link Channel} requesting an update
     */
    public void assignRequest(Channel ch) {
        updateQueue.queueRequest(ch);
    }
    
    @Expose
    private final long id;
    
    private final String ipAddresss;
    
    private final SessionType clientType;
    private final SessionUpdateQueue updateQueue;
}
