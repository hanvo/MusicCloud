/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.Message;
import beatboxserver.Message.MessageType;
import beatboxserver.Client;

import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.nio.channels.SelectionKey;



/**
 *
 * @author rahmanj
 */
public class Server {
    
    public Server(int port) {
        
        // Create data structures
        listeners = new Hashtable<MessageType, MessageListener>();
        clients = new ConcurrentHashMap<String, Client>();
        outboundMessages = new ConcurrentHashMap<String, ConcurrentLinkedQueue<Message>>();
        
        // Create reading and writing thread
        
    }
    
    
    public void registerListener(MessageType type, MessageListener listener) {
        listeners.put(type, listener);
    }
    
    public void sendMessage(Message message) {
    }
    
    // TODO Consider multiple listeners
    private Hashtable<MessageType, MessageListener> listeners;
    
    /**
     * Map client IDs to client objects
     */
    private ConcurrentHashMap<String, Client> clients;

    
    /**
     * Map client IDs to outbound message queues
     */
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Message>> outboundMessages;
    
    /**
     * Queue of messages queued for processing
     */
    private ConcurrentLinkedQueue<Message> inboundMessages;
}

interface MessageListener {
    public void MessageRecieved(Message message, Client client);
}



