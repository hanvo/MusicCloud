/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.channel.group.ChannelGroup;

import java.util.HashMap;

/**
 *
 * @author rahmanj
 */
public class ClientManager {
    
    
    public void createClient() {
        
    }
    
    public void broadcastMessage(FullHttpResponse message, Class type) {
        ChannelGroup grp;
        if (message != null && type != null) {
            synchronized (this) {
                grp = groups.get(type);
            }
            
            
        } else {
            
        }
    }
    
    
    private HashMap<Class, ChannelGroup> groups;
}
