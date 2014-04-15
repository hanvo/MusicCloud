/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.old.messages;

import beatboxserver.old.Message;

import java.nio.channels.SocketChannel;

/**
 *
 * @author rahmanj
 */
public class RequestSongListMessage extends Message {
 
    public RequestSongListMessage(int messageID, String clientID) {
        super(messageName, messageID, clientID);
    }
    
    public void readHeaderContent(SocketChannel channel) {}
    public void readBody(SocketChannel channel) {}
    
    public void writeHeaderContent(SocketChannel channel) {}
    public void writeBody(SocketChannel channel) {}
    
    public final static String messageName = "REQUEST_SONG_LIST";
}
