/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.messages;

import beatboxserver.Message;

import java.nio.channels.SocketChannel;

/**
 *
 * @author rahmanj
 */
public class SongListMessage extends Message {
 
    public SongListMessage(int messageID, String clientID) {
        super(messageName, messageID, clientID);
    }
    
    public void parseHeaderContent(SocketChannel channel) {}
    public void parseBody(SocketChannel channel) {}
    
    public void writeHeaderContent(SocketChannel channel) {}
    public void writeBody(SocketChannel channel) {}
    
    public final static String messageName = "SONG_LIST";
}
