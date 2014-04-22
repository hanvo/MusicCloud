/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import io.netty.buffer.ByteBuf;

/**
 *
 * @author rahmanj
 */
public class SongData {
 
    public SongData(ByteBuf data, String type) {
        songData = data;
        songType = type;
    }
    
    public String getSongType() {
        return songType;
    }
    
    public ByteBuf getSongData() {
        return songData;
    }
    
    private String songType;
    private ByteBuf songData;
}
