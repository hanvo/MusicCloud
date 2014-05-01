/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.updates;


import com.google.gson.annotations.Expose;

/**
 *
 * @author rahmanj
 */
public class PlaybackCommand {
    
    /**
     * 
     */
    public enum Command {Play, Stop};
    
    /**
     * Construct a new {@link PlaybackCommand}
     * @param command {@link Command} for the {@link SpeakerSession}
     * @param songID ID of the song
     */
    public PlaybackCommand(Command command, long songID) {
        this.command = command;
        id = songID;
    }
    
    
    @Expose
    private final Command command;
    
    @Expose
    private final long id;
}
