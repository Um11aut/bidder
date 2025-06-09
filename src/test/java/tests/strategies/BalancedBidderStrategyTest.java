package tests.strategies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tradingbot.impl.BidderContext;
import org.tradingbot.impl.BidderState;
import org.tradingbot.strategies.BalancedBidderStrategy;
import org.tradingbot.strategies.builder.BidderStrategyParameters;
import org.tradingbot.strategies.builder.BidderStrategyParametersBuilder;
import org.tradingbot.strategies.builder.enums.BidderStrategyGreediness;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

public class BalancedBidderStrategyTest {
    private BidderStrategyParameters params;

    @BeforeEach
    public void setup() {
        params = BidderStrategyParametersBuilder.defaultBuilder()
                .withRiskRewardRatio(1, 2)
                .withMaxRounds(10)
                .withGreediness(BidderStrategyGreediness.MEDIUM)
                .build();
    }

    @Test
    public void testInitialBid_FirstRound() {
        BalancedBidderStrategy strategy = new BalancedBidderStrategy(params);

        var own = new BidderState(0, 100, 10);
        var other = new BidderState(0, 100, 10);
        BidderContext ctx = new BidderContext(own, other);
        OptionalInt bid = strategy.nextBid(ctx);
        assertTrue(bid.isPresent());
        assertTrue(bid.getAsInt() > 0 && bid.getAsInt() <= 100);
    }

    @Test
    public void testAggressiveBid_WhenBehindInQuantity() {
        BalancedBidderStrategy strategy = new BalancedBidderStrategy(params);

        var own = new BidderState(0, 100, 100);
        var other = new BidderState(0, 100, 100);
        BidderContext ctx = new BidderContext(own, other);

        OptionalInt bid = strategy.nextBid(ctx);
        assertTrue(bid.isPresent());
        int value = bid.getAsInt();
        assertEquals(49, value); // Shouldn't bid more than half on the first round
    }

    @Test
    public void testNoBid_WhenOutOfCash() {
        BalancedBidderStrategy strategy = new BalancedBidderStrategy(params);

        var own = new BidderState(0, 100, 0);
        var other = new BidderState(0, 100, 5);
        BidderContext ctx = new BidderContext(own, other);
        OptionalInt bid = strategy.nextBid(ctx);

        assertTrue(bid.isEmpty());
    }

    @Test
    public void testGreedinessInfluence() {
        var lowGreedyParams = BidderStrategyParametersBuilder.defaultBuilder()
                .withRiskRewardRatio(1, 2)
                .withGreediness(BidderStrategyGreediness.WEAK)
                .build();

        var highGreedyParams = BidderStrategyParametersBuilder.defaultBuilder()
                .withRiskRewardRatio(1, 2)
                .withGreediness(BidderStrategyGreediness.STRONG)
                .build();

        var own = new BidderState(0, 100, 100);
        var other = new BidderState(0, 100, 100);
        BidderContext ctx = new BidderContext(own, other);

        BalancedBidderStrategy lowGreedy = new BalancedBidderStrategy(lowGreedyParams);
        BalancedBidderStrategy highGreedy = new BalancedBidderStrategy(highGreedyParams);

        int lowBid = lowGreedy.nextBid(ctx).getAsInt();
        int highBid = highGreedy.nextBid(ctx).getAsInt();

        assertTrue(lowBid < highBid);
    }

    @Test
    public void testRiskRewardRatioImpact() {
        var riskHigh = BidderStrategyParametersBuilder.defaultBuilder()
                .withRiskRewardRatio(3, 4)
                .build();

        var rewardHigh = BidderStrategyParametersBuilder.defaultBuilder()
                .withRiskRewardRatio(1, 3)
                .build();

        var own = new BidderState(0, 100, 25);
        var other = new BidderState(0, 100, 25);
        BidderContext ctx = new BidderContext(own, other);

        BalancedBidderStrategy riskHighStrat = new BalancedBidderStrategy(riskHigh);
        BalancedBidderStrategy rewardHighStrat = new BalancedBidderStrategy(rewardHigh);

        int riskBid = riskHighStrat.nextBid(ctx).getAsInt();
        int rewardBid = rewardHighStrat.nextBid(ctx).getAsInt();

        assertTrue(riskBid < rewardBid);
    }

    @Test
    public void testMaxRoundsStopsBidding() {
        var limitedRounds = BidderStrategyParametersBuilder.defaultBuilder()
                .withMaxRounds(2)
                .build();

        BalancedBidderStrategy strat = new BalancedBidderStrategy(limitedRounds);
        var own = new BidderState(0, 100, 100);
        var other = new BidderState(0, 100, 100);
        BidderContext ctx = new BidderContext(own, other);

        OptionalInt bid1 = strat.nextBid(ctx);
        strat.finishRound();
        OptionalInt bid2 = strat.nextBid(ctx);
        strat.finishRound();
        OptionalInt bid3 = strat.nextBid(ctx); // Should be empty

        assertTrue(bid1.isPresent());
        assertTrue(bid2.isPresent());
        assertTrue(bid3.isEmpty());
    }
}
