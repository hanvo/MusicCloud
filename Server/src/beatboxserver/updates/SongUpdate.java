/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.updates;

import beatboxserver.ActiveSong;

/**
 *
 * @author rahmanj
 */
public class SongUpdate extends SessionUpdate<ActiveSong> {
    public SongUpdate(ActiveSong song) {
        super(UpdateType.current_song, song);
    }
}
