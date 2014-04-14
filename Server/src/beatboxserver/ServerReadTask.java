/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.Message;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author rahmanj
 */
public final class ServerReadTask implements Runnable {
    
    public ServerReadTask(ConcurrentLinkedQueue<Message> queue, ConcurrentHashMap<String, Client> clients, MessageReader reader) {
        if (queue != null && clients != null) {
            this.clients = clients;
            this.recievedMessages = queue;
            this.messageReader = reader;
            
            nextClientID = 0;
            
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public void run() {
        
    }
    
    protected int nextClientID;
    
    protected ConcurrentHashMap<String, Client> clients;
    protected ConcurrentLinkedQueue<Message> recievedMessages;
    protected MessageReader messageReader;
}
