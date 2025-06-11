package tests.strategies;

import com.optimax.tradingbot.impl.BidderContext;
import com.optimax.tradingbot.impl.BidderStateImpl;
import com.optimax.tradingbot.strategies.BalancedBidderStrategy;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParameters;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParametersBuilder;
import com.optimax.tradingbot.strategies.builder.enums.BidderStrategyGreediness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

class BalancedBidderStrategyTest {
    private BidderStrategyParameters params;

    @BeforeEach
    void setup() {
        params = BidderStrategyParametersBuilder.defaultBuilder()
                .withRiskRewardRatio(1, 2)
                .withMaxRounds(10)
                .withGreediness(BidderStrategyGreediness.MEDIUM)
                .build();
    }

    @Test
    @DisplayName("Should generate a valid initial bid for the first round")
    void shouldGenerateValidInitialBidFirstRound() {
        BalancedBidderStrategy strategy = new BalancedBidderStrategy(params);

        var own = new BidderStateImpl(0, 100, 10, "own");
        var other = new BidderStateImpl(0, 100, 10, "other");

        BidderContext ctx = new BidderContext();
        ctx.putState(own);
        ctx.putState(other);

        OptionalInt bid = strategy.nextBid(own, ctx);
        assertTrue(bid.isPresent(), "Bid should be present");
        assertTrue(bid.getAsInt() > 0 && bid.getAsInt() <= 10, "Bid should be positive and within cash limits");
    }

    @Test
    @DisplayName("Should generate an aggressive bid when own bidder is behind in quantity")
    void shouldGenerateAggressiveBidWhenBehindInQuantity() {
        BalancedBidderStrategy strategy = new BalancedBidderStrategy(params);

        var own = new BidderStateImpl(0, 100, 100, "own");
        var other = new BidderStateImpl(0, 100, 100, "other");

        BidderContext ctx = new BidderContext();
        ctx.putState(own);
        ctx.putState(other);

        OptionalInt bid = strategy.nextBid(own, ctx);

        assertTrue(bid.isPresent(), "Bid should be present");
        assertEquals(49, bid.getAsInt(), "Should bid approximately half of initial cash on the first round under medium greediness");
    }

    @Test
    @DisplayName("Should return empty bid when own bidder is out of cash")
    void shouldReturnEmptyBidWhenOutOfCash() {
        BalancedBidderStrategy strategy = new BalancedBidderStrategy(params);

        var own = new BidderStateImpl(0, 100, 0, "own");
        var other = new BidderStateImpl(0, 100, 5, "other");

        BidderContext ctx = new BidderContext();
        ctx.putState(own);
        ctx.putState(other);

        OptionalInt bid = strategy.nextBid(own, ctx);

        assertTrue(bid.isEmpty(), "Bid should be empty when own bidder has no cash");
    }

    @Test
    @DisplayName("Should reflect greediness influence on bid amount")
    void shouldReflectGreedinessInfluence() {
        var lowGreedyParams = BidderStrategyParametersBuilder.defaultBuilder()
                .withRiskRewardRatio(1, 2)
                .withGreediness(BidderStrategyGreediness.WEAK)
                .build();

        var highGreedyParams = BidderStrategyParametersBuilder.defaultBuilder()
                .withRiskRewardRatio(1, 2)
                .withGreediness(BidderStrategyGreediness.STRONG)
                .build();

        var own = new BidderStateImpl(0, 100, 100, "own");
        var other = new BidderStateImpl(0, 100, 100, "other");

        BidderContext ctx = new BidderContext();
        ctx.putState(own);
        ctx.putState(other);

        BalancedBidderStrategy lowGreedy = new BalancedBidderStrategy(lowGreedyParams);
        BalancedBidderStrategy highGreedy = new BalancedBidderStrategy(highGreedyParams);

        OptionalInt lowBidOpt = lowGreedy.nextBid(own, ctx);
        OptionalInt highBidOpt = highGreedy.nextBid(own, ctx);

        assertTrue(lowBidOpt.isPresent(), "Low greedy bid should be present");
        assertTrue(highBidOpt.isPresent(), "High greedy bid should be present");
        assertTrue(lowBidOpt.getAsInt() < highBidOpt.getAsInt(), "Low greediness should result in a smaller bid than high greediness");
    }

    @Test
    @DisplayName("Should reflect risk-reward ratio impact on bid amount")
    void shouldReflectRiskRewardRatioImpact() {
        var riskHigh = BidderStrategyParametersBuilder.defaultBuilder()
                .withRiskRewardRatio(3, 4)
                .withGreediness(BidderStrategyGreediness.MEDIUM)
                .build();

        var rewardHigh = BidderStrategyParametersBuilder.defaultBuilder()
                .withRiskRewardRatio(1, 3)
                .withGreediness(BidderStrategyGreediness.MEDIUM)
                .build();

        var own = new BidderStateImpl(0, 100, 25, "own");
        var other = new BidderStateImpl(0, 100, 25, "other");

        BidderContext ctx = new BidderContext();
        ctx.putState(own);
        ctx.putState(other);

        BalancedBidderStrategy riskHighStrat = new BalancedBidderStrategy(riskHigh);
        BalancedBidderStrategy rewardHighStrat = new BalancedBidderStrategy(rewardHigh);

        OptionalInt riskBidOpt = riskHighStrat.nextBid(own, ctx);
        OptionalInt rewardBidOpt = rewardHighStrat.nextBid(own, ctx);

        assertTrue(riskBidOpt.isPresent(), "Risk-high bid should be present");
        assertTrue(rewardBidOpt.isPresent(), "Reward-high bid should be present");
        assertTrue(riskBidOpt.getAsInt() < rewardBidOpt.getAsInt(), "Higher risk-reward ratio should result in a smaller bid");
    }

    @Test
    @DisplayName("Should stop bidding after maximum rounds are reached")
    void shouldStopBiddingAfterMaxRounds() {
        var limitedRounds = BidderStrategyParametersBuilder.defaultBuilder()
                .withMaxRounds(2)
                .withGreediness(BidderStrategyGreediness.MEDIUM)
                .build();

        BalancedBidderStrategy strat = new BalancedBidderStrategy(limitedRounds);
        var own = new BidderStateImpl(0, 100, 100, "own");
        var other = new BidderStateImpl(0, 100, 100, "other");

        BidderContext ctx = new BidderContext();
        ctx.putState(own);
        ctx.putState(other);

        OptionalInt bid1 = strat.nextBid(own, ctx);
        strat.finishRound();

        OptionalInt bid2 = strat.nextBid(own, ctx);
        strat.finishRound();

        OptionalInt bid3 = strat.nextBid(own, ctx);

        assertTrue(bid1.isPresent(), "Bid 1 should be present before max rounds");
        assertTrue(bid2.isPresent(), "Bid 2 should be present before max rounds");
        assertTrue(bid3.isEmpty(), "Bid 3 should be empty after max rounds are reached");
    }
}
