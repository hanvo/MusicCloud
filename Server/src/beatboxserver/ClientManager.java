/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.channel.group.ChannelGroup;

import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author rahmanj
 */
public class ClientManager {
    
    public ClientManager(AuthenticationManager authManager) {
        nextClientID = 0;
        clientMap = new HashMap<>();
        authenticationManager = authManager;
    }
    
    
    public void createClient(String pin) {
        synchronized(this) {
            
        }
    }
    
    /**
     * 
     * @param c 
     */
    public void destroyClient(Client c) {
        synchronized(this) {
            clientMap.remove(c.getID());
        }
        c.destroyClient();
    }
    
    /**
     * 
     * @param update
     * @param type 
     */
    public void broadcastUpdate(ClientUpdate update, Class type) {
        synchronized(this) {
            
        }
    }
    
    /**
     * 
     * @param id
     * @return 
     */
    public boolean validClient(String id) {
        if (id == null) {
            throw new IllegalArgumentException();
        }
        
        boolean valid;
        synchronized (this) {
            valid = clientMap.containsKey(id);
        }
        return valid;
    }
    
    private int nextClientID;
    private Map<String, Client> clientMap;
    private AuthenticationManager authenticationManager;
}
