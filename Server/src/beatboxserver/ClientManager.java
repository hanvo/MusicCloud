/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.Client;

import java.nio.channels.SocketChannel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author rahmanj
 */
public class ClientManager {
    
    public ClientManager() {
        outboundQueues = new ConcurrentHashMap<>();
        clients = new ConcurrentHashMap<>();
        nextClientID = 0;
    }
    
    
    public Client createClient(SocketChannel channel) {
        if (channel != null) {
            ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<>();
            
            String id = String.valueOf(nextClientID);
            nextClientID = nextClientID + 1;
            
            Client client = new Client(String.valueOf(nextClientID), channel, queue);
            clients.put(id, client);
            
            return client;
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * 
     * @param client
     * @return 
     */
    public ConcurrentLinkedQueue<Message> getClientOutboundQueue(Client client) {
        if (client != null) {
            ConcurrentLinkedQueue<Message> queue;
            
            queue = outboundQueues.get(client.getClientID());
            
            return queue;
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * 
     */
    private final ConcurrentHashMap<String, Client> clients;
    
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Message>> outboundQueues;
    
    private int nextClientID;
    
}
