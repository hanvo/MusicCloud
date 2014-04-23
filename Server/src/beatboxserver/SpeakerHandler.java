/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;


import beatboxserver.messages.*;


import java.util.NoSuchElementException;

import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static beatboxserver.Session.SessionType;
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
    
    /**
     * 
     * @param ctx {@link ChannelHandlerContext} for this request
     * @param req {@link FullHttpRequest} send by the client
     * @param sessionID {@link long} Session ID parsed from the URI
     * @param ipAddress {@link String} IP address of the remote client
     * @param body 
     */
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
        }
    }
    
    /**
     * 
     * @param ctx {@link ChannelHandlerContext} for this request
     * @param req {@link FullHttpRequest} send by the client
     * @param sessionID {@link long} Session ID parsed from the URI
     * @param ipAddress {@link String} IP address of the remote client
     * @param body 
     */
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
                
               logger.warn("Failed to destroy session", e);
               sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
               return;
           }
           
           sendResponse(ctx.channel(), HttpResponseStatus.OK, false);
       }
    }
    
    /**
     * 
     * @param ctx {@link ChannelHandlerContext} for this request
     * @param req {@link FullHttpRequest} send by the client
     * @param sessionID {@link long} Session ID parsed from the URI
     * @param ipAddress {@link String} IP address of the remote client
     */
    public void requestUpdate(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress) {
        if (validateMethod(ctx.channel(), req, HttpMethod.GET) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            try {
                sessionMgr.registerRequest(sessionID, ctx.channel());
            } catch (Exception e) {
                logger.warn("Failed to register update request", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    /**
     * 
     * @param ctx {@link ChannelHandlerContext} for this request
     * @param req {@link FullHttpRequest} send by the client
     * @param sessionID {@link long} Session ID parsed from the URI
     * @param ipAddress {@link String} IP address of the remote client
     * @param body 
     */
    public void statusUpdate(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress, Message body) {
        if (validateMethod(ctx.channel(), req, HttpMethod.POST) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            StatusUpdateMessage message;
            
            try {
                message = (StatusUpdateMessage)body;
            } catch (ClassCastException e) {
                
                sendError(ctx.channel(), BAD_REQUEST);
                return;
            }
            
            try {
                songMgr.speakerStatusUpdate(sessionID, message);
            } catch (Exception e) {
                logger.warn("Speaker status update failed", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            sendResponse(ctx.channel(), OK, false);
        }
    }
    
    /**
     * 
     * @param ctx {@link ChannelHandlerContext} for this request
     * @param req {@link FullHttpRequest} send by the client
     * @param sessionID {@link long} Session ID parsed from the URI
     * @param ipAddress {@link String} IP address of the remote client
     */
    public void requestSong(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress) {
        if (validateMethod(ctx.channel(), req, HttpMethod.GET) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            QueryStringDecoder decoder;
            long songID;
            try {
                decoder = new QueryStringDecoder(req.getUri());
                songID = Long.parseLong(decoder.parameters().get("songID").get(0));
            } catch (Exception e) {
                logger.warn("Invalid song ID", e);
                sendError(ctx.channel(), BAD_REQUEST);
                return;
            }
            
            SongData data;
            try {
                data = songMgr.getSongData(songID);
            } catch (NoSuchElementException e) {
                logger.warn("Song not found", e);
                sendError(ctx.channel(), NOT_FOUND);
                return;
            } catch (Exception e) {
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            sendResponse(ctx.channel(), data.getSongData(), data.getSongType(), false);
        }
    }
    
    
    private final static Logger logger = LogManager.getFormatterLogger(SpeakerHandler.class.getName());
}
