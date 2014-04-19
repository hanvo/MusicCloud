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
    
    public Client(String id) {
        if (id != null) {
            this.id = id;
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    public String getID() {
        return id;
    }
    
    
    private String id;
}
