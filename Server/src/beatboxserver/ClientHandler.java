/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.messages.*;
import beatboxserver.updates.*;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.sql.SQLException;

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
    }
    
    
    public void authenticate(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress, Message body) {
        if (validateMethod(ctx.channel(), req, HttpMethod.POST)) {
            
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
                
                logger.warn("Failure occured", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            sendResponse(ctx.channel(), session, false);
        }
    }
    
    
    public void deauthenticate(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress, Message body) {
        if (validateMethod(ctx.channel(), req, HttpMethod.POST) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            DeauthenticateMessage message;
            UserSession session;
            
            // Validate the session
            try {
                if (!sessionMgr.validSession(sessionID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                logger.warn("Failed to validate session", e);
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
            
        } else {
            sendError(ctx.channel(), METHOD_NOT_ALLOWED);
        }
    }
    
    
    public void requestSongList(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress) {
        if (validateMethod(ctx.channel(), req, HttpMethod.GET) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            // Validate the session
            try {
                if (!sessionMgr.validSession(sessionID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                logger.warn("Failed to validate session", e);
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            List<Song> songList;
            try {
                songList = songMgr.getSongList();
            } catch (SQLException e) {
                logger.warn("Exception while retreiving the song list", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            sendResponse(ctx.channel(), songList, false);
        }
    }
    
    
    public void vote(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress, Message body) throws SQLException {
        if (validateMethod(ctx.channel(), req, HttpMethod.POST) && validateSession(ctx.channel(), sessionID, ipAddress)) {
           
            // Validate the session
            try {
                if (!sessionMgr.validSession(sessionID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                logger.warn("Failed to validate session", e);
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            UserSession client = (UserSession)sessionMgr.getSession(sessionID);
  
            // TODO, your patriotic duty, and vote
            VoteMessage message;
            try {
                message = (VoteMessage)body;
            } catch (ClassCastException e) {
                sendError(ctx.channel(), BAD_REQUEST);
                return;
            }
            
            try {
                songMgr.vote(message.id, sessionID);
            } catch (SQLException e) {
                logger.warn("Failed to record vote for the song", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            sendResponse(ctx.channel(), OK, false);
        }
    }
    
    
    public void like(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress, Message body) {
        if (validateMethod(ctx.channel(), req, HttpMethod.POST) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            LikeMessage message;
            try {
                message = (LikeMessage)body;
            } catch (ClassCastException e) {
                logger.warn("Bad request", e);
                sendError(ctx.channel(), BAD_REQUEST);
                return;
            }
            
            try {
                songMgr.like(message.id, sessionID);
            } catch (Exception e) {
                logger.warn("Failed to like song", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            sendResponse(ctx.channel(), OK, false);
        }
    }
    
    
    public void dislike(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress, Message body) {
        if (validateMethod(ctx.channel(), req, HttpMethod.POST) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            // Validate the session
            try {
                if (!sessionMgr.validSession(sessionID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            DislikeMessage message;
            try {
                message = (DislikeMessage)body;
            } catch (ClassCastException e) {
                logger.warn("Bad request", e);
                sendError(ctx.channel(), BAD_REQUEST);
                return;
            }
            
            try {
                songMgr.dislike(message.id, sessionID);
            } catch (Exception e) {
                logger.warn("Failed to like song", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            sendResponse(ctx.channel(), OK, false);
        }
    }
    
    
    public void requestUpdate(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress) {
        if (validateMethod(ctx.channel(), req, HttpMethod.GET) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            // Validate the session
            try {
                if (!sessionMgr.validSession(sessionID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            try {
                sessionMgr.registerRequest(sessionID, ctx.channel());
            } catch (Exception e) {
                logger.warn("Failed to register update request", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    
    public void requestLikeUpdate(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress) {
        if (validateMethod(ctx.channel(), req, HttpMethod.GET) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            // Validate the session
            try {
                if (!sessionMgr.validSession(sessionID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            // TODO, use like data? Change SongManager Interface?
            SongStats stats;
            try {
                stats = songMgr.getStats();
            } catch (Exception e) {
                logger.warn("Failed to get current like stats", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            if (stats == null) {
                sendError(ctx.channel(), NOT_FOUND);
                return;
            }
            
            // TODO sendResponse(ctx.channel(), new LikeUpdate(stats), false);
        }
    }
    
    
    public void requestVoteUpdate(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress) {
        if (validateMethod(ctx.channel(), req, HttpMethod.GET) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            // Validate the session
            try {
                if (!sessionMgr.validSession(sessionID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        }
    }
    
    
    public void requestSongUpdate(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress) {
        if (validateMethod(ctx.channel(), req, HttpMethod.GET) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            // Validate the session
            try {
                if (!sessionMgr.validSession(sessionID, ipAddress)) {
                    sendError(ctx.channel(), FORBIDDEN);
                    return;
                }
            } catch (Exception e) {
                sendError(ctx.channel(), FORBIDDEN);
                return;
            }
            
            ActiveSong activeSong;
            try {
                activeSong = songMgr.getActiveSong();
            } catch (Exception e) {
                logger.warn("Failed to get active song", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            if (activeSong == null) {
                sendError(ctx.channel(), NOT_FOUND);
                return;
            }
            
            sendResponse(ctx.channel(), new SongUpdate(activeSong), false);
        }
    }
    
    
    public void requestPhoto(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress) {
        if (validateMethod(ctx.channel(), req, HttpMethod.GET) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            
            QueryStringDecoder decoder;
            long songID;
            try {
                decoder = new QueryStringDecoder(req.getUri());
                songID = Long.parseLong(decoder.parameters().get("songID").get(0));
            } catch (Exception e) {
                logger.warn("Invalid song ID", e);
                sendError(ctx.channel(), BAD_REQUEST);
            }
            
            sendError(ctx.channel(), NOT_IMPLEMENTED);
        }
    }
    
    private final static Logger logger = LogManager.getFormatterLogger(ClientHandler.class.getName());
}
