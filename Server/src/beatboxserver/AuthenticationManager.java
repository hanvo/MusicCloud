/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import beatboxserver.Client.ClientType;

 /**
  *
 * @author rahmanj
 */
public class AuthenticationManager {
    
    public AuthenticationManager() {
        
    }
    
    
    public boolean authenticate(String pin) {
        // TODO
        return pin.equals("1234");
    }
}
