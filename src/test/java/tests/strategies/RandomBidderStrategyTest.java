package tests.strategies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.tradingbot.impl.BidderContext;
import org.tradingbot.impl.BidderState;
import org.tradingbot.strategies.RandomBidderStrategy;
import org.tradingbot.strategies.builder.BidderStrategyParameters;
import org.tradingbot.strategies.builder.BidderStrategyParametersBuilder;
import org.tradingbot.strategies.builder.enums.BidderStrategyGreediness;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

public class RandomBidderStrategyTest {
    private BidderStrategyParameters params;

    @BeforeEach
    public void setup() {
        params = BidderStrategyParametersBuilder.defaultBuilder()
                .withRiskRewardRatio(1, 2)
                .withMaxRounds(10)
                .withGreediness(BidderStrategyGreediness.MEDIUM)
                .build();
    }

    @RepeatedTest(10)
    public void testRandomBid_WithinLimits() {
        RandomBidderStrategy strategy = new RandomBidderStrategy(params);

        var own = new BidderState(0, 100, 100);
        var other = new BidderState(0, 100, 100);
        BidderContext ctx = new BidderContext(own, other);
        OptionalInt bid = strategy.nextBid(ctx);
        assertTrue(bid.isPresent());
        assertTrue(bid.getAsInt() > 0 && bid.getAsInt() <= 49);
    }

    @Test
    public void testNoBid_WhenOutOfCash() {
        RandomBidderStrategy strategy = new RandomBidderStrategy(params);

        var own = new BidderState(0, 100, 0);
        var other = new BidderState(0, 100, 5);
        BidderContext ctx = new BidderContext(own, other);
        OptionalInt bid = strategy.nextBid(ctx);

        assertTrue(bid.isEmpty());
    }

    @Test
    public void testRandomBidRespectsGreedLimit() {
        var own = new BidderState(0, 100, 100);
        var other = new BidderState(0, 100, 100);
        var ctx = new BidderContext(own, other);

        RandomBidderStrategy strat = new RandomBidderStrategy(params);

        for (int i = 0; i < 100; i++) {
            OptionalInt bid = strat.nextBid(ctx);
            assertTrue(bid.isPresent());
            assertTrue(bid.getAsInt() > 0 && bid.getAsInt() <= 49);
        }
    }
}
