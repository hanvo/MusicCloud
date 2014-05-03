/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.updates.VoteData;
import beatboxserver.messages.*;
import beatboxserver.updates.*;

import java.util.List;
import java.util.NoSuchElementException;

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
     * Construct a new {@link ClientHandler}
     * @param sessionManager {@link SessionManager} for the handler
     * @param songManager {@link SongManager} for this handler
     */
    public ClientHandler(SessionManager sessionManager, SongManager songManager) {
        super(sessionManager, songManager);
    }
    
    
    /**
     * Authenticate an incoming connection
     * @param ctx {@link ChannelHandlerContext} for this request
     * @param req {@link FullHttpRequest} send by the client
     * @param sessionID {@link long} Session ID parsed from the URI
     * @param ipAddress {@link String} IP address of the remote client
     * @param body {@link Message} from the incoming request body
     */
    public void authenticate(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress, Message body) {
        if (validateMethod(ctx.channel(), req, HttpMethod.POST)) {
            
            AuthenticateMessage message;
            UserSession session;
            
            try {
                message = (AuthenticateMessage)body;
            } catch (ClassCastException e) {
                
                logger.warn("Error with message type", e);
                sendError(ctx.channel(), BAD_REQUEST);
                return;
            }
            
            try {
                session = (UserSession)sessionMgr.createSession(message.pin, ipAddress, SessionType.User);
            } catch (SecurityException e) {
                
                logger.warn("Security exception", e);
                sendError(ctx.channel(), FORBIDDEN);
                return;
            } catch (Exception e) {
                
                logger.warn("Failure occured", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            try {
                sendResponse(ctx.channel(), session, false);
            } catch (Exception e) {
                
                logger.warn("Failed create response", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    /**
     * Deauthenticate a given session
     * @param ctx {@link ChannelHandlerContext} for this request
     * @param req {@link FullHttpRequest} send by the client
     * @param sessionID {@link long} Session ID parsed from the URI
     * @param ipAddress {@link String} IP address of the remote client
     * @param body body {@link Message} from the incoming request body
     */
    public void deauthenticate(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress, Message body) {
        if (validateMethod(ctx.channel(), req, HttpMethod.POST)) {
            DeauthenticateMessage message;
            
            try {
                message = (DeauthenticateMessage)body;
            } catch (ClassCastException e) {
                
                logger.warn("Error with message type", e);
                sendError(ctx.channel(), BAD_REQUEST);
                return;
            }
            
            //  Validate session before deauthenticating
            if (!validateSession(ctx.channel(), message.id, ipAddress)) {
                return;
            }
            
            try {
                sessionMgr.destroySession(message.id);
            } catch (SecurityException e) {
                
                logger.warn("Failed to remove session", e);
                sendError(ctx.channel(), FORBIDDEN);
                return;
            } catch (ClassCastException e) {
                
                logger.warn("Bad request from client", e);
                sendError(ctx.channel(), BAD_REQUEST);
                return;
            } catch (Exception e) {
                
                logger.warn("Failure occured", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            sendResponse(ctx.channel(), OK, false);
        }
    }
    

    //<editor-fold defaultstate="collapsed" desc="Client Actions">
    /**
     * Vote on a given song
     * @param ctx {@link ChannelHandlerContext} for this request
     * @param req {@link FullHttpRequest} send by the client
     * @param sessionID {@link long} Session ID parsed from the URI
     * @param ipAddress {@link String} IP address of the remote client
     * @param body body {@link Message} from the incoming request body
     * @throws SQLException
     */
    public void vote(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress, Message body) throws SQLException {
        if (validateMethod(ctx.channel(), req, HttpMethod.POST) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            // Do, your patriotic duty, and vote
            VoteMessage message;
            try {
                message = (VoteMessage)body;
            } catch (Exception e) {
                
                logger.warn("Failed to submit ballot", e);
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
    
    /**
     * Like the current song (ID given for consistency)
     * @param ctx {@link ChannelHandlerContext} for this request
     * @param req {@link FullHttpRequest} send by the client
     * @param sessionID {@link long} Session ID parsed from the URI
     * @param ipAddress {@link String} IP address of the remote client
     * @param body body {@link Message} from the incoming request body
     */
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
    
    /**
     * Dislike the current song (ID given for consistency)
     * @param ctx {@link ChannelHandlerContext} for this request
     * @param req {@link FullHttpRequest} send by the client
     * @param sessionID {@link long} Session ID parsed from the URI
     * @param ipAddress {@link String} IP address of the remote client
     * @param body body {@link Message} from the incoming request body
     */
    public void dislike(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress, Message body) {
        if (validateMethod(ctx.channel(), req, HttpMethod.POST) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
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
//</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Client Requests">
    
    /**
     * Request a subscription for an update
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
     * Request an immediate like update for the current song
     * @param ctx {@link ChannelHandlerContext} for this request
     * @param req {@link FullHttpRequest} send by the client
     * @param sessionID {@link long} Session ID parsed from the URI
     * @param ipAddress {@link String} IP address of the remote client
     */
    public void requestLikeUpdate(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress) {
        if (validateMethod(ctx.channel(), req, HttpMethod.GET) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            LikeData stats;
            
            try {
                stats = songMgr.getStats();
            } catch (NoSuchElementException e) {
                
                logger.warn("No current song with stats available");
                sendError(ctx.channel(), SERVICE_UNAVAILABLE);
                return;
            } catch (Exception e) {
                
                logger.warn("Failed to get current like stats", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            if (stats == null) {
                
                logger.warn("Failed to find stats, NULL given");
                sendError(ctx.channel(), NOT_FOUND);
                return;
            }
            
            try {
                sendResponse(ctx.channel(), new LikeUpdate(stats), false);
            } catch (Exception e) {
                
                logger.warn("Failed create response", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    /**
     * Request an immediate vote update
     * @param ctx {@link ChannelHandlerContext} for this request
     * @param req {@link FullHttpRequest} send by the client
     * @param sessionID {@link long} Session ID parsed from the URI
     * @param ipAddress {@link String} IP address of the remote client
     */
    public void requestVoteUpdate(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress) {
        if (validateMethod(ctx.channel(), req, HttpMethod.GET) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            List<VoteData> votes;
            try {
                votes = songMgr.getVotes();
            } catch (Exception e) {
                
                logger.warn("Failed to get votes", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            try {
                sendResponse(ctx.channel(), new VoteUpdate(votes), false);
            } catch (Exception e) {
                
                logger.warn("Failed create response", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    /**
     * Request an immediate update on the current song
     * @param ctx {@link ChannelHandlerContext} for this request
     * @param req {@link FullHttpRequest} send by the client
     * @param sessionID {@link long} Session ID parsed from the URI
     * @param ipAddress {@link String} IP address of the remote client
     */
    public void requestSongUpdate(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress) {
        if (validateMethod(ctx.channel(), req, HttpMethod.GET) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            ActiveSong activeSong;
            try {
                activeSong = songMgr.getActiveSong(); // TODO Need to finish this
            } catch (Exception e) {
                
                logger.warn("Failed to get active song", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            if (activeSong == null) {
                
                logger.warn("No active song, NULL returned");
                sendError(ctx.channel(), NOT_FOUND);
                return;
            }
            
            try {
                sendResponse(ctx.channel(), new SongUpdate(activeSong), false);
            } catch (Exception e) {
                
                logger.warn("Failed create response", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    /**
     * Request the full song list from the server
     * @param ctx {@link ChannelHandlerContext} for this request
     * @param req {@link FullHttpRequest} send by the client
     * @param sessionID {@link long} Session ID parsed from the URI
     * @param ipAddress {@link String} IP address of the remote client 
     */
    public void requestSongList(ChannelHandlerContext ctx, FullHttpRequest req, long sessionID, String ipAddress) {
        if (validateMethod(ctx.channel(), req, HttpMethod.GET) && validateSession(ctx.channel(), sessionID, ipAddress)) {
            
            List<Song> songList;
            try {
                songList = songMgr.getSongList();
            } catch (SQLException e) {
                
                logger.warn("Exception while retreiving the song list", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            try {
                sendResponse(ctx.channel(), new SongListUpdate(songList), false);
            } catch (Exception e) {
                
                logger.warn("Failed create response", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
            }
        }
    }
    
    /**
     * Request the photo for a given song
     * @param ctx {@link ChannelHandlerContext} for this request
     * @param req {@link FullHttpRequest} send by the client
     * @param sessionID {@link long} Session ID parsed from the URI
     * @param ipAddress {@link String} IP address of the remote client
     */
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
                return;
            }
            
            SongPhoto photo;
            try {
                photo = songMgr.getSongPhoto(songID);
            } catch (NoSuchElementException e) {
                
                logger.warn("Failed to find photo", e);
                sendError(ctx.channel(), NOT_FOUND);
                return;
            } catch (Exception e) {
                
                logger.warn("Error retrieving photo", e);
                sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
                return;
            }
            
            sendResponse(ctx.channel(), photo.getImageData(), photo.getImageType(), false);
        }
    }
    
//</editor-fold>
    
    private final static Logger logger = LogManager.getFormatterLogger(ClientHandler.class.getName());
}
