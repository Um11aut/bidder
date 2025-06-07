package org.tradingbot.core;

import org.tradingbot.impl.BidderImpl;
import org.tradingbot.strategies.BidderStrategy;

public class Auction {
    private BidderImpl ownBidder;
    private BidderImpl opponentBidder;

    Auction(int baseQuantity, int baseCash, BidderStrategy ownStrategy, BidderStrategy opponentStrategy) {
        ownBidder = new BidderImpl(baseQuantity, baseCash, ownStrategy);
        opponentBidder = new BidderImpl(baseQuantity, baseCash, opponentStrategy);
    }

    void auctionLoop() {

    }
}
