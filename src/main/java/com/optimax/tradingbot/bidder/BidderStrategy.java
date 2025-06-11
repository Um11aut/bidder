package com.optimax.tradingbot.bidder;

import org.springframework.lang.NonNull;
import com.optimax.tradingbot.impl.BidderContext;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParameters;

import java.util.OptionalInt;

/**
 * The Strategy for bidding
 */
public interface BidderStrategy {

    /**
     * @param params
     *              Unified parameters for the strategy
     */
    void init(@NonNull BidderStrategyParameters params);

    /**
     * Pull next bid from the strategy
     * @param own
     *           The POV BidderState
     * @param ctx
     *           The shared between bidders context
     * @return
     *           empty if no bid possible, otherwise finite amount of cash to be sacrificed
     */
    OptionalInt nextBid(BidderState own, BidderContext ctx);

    /**
     * Finish the round after all the bids done
     * Needed if we have more than one bid per round
     */
    default void finishRound() {
    }
}
