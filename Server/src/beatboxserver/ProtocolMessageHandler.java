/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.MessageHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.*;
import io.netty.handler.codec.http.QueryStringDecoder;

import io.netty.util.AttributeKey;
import io.netty.util.Attribute;
import io.netty.util.CharsetUtil;

import java.util.logging.Logger;
import java.lang.reflect.Method;
import java.util.logging.Handler;
import java.util.logging.Level;

/**
 *
 * @author rahmanj
 */
public class ProtocolMessageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    
    public ProtocolMessageHandler(MessageHandler handler) {
        Logger logger = Logger.getLogger(this.getClass().getName());
        for (Handler h : logger.getHandlers()) {
            h.setLevel(Level.ALL);
        }
        messageHandler = handler;
    }
    
    /**
     * Respond to the arrival of a new HTTP request
     * @param ctx The {@link ChannelHandlerContext} for this channel
     * @param req The {@link FullHttpRequest} received
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        Logger.getLogger(this.getClass().getName()).fine("Request recieved");
        
        dispatchRequest(req, ctx);
    }
    
    /**
     * Dispatch a request based on given information
     * @param request
     * @param ctx 
     */
    protected void dispatchRequest(FullHttpRequest request, ChannelHandlerContext ctx) {
        
        // Parse the query string to determine the request type
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        
        // Get the path
        String path = decoder.path();
        path = path.substring(1); // Remove '/'
        
        String messageType = messageHandler.normalizeMethod(path);
        Method method;
        
        boolean keepAlive = false;
        String clientID;
        
        // Determine KeepAlive status and set context attribute
        if (decoder.parameters().containsKey("Connection")) {
            if (decoder.parameters().get("Connection").get(0).equalsIgnoreCase("Connection")) {
                keepAlive = true;
            }
        }
        
        Attribute attr = ctx.attr(AttributeKey.valueOf("KeepAlive"));
        attr.set(keepAlive);
        
                
        // Handler method signature is
        //
        // ChannelHandlerContext
        // FullHttpRequest
        // String (Client ID, null if not present)
        
        // Try to get method, send 404 if not found
        try {
            method = messageHandler.getClass().getMethod(messageType, ChannelHandlerContext.class, FullHttpRequest.class, String.class);
        } catch (NoSuchMethodException e) {
            Logger.getLogger(this.getClass().getName()).warning("Invalid request for method: \"" + messageType + "\"");
            
            messageHandler.sendError(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }
        
        // Parse out client ID if present
        if (decoder.parameters().containsKey("clientID")) {
            clientID = decoder.parameters().get("clientID").get(0);
        } else {
            clientID = null;
        }
        
        try {
            method.invoke(messageHandler, ctx, request, clientID);
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Exception thrown while dispatching", e);
        }
    }
        
        /*switch (type) {
            // Phone client methods first
            case "authenticate_client":
                // Requesting authentication
                // TODO 
                break;
            case "deauthenticate_client":
                // Deauthenticating
                // TODO
                break;
            case "request_update":
                // Opening an HTTP Long polling session for updates
                // TODO
                break;
            case "request_song_update":
                // Requesting an immediate current song update
                // TODO
                break;
            case "request_like_update":
                // Requesting an immediate like update
                // TODO 
                break;
            case "request_vote_update":
                // Requesting an immediate vote update
                // TODO
                break;
            case "request_song_list":
                // Requesting an immediate song list
                // TODO
                break;
            case "like":
                // Sending a like for a song
                // TODO
                break;
            case "dislike":
                // Sending a dislike for a song
                // TODO
                break;
            case "vote":
                // Sending a vote for a song
                // TODO
                break;
                
            --------> Speaker Request Dispatch <-------------
                
                
            case "authenticate_speaker":
                // Request from speaker to authenticate
                // TODO
                break;
            case "deauthenticate_speaker":
                // Server disconnecting and leaving
                // TODO
                break;
            case "status_update":
                // Speaker sending status update
                // TODO
                break;
            case "request_speaker_update":
                // Speaker openning an HTTP long polling connection
                // to recieve speaker commands
                // TODO
                break;
            case "ready":
                // Speaker indicating a song is ready to play
                // TODO
                break;
            case "request_song":
                // Ask server for a given song
                // TODO
                break;
            default:
                l.warning("Unknown request type: \"" + type + "\" received from client: \"" + clientID + "\"" );
         
        }*/
        
    private MessageHandler messageHandler;

}
