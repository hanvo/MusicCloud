/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

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
public class SpeakerHandler extends RequestHandler {
    
    public SpeakerHandler() {
        Logger logger = Logger.getLogger(this.getClass().getName());
        for (Handler h : logger.getHandlers()) {
            h.setLevel(Level.ALL);
        }
    }
    
    public void authenticate(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.POST)) {
            sendError(ctx, NOT_IMPLEMENTED);
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void deauthenticate(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
       if (validateMethod(req, HttpMethod.POST)) {
           sendError(ctx, NOT_IMPLEMENTED);
       } else {
           sendError(ctx, METHOD_NOT_ALLOWED);
       }
    }
    
    
    public void requestSpeakerUpdate(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.GET)) {
            sendError(ctx, NOT_IMPLEMENTED);
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void statusUpdate(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.POST)) {
            sendError(ctx, NOT_IMPLEMENTED);
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestSong(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req, HttpMethod.GET)) {
            sendError(ctx, NOT_IMPLEMENTED);
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void ready(ChannelHandlerContext ctx, FullHttpRequest req, String clientID) {
        if (validateMethod(req,HttpMethod.POST)) {
            sendError(ctx, NOT_IMPLEMENTED);
        } else {
            sendError(ctx, METHOD_NOT_ALLOWED);
        }
    }
}
