package org.tradingbot.bidder;

import org.springframework.lang.NonNull;
import org.tradingbot.impl.BidderContext;
import org.tradingbot.strategies.builder.BidderStrategyParameters;

import java.util.OptionalInt;

public interface BidderStrategy {
    /**
     * @param params
     *              Unified parameters for the strategy
     */
    void init(@NonNull BidderStrategyParameters params);

    /**
     * @return
     *        empty if no bid possible, otherwise finite amount of cash to be sacrificed
     */
    OptionalInt nextBid(BidderContext ctx);

    /**
     * Finish the round after all the bids done
     */
    default void finishRound() {
    }
}
