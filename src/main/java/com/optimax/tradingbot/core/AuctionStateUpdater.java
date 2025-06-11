package com.optimax.tradingbot.core;

import com.optimax.tradingbot.bidder.BidderWinEvaluator;

/**
 * Responsible for updating the auction state based on the bids placed in a round.
 */
public record AuctionStateUpdater(BidderWinEvaluator ownWinEvaluator, BidderWinEvaluator otherWinEvaluator) {
    /**
     * Updates the given auction state based on the current bids.
     *
     * @param state    The current auction state.
     * @param ownBid   The bid placed by the "own" bidder.
     * @param otherBid The bid placed by the "other" bidder.
     */
    public void updateAuctionState(AuctionState state, int ownBid, int otherBid) {
        // Evaluate won quantities based on the rules.
        int ownWonQuantity = ownWinEvaluator.evaluateWonQuantity(ownBid, otherBid);
        int otherWonQuantity = otherWinEvaluator.evaluateWonQuantity(otherBid, ownBid);

        // Update quantities won
        state.setOwnBidderCurrentQuantityWon(state.getOwnBidderCurrentQuantityWon() + ownWonQuantity);
        state.setOtherBidderCurrentQuantityWon(state.getOtherBidderCurrentQuantityWon() + otherWonQuantity);

        // Update remaining quantity
        // Note: Assumes that the sum of possible quantity is always the same as the auctioned amount
        state.setRemainingQuantity(state.getRemainingQuantity() - (ownWonQuantity + otherWonQuantity));

        // Update cash
        state.setOwnBidderCurrentCash(state.getOwnBidderCurrentCash() - ownBid);
        state.setOtherBidderCurrentCash(state.getOtherBidderCurrentCash() - otherBid);
    }
}