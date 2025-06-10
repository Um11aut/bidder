package tests.strategies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.optimax.tradingbot.impl.BidderContext;
import com.optimax.tradingbot.impl.BidderState;
import com.optimax.tradingbot.strategies.GodlikeBidderStrategy;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParameters;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParametersBuilder;
import com.optimax.tradingbot.strategies.builder.enums.BidderStrategyGreediness;

import java.util.OptionalInt;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class GodlikeBidderStrategyTest {
    private BidderStrategyParameters params;

    @BeforeEach
    public void setup() {
        params = BidderStrategyParametersBuilder.defaultBuilder()
                .withRiskRewardRatio(1, 3)
                .withMaxRounds(20)
                .withGreediness(BidderStrategyGreediness.STRONG)
                .build();
    }

    @Test
    @DisplayName("Should generate a valid initial bid adaptively")
    public void shouldGenerateValidInitialBidAdaptively() {
        GodlikeBidderStrategy strategy = new GodlikeBidderStrategy(params, new Random(42));

        var own = new BidderState(0, 100, 100);
        var other = new BidderState(0, 100, 100);
        BidderContext ctx = new BidderContext(own, other);

        OptionalInt bid = strategy.nextBid(ctx);
        assertTrue(bid.isPresent(), "Bid should be present");
        assertTrue(bid.getAsInt() > 0 && bid.getAsInt() <= 49, "Bid should be positive and within cash limits (<=49 for initial balanced bid)");
    }

    @Test
    @DisplayName("Should generate an adaptive bid after several rounds based on history")
    public void shouldGenerateAdaptiveBidAfterRoundsBasedOnHistory() {
        GodlikeBidderStrategy strategy = new GodlikeBidderStrategy(params, new Random(42));

        var own = new BidderState(20, 100, 40);
        var other = new BidderState(50, 100, 20);
        own.addHistory(10, 15);
        own.addHistory(5, 25);
        own.addHistory(20, 30);

        BidderContext ctx = new BidderContext(own, other);
        strategy.finishRound();
        strategy.finishRound();
        strategy.finishRound();

        OptionalInt bid = strategy.nextBid(ctx);
        assertTrue(bid.isPresent(), "Bid should be present");
        assertTrue(bid.getAsInt() > 0 && bid.getAsInt() <= 40, "Bid should be positive and within current cash limits");
    }

    @Test
    @DisplayName("Should return empty bid if own bidder is out of cash")
    public void shouldReturnEmptyBidWhenOutOfCash() {
        GodlikeBidderStrategy strategy = new GodlikeBidderStrategy(params, new Random(42));

        var own = new BidderState(50, 100, 0);
        var other = new BidderState(30, 100, 20);
        BidderContext ctx = new BidderContext(own, other);

        OptionalInt bid = strategy.nextBid(ctx);
        assertTrue(bid.isEmpty(), "Bid should be empty when own bidder has no cash");
    }
}
