/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import com.google.gson.FieldNamingPolicy;
import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBuf;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.HttpResponseStatus;

import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

import java.util.List;
import java.util.Map;

import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;

/**
 * Giant class to hold all our logic to handle messages
 * @author rahmanj
 */
public abstract class RequestHandler {
    
    
    /**
     * Constructor for {@link RequestHandler}
     * @param clientManager {@link ClientManager} for the server
     * @param songManager {@link SongManager} for the server
     */
    public RequestHandler(ClientManager clientManager, SongManager songManager) {
        Logger logger = Logger.getLogger(this.getClass().getName());
        for (Handler h : logger.getHandlers()) {
            h.setLevel(Level.ALL);
        }
        
        if (clientManager == null || songManager == null) {
            throw new IllegalArgumentException();
        }
        
        clientMgr = clientManager;
        songMgr = songManager;
    }
    
    
    /**
     * Converts a URI path into a usable method name for reflection
     * @param path Path from the request with leading '/' removed if present
     * @return Returns the path converted into camel-case appropriate for reflection
     */
    public static String normalizeMethod(String path) {
        StringBuilder sb = new StringBuilder();
        
        // Split on underscores
        String[] components = path.split("_");
        
        String original;
        String modified;
        
        // Perform lower_case_underscore to camelCase conversion consistent with method names
        for (int i = 0; i < components.length; i++) {
            if (components[i].length() > 0) {
                if (i != 0) {
                    original = String.valueOf(components[i].charAt(0));
                    modified = original.toUpperCase();
                } else {
                    original = String.valueOf(components[i].charAt(0));
                    modified = original.toLowerCase();
                }
                components[i] = components[i].replaceFirst(original, modified);
            }
        }
        
        // Stich these together
        for (String s : components) {
            sb.append(s);
        }
        
        return sb.toString();
    }
    
    
    /**
     * 
     * @param name
     * @return 
     */
    public static String normalizeHandler(String name) {
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
        sb.append(RequestHandler.class.getPackage().getName());
        sb.append(".");
        for (String s : components) {
            sb.append(s);
        }
        sb.append("Handler");
        
        return sb.toString();
    }
    
    
    /**
     * Parse a {@link Map<String, List<String>>} containing the query string from the request
     * @param request The {@link FullHttpRequest} to parse the query string from
     * @return Returns the parsed query string
     */
    private Map<String, List<String>> getQueryString(FullHttpRequest request) {
        if (request != null) {
            QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
            return decoder.parameters();
        } else {
            throw new IllegalArgumentException();
        }
    } 
    
   
    /**
     * Send an error response to the client
     * @param ch {@link Channel} to be used to send the error message
     * @param status {@link HttpResponseStatus} indicating the error condition
     */
    public static void sendError(Channel ch, HttpResponseStatus status) {
        if (ch != null && status != null) {
            
            Logger.getLogger(RequestHandler.class.getName()).warning("Sending " + status.toString() + " error");
            
            ByteBuf message = Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.US_ASCII);
            
            FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, message);
            res.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=ASCII");
            
            // Write the response back
            ch.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
            
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    
    /**
     * Send a given response object to the client
     * @param ch {@link Channel} to be used to send the response
     * @param response {@link FullHttpResponse} response to be sent to the client
     * @param keepAlive 
     */
    public static void sendResponse(Channel ch, Object data, boolean keepAlive) {
        if (ch != null && data != null) {
            
            InetSocketAddress addr = (InetSocketAddress)ch.remoteAddress();
            Logger.getLogger(RequestHandler.class.getName()).info("Sending response to " + addr.getHostString());
            
            Gson gson = (new GsonBuilder())
                    .excludeFieldsWithoutExposeAnnotation()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
            
            String body = gson.toJson(data);
            FullHttpResponse response = createResponse(HttpResponseStatus.OK, body);
            
            if (keepAlive) {
                
                // Keep alive in effect
                ch.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                
                // No keep alive
                ch.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    
    /**
     * Creates an {@link FullHttpResponse} object containing the given JSON encoded string
     * @param status {@link HttpResponseStatus} for the response
     * @param content {@link String} containing JSON for the request
     * @return Returns a {@link FullHttpResponse} that can be sent to the client
     */
    protected static FullHttpResponse createResponse(HttpResponseStatus status, String content) {
        
        ByteBuf body = Unpooled.copiedBuffer(content, CharsetUtil.US_ASCII);
        
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, body);
        response.headers().add(CONTENT_TYPE, "application/json");
        
        return response;
    }
    
    
    /**
     * Create a basic {@link FullHttpResponse} instance for later use
     * @param status {@link HttpResponseStatus} for the {@link FullHttpResponse}
     * @return Returns a {@link FullHttpResponse} instance for use
     */
    protected static FullHttpResponse createResponse(HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        return response;
    }
    
    
    /**
     * 
     * @param req
     * @param method
     * @return 
     */
    protected boolean validateMethod(FullHttpRequest req, HttpMethod method) {
        if (req != null && method != null) {
            if (!req.getMethod().equals(method)) {
                return false;
            } else {
                return true;
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    protected ClientManager clientMgr;
    protected SongManager songMgr;
}
