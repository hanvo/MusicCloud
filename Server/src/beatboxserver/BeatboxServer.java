/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.logging.Handler;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;



/**
 *
 * @author rahmanj
 */
public class BeatboxServer {
    
    public BeatboxServer(ClientManager clientManager, SongManager songManager) {
        Logger logger = Logger.getLogger(this.getClass().getName());
        for (Handler h : logger.getHandlers()) {
            h.setLevel(Level.ALL);
        }
        
        this.clientManager = clientManager;
        this.songManager = songManager;
    }
    
    public void run() throws InterruptedException {
        // Perform application specific initialization here
        
        // TODO Application specific initialization
        
        // Read songs from crawler DB
        
        // Read config files or command line stuff
        
        // Create various objects and managers for clients, etc
        
        // Perform any needed registrations
        
        // Initialze netty
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        // Register service
        try {
            RegisterService.registerService("Beatbox Central Server", RegisterService.servicePort);
        } catch (IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Failed to register service", e);
        }
        
        // Start the server and register our channel initializer
        try {
            Logger.getLogger(this.getClass().getName()).info("Starting server");
            
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new BeatboxChannelInitializer(clientManager, songManager));
            
            b.bind(RegisterService.servicePort /* TODO TEMP */).channel().closeFuture().sync();
        } finally {
            Logger.getLogger(BeatboxServer.class.getName()).info("Shutting down server...");
            
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    
    public static void main(String[] args) {
         
        AuthenticationManager authManager = new AuthenticationManager();
        ClientManager clientManager = new ClientManager(authManager);
        SongManager songManager = new SongManager();
        
        BeatboxServer server = new BeatboxServer(clientManager, songManager);
        
        try {
            server.run();
        } catch (Exception e) {
            Logger.getLogger(server.getClass().getName()).log(Level.SEVERE, "Exception thrown in run()", e);
        }
    }
    
    private ClientManager clientManager;
    private SongManager songManager;
}
