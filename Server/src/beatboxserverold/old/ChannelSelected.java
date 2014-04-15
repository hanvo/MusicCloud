/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver;

import java.nio.channels.SelectionKey;

/**
 *
 * @author rahmanj
 */
public interface ChannelSelected {
    public void channelSelected(SelectionKey selectedKey);
}
