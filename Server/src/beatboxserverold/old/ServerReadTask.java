/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.old;

import beatboxserver.old.messages.FailedMessage;

import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.ClosedChannelException;

import java.io.IOException;

/**
 *
 * @author rahmanj
 */
public final class ServerReadTask implements Runnable, ChannelSelected {
    
    public ServerReadTask(ClientManager clientMgr, MessageManager messageMgr, SelectorManager selectorMgr, ServerSocketChannel chan) throws IOException {
        if (clientMgr != null && messageMgr != null) {
            clientManager = clientMgr;
            messageManager = messageMgr;
            selectorManager = selectorMgr;
            
            // Set this instance as the read listener
            selectorManager.registerReadListener(this);
            selectorManager.registerAcceptSocket(chan);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public void run() {
        selectorManager.runReadSelection();
    }
    
    @Override
    public void channelSelected(SelectionKey key) {
        if (key != null) {
            if (key.isAcceptable()) {
                // TODO Accept new connection
            } else if (key.isReadable()) {
                // TODO Read new message
                
                Client client = (Client)key.attachment();
                Message newMessage = messageManager.readMessage(client);
                
                if (newMessage.getClientID().equals(client.getClientID())) {
                    
                    // Forward the message onward to the main thread
                    messageManager.enqueueMessage(newMessage);
                } else {
                    // Send failure message since the client ID's don't match
                    
                    // TODO Send FailedMessage to the client
                    
                }
            }
        }
    }
    
    protected MessageManager messageManager;
    protected ClientManager clientManager;
    protected SelectorManager selectorManager;
}
