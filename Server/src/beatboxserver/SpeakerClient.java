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
public class SpeakerClient extends Client {
    
    public SpeakerClient(int id) {
        super(id, ClientType.Speaker);
    }
    
    
    ActiveSong.StatusType status;
    
    /**
     * ID for the song the speaker is currently playing
     */
    private int activeSongID;
}
