package tests.strategies;

import com.optimax.tradingbot.impl.BidderContext;
import com.optimax.tradingbot.impl.BidderStateImpl;
import com.optimax.tradingbot.strategies.RandomBidderStrategy;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParameters;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParametersBuilder;
import com.optimax.tradingbot.strategies.builder.enums.BidderStrategyGreediness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

class RandomBidderStrategyTest {
    private BidderStrategyParameters params;

    @BeforeEach
    void setup() {
        params = BidderStrategyParametersBuilder.defaultBuilder()
                .withRiskRewardRatio(1, 2)
                .withMaxRounds(10)
                .withGreediness(BidderStrategyGreediness.MEDIUM)
                .build();
    }

    @RepeatedTest(10)
    @DisplayName("Should generate a random bid within specified limits")
    void shouldGenerateRandomBidWithinLimits() {
        RandomBidderStrategy strategy = new RandomBidderStrategy(params);

        var own = new BidderStateImpl(0, 100, 100, "own");
        var other = new BidderStateImpl(0, 100, 100, "other");

        BidderContext ctx = new BidderContext();
        ctx.putState(own);
        ctx.putState(other);

        OptionalInt bid = strategy.nextBid(own, ctx);

        assertTrue(bid.isPresent(), "Bid should be present");
        assertTrue(bid.getAsInt() > 0 && bid.getAsInt() <= 49, "Bid must be > 0 and ≤ 49 under MEDIUM greediness");
    }

    @Test
    @DisplayName("Should return empty bid when own bidder is out of cash")
    void shouldReturnEmptyBidWhenOutOfCash() {
        RandomBidderStrategy strategy = new RandomBidderStrategy(params);

        var own = new BidderStateImpl(0, 100, 0, "own");
        var other = new BidderStateImpl(0, 100, 5, "other");

        BidderContext ctx = new BidderContext();
        ctx.putState(own);
        ctx.putState(other);

        OptionalInt bid = strategy.nextBid(own, ctx);

        assertTrue(bid.isEmpty(), "Bid should be empty when cash is 0");
    }

    @Test
    @DisplayName("Should generate random bids respecting the greediness limit")
    void shouldGenerateRandomBidRespectingGreedLimit() {
        var own = new BidderStateImpl(0, 100, 100, "own");
        var other = new BidderStateImpl(0, 100, 100, "other");

        BidderContext ctx = new BidderContext();
        ctx.putState(own);
        ctx.putState(other);

        RandomBidderStrategy strat = new RandomBidderStrategy(params);

        for (int i = 0; i < 100; i++) {
            OptionalInt bid = strat.nextBid(own, ctx);
            assertTrue(bid.isPresent(), "Bid should be present on iteration " + i);
            assertTrue(bid.getAsInt() > 0 && bid.getAsInt() <= 49, "Bid should be within (0, 49] on iteration " + i);
        }
    }
}