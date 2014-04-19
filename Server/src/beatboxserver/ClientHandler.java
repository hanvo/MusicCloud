/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.messages.Message;

import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 *
 * @author rahmanj
 */
public class ClientHandler extends RequestHandler {
    
    public ClientHandler(ClientManager clientManager, SongManager songManager) {
        super(clientManager, songManager);
        
        Logger logger = Logger.getLogger(this.getClass().getName());
        for (Handler h : logger.getHandlers()) {
            h.setLevel(Level.ALL);
        }
    }
    
    public void authenticate(ChannelHandlerContext ctx, FullHttpRequest req, String clientID, Message body) {
        if (validateMethod(req, HttpMethod.POST)) {
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void deauthenticate(ChannelHandlerContext ctx, FullHttpRequest req, String clientID, Message body) {
        if (validateMethod(req,HttpMethod.POST)) {
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestSongList(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.GET)) {
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void vote(ChannelHandlerContext ctx, FullHttpRequest req, String clientID, Message body) {
        if (validateMethod(req, HttpMethod.POST)) {
           
            
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void like(ChannelHandlerContext ctx, FullHttpRequest req, String clientID, Message body) {
        if (validateMethod(req, HttpMethod.POST)) {
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void dislike(ChannelHandlerContext ctx, FullHttpRequest req, String clientID, Message body) {
        if (validateMethod(req, HttpMethod.POST)) {
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestUpdate(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.GET)) {
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestLikeUpdate(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.GET)) {
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestVoteUpdate(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.GET)) {
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestSongUpdate(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.GET)) {
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestPhoto(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.GET)) {
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
}
