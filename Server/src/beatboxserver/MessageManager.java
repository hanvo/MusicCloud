/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 *
 * @author rahmanj
 */
public class MessageManager {
    
    public MessageManager() {
        messageRegistrations = new ConcurrentHashMap<String, Class>();
    }
    
    
    public Message readMessage(SocketChannel channel) {
        try {
            if (channel != null) {
                Message message = readHeader(channel);
                message.readMessage(channel);
                return message;
            } else {
                throw new IllegalArgumentException();
            }
        } catch (NoSuchMethodException|InstantiationException|IllegalAccessException|InvocationTargetException ex) {
            // Handle exception
            throw new RuntimeException();
        }
    }
    
    /**
     * 
     * @param channel
     * @return
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException 
     */
    protected final Message readHeader(SocketChannel channel) throws
        NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        
        // Scan the channel in
        Scanner scan = new Scanner(channel);
        
        // Read in the standard "MessageType MessageID ClientID\n" header
        String messageType = scan.next();
        int messageID = scan.nextInt();
        String clientID = scan.next();
        
        return createMessage(messageType, messageID, clientID);
    }
    
    /**
     * 
     * @param type
     * @param message 
     */
    public void registerMessage(String type, Class message) {
        if (type != null && message != null) {
            Class prev = messageRegistrations.putIfAbsent(type, message);
            
            // Check if attempt made to overwrite previous message registration
            if (prev != null && !prev.equals(message)) {
                throw new UnsupportedOperationException();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Create a message object using reflection from a set of registered classes
     * @param type
     * @param messageID
     * @param clientID
     * @return
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException 
     */
    protected Message createMessage(String type, int messageID, String clientID) throws
        NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (type != null) {
            Class c = messageRegistrations.get(type);
            
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
    
    
    /**
     * Register given message names with implementation classes
     */
    private final ConcurrentHashMap<String, Class> messageRegistrations;
}
