package com.optimax.tradingbot.bidder;

import org.springframework.lang.NonNull;
import com.optimax.tradingbot.impl.BidderContext;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParameters;

import java.util.OptionalInt;

/**
 * Strategy for which the bidder chooses
 */
public interface BidderStrategy {

    /**
     * @param params
     *              Unified algorithm parameters for the strategy
     */
    void init(@NonNull BidderStrategyParameters params);

    /**
     * Pull next bid from the strategy
     * @param own
     *           The POV BidderState
     * @param ctx
     *           Shared between bidders context
     * @return
     *           empty if no bid possible, otherwise finite amount of cash to be sacrificed
     */
    OptionalInt nextBid(BidderState own, BidderContext ctx);

    /**
     * Finish the round after all the bids done
     */
    default void finishRound() {
    }
}
