package org.tradingbot.strategies.builder;

import org.tradingbot.utils.Pair;

public class BidderParametersBuilder {
    private BidderGreediness greedFactor;
    private Pair<Integer, Integer> riskRewardRatio;

    public void setGreedFactor(BidderGreediness greedFactor) {
        this.greedFactor = greedFactor;
    }

    BidderParametersBuilder addGreediness(BidderGreediness greediness) {
        greedFactor = greediness;
        return this;
    }

    BidderParametersBuilder addRiskRewardRatio(int risk, int reward) {
        riskRewardRatio = new Pair<Integer, Integer>(risk, reward);
        return this;
    }

    public BidderGreediness getGreedFactor() {
        return greedFactor;
    }

    public Pair<Integer, Integer> getRiskRewardRatio() {
        return riskRewardRatio;
    }
}
