package com.optimax.tradingbot.bidder;

/**
 * Represents the current state of a bidder participating in the trading bot.
 * Provides information about the bidder's remaining resources and identity.
 */
public interface BidderState {

    /**
     * Returns the remaining quantity that the bidder still holds and can trade.
     *
     * @return
     *          the current quantity available to the bidder
     */
    int getQuantity();

    /**
     * Returns the remaining cash available to the bidder for placing bids.
     *
     * @return
     *          the current cash balance of the bidder
     */
    int getCash();

    /**
     * Returns the unique identifier of the bidder.
     *
     * @return
     *          the identifier string assigned to this bidder
     */
    String getId();

    /**
     * Returns the initial total quantity allocated to the bidder at the start.
     *
     * @return
     *          the original total quantity assigned to the bidder
     */
    int getTotalQuantity();
}
