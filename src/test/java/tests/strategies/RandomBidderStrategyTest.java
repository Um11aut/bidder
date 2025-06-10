package tests.strategies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import com.optimax.tradingbot.impl.BidderContext;
import com.optimax.tradingbot.impl.BidderState;
import com.optimax.tradingbot.strategies.RandomBidderStrategy;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParameters;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParametersBuilder;
import com.optimax.tradingbot.strategies.builder.enums.BidderStrategyGreediness;

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
    @DisplayName("Should generate a random bid within specified limits")
    public void shouldGenerateRandomBidWithinLimits() {
        RandomBidderStrategy strategy = new RandomBidderStrategy(params);

        var own = new BidderState(0, 100, 100);
        var other = new BidderState(0, 100, 100);
        BidderContext ctx = new BidderContext(own, other);
        OptionalInt bid = strategy.nextBid(ctx);
        assertTrue(bid.isPresent());
        assertTrue(bid.getAsInt() > 0 && bid.getAsInt() <= 49);
    }

    @Test
    @DisplayName("Should return empty bid when own bidder is out of cash")
    public void shouldReturnEmptyBidWhenOutOfCash() {
        RandomBidderStrategy strategy = new RandomBidderStrategy(params);

        var own = new BidderState(0, 100, 0);
        var other = new BidderState(0, 100, 5);
        BidderContext ctx = new BidderContext(own, other);
        OptionalInt bid = strategy.nextBid(ctx);

        assertTrue(bid.isEmpty());
    }

    @Test
    @DisplayName("Should generate random bids respecting the greediness limit")
    public void shouldGenerateRandomBidRespectingGreedLimit() {
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