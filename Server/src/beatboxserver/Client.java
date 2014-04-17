/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import io.netty.channel.Channel;

/**
 *
 * @author rahmanj
 */
public class Client {
    
    public Client(Channel ch) {
        if (ch != null) {
            chan = ch;
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    public Channel getContext() {
        return chan;
    }
    
    private String id;
}
