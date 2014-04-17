/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.MessageHandler;
import io.netty.channel.ChannelFutureListener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.LinkedList;
import java.util.logging.Logger;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.nio.charset.Charset;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 *
 * @author rahmanj
 */
public class ProtocolMessageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    
    public ProtocolMessageHandler(MessageHandler handler) {
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
        
        HashMap<String,String> map;
        String[] flattenedQueryString = null;
        String clientID = null;
                
        // Signature is
        // HashMap<String, String> (Request body, null if not present)
        // String (Client ID, null if not present)
        // String[] (Pairs of additional query string keys and values 2i is key 2i+1 is value)
        
        // Try to get method, send 404 if not found
        try {
            method = messageHandler.getClass().getMethod(messageType, Object.class, String.class, String[].class);
        } catch (NoSuchMethodException e) {
            Logger.getLogger(this.getClass().getName()).warning("Invalid request for method: \"" + messageType + "\"");
            FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
            
            // Write the response back, respecting any keep alive given
            HttpHeaders headers = request.headers();
            if (headers.contains("Connection") && headers.get("Connection").equalsIgnoreCase("Keep-Alive")) {
                ctx.write(res);
            } else {
                ctx.write(res).addListener(ChannelFutureListener.CLOSE);
            }
            return;
        }
        
        // Process the query string and parse the request body
        if (decoder.parameters().keySet().size() > 0) {
            LinkedList<String> l = new LinkedList<>();
            for (String key : decoder.parameters().keySet()) {
                for (String value : decoder.parameters().get(key)) {
                    l.addLast(key);
                    l.addLast(value);
                }
            }

            flattenedQueryString = l.toArray(new String[1]);
        } else {
            flattenedQueryString = null;
        }
        
        // Try parsing the request body if JSON
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<HashMap<String,String>>(){}.getClass();
            map = gson.fromJson(request.content().toString(Charset.forName("US-ASCII")), type);
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, "Exception while parsing JSON", e);
            map = null;
        }
        
        // Parse out client ID if present
        if (decoder.parameters().containsKey("clientID")) {
            clientID = decoder.parameters().get("clientID").get(0);
        } else {
            clientID = null;
        }
        
        try {
            method.invoke(ctx, map, clientID, flattenedQueryString);
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
