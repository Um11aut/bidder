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
    private int initialQuantity; // Refers to initial total quantity of items in the auction
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
        if (initialQuantity == 0) {
            initialQuantity = own.totalQuantity();
        }
        if (initialCash == 0) {
            initialCash = own.cash();
        }

        if (params.maxRounds().isPresent() && round > params.maxRounds().getAsInt()) {
            return OptionalInt.empty();
        }

        int ownCash = own.cash();
        if (ownCash <= 0) {
            return OptionalInt.empty();
        }

        int bidValue;
        int opponentLastMaxBid = 0;

        if (!ctx.getHistory().isEmpty()) {
            // Get the last round's history
            BidderHistoryUnit lastRound = ctx.getHistory().getLast();
            Optional<Integer> otherBidOpt = lastRound.getMaxBidInRound(own.id());
            if (otherBidOpt.isPresent()) {
                opponentLastMaxBid = otherBidOpt.get();
            }
        }

        if (opponentLastMaxBid > 0) {
            // Bid slightly more than the opponent's last highest bid
            bidValue = Math.min(ownCash, opponentLastMaxBid + 1);
        } else {
            // If no history or opponent didn't bid last round, bid aggressively (e.g., 70-80% of cash)
            bidValue = (int) (ownCash * 0.7 + random.nextInt() * ownCash * 0.1);
        }

        // Ensure bid is at least 1 and capped by remaining cash
        bidValue = Math.max(1, bidValue);
        bidValue = Math.min(bidValue, ownCash);

        // Godlike's max bid per round for quantity. For TOTAL_QUANTITY=10, this is 5.
        // This is crucial to allow Godlike to bid for one more unit than Balanced's cap (4).
        int godlikeMaxBidPerRoundQuantityCap = (int) (initialQuantity * 0.5);
        if (godlikeMaxBidPerRoundQuantityCap == 0) godlikeMaxBidPerRoundQuantityCap = 1;

        bidValue = Math.min(bidValue, godlikeMaxBidPerRoundQuantityCap);

        return OptionalInt.of(bidValue);
    }

    @Override
    public void finishRound() {
        round++;
    }
}
