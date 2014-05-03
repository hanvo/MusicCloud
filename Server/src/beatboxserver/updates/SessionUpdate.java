/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.updates;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Represents client update messages to be sent to clients
 * @author rahmanj
 */
public abstract class SessionUpdate<T> {
    
    /**
     * Enum describing the various possible update message types
     */
    public enum UpdateType {current_song, votes, likes, song_list, upcoming_song, playback_command};
    
    /**
     * Construct a new instance of ClientUpdate
     * @param type {@link UpdateType} describing this {@link ClientUpdate}
     * @param value {@link T} value to be included in update, may be a class instance or array
     */
    public SessionUpdate(UpdateType type, T value) {
        this.values = value;
        this.updateType = type;
    }
    
    /**
     * Convert the given update into a
     * @return 
     */
    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        String s = null;
        try {
            s = mapper.writeValueAsString(this);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return s;
    }
    
    /**
     * 
     * @return 
     */
    public UpdateType getUpdateType() {
        return updateType;
    }
    
    public T getValues() {
        return values;
    }
    
    protected final T values;
     
    protected UpdateType updateType;
    
    private static final Logger logger = LogManager.getFormatterLogger(SessionUpdate.class.getName());
}
