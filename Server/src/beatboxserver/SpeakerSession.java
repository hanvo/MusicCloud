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
public class SpeakerSession extends Session {
    
    public SpeakerSession(long id, String ipAddress) {
        super(id, ipAddress, SessionType.Speaker);
    }
    
    
    ActiveSong.StatusType status;
    
    /**
     * ID for the song the speaker is currently playing
     */
    private int activeSongID;
}
