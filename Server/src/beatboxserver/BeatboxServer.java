/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 *
 * @author rahmanj
 */
public class BeatboxServer {
    
    /**
     * Construct a new {@link BeatboxServer}
     * @param sessionManager {@link SessionManager} for the server
     * @param songManager {@link SongManager} for the server
     */
    public BeatboxServer(SessionManager sessionManager, SongManager songManager) {
        this.sessionManager = sessionManager;
        this.songManager = songManager;
    }
    
    /**
     * Run the server loop
     * @throws InterruptedException
     * @throws IOException 
     */
    public void run() throws InterruptedException, IOException {
        
        RegisterService registrar = null;
        try {
            registrar = new RegisterService();
        } catch (IOException e) {
            logger.warn("Failed to register service", e);
        }
        
        
        
        // Initialze netty
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        // Register service
        try {
            if (registrar != null) {
                registrar.registerService("Beatbox Central Server", RegisterService.servicePort);
            } else {
                logger.warn("Failed to register service");
            }
        } catch (IOException e) {
            logger.warn("Failed to register service", e);
        }
        
        // Start the server and register our channel initializer
        try {
            logger.info("Starting server");
            
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new BeatboxChannelInitializer(sessionManager, songManager));
            
            ChannelFuture future = b.bind(RegisterService.servicePort /* TODO TEMP */);

            
            future.sync();
            
            // Block on channel closure
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            
            logger.fatal("Exception while running server", e);
        } finally {
            logger.info("Shutting down server...");
            
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        
        if (registrar != null) {
            registrar.DeregisterService();
        }
    }
    
    public static void main(String[] args) {
        
        DatabaseManager databaseManager;
        AuthenticationManager authManager;
        SessionManager sessionManager;
        SongManager songManager;
        
        // Perform application specific initialization here
        
        // Read config files or command line stuff
        
        // Create various objects and managers for clients, etc
        
        // Perform any needed registrations
        
        try {
            
            // Create manager objects for the major sub-systems
            databaseManager = new DatabaseManager("song_list.db");
            authManager = new AuthenticationManager();
            sessionManager = new SessionManager(databaseManager, authManager);
            songManager = new SongManager(databaseManager, sessionManager);
            
        } catch (Exception e) {
            
            logger.fatal("Failed to initialize server", e);
            System.exit(1);
            return; // Make compiler happy about uninitialized sessionManager and songManager objects
        }
        
        BeatboxServer server = new BeatboxServer(sessionManager, songManager);
        
        try {
            server.run();
        } catch (Exception e) {
            
            logger.error("Exception thrown in run()", e);
            System.exit(1);
        }
    }
    
    private SessionManager sessionManager;
    private SongManager songManager;
    
    private static final Logger logger = LogManager.getFormatterLogger(BeatboxServer.class.getName());
}
