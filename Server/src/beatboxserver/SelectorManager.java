/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.Client;
import beatboxserver.messages.FailedMessage;

import java.util.Iterator;

import java.io.IOException;

import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.ClosedChannelException;

/**
 *
 * @author rahmanj
 */
public class SelectorManager {
    
    public SelectorManager() throws IOException {
        readSelector = Selector.open();
        writeSelector = Selector.open();
        
        readListener = null;
        writeListener = null;
    }
    
    /**
     * 
     */
    public void runReadSelection() {
        selectLoop(readSelector, readListener);
    }
    
    /**
     * 
     */
    public void runWriteSelection() {
        selectLoop(writeSelector, writeListener);
    }
    
    /**
     * 
     * @param listener 
     */
    public void registerReadListener(ChannelSelected listener) {
        readListener = listener;
    }
    
    /**
     * 
     * @param listener 
     */
    public void registerWriteListener(ChannelSelected listener) {
        writeListener = listener;
    }
    
    /**
     * 
     * @param client
     * @throws ClosedChannelException 
     */
    public void registerReadClient(Client client) throws ClosedChannelException {
        registerSelector(readSelector, client, SelectionKey.OP_READ);
    }
    
    /**
     * 
     * @param client 
     */
    public void deregisterReadClient(Client client) {
        deregisterSelector(readSelector, client);
    }
    
    /**
     * 
     * @param client
     * @throws ClosedChannelException 
     */
    public void registerWriteClient(Client client) throws ClosedChannelException {
        registerSelector(writeSelector, client, SelectionKey.OP_WRITE);
    }
    
    /**
     * 
     * @param client 
     */
    public void deregisterWriteClient(Client client) {
        deregisterSelector(writeSelector, client);
    }
    
    /**
     * 
     * @param channel
     * @throws ClosedChannelException 
     */
    public void registerAcceptSocket(ServerSocketChannel channel) throws ClosedChannelException {
        if (channel != null) {
            SelectionKey key = channel.register(readSelector, SelectionKey.OP_ACCEPT);
            key.attach(null);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * 
     * @param channel 
     */
    public void deregisterAcceptSocket(ServerSocketChannel channel) {
        if (channel != null) {
            SelectionKey key = channel.keyFor(readSelector);
            if (key != null) {
                key.cancel();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * 
     * @param selector
     * @param listener 
     */
    protected final void selectLoop(Selector selector, ChannelSelected listener) {
         // Loop infinitely searching for connections or data to read
        while (true) {
            try {
                if (selector.select() != 0) {
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    SelectionKey key;
                    
                     while (iter.hasNext()) {
                         key = iter.next();
                         
                         // TODO Consider having the callback notify infomation about the channel status
                         listener.channelSelected(key);
                         
                         // Key has been processed, remove from selected set
                         selector.selectedKeys().remove(key);
                     }
                }
            } catch (IOException e) {
                // TODO Handle
            }
        }
    }
     
    /**
     * 
     * @param selector
     * @param client
     * @param ops
     * @throws ClosedChannelException 
     */
    protected final void registerSelector(Selector selector, Client client, int ops) throws ClosedChannelException {
        if (selector != null && client != null) {
            SelectionKey key = client.getChannel().register(selector, ops);
            key.attach(client);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * 
     * @param selector
     * @param client 
     */
    protected final void deregisterSelector(Selector selector, Client client) {
        if (client != null && selector != null) {
            SelectionKey key = client.getChannel().keyFor(selector);
            if (key != null) {
                key.cancel();
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    protected ChannelSelected readListener;
    protected ChannelSelected writeListener;
    protected Selector readSelector;
    protected Selector writeSelector;
}



