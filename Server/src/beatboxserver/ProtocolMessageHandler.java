/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.messages.*;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;

import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * 
 * @author rahmanj
 */
public class ProtocolMessageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    
    /**
     * Construct new instance of the {@link ProtocolMessageHandler}
     * @param clientManager {@link SessionManager} to use when handling messages
     * @param songManager {@link SongManager} to use when handling messages
     */
    public ProtocolMessageHandler(SessionManager clientManager, SongManager songManager) {
        sessionMgr = clientManager;
        songMgr = songManager;
    }
    
    /**
     * Respond to the arrival of a new HTTP request
     * @param ctx The {@link ChannelHandlerContext} for this channel
     * @param req The {@link FullHttpRequest} received
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        logger.debug("Request recieved");
        
        dispatchRequest(req, ctx);
    }
    
    /**
     * Dispatch a request based on given information
     * @param request {@link FullHttpRequest} to be dispatched to the appropriate handler class and method
     * @param ctx {@link ChannelHandlerContext} for this request
     */
    protected void dispatchRequest(FullHttpRequest request, ChannelHandlerContext ctx) {
        
        // Parse the query string to determine the request type
        QueryStringDecoder decoder = new QueryStringDecoder(request.getUri());
        
        // Get the path
        String path = decoder.path();
        String handlerName;
        String methodName;
        String messageName;
        Message message = null;
        
        String ipAddress;
        
        
        String[] components = path.split("/");
        if (components.length != 3) {
            logger.warn("Invalid request for path: \"%s\"", path);
            RequestHandler.sendError(ctx.channel(), NOT_FOUND);
            return;
        }
        
        handlerName = components[1];
        methodName = components[2];
        messageName = components[2];
        
        // Normalize names
        handlerName = RequestHandler.normalizeHandler(handlerName);
        methodName = RequestHandler.normalizeMethod(methodName);
        messageName = Message.normalizeName(messageName);
        
 
        boolean keepAlive = false;
        long sessionID;
        
        // Determine KeepAlive status and set context attribute
        if (decoder.parameters().containsKey("Connection")) {
            if (decoder.parameters().get("Connection").get(0).equalsIgnoreCase("Connection")) {
                keepAlive = true;
            }
        }
        
        // TODO Deal with attr key issues
        
        // Try to get request handler
        Class handlerClass;
        try {
            handlerClass = Class.forName(handlerName);
            if (!RequestHandler.class.isAssignableFrom(handlerClass)) {
                
                logger.error("Invalid handler class");
                RequestHandler.sendError(ctx.channel(), NOT_FOUND);
                return;
            }
        } catch (ClassNotFoundException e) {
            
            logger.error("Invalid request for handler: %s", handlerName, e);
            RequestHandler.sendError(ctx.channel(), NOT_FOUND);
            return;
        }
        
        // Create the request handler class
        RequestHandler requestHandler; 
        try {
            Constructor ctor = handlerClass.getDeclaredConstructor(SessionManager.class, SongManager.class);
            requestHandler = (RequestHandler)handlerClass.cast(ctor.newInstance(sessionMgr, songMgr));
        } catch (Exception e) {
            
            logger.error("Invalid request for constructor", e);
            RequestHandler.sendError(ctx.channel(), NOT_FOUND);
            return;    
        }
       
                
        // Handler method signature is
        //
        // ChannelHandlerContext
        // FullHttpRequest
        // long (Client ID, -1 if not present)
        // String IP Address
        // Object (Json Decoded request body, if POST
        
        // Try to get method, send 404 if not found
        Method method;
        try {
            
            // Get the appropriate method based on request type
            if (request.getMethod().equals(HttpMethod.GET)) {
                
                method = handlerClass.getMethod(methodName, ChannelHandlerContext.class, FullHttpRequest.class, long.class, String.class);
            } else if (request.getMethod().equals(HttpMethod.POST)) {
                
                method = handlerClass.getMethod(methodName, ChannelHandlerContext.class, FullHttpRequest.class, long.class, String.class, Message.class);
            } else {
                
                logger.warn("Invalid request method");
                RequestHandler.sendError(ctx.channel(), NOT_ACCEPTABLE);
                return;
            }
            
        } catch (NoSuchMethodException e) {
            
            logger.error("Invalid request for method: %s", methodName);
            RequestHandler.sendError(ctx.channel(), HttpResponseStatus.NOT_FOUND);
            return;
        }
        
        // Parse out session ID if present
        if (decoder.parameters().containsKey("clientID")) {
            try {
                sessionID = Long.parseLong(decoder.parameters().get("clientID").get(0));
            } catch (NumberFormatException e) {
                
                logger.warn("Invalid clientID");
                RequestHandler.sendError(ctx.channel(), BAD_REQUEST);
                return;
            }
        } else {
            logger.warn("No session id given in request");
            sessionID = -1;
        }
        
        // Extract the IP address
        try {
            ipAddress = ((InetSocketAddress)ctx.channel().remoteAddress()).getAddress().getHostAddress();
        } catch (Exception e) {
            
            logger.warn("Invalid IP address");
            RequestHandler.sendError(ctx.channel(), INTERNAL_SERVER_ERROR);
            return;
        }
        
        // Parse out JSON encoded contents if needed
        if (request.getMethod().equals(HttpMethod.POST)) {
            try {
                logger.trace("JSON Message: %s", request.content().toString(CharsetUtil.US_ASCII));
                message = Message.constructMessage(messageName, request.content().toString(CharsetUtil.US_ASCII));
                logger.trace("Message class: %s", message.getClass().getName());
            } catch (Exception e) {
                
                logger.error("Exception occured while creating message", e);
                RequestHandler.sendError(ctx.channel(), BAD_REQUEST);
                return;
            }
        }
        
        try {
            
            logger.info("Dispatching request to %s.%s", handlerName, methodName);
            
            // Dispatch based on the request type
            if (request.getMethod().equals(HttpMethod.GET)) {   
                method.invoke(requestHandler, ctx, request, sessionID, ipAddress);
            } else {
                method.invoke(requestHandler, ctx, request, sessionID, ipAddress, message);
            }
            
        } catch (Exception e) {
            logger.error("Exception thrown while dispatching", e);
        }
    }
    
    private final SessionManager sessionMgr;
    private final SongManager songMgr;
    
    private final static Logger logger = LogManager.getFormatterLogger((ProtocolMessageHandler.class.getName()));
}
