/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import com.google.gson.FieldNamingPolicy;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.annotations.Expose;

/**
 * Represents client update messages to be sent to clients
 * @author rahmanj
 */
public abstract class ClientUpdate<T> {
    
    /**
     * Enum describing the various possible update message types
     */
    public enum UpdateType {current_song, votes, likes, song_list, upcoming_song, playback_command};
    
    /**
     * Construct a new instance of ClientUpdate
     * @param value Value to be included in update, may be a class instance or array
     */
    public ClientUpdate(UpdateType type, T value) {
        this.values = value;
        this.updateType = type;
    }
    
    /**
     * Convert the given update into a
     * @return 
     */
    public String toJson() {
        Gson gson = (new GsonBuilder())
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        Type type = new TypeToken<ClientUpdate<T>>(){}.getClass();
        return gson.toJson(this, type);
    }
    
    /**
     * 
     * @return 
     */
    public UpdateType getUpdateType() {
        return updateType;
    }
    
    @Expose
    protected final T values;
    
    @Expose
    protected UpdateType updateType;
}
