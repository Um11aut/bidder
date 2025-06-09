package org.tradingbot.strategies;

import org.springframework.lang.NonNull;
import org.tradingbot.bidder.BidderStrategy;
import org.tradingbot.impl.BidderContext;
import org.tradingbot.strategies.builder.BidderStrategyParameters;
import org.tradingbot.utils.Pair;

import java.util.OptionalInt;

public class BalancedBidderStrategy implements BidderStrategy {
    private BidderStrategyParameters params;
    private int round;
    private int initialQuantity;

    public BalancedBidderStrategy(@NonNull BidderStrategyParameters params) {
        init(params);
    }

    @Override
    public void init(@NonNull BidderStrategyParameters params) {
        this.params = params;
        this.round = 1;
        this.initialQuantity = 0;
    }

    @NonNull
    @Override
    public OptionalInt nextBid(BidderContext ctx) {
        if (initialQuantity == 0) {
            initialQuantity = ctx.own().getTotalQuantity();
        }

        OptionalInt maxRounds = params.maxRounds();
        if (maxRounds.isPresent() && round > maxRounds.getAsInt()) {
            return OptionalInt.empty();
        }

        double greedMultiplier = switch (params.greediness()) {
            case STRONG -> 1.5;
            case MEDIUM -> 1.0;
            case WEAK -> 0.5;
        };

        Pair<Integer, Integer> riskRewardRatio = params.riskRewardRatio();
        int riskRatio = riskRewardRatio.getFirst();
        int rewardRatio = riskRewardRatio.getSecond();

        int ownCash = ctx.own().getCash();
        if (riskRatio + rewardRatio == 0 || ownCash <= 0) {
            return OptionalInt.empty();
        }

        double ratioFactor = (double) rewardRatio / (riskRatio + rewardRatio);
        double bidEstimate = ownCash * greedMultiplier * ratioFactor;
        int halfQtyLimit = (int) (initialQuantity * 0.5);
        int cappedBid = Math.min((int) Math.round(bidEstimate), halfQtyLimit - 1);
        int bid = Math.max(1, Math.min(ownCash, cappedBid));
        return OptionalInt.of(bid);
    }

    @Override
    public void finishRound() {
        round++;
    }
}
