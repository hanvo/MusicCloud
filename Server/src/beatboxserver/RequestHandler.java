/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;


import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBuf;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
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
import io.netty.util.Attribute;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;

import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.Level;

import com.google.gson.JsonObject;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * Giant class to hold all our logic to handle messages
 * @author rahmanj
 */
public class MessageHandler {
    
    public MessageHandler() {
        Logger logger = Logger.getLogger(this.getClass().getName());
        for (Handler h : logger.getHandlers()) {
            h.setLevel(Level.ALL);
        }
    }
    
    /**
     * Converts a URI path into a usable method name for reflection
     * @param path Path from the request with leading '/' removed if present
     * @return Returns the path converted into camel-case appropriate for reflection
     */
    public String normalizeMethod(String path) {
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
    
    public void authenticateClient(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.POST)) {
            // TODO Process
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void deauthenticateClient(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req,HttpMethod.POST)) {
            // TODO Process
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestSongList(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.GET)) {
            
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void vote(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.POST)) {
        
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void like(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.POST)) {
            
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void dislike(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.POST)) {
            
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestClientUpdate(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.GET)) {
            
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestLikeUpdate(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.GET)) {
            
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestVoteUpdate(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.GET)) {
            
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestSongUpdate(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.GET)) {
            
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestPhoto(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.GET)) {
            
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void authenticateSpeaker(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.POST)) {
            
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void deauthenticateSpeaker(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
       if (validateMethod(req, HttpMethod.POST)) {
           
       } else {
           sendError(ctx, METHOD_NOT_ALLOWED);
       }
    }
    
    
    public void requestSpeakerUpdate(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.GET)) {
            
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void statusUpdate(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.POST)) {
            
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestSong(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.GET)) {
            
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void ready(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req,HttpMethod.POST)) {
            
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
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
     * @param ctx {@link ChannelHandlerContext} to be used to send the error message
     * @param status {@link HttpResponseStatus} indicating the error condition
     */
    public void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        if (ctx != null && status != null) {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
            ByteBuf message = Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.US_ASCII);
            
            FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, message);
            res.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=ASCII");
            
            // Write the response back
            ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE);
            
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Send a given response object to the client
     * @param ctx {@link ChannelHandlerContext} to be used to send the response
     * @param response {@link FullHttpResponse} response to be sent to the client
     */
    public void sendResponse(ChannelHandlerContext ctx, FullHttpResponse response) {
        if (ctx != null && response != null) {
            if ((Boolean)ctx.attr(AttributeKey.valueOf("KeepAlive")).get()) {
                // Keep alive in effect
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                // No keep alive
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
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
    protected FullHttpResponse createResponse(HttpResponseStatus status, String content) {
        
        ByteBuf body = Unpooled.copiedBuffer(content, CharsetUtil.US_ASCII);
        
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, body);
        response.headers().add(CONTENT_TYPE, "application/json");
        
        return response;
    }
    
    /**
     * 
     * @param status
     * @return 
     */
    protected FullHttpResponse createResponse(HttpResponseStatus status) {
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
}
