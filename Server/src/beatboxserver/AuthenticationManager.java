/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;


 /**
  *
 * @author rahmanj
 */
public class AuthenticationManager {
    
    /**
     * Construct new {@link AuthenticationManager}
     */
    public AuthenticationManager() {
        
    }
    
    /**
     * Authenticate a session given the pin
     * @param pin
     * @return 
     */
    public boolean authenticate(String pin) {
        // TODO, more thoughout logic
        return pin.equals("1234");
    }
}
