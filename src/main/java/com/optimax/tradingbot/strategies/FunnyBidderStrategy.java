package com.optimax.tradingbot.strategies;

import com.optimax.tradingbot.bidder.BidderState;
import com.optimax.tradingbot.bidder.BidderStrategy;
import com.optimax.tradingbot.impl.BidderContext;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParameters;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.OptionalInt;
import java.util.Random;

/**
 * A humorous bidder strategy that makes random bids with funny commentary.
 * Perfect for lighthearted testing or just making the auction more entertaining.
 */
public class FunnyBidderStrategy implements BidderStrategy {

    private BidderStrategyParameters params;
    private int round;
    private final Random random;

    private static final String[] QUOTES = {
            "I'm feeling lucky today!",
            "Betting all my imaginary money!",
            "May the odds be ever in my favor!",
            "Is this how you do it? No clue, just guessing!",
            "Who needs strategy when you have charm?",
            "Betting big because I can!",
            "Throwing darts at the bid board!",
            "Let's see what fate decides!",
            "My crystal ball says: bid higher!",
            "Randomness is the spice of life!"
    };

    public FunnyBidderStrategy() {
        this(new Random());
    }

    public FunnyBidderStrategy(Random random) {
        this.random = random;
        this.round = 1;
    }

    @Override
    public void init(@NonNull BidderStrategyParameters params) {
        this.params = params;
        this.round = 1;
    }

    @NonNull
    @Override
    public OptionalInt nextBid(BidderState own, BidderContext ctx) {
        int maxBid = own.cash();

        if (maxBid <= 0) {
            System.out.println("[FunnyBidder] Out of cash! Guess I'm out of the game...");
            return OptionalInt.empty();
        }

        if (params.maxRounds().isPresent() && round > params.maxRounds().getAsInt()) {
            System.out.println("[FunnyBidder] Max rounds reached. Time to retire my bidding hat.");
            return OptionalInt.empty();
        }

        // Show context info humorously
        List<BidderState> others = ctx.getFilteredStates(own.id());
        System.out.printf("[FunnyBidder] Round %d: I've got $%d and %d items left.%n", round, own.cash(), own.getQuantity());
        System.out.println("[FunnyBidder] Here's what the competition looks like:");
        for (BidderState other : others) {
            System.out.printf("  - Bidder %s is lurking with $%d and %d items.%n", other.id(), other.cash(), other.getQuantity());
        }

        // Pick a random bid between 1 and maxBid (inclusive)
        int bid = 1 + random.nextInt(maxBid);

        // Print a random funny quote
        String quote = QUOTES[random.nextInt(QUOTES.length)];
        System.out.printf("[FunnyBidder] %s I'll bid... %d!%n", quote, bid);

        return OptionalInt.of(bid);
    }

    @Override
    public void finishRound() {
        round++;
    }
}
