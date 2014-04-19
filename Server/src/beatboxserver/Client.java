/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import io.netty.channel.Channel;

/**
 *
 * @author rahmanj
 */
public class Client {
    
    public enum ClientType {User, Speaker};
    
    /**
     * 
     * @param id
     * @param type 
     */
    public Client(String id, ClientType type) {
        if (id != null) {
            this.id = id;
            this.clientType = type;
        } else {
            throw new IllegalArgumentException();
        }
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
    
    public String getID() {
        return id;
    }
    
    
    
    private ClientUpdateQueue updateQueue;
    private ClientType clientType;
    private String id;
}
