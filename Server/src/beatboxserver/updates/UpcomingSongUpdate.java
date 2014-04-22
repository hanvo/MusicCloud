/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.updates;

import beatboxserver.Song;

/**
 *
 * @author rahmanj
 */
public class UpcomingSongUpdate extends SessionUpdate<Song> {
    public UpcomingSongUpdate(Song s) {
        super(UpdateType.upcoming_song, s);
    }
}
