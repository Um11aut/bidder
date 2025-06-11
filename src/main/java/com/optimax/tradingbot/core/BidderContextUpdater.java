package com.optimax.tradingbot.core;

import com.optimax.tradingbot.bidder.Bidder;
import com.optimax.tradingbot.impl.BidderContext;
import com.optimax.tradingbot.impl.BidderHistoryUnit;

import java.util.HashMap;
import java.util.Map;

public class BidderContextUpdater {

    BidderContextUpdater() {
    }

    /**
     * Update bidder context history and states
     * @param ctx
     *           The Bidder context
     */
    public static void updateBidderContext(BidderContext ctx, Bidder ownBidder, Bidder otherBidder, int ownBid, int otherBid) {
        Map<String, Integer> map = new HashMap<>();

        ctx.putState(ownBidder.getState());
        ctx.putState(otherBidder.getState());

        map.put(ownBidder.getState().id(), ownBid);
        map.put(otherBidder.getState().id(), otherBid);

        ctx.addHistoryUnit(new BidderHistoryUnit(map));
    }
}
