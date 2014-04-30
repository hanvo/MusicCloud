/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.updates;

import beatboxserver.Song;

import java.util.List;

/**
 *
 * @author rahmanj
 */
public class SongListUpdate extends SessionUpdate<List<Song>> {
    public SongListUpdate(List<Song> songs) {
        super(UpdateType.song_list, songs);
    }
}
