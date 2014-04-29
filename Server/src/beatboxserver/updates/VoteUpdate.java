/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package beatboxserver.updates;

import java.util.List;

/**
 *
 * @author rahmanj
 */
public class VoteUpdate extends SessionUpdate<List<VoteData>>{
    public VoteUpdate(List<VoteData> votes) {
        super(UpdateType.votes, votes);
    }
}
