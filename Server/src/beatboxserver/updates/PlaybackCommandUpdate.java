/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.updates;

/**
 *
 * @author rahmanj
 */
public class PlaybackCommandUpdate extends SessionUpdate<PlaybackCommand> {
    public PlaybackCommandUpdate(PlaybackCommand command) {
        super(UpdateType.playback_command, command);
    }
}
