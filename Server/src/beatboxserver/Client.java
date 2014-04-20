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
public abstract class Client {
    
    public enum ClientType {User, Speaker};
    
    /**
     * 
     * @param id
     * @param type 
     */
    public Client(int id, ClientType type) {
        this.id = id;
        this.clientType = type;
    }
    
    
    /**
     * 
     * @return 
     */
    public int getID() {
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
    private int id;
    
    private ClientType clientType;
    private ClientUpdateQueue updateQueue;
}
