package org.tradingbot.strategies.builder;

import org.springframework.lang.NonNull;
import org.tradingbot.strategies.builder.enums.BidderStrategyGreediness;
import org.tradingbot.utils.Pair;

import java.security.InvalidParameterException;

/**
 * The Parameters Builder for the BidderStrategy
 */
public class BidderStrategyParametersBuilder {
    private BidderStrategyGreediness greediness = BidderStrategyGreediness.MEDIUM;
    private Pair<Integer, Integer> riskRewardRatio = new Pair<>(1,1);
    private int maxRounds = 0;

    BidderStrategyParametersBuilder() {
    }

    /**
     * Initializes the builder with default parameters.
     * Greediness: MEDIUM
     * Risk/Reward Ratio: 1:1
     * Max Rounds: unset
     */
    public static BidderStrategyParametersBuilder defaultBuilder() {
        return new BidderStrategyParametersBuilder();
    }

    /**
     * @param greediness
     *                  greed factor
     */
    @NonNull
    public BidderStrategyParametersBuilder withGreediness(@NonNull BidderStrategyGreediness greediness) {
        this.greediness = greediness;
        return this;
    }

    /**
     * @param risk
     *            cash that can be exchanged for reward
     * @param reward
     *            cash that can be acquired with given risk
     */
    @NonNull
    public BidderStrategyParametersBuilder withRiskRewardRatio(int risk, int reward) {
        riskRewardRatio = new Pair<>(risk, reward);
        return this;
    }

    @NonNull
    public BidderStrategyParametersBuilder withMaxRounds(int maxRounds) {
        this.maxRounds = maxRounds;
        return this;
    }

    @NonNull
    public BidderStrategyParameters build() throws InvalidParameterException, NullPointerException {
        if (maxRounds < 0) {
            throw new InvalidParameterException("maxRounds must meet the condition >= 0");
        }
        if (riskRewardRatio.getFirst() <= 0) {
            throw new InvalidParameterException("Risk parameter must meet condition > 0");
        }
        if (riskRewardRatio.getSecond() <= 0) {
            throw new InvalidParameterException("Reward parameter must meet the condition > 0");
        }
        if (riskRewardRatio.getFirst() > riskRewardRatio.getSecond()) {
            throw new InvalidParameterException("Risk cannot be greater than reward");
        }
        if (greediness == null) {
            throw new NullPointerException("Greediness is null");
        }
        return new BidderStrategyParameters(greediness, riskRewardRatio, maxRounds);
    }
}