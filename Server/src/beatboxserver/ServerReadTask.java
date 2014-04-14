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
    
    public ServerReadTask(ConcurrentLinkedQueue<Message> queue, ConcurrentHashMap<String, Client> clients, ConcurrentHashMap<String, Class> messageList) {
        if (queue != null && clients != null) {
            this.clients = clients;
            this.recievedMessages = queue;
            this.messageRegistrations = messageList;
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public void run() {
        
    }
    
    /**
     * Create a message object using reflection from a set of registered classes
     * @param name
     * @param messageID
     * @param clientID
     * @return
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException 
     */
    protected Message createMessage(String name, int messageID, String clientID) throws
                NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (name != null) {
            Class c = messageRegistrations.get(name);
            
            if (!Message.class.isAssignableFrom(c)) {
                throw new ClassCastException();
            }
            
            Class types[] = {int.class, String.class};
            Constructor con = c.getConstructor(types);
            
            Object args[] = {messageID, clientID};
            return (Message)con.newInstance(args);
            
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    protected ConcurrentHashMap<String, Client> clients;
    protected ConcurrentLinkedQueue<Message> recievedMessages;
    protected ConcurrentHashMap<String, Class> messageRegistrations;
}
