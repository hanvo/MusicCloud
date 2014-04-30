/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import io.netty.channel.socket.SocketChannel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author rahmanj
 */
public class BeatboxChannelInitializer extends ChannelInitializer<SocketChannel> {
    
    /**
     * Construct a new {@link BeatboxChannelInitializer}
     * @param clientManager {@link SessionManager} for the server's clients
     * @param songManager {@link SongManager} for the song selection
     */
    public BeatboxChannelInitializer(SessionManager clientManager, SongManager songManager) {
        clientMgr = clientManager;
        songMgr = songManager;
    }
    
    /**
     * Initialize a newly established channel for further use
     * @param ch {@link SocketChannel} to be initialized
     */
    @Override
    protected void initChannel(SocketChannel ch) {
        final ChannelPipeline pipeline = ch.pipeline();

        logger.info("Recieved connection from %s", ch.remoteAddress().getHostString());
        
        // Create pipeline for handling HTTP requests to this channel
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("handler", new ProtocolMessageHandler(clientMgr, songMgr));
    }
    
    private SessionManager clientMgr;
    private SongManager songMgr;
    
    private final static Logger logger = LogManager.getFormatterLogger(BeatboxChannelInitializer.class.getName());
}
