/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import java.util.HashMap;
import java.util.Queue;


import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.Future;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import io.netty.handler.codec.http.FullHttpResponse;

import io.netty.util.Attribute;
import io.netty.util.AttributeKey;



/**
 *
 * @author rahmanj
 */
public class ClientUpdateQueue {
    
    public ClientUpdateQueue() {
        /*executor = Executors.newFixedThreadPool(1);*/
        messageQueues = new ConcurrentHashMap<>();
        channelQueues = new ConcurrentHashMap<>();
    }
    
    public void queueRequest(String clientID, Channel ch) {
        FullHttpResponse response;
        
        // Create empty queue if needed
        channelQueues.putIfAbsent(clientID, new ConcurrentLinkedQueue<Channel>());
        
        synchronized (this) {
            // Check if messages are available
            if (messageQueues.containsKey(clientID) && messageQueues.get(clientID).size() > 0) {
                // Send message
                response = messageQueues.get(clientID).poll();
                if (response != null) {
                    
                    // Check if keep-alive was set, and send the response
                    Attribute<String> keepAlive = ch.attr(new AttributeKey<String>("Connection"));
                    if (keepAlive.get().equalsIgnoreCase("Keep-Alive")) {
                        ch.write(response).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                    } else {
                        ch.write(response).addListener(ChannelFutureListener.CLOSE);
                    }
                } else {
                    // Queue channel for later
                    channelQueues.get(clientID).add(ch);
                }
            } else {
                // Queue channel
                channelQueues.get(clientID).add(ch);
            }
        }
        
    }
    
    public void queueMessage(String clientID, FullHttpResponse message) {
        
        // Create empty queue if needed
        messageQueues.putIfAbsent(clientID, new ConcurrentLinkedQueue<FullHttpResponse>());
        
        synchronized (this) {
             // Check if a channel is available for use
            if (channelQueues.containsKey(clientID) && channelQueues.get(clientID).size() > 0) {
                
                Channel chan = channelQueues.get(clientID).poll();
                
                // Check that channel seems viable
                if (chan != null && chan.isActive() && chan.isOpen()) {
                    
                } else {
                    // Queue message
                    messageQueues.get(clientID).add(message);
                }
            } else {
                // Queue message
                messageQueues.get(clientID).add(message);
            }
        }
    }
    
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<FullHttpResponse>> messageQueues;
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Channel>> channelQueues;
}
