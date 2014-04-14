/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.ClosedChannelException;

import java.io.IOException;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author rahmanj
 */
public final class ServerReadTask implements Runnable {
    
    public ServerReadTask(ConcurrentLinkedQueue<Message> queue, ClientManager clientMgr, MessageManager messageMgr, ServerSocketChannel chan) throws IOException {
        if (queue != null && clientMgr != null && messageMgr != null) {
            clientManager = clientMgr;
            messageManager = messageMgr;
            receivedMessages = queue;
            
            listeningChannel = chan;
            channelSelector = Selector.open();
            
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public void run() {
        
        // Register our server socket to accept connections
        try {
            acceptKey = listeningChannel.register(channelSelector, SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            // TODO Handle this
        }
        
        // Loop infinitely searching for connections or data to read
        while (true) {
            try {
                if (channelSelector.select() != 0) {
                     for (SelectionKey key : channelSelector.selectedKeys()) {
                         if (key.isAcceptable()) {
                             
                            // Accept new connection
                             ServerSocketChannel chan = (ServerSocketChannel)key.channel();
                             SocketChannel clientChannel = chan.accept();
                             
                             // Register client channel
                             SelectionKey clientKey = clientChannel.register(channelSelector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                             
                             // TODO Improve client creation
                             Client client = clientManager.createClient(clientChannel);
                             
                             // Associate client with key for later use
                             clientKey.attach(client);
                         } else if (key.isReadable()) {
                             Client client = (Client)key.attachment();
                             SocketChannel channel = (SocketChannel)key.channel();
                             
                             Message message = messageManager.readMessage(channel);
                             
                             // TODO Add checks somewhere to determine
                             // if the message is coming from the correct
                             // client, and not an impostour
                             boolean valid = true;
                             
                             if (valid) {
                                 
                                 
                                 
                             } else {
                                 // TODO Create failure message
                                 Message failureMessage = null;
                                 client.sendMessage(failureMessage);
                                 
                                 // Don't keep listening for this client
                                 // Note, if we move to one selector, we'll
                                 // Need to change this to simply remove the
                                 // Read interest set
                                 channelSelector.keys().remove(key);
                             }
                             
                             receivedMessages.add(message);
                         }
                     }
                }
            } catch (IOException e) {
                // TODO Handle
            }
        }
    }
    
    protected ConcurrentLinkedQueue<Message> receivedMessages;
    protected MessageManager messageManager;
    
    protected ClientManager clientManager;
    
    protected ServerSocketChannel listeningChannel;
    protected Selector channelSelector;
    protected SelectionKey acceptKey;
}
