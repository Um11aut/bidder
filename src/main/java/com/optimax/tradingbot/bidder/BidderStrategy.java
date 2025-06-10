package com.optimax.tradingbot.bidder;

import org.springframework.lang.NonNull;
import com.optimax.tradingbot.impl.BidderContext;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParameters;

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
