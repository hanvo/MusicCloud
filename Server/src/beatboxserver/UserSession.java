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
public class UserSession extends Session {
    
    /**
     * 
     * @param id
     * @param ipAddress
     */
    public UserSession(long id, String ipAddress) {
        super(id, ipAddress, SessionType.User);
    }
    
    

    
}
