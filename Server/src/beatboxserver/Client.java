/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.updates.ClientUpdate;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.HashMap;

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
     * 
     * @return 
     */
    public String getID() {
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
    
    private ClientType clientType;
    private String id;
    private ClientUpdateQueue updateQueue;
}
