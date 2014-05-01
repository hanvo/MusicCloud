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
public class SongPhoto {
    
    /**
     * Construct a new {@link SongPhoto} instance
     * @param data {@link ByteBuf} containing image data
     * @param type {@link String} MIME image type
     */
    public SongPhoto(ByteBuf data, String type) {
        imageData = data;
        imageType = type;
    }
    
    public String getImageType() {
        return imageType;
    }
    
    public ByteBuf getImageData() {
        return imageData;
    }
    
    private final String imageType;
    private final ByteBuf imageData;
}
