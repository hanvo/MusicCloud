/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import java.util.Scanner;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.Channels;




/**
 *
 * @author rahmanj
 */
public abstract class Message implements ReadableMessage, WritableMessage {
    
    
    
    protected Message(String messageType, int messageID, String clientID) {
        this.messageID = messageID;
        this.messageType = messageType;
        this.clientID = clientID;
    }
    
    
    /**
     * 
     * @param channel 
     */
    public final void readMessage(SocketChannel channel) {
        readHeaderContent(channel);
        readBody(channel);
    }   
    
    
    public String getClientID() {
        return clientID;
    }
    
    public int getMessageID() {
        return messageID;
    }
    
    public final void writeMessage(SocketChannel channel) throws IOException {
        writeHeader(channel);
        writeHeaderContent(channel);
        writeBody(channel);
    }
    
    protected final void writeHeader(SocketChannel channel) throws IOException {
        if (channel != null) {
           ByteBuffer buf = createHeader();
           channel.write(buf);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    
    /**
     * Create a buffer to be sent via socket to the client
     * @return
     * @throws UnsupportedEncodingException
     */
    protected ByteBuffer createHeader() throws UnsupportedEncodingException {
        String header = messageType;
        header = header + " " + messageID + "\n";
        byte[] bytes = header.getBytes("US-ASCII");
        return ByteBuffer.wrap(bytes);
    }
    
    protected String messageType;
    protected int messageID;
    private String clientID;
}
