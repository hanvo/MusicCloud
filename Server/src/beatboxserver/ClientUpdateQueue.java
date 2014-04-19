/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.ClientUpdate.UpdateType;

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
public class ClientUpdateQueue {
    
    public ClientUpdateQueue() {
        /*executor = Executors.newFixedThreadPool(1);*/
        channelQueue = new ArrayDeque<>();
        updateTypeQueue = new ArrayDeque<>();
        updates = new HashMap<>();
    }
    
    /**
     * Queue an incoming update request to be matched with an update for the client
     * @param ch {@link Channel} representing the channel the update request was received on
     */
    public void queueRequest(Channel ch) {
        ClientUpdate update;
        FullHttpResponse response;
        String json;
        UpdateType updateType;
        
        if (ch == null) {
            throw new IllegalArgumentException();
        }
        
        synchronized(this) {
            
            // Check if there are updates to send
            if (updates.size() > 0) {
                updateType = updateTypeQueue.element();
                
                update = updates.get(updateType);
                updates.remove(updateType);
                
                json = update.toJson();
                response = RequestHandler.createResponse(HttpResponseStatus.OK, json);
                
                RequestHandler.sendResponse(ch, response);
            } else {
                // Enqueue the request
                channelQueue.add(ch);
            }
            
        }
        
    }
    
    /**
     * Queue a {@ClientUpdate} to be matched with an incoming request
     * @param update 
     */
    public void queueUpdate(ClientUpdate update) {
        FullHttpResponse response;
        Channel chan;
        String json;
        
        synchronized(this) {
            if (channelQueue.size() > 0) {
                chan = channelQueue.element();
                
                json = update.toJson();
                response = RequestHandler.createResponse(HttpResponseStatus.OK, json);
                
                RequestHandler.sendResponse(chan, response);
            } else {
                updates.put(update.getUpdateType(), update);
            }
        }
    }
    
    private final Queue<Channel> channelQueue;
    private final HashMap<UpdateType, ClientUpdate> updates;
    private final Queue<UpdateType> updateTypeQueue;
}
