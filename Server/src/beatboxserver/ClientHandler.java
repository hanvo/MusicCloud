/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.messages.*;

import beatboxserver.Session.SessionType;

import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static beatboxserver.Session.SessionType;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 *
 * @author rahmanj
 */
public class ClientHandler extends RequestHandler {
    
    /**
     * 
     * @param clientManager
     * @param songManager 
     */
    public ClientHandler(SessionManager clientManager, SongManager songManager) {
        super(clientManager, songManager);
        
        Logger logger = Logger.getLogger(this.getClass().getName());
        for (Handler h : logger.getHandlers()) {
            h.setLevel(Level.ALL);
        }
    }
    
    
    public void authenticate(ChannelHandlerContext ctx, FullHttpRequest req, long clientID, String ipAddress, Message body) {
        if (validateMethod(req, HttpMethod.POST)) {
            
            AuthenticateMessage message;
            UserSession session;
            
            try {
                message = (AuthenticateMessage)body;
            } catch (ClassCastException e) {
                sendError(ctx.channel(), BAD_REQUEST);
                return;
            }
            
            try {
                session = (UserSession)sessionMgr.createSession(message.pin, ipAddress, SessionType.User);
            } catch (SecurityException e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            } catch (Exception e) {
                
                Logger.getLogger(SpeakerHandler.class.getName()).log(Level.WARNING, "Failure occured", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            sendResponse(ctx.channel(), session, false);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void deauthenticate(ChannelHandlerContext ctx, FullHttpRequest req, long clientID, String ipAddress, Message body) {
        if (validateMethod(req,HttpMethod.POST)) {
            DeauthenticateMessage message;
            UserSession session;
            
            // Validate the session
            try {
                if (!sessionMgr.validSession(clientID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            try {
                message = (DeauthenticateMessage)body;
            } catch (ClassCastException e) {
                sendError(ctx.channel(), BAD_REQUEST);
                return;
            }
            
            try {
                session = (UserSession)sessionMgr.getSession(message.id);
                sessionMgr.destroySession(session);
            } catch (SecurityException e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            } catch (ClassCastException e) {
                
                sendError(ctx.channel(), BAD_REQUEST);
                return;
            } catch (Exception e) {
                
                Logger.getLogger(SpeakerHandler.class.getName()).log(Level.WARNING, "Failure occured", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestSongList(ChannelHandlerContext ctx, FullHttpRequest req, long clientID, String ipAddress) {
        if (validateMethod(req, HttpMethod.GET)) {
            
            // Validate the session
            try {
                if (!sessionMgr.validSession(clientID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void vote(ChannelHandlerContext ctx, FullHttpRequest req, long clientID, String ipAddress, Message body) {
        if (validateMethod(req, HttpMethod.POST)) {
           
            // Validate the session
            try {
                if (!sessionMgr.validSession(clientID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            UserSession client = (UserSession)sessionMgr.getSession(clientID);
  
            // TODO, your patriotic duty, and vote
            
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void like(ChannelHandlerContext ctx, FullHttpRequest req, long clientID, String ipAddress, Message body) {
        if (validateMethod(req, HttpMethod.POST)) {
            
            // Validate the session
            try {
                if (!sessionMgr.validSession(clientID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            UserSession client = (UserSession)sessionMgr.getSession(clientID);
            
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void dislike(ChannelHandlerContext ctx, FullHttpRequest req, long clientID, String ipAddress, Message body) {
        if (validateMethod(req, HttpMethod.POST)) {
            
            // Validate the session
            try {
                if (!sessionMgr.validSession(clientID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestUpdate(ChannelHandlerContext ctx, FullHttpRequest req, long clientID, String ipAddress) {
        if (validateMethod(req, HttpMethod.GET)) {
            
            // Validate the session
            try {
                if (!sessionMgr.validSession(clientID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestLikeUpdate(ChannelHandlerContext ctx, FullHttpRequest req, long clientID, String ipAddress) {
        if (validateMethod(req, HttpMethod.GET)) {
            
            // Validate the session
            try {
                if (!sessionMgr.validSession(clientID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestVoteUpdate(ChannelHandlerContext ctx, FullHttpRequest req, long clientID, String ipAddress) {
        if (validateMethod(req, HttpMethod.GET)) {
            
            // Validate the session
            try {
                if (!sessionMgr.validSession(clientID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestSongUpdate(ChannelHandlerContext ctx, FullHttpRequest req, long clientID, String ipAddress) {
        if (validateMethod(req, HttpMethod.GET)) {
            
            // Validate the session
            try {
                if (!sessionMgr.validSession(clientID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestPhoto(ChannelHandlerContext ctx, FullHttpRequest req, long clientID, String ipAddress) {
        if (validateMethod(req, HttpMethod.GET)) {
            
            // Validate the session
            try {
                if (!sessionMgr.validSession(clientID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
}
