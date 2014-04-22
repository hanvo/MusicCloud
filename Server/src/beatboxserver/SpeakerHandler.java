/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.messages.AuthenticateMessage;
import beatboxserver.messages.DeauthenticateMessage;
import beatboxserver.messages.Message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static beatboxserver.Session.SessionType;
import static beatboxserver.RequestHandler.sendError;
import static beatboxserver.RequestHandler.sendResponse;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 *
 * @author rahmanj
 */
public class SpeakerHandler extends RequestHandler {
    
    /**
     * 
     * @param clientManager
     * @param songManager 
     */
    public SpeakerHandler(SessionManager clientManager, SongManager songManager) {
        super(clientManager, songManager);
    }
    
    public void authenticate(ChannelHandlerContext ctx, FullHttpRequest req, long clientID, String ipAddress, Message body) {
        if (validateMethod(ctx.channel(), req, HttpMethod.POST)) {
            
            AuthenticateMessage message;
            SpeakerSession session;
            
            try {
                message = (AuthenticateMessage)body;
            } catch (ClassCastException e) {
                sendError(ctx.channel(), BAD_REQUEST);
                return;
            }
            
            try {
                session = (SpeakerSession)sessionMgr.createSession(message.pin, ipAddress, SessionType.Speaker);
            } catch (SecurityException e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            } catch (Exception e) {
                
                logger.warn("Failure occured", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            sendResponse(ctx.channel(), session, false);
            
            
            sendResponse(ctx.channel(), session, false);
        }
    }
    
    
    public void deauthenticate(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress, Message body) {
       if (validateMethod(ctx.channel(), req, HttpMethod.POST) && validateSession(ctx.channel(), sessionID, ipAddress)) {
           DeauthenticateMessage message;
           SpeakerSession session;
            
           try {
               message = (DeauthenticateMessage)body;
           } catch (ClassCastException e) {
               sendError(ctx.channel(), BAD_REQUEST);
               return;
           }
           
           try {
               sessionMgr.destroySession(message.id);
           } catch (SecurityException e) {
               sendError(ctx.channel(), FORBIDDEN);
               return;
           } catch (ClassCastException e) {
               
               sendError(ctx.channel(), BAD_REQUEST);
               return;
           } catch (Exception e) {
                
               logger.warn("Failure occured", e);
               sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
               return;
           }
           
           sendResponse(ctx.channel(), HttpResponseStatus.OK, false);
       }
    }
    
    
    public void requestSpeakerUpdate(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress) {
        if (validateMethod(ctx.channel(), req, HttpMethod.GET) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        }
    }
    
    
    public void statusUpdate(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress, Message body) {
        if (validateMethod(ctx.channel(), req, HttpMethod.POST) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        }
    }
    
    
    public void requestSong(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress) {
        if (validateMethod(ctx.channel(), req, HttpMethod.GET) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
           sendError(ctx.channel(), NOT_IMPLEMENTED);
        }
    }
    
    
    public void ready(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress, Message body) {
        if (validateMethod(ctx.channel(), req, HttpMethod.POST) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        }
    }
    
    private final static Logger logger = LogManager.getFormatterLogger(SpeakerHandler.class.getName());
}
