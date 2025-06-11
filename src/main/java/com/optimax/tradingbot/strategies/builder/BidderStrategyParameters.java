package com.optimax.tradingbot.strategies.builder;

import org.springframework.lang.NonNull;
import com.optimax.tradingbot.strategies.builder.enums.BidderStrategyGreediness;
import com.optimax.tradingbot.utils.Pair;

import java.util.OptionalInt;

/**
 * Parameters for Bidder Strategy
 * Includes greediness, risk-to-reward ratio and maximum rounds
 */
public final class BidderStrategyParameters {

    private final BidderStrategyGreediness greediness;
    private final Pair<Integer, Integer> riskRewardRatio;
    private final int maxRounds;

    BidderStrategyParameters(BidderStrategyGreediness greediness, Pair<Integer, Integer> riskRewardRatio, int maxRounds) {
        this.greediness = greediness;
        this.riskRewardRatio = riskRewardRatio;
        this.maxRounds = maxRounds;
    }

    /**
     * @return
     *        Enum that specifies greediness
     */
    @NonNull
    public BidderStrategyGreediness greediness() {
        return greediness;
    }

    /**
     * @return
     *        First specifies the risk, Second the reward
     */
    @NonNull
    public Pair<Integer, Integer> riskRewardRatio() {
        return riskRewardRatio;
    }

    /**
     * @return
     *        Maximum amount of rounds can be played, empty if not provided
     */
    @NonNull
    public OptionalInt maxRounds() {
        return maxRounds != 0 ? OptionalInt.of(maxRounds) : OptionalInt.empty();
    }
}