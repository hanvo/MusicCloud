/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.updates.ClientUpdate;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.channel.group.ChannelGroup;

import java.util.Map;
import java.util.HashMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
    
    /**
     * 
     * @param pin
     * @param clientType
     * @return
     * @throws InvocationTargetException
     */
    public Client createClient(String pin, Class clientType) throws InvocationTargetException {
        if (pin == null || clientType == null) {
            throw new IllegalArgumentException();
        }
        
        Client client;
        int newID;
        boolean authenticated;
        // TODO, Use auth manager to login via pin
        
        authenticated = this.authenticationManager.authenticate(pin);
        
        if (!authenticated) {
            throw new SecurityException();
        }
        
        if (!Client.class.isAssignableFrom(clientType)) {
            throw new IllegalArgumentException("Invalid class type, must be subclass of Client");
        } 
        
        synchronized(this) {
            newID = nextClientID++;   
        }
        
        try {
            Constructor ctor = clientType.getDeclaredConstructor(int.class);
            client = (Client)ctor.newInstance(newID);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
        
        clientMap.put(String.valueOf(newID), client);
        
        return client;
    }
    
    /**
     * 
     * @param c 
     */
    public void destroyClient(Client c) {
        synchronized(this) {
            clientMap.remove(c.getID());
            c.destroyClient();
        }
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
    public Client getClient(String id) {
        Client c;
        
        if (id == null) {
            throw new IllegalArgumentException();
        }
        
        synchronized(this) {
            if (clientMap.containsKey(id)) {
                c = clientMap.get(id);
            } else {
                c = null;
            }
        }
        return c;
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
