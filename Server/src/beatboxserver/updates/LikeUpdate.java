/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.updates;

import java.util.Map;

/**
 *
 * @author rahmanj
 */
public class LikeUpdate extends SessionUpdate<LikeData> {
    public LikeUpdate(LikeData likes) {
        super(UpdateType.likes, likes);
    }
}
