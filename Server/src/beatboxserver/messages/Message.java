/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.messages;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author rahmanj
 */
public class Message {
    public enum MessageType {AuthenticateMessage, DeauthenticateMessage, LikeMessage, DislikeMessage, ReadyMessage, StatusUpdateMessage, VoteMessage};
    
    public Message(MessageType type) {
        messageType = type;
    }
    
    /**
     * 
     * @param name
     * @param json
     * @return
     * @throws ClassNotFoundException 
     * @throws IOException
     */
    public static final Message constructMessage(String name, String json) throws ClassNotFoundException, IOException {
        if (name == null || json == null) {
            throw new IllegalArgumentException();
        }
        
         // Get the message class
        Class messageClass = Class.forName(name);
        /*Gson gson = (new GsonBuilder()).create();*/
        
        
        // Check if messageClass is a subclass of Message as required
        if (!Message.class.isAssignableFrom(messageClass)) {
            throw new ClassNotFoundException();
        }
        
        /*return (Message)gson.fromJson(json, messageClass);*/
        ObjectMapper mapper = new ObjectMapper();
        return (Message)mapper.readValue(json, messageClass);
    }
    
    /**
     * Normalize a request 
     * @param name
     * @return 
     */
    public static final String normalizeName(String name) {
        StringBuilder sb = new StringBuilder();
        
        // Split on underscores
        String[] components = name.split("_");
        
        String original;
        String modified;
        
        // Perform lower_case_underscore to camelCase conversion consistent with method names
        for (int i = 0; i < components.length; i++) {
            if (components[i].length() > 0) {
                original = String.valueOf(components[i].charAt(0));
                modified = original.toUpperCase();
                components[i] = components[i].replaceFirst(original, modified);
            }
        }
        
        // Stich these together
        sb.append(Message.class.getPackage().getName());
        sb.append(".");
        for (String s : components) {
            sb.append(s);
        }
        sb.append("Message");
        
        return sb.toString();
    }
    
    @JsonIgnore
    private MessageType messageType;
}
