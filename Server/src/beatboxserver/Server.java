/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.Message;
import beatboxserver.Message.MessageType;
import beatboxserver.Client;
import beatboxserver.Client.ClientType;

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
        listeners = new Hashtable<String, MessageListener>();
        clients = new ConcurrentHashMap<String, Client>();
        outboundMessages = new ConcurrentHashMap<String, ConcurrentLinkedQueue<Message>>();
        messageRegistrations = new ConcurrentHashMap<String, Class>();
        
        // Create reading and writing thread
        
    }
    
    /**
     * 
     */
    public void startServer() {
       // TODO, starting running reading and writing thread at this point 
    }
    
    /**
     * 
     * @param type
     * @param listener 
     */
    public void registerListener(String type, MessageListener listener) {
        listeners.put(type, listener);
    }
    
    /**
     * 
     * @param type
     * @param messageClass 
     */
    public void registerMessage(String type, Class messageClass) {
        
    }
    
    /**
     * 
     * @param client
     * @param message 
     */
    public void sendMessage(Client client, Message message) {
        
    }
    
    
    /**
     * 
     * @param client
     * @param message 
     */
    public void broadcastMessage(ClientType client, Message message) {
        
    }
    
    // TODO Consider multiple listeners
    private Hashtable<String, MessageListener> listeners;
    
    /**
     * Map client IDs to client objects
     */
    private ConcurrentHashMap<String, Client> clients;

    
    /**
     * Map client IDs to outbound message queues
     */
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Message>> outboundMessages;
    
    /**
     * Register given message names with implementation classes
     */
    private ConcurrentHashMap<String, Class> messageRegistrations;
    
    /**
     * Queue of messages queued for processing
     */
    private ConcurrentLinkedQueue<Message> inboundMessages;
}

interface MessageListener {
    public void MessageRecieved(Message message, Client client);
}



