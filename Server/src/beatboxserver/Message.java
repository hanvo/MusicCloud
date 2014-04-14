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
public abstract class Message {
    
    
    
    protected Message(String messageType, int messageID, String clientID) {
        this.messageID = messageID;
        this.messageType = messageType;
        this.clientID = clientID;
    }
    
    
    
    
    /**
     * 
     * @param channel 
     */
    public void parseMessage(SocketChannel channel) {
        parseHeader(channel);
        parseHeaderContent(channel);
        parseBody(channel);
    }
    
    /**
     * 
     * @param channel
     * @return 
     */
    protected final Message parseHeader(SocketChannel channel) {
        InputStream s = Channels.newInputStream(channel);
        InputStreamReader reader = new InputStreamReader(s);
        
        // TODO Read in input and parse
        
        String headerLine = "";
        Scanner scan = new Scanner(headerLine);
        
        // Read in the standard "MessageType MessageID ClientID\n" header
        messageType = scan.next();
        messageID = scan.nextInt();
        clientID = scan.next();
        
        // TODO Use message type to construct derived type message
        return null;
    }
    
    /**
     * 
     * @param channel 
     */
    protected void parseHeaderContent(SocketChannel channel) {}
    
    /**
     * Optional method to read and parse body content.
     * Override in derived class if desired
     * @param channel 
     */
    protected void parseBody(SocketChannel channel) {}
    
    
    public String getClientID() {
        return clientID;
    }
    
    private String clientID;
    
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
     * Write the bulk of the header content.
     * Override in derived class.
     */
    protected void writeHeaderContent(SocketChannel channel) {}
    
    /**
     * Write body content after the end of the header.
     * Override in derived class if desired
     */
    protected void writeBody(SocketChannel channel) {}
    
    /**
     * Create a buffer to be sent via socket to the client
     * @return 
     */
    protected ByteBuffer createHeader() throws UnsupportedEncodingException {
        String header = messageType;
        header = header + " " + messageID + "\n";
        byte[] bytes = header.getBytes("US-ASCII");
        return ByteBuffer.wrap(bytes);
    }
    
    protected String messageType;
    protected int messageID;
}
