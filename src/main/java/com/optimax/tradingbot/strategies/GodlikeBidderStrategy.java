package com.optimax.tradingbot.strategies;

import com.optimax.tradingbot.bidder.BidderState;
import com.optimax.tradingbot.impl.BidderHistoryUnit;
import com.optimax.tradingbot.bidder.BidderStrategy;
import com.optimax.tradingbot.impl.BidderContext;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParameters;
import org.springframework.lang.NonNull;

import java.util.*;

public class GodlikeBidderStrategy implements BidderStrategy {
    private BidderStrategyParameters params;
    private int round;
    private int initialQuantity;
    private int initialCash;
    private final Random random;

    public GodlikeBidderStrategy(@NonNull BidderStrategyParameters params) {
        this(params, new Random());
    }

    public GodlikeBidderStrategy(@NonNull BidderStrategyParameters params, Random random) {
        this.params = params;
        this.random = random;
        this.round = 1;
    }

    @Override
    public void init(@NonNull BidderStrategyParameters params) {
        this.params = params;
        this.round = 1;
        this.initialQuantity = 0;
        this.initialCash = 0;
    }

    @NonNull
    @Override
    public OptionalInt nextBid(BidderState own, BidderContext ctx) {
        List<BidderState> otherBidders = ctx.getFilteredStates(own.getId());

        if (initialQuantity == 0) {
            initialQuantity = own.getTotalQuantity();
        }

        if (initialCash == 0) {
            initialCash = own.getCash();
        }

        if (params.maxRounds().isPresent() && round > params.maxRounds().getAsInt()) {
            return OptionalInt.empty();
        }

        int ownCash = own.getCash();
        if (ownCash <= 0) {
            return OptionalInt.empty();
        }

        int halfQtyCap = Math.max(1, (int) (initialQuantity * 0.5) - 1);

        // --- Updated opponent aggression analysis ---
        double dynamicRisk = getDynamicRisk(own, ctx);
        double diminishingFactor = 1.0 - ((double) own.getQuantity() / initialQuantity);

        // Estimate expected opponent bid based on average remaining cash
        double totalOpponentCash = otherBidders.stream().mapToInt(BidderState::getCash).sum();

        double expectedOpponentBid = 0;
        int remainingRounds = params.maxRounds().orElse(100) - round + 1;
        if (!otherBidders.isEmpty() && remainingRounds > 0) {
            expectedOpponentBid = (totalOpponentCash / otherBidders.size()) / remainingRounds;
        }

        double utility = dynamicRisk * diminishingFactor * expectedOpponentBid;
        double entropy = 0.8 + (random.nextDouble() * 0.4);
        int calculatedBid = (int) Math.round(Math.min(utility * entropy, ownCash));

        int cappedBid = Math.min(calculatedBid, halfQtyCap);
        int finalBid = Math.max(1, cappedBid);

        finalBid = Math.min(finalBid, ownCash);

        return OptionalInt.of(finalBid);
    }

    private double getDynamicRisk(BidderState own, BidderContext ctx) {
        List<BidderHistoryUnit> history = ctx.getHistory();
        double opponentAggression = 0.5; // Default aggression

        if (!history.isEmpty()) {
            double totalHighestOpponentBids = 0;
            int roundsConsidered = 0;

            for (BidderHistoryUnit roundBids : history) {
                if (roundBids != null && !roundBids.bids().isEmpty()) {
                    Optional<Integer> maxBid = roundBids.getMaxBidInRound(own.getId());
                    if (maxBid.isPresent()) {
                        totalHighestOpponentBids += maxBid.get();
                        roundsConsidered++;
                    }
                }
            }

            if (roundsConsidered > 0 && initialCash > 0) {
                opponentAggression = totalHighestOpponentBids / (roundsConsidered * initialCash);
                opponentAggression = Math.clamp(opponentAggression, 0.1, 1.0);
            }
        }

        return Math.pow(opponentAggression, 0.8) * 0.7 + 0.3;
    }

    @Override
    public void finishRound() {
        round++;
    }
}
