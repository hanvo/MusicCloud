/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.ProtocolMessageHandler;
import beatboxserver.RequestHandler;

import io.netty.channel.socket.SocketChannel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import java.util.logging.Handler;
import java.util.logging.Level;

import java.util.logging.Logger;

/**
 *
 * @author rahmanj
 */
public class BeatboxChannelInitializer extends ChannelInitializer<SocketChannel> {
    
    /**
     * Construct a new instance of {@link BeatboxChannelInitializer}
     * @param clientManager {@link ClientManager} for the server's clients
     * @param songManager {@link SongManager} for the song selection
     */
    public BeatboxChannelInitializer(ClientManager clientManager, SongManager songManager) {
        Logger logger = Logger.getLogger(this.getClass().getName());
        for (Handler h : logger.getHandlers()) {
            h.setLevel(Level.ALL);
        }
    }
    
    /**
     * Initialize a newly established channel for further use
     * @param ch {@link SocketChannel} Newly created channel to be initialized
     */
    @Override
    protected void initChannel(SocketChannel ch) {
        final ChannelPipeline pipeline = ch.pipeline();
        

        Logger l = Logger.getLogger(this.getClass().getName());
        l.log(Level.INFO, "Recieved connection from " + ch.remoteAddress().getHostString());
        
        // Create pipeline for handling HTTP requests to this channel
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("handler", new ProtocolMessageHandler(clientMgr, songMgr));
    }
    
    private ClientManager clientMgr;
    private SongManager songMgr;
}
