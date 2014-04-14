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
public interface WritableMessage {
    /**
     * Write the bulk of the header content.
     * Override in derived class.
     * @param channel
     */
    void writeHeaderContent(SocketChannel channel);
    
    /**
     * Write body content after the end of the header.
     * Override in derived class if desired
     * @param channel
     */
    void writeBody(SocketChannel channel);
}
