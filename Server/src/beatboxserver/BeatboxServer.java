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
     * 
     * @param clientManager
     * @param songManager 
     */
    public BeatboxServer(SessionManager clientManager, SongManager songManager) {
        this.clientManager = clientManager;
        this.songManager = songManager;
    }
    
    /**
     * 
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
                    .childHandler(new BeatboxChannelInitializer(clientManager, songManager));
            
            ChannelFuture future = b.bind(RegisterService.servicePort /* TODO TEMP */);
            future.sync();
            
            // Request playback to start, TODO Need more implementation on server side
            songManager.playNextSong();
            
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
        
        // TODO Application specific initialization
        
        // Read songs from crawler DB
        
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
    
    private SessionManager clientManager;
    private SongManager songManager;
    
    private static final Logger logger = LogManager.getFormatterLogger(BeatboxServer.class.getName());
}