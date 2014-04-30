/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.updates.SessionUpdate;
import beatboxserver.updates.SessionUpdate.UpdateType;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayDeque;
import java.util.Queue;

import io.netty.channel.Channel;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;


/**
 *
 * @author rahmanj
 */
public class SessionUpdateQueue {
    
    /**
     * Create new {@link SessionUpdateQueue}
     */
    public SessionUpdateQueue() {
        /*executor = Executors.newFixedThreadPool(1);*/
        channelQueue = new ArrayDeque<>();
        updateTypeQueue = new ArrayDeque<>();
        updates = new HashMap<>();
    }
    
    /**
     * Queue an incoming update request to be matched with an update for the client
     * @param ch The {@link Channel} the update request was received on
     */
    public void queueRequest(Channel ch) {
        SessionUpdate update;
        FullHttpResponse response;
        String json;
        UpdateType updateType;
        
        if (ch == null) {
            throw new IllegalArgumentException();
        }
        
        synchronized(this) {
            
            // Check if there are updates to send
            if (updates.size() > 0) {
                updateType = updateTypeQueue.poll();
                
                update = updates.get(updateType);
                updates.remove(updateType);
                
                json = update.toJson();
                response = RequestHandler.createResponse(HttpResponseStatus.OK, json);
                
                RequestHandler.sendResponse(ch, response, false);
            } else {
                // Enqueue the request
                channelQueue.add(ch);
            }
            
        }
        
    }
    
    /**
     * Queue a {@ClientUpdate} to be matched with an incoming request
     * @param update {@link SessionUpdate} to send to the {@link Session}
     */
    public void queueUpdate(SessionUpdate update) {
        FullHttpResponse response;
        Channel chan;
        String json;
        
        synchronized(this) {
            if (channelQueue.size() > 0) {
                chan = channelQueue.poll();
                
                json = update.toJson();
                response = RequestHandler.createResponse(HttpResponseStatus.OK, json);
                
                RequestHandler.sendResponse(chan, response, false);
            } else {
                
                // Overwrite the old update with the same type
                updates.put(update.getUpdateType(), update);
            }
        }
    }
    
    /**
     * Close any pending channel requests waiting
     */
    public void closePendingRequests() {
        synchronized(this) {
            Channel chan;
            while (channelQueue.size() > 0) {
                chan = channelQueue.element();
                channelQueue.poll();
                chan.close();
            }
        }
    }
    
    private final Queue<Channel> channelQueue;
    private final Map<UpdateType, SessionUpdate> updates;
    private final Queue<UpdateType> updateTypeQueue;
}
