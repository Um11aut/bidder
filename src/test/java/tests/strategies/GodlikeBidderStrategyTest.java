package tests.strategies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tradingbot.impl.BidderContext;
import org.tradingbot.impl.BidderState;
import org.tradingbot.strategies.GodlikeBidderStrategy;
import org.tradingbot.strategies.builder.BidderStrategyParameters;
import org.tradingbot.strategies.builder.BidderStrategyParametersBuilder;
import org.tradingbot.strategies.builder.enums.BidderStrategyGreediness;

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
    public void testInitialBid_Adaptive() {
        GodlikeBidderStrategy strategy = new GodlikeBidderStrategy(params, new Random(42));

        var own = new BidderState(0, 100, 100);
        var other = new BidderState(0, 100, 100);
        BidderContext ctx = new BidderContext(own, other);

        OptionalInt bid = strategy.nextBid(ctx);
        assertTrue(bid.isPresent());
        assertTrue(bid.getAsInt() > 0 && bid.getAsInt() <= 49);
    }

    @Test
    public void testBid_AdaptiveAfterRounds() {
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
        assertTrue(bid.isPresent());
        assertTrue(bid.getAsInt() > 0 && bid.getAsInt() <= 40);
    }

    @Test
    public void testNoBid_IfBroke() {
        GodlikeBidderStrategy strategy = new GodlikeBidderStrategy(params, new Random(42));

        var own = new BidderState(50, 100, 0);
        var other = new BidderState(30, 100, 20);
        BidderContext ctx = new BidderContext(own, other);

        OptionalInt bid = strategy.nextBid(ctx);
        assertTrue(bid.isEmpty());
    }
}
