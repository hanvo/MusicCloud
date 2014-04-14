/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

/**
 * Listener for messages arriving at the server
 * @author rahmanj
 */
public interface MessageListener {
    public void MessageRecieved(Message message, Client client);
}
