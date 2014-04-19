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
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;

import io.netty.util.AttributeKey;
import io.netty.util.Attribute;
import io.netty.util.CharsetUtil;

import java.util.logging.Logger;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.logging.Handler;
import java.util.logging.Level;

import static io.netty.handler.codec.http.HttpResponseStatus.*;

/**
 * 
 * @author rahmanj
 */
public class ProtocolMessageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    
    /**
     * Construct new instance of the {@link ProtocolMessageHandler}
     * @param clientManager {@link ClientManager} to use when handling messages
     * @param songManager {@link SongManager} to use when handling messages
     */
    public ProtocolMessageHandler(ClientManager clientManager, SongManager songManager) {
        Logger logger = Logger.getLogger(this.getClass().getName());
        for (Handler h : logger.getHandlers()) {
            h.setLevel(Level.ALL);
        }
    }
    
    /**
     * Respond to the arrival of a new HTTP request
     * @param ctx The {@link ChannelHandlerContext} for this channel
     * @param req The {@link FullHttpRequest} received
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        Logger.getLogger(this.getClass().getName()).fine("Request recieved");
        
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
        
        
        
        String[] components = path.split("/");
        if (components.length != 3) {
            Logger.getLogger(this.getClass().getName()).warning("Invalid request for method: \"" + path + "\"");
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
        String clientID;
        
        // Determine KeepAlive status and set context attribute
        if (decoder.parameters().containsKey("Connection")) {
            if (decoder.parameters().get("Connection").get(0).equalsIgnoreCase("Connection")) {
                keepAlive = true;
            }
        }
        
        Attribute attr = ctx.attr(AttributeKey.valueOf("KeepAlive"));
        attr.set(keepAlive);
        
        // Try to get request handler
        Class handlerClass;
        try {
            handlerClass = Class.forName(handlerName);
            if (!RequestHandler.class.isAssignableFrom(handlerClass)) {
                RequestHandler.sendError(ctx.channel(), NOT_FOUND);
                return;
            }
        } catch (ClassNotFoundException e) {
            Logger.getLogger(this.getClass().getName()).warning("Invalid request for handler: \"" + handlerName + "\"");
            RequestHandler.sendError(ctx.channel(), NOT_FOUND);
            return;
        }
        
        RequestHandler requestHandler; 
        try {
            Constructor ctor = handlerClass.getDeclaredConstructor(ClientManager.class, SongManager.class);
            requestHandler = (RequestHandler)handlerClass.cast(ctor.newInstance());
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).warning("Invalid request for method: \"" + handlerName + "\"");
            RequestHandler.sendError(ctx.channel(), NOT_FOUND);
            return;    
        }
       
                
        // Handler method signature is
        //
        // ChannelHandlerContext
        // FullHttpRequest
        // String (Client ID, null if not present)
        // Object (Json Decoded request body, if POST
        
        // Try to get method, send 404 if not found
        Method method;
        try {
            
            // Get the appropriate method based on request type
            if (request.getMethod().equals(HttpMethod.GET)) {
                method = handlerClass.getMethod(methodName, ChannelHandlerContext.class, FullHttpRequest.class, String.class);
            } else if (request.getMethod().equals(HttpMethod.POST)) {
                method = handlerClass.getMethod(methodName, ChannelHandlerContext.class, FullHttpRequest.class, String.class, Message.class);
            } else {
                
                Logger.getLogger(this.getClass().getName()).warning("Invalid request method");
                RequestHandler.sendError(ctx.channel(), NOT_ACCEPTABLE);
                return;
            }
            
        } catch (NoSuchMethodException e) {
            
            Logger.getLogger(this.getClass().getName()).warning("Invalid request for method: \"" + methodName + "\"");
            RequestHandler.sendError(ctx.channel(), HttpResponseStatus.NOT_FOUND);
            return;
        }
        
        // Parse out client ID if present
        if (decoder.parameters().containsKey("clientID")) {
            clientID = decoder.parameters().get("clientID").get(0);
        } else {
            clientID = null;
        }
        
        // Parse out JSON encoded contents if needed
        if (request.getMethod().equals(HttpMethod.POST)) {
            try {
                message = Message.constructMessage(messageName, request.content().toString(CharsetUtil.US_ASCII));
            } catch (Exception e) {
                RequestHandler.sendError(ctx.channel(), BAD_REQUEST);
                return;
            }
        }
        
        try {
            
            // Dispatch based on the request type
            if (request.getMethod().equals(HttpMethod.GET)) {   
                method.invoke(requestHandler, ctx, request, clientID);
            } else {
                method.invoke(requestHandler, ctx, request, clientID, message);
            }
            
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Exception thrown while dispatching", e);
        }
    }
    
    private ClientManager clientMgr;
    private SongManager songMgr;
}
