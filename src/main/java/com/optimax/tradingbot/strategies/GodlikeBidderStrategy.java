package com.optimax.tradingbot.strategies;

import org.springframework.lang.NonNull;
import com.optimax.tradingbot.bidder.BidderStrategy;
import com.optimax.tradingbot.impl.BidderContext;
import com.optimax.tradingbot.impl.BidderState;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParameters;
import com.optimax.tradingbot.utils.Pair;

import java.util.List;
import java.util.OptionalInt;
import java.util.Random;

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
    public OptionalInt nextBid(BidderContext ctx) {
        BidderState own = ctx.own();
        BidderState other = ctx.other();

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
        if (ownCash <= 0) return OptionalInt.empty();

        int halfQtyCap = Math.max(1, (int) (initialQuantity * 0.5) - 1);

        List<Pair<Integer, Integer>> history = own.getHistory();
        double opponentAggression = 0.5;
        if (!history.isEmpty()) {
            double total = 0;
            for (Pair<Integer, Integer> roundBid : history) {
                total += roundBid.getSecond();
            }
            opponentAggression = total / (history.size() * initialCash);
        }

        double dynamicRisk = Math.pow(opponentAggression, 0.8) * 0.7 + 0.3;
        double diminishingFactor = 1.0 - ((double) own.getQuantity() / initialQuantity);
        double expectedOpponentBid = other.getCash() / (params.maxRounds().orElse(100) - round + 1.0);

        double utility = dynamicRisk * diminishingFactor * expectedOpponentBid;
        double entropy = 0.8 + (random.nextDouble() * 0.4);
        int calculatedBid = (int) Math.round(Math.min(utility * entropy, ownCash));

        int cappedBid = Math.min(calculatedBid, halfQtyCap);
        int finalBid = Math.max(1, cappedBid);

        return OptionalInt.of(finalBid);
    }

    @Override
    public void finishRound() {
        round++;
    }
}
