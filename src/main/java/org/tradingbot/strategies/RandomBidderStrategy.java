package org.tradingbot.strategies;

import org.springframework.lang.NonNull;
import org.tradingbot.bidder.BidderStrategy;
import org.tradingbot.impl.BidderContext;
import org.tradingbot.strategies.builder.BidderStrategyParameters;

import java.util.OptionalInt;
import java.util.Random;

public class RandomBidderStrategy implements BidderStrategy {
    private BidderStrategyParameters params;
    private int round;
    private int initialQuantity;
    private final Random random;

    public RandomBidderStrategy(@NonNull BidderStrategyParameters params) {
        this(params, new Random());
    }

    public RandomBidderStrategy(@NonNull BidderStrategyParameters params, Random random) {
        this.params = params;
        this.random = random;
        this.round = 1;
        this.initialQuantity = 0;
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

        if (params.maxRounds().isPresent() && round > params.maxRounds().getAsInt()) {
            return OptionalInt.empty();
        }

        int ownCash = ctx.own().getCash();
        if (ownCash <= 0) {
            return OptionalInt.empty();
        }

        int halfQtyLimit = Math.max(1, (int) (initialQuantity * 0.5) - 1);
        int maxAllowedBid = Math.min(ownCash, halfQtyLimit);
        int bid = random.nextInt(maxAllowedBid) + 1; // 1 to maxAllowedBid inclusive

        return OptionalInt.of(bid);
    }

    @Override
    public void finishRound() {
        round++;
    }
}
