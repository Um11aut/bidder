package org.tradingbot.core;

import org.springframework.lang.NonNull;
import org.tradingbot.bidder.Bidder;
import org.tradingbot.impl.BidderImpl;
import org.tradingbot.bidder.BidderStrategy;
import org.tradingbot.impl.DefaultBidderWinEvaluator;

public class Auction {
    private final Bidder ownBidder;
    private final Bidder otherBidder;

    public Auction(int baseCash, @NonNull BidderStrategy ownStrategy, @NonNull BidderStrategy opponentStrategy) {
        ownBidder = new BidderImpl(10, baseCash, ownStrategy, new DefaultBidderWinEvaluator());
        otherBidder = new BidderImpl(10, baseCash, opponentStrategy, new DefaultBidderWinEvaluator());
        auctionLoop(10 / 2);
    }

    void auctionLoop(int iterations) {
        for (int i = 0; i < iterations; i++) {
            int ownBid = ownBidder.placeBid();
            int otherBid = otherBidder.placeBid();

            ownBidder.bids(ownBid, otherBid);
            otherBidder.bids(otherBid, ownBid);

            System.out.println("Bidding: " + ownBid + ' ' + otherBid);
            if (ownBid == 0 && otherBid == 0) {
                break;
            }
        }
    }
}
