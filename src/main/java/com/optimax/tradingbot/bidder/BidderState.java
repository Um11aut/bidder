package com.optimax.tradingbot.bidder;

public interface BidderState {
    /**
     * @return
     *        Remaining quantity
     */
    int getQuantity();

    /**
     * @return
     *        Remaining cash
     */
    int getCash();

    /**
     * @return
     *       Set id for bidder
     */
    String getId();

    /**
     * @return
     *       Initial quantity
     */
    int getTotalQuantity();
}
