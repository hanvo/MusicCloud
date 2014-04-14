/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.Message;
import beatboxserver.Message;
import beatboxserver.Client;
import beatboxserver.Client.ClientType;
import beatboxserver.ServerReadTask;

import java.io.IOException;

import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.nio.channels.SelectionKey;



/**
 *
 * @author rahmanj
 */
public class Server {
    
    public Server(int port) throws IOException {
        
        // Create data structures for global state
        listeners = new Hashtable<String, MessageListener>();
        clientManager = new ClientManager();
        
        // Create message queues
        outboundMessages = new ConcurrentHashMap<String, ConcurrentLinkedQueue<Message>>();
        inboundMessages = new ConcurrentLinkedQueue<Message>();
        
        // Create message reader
        messageManager = new MessageManager();
        
        // TODO Create ServerSocketChannel
        
        // Create reading and writing thread
        readThread = new Thread(new ServerReadTask(inboundMessages, clientManager, messageManager, null));
        writeThread = new Thread();
        
    }
    
    /**
     * 
     */
    public void startServer() {
       // TODO, starting running reading and writing thread at this point
        
        writeThread.start();
        readThread.start();
    }
    
    /**
     * 
     * @param type
     * @param listener 
     */
    public void registerListener(String type, MessageListener listener) {
        if (type != null && listener != null) {
            listeners.put(type, listener);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * 
     * @param type
     * @param messageClass 
     */
    public void registerMessage(String type, Class messageClass) {
        if (type != null && messageClass != null) {
            messageManager.registerMessage(type, messageClass);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * 
     * @param client
     * @param message 
     */
    public void sendMessage(Client client, Message message) {
        if (client != null && message != null) {
            client.sendMessage(message);
        } else {
            throw new IllegalArgumentException();
        }
    } 
    
    
    /**
     * 
     * @param type
     * @param message 
     */
    public void broadcastMessage(ClientType type, Message message) {
        
    }
    
    private ClientManager clientManager;
    
    /**
     * 
     */
    private MessageManager messageManager;
    
    // TODO Consider multiple listeners
    private Hashtable<String, MessageListener> listeners;

    
    /**
     * Map client IDs to outbound message queues
     */
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Message>> outboundMessages;
    
    
    /**
     * Queue of messages queued for processing
     */
    private ConcurrentLinkedQueue<Message> inboundMessages;
    
    /**
     * 
     */
    private Thread readThread;
    
    /**
     * 
     */
    private Thread writeThread;
}



