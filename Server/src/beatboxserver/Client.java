/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.Message;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.nio.channels.*;

/**
 * Class to represent abstract client on other end of a given connection
 * @author rahmanj
 */
public class Client {
    
    public Client(String id, SocketChannel chan, ConcurrentLinkedQueue<Message> queue) {
        if (id != null && chan != null && queue != null) {
            clientID = id;
            channel = chan;
            messageQueue = queue;
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Send message to client from the main server thread
     * @param message 
     */
    public void sendMessage(Message message) {
        
    }
    
    protected ConcurrentLinkedQueue<Message> messageQueue;
    
    protected String clientID;
    protected SocketChannel channel;
}
