package tests.strategies;

import com.optimax.tradingbot.impl.BidderStateImpl;
import com.optimax.tradingbot.impl.BidderContext;
import com.optimax.tradingbot.strategies.GodlikeBidderStrategy;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParameters;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParametersBuilder;
import com.optimax.tradingbot.strategies.builder.enums.BidderStrategyGreediness;
import com.optimax.tradingbot.impl.BidderHistoryUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GodlikeBidderStrategyTest {
    private BidderStrategyParameters params;

    @BeforeEach
    void setup() {
        params = BidderStrategyParametersBuilder.defaultBuilder()
                .withRiskRewardRatio(1, 3)
                .withMaxRounds(20)
                .withGreediness(BidderStrategyGreediness.STRONG)
                .build();
    }

    @Test
    @DisplayName("Should generate a valid initial bid adaptively")
    void shouldGenerateValidInitialBidAdaptively() {
        GodlikeBidderStrategy strategy = new GodlikeBidderStrategy(params, new Random(42));

        var own = new BidderStateImpl(0, 100, 100, "own");
        var other = new BidderStateImpl(0, 100, 100, "other");

        BidderContext ctx = new BidderContext();
        ctx.putState(own);
        ctx.putState(other);

        var bid = strategy.nextBid(own, ctx);
        assertTrue(bid.isPresent(), "Bid should be present");
        assertTrue(bid.getAsInt() > 0 && bid.getAsInt() <= 49, "Bid should be positive and within half quantity cap");
    }

    @Test
    @DisplayName("Should generate an adaptive bid after several rounds based on history")
    void shouldGenerateAdaptiveBidAfterRoundsBasedOnHistory() {
        GodlikeBidderStrategy strategy = new GodlikeBidderStrategy(params, new Random(42));

        var own = new BidderStateImpl(20, 100, 40, "own");
        var other = new BidderStateImpl(50, 100, 20, "other");

        BidderContext ctx = new BidderContext();
        ctx.putState(own);
        ctx.putState(other);

        // Simulated history
        ctx.addHistoryUnit(new BidderHistoryUnit(Map.of("own", 10, "other", 15)));
        ctx.addHistoryUnit(new BidderHistoryUnit(Map.of("own", 5, "other", 25)));
        ctx.addHistoryUnit(new BidderHistoryUnit(Map.of("own", 20, "other", 30)));

        strategy.finishRound(); // round 2
        strategy.finishRound(); // round 3
        strategy.finishRound(); // round 4

        var bid = strategy.nextBid(own, ctx);
        assertTrue(bid.isPresent(), "Bid should be present");
        assertTrue(bid.getAsInt() > 0 && bid.getAsInt() <= 40, "Bid should be positive and within own cash");
    }

    @Test
    @DisplayName("Should return empty bid if own bidder is out of cash")
    void shouldReturnEmptyBidWhenOutOfCash() {
        GodlikeBidderStrategy strategy = new GodlikeBidderStrategy(params, new Random(42));

        var own = new BidderStateImpl(50, 100, 0, "own");
        var other = new BidderStateImpl(30, 100, 20, "other");

        BidderContext ctx = new BidderContext();
        ctx.putState(own);
        ctx.putState(other);

        var bid = strategy.nextBid(own, ctx);
        assertTrue(bid.isEmpty(), "Bid should be empty when own bidder has no cash");
    }
}