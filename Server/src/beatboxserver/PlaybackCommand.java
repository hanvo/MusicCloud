/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;


import com.google.gson.annotations.Expose;

/**
 *
 * @author rahmanj
 */
public class PlaybackCommand {
    
    /**
     * 
     */
    public enum Command {Play, Pause, Stop};
    
    /**
     * 
     * @param command
     * @param songID 
     */
    public PlaybackCommand(Command command, String songID) {
        this.command = command;
        id = songID;
    }
    
    
    @Expose
    private Command command;
    
    @Expose
    private String id;
}
