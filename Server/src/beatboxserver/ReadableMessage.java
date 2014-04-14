/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import java.nio.channels.SocketChannel;

/**
 *
 * @author rahmanj
 */
public interface ReadableMessage {
    /**
     * 
     * @param channel 
     */
    void readHeaderContent(SocketChannel channel);
    
    /**
     * Optional method to read and parse body content.
     * Override in derived class if desired
     * @param channel 
     */
    void readBody(SocketChannel channel);   
}
