package tests.core;

import com.optimax.tradingbot.bidder.BidderStrategy;
import com.optimax.tradingbot.core.Auction;
import com.optimax.tradingbot.core.AuctionState;
import com.optimax.tradingbot.strategies.BalancedBidderStrategy;
import com.optimax.tradingbot.strategies.GodlikeBidderStrategy;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParametersBuilder;
import com.optimax.tradingbot.strategies.builder.enums.BidderStrategyGreediness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AuctionIntegrationTests {

    private static final int TOTAL_QUANTITY = 10;
    private static final int BASE_CASH = 100;
    private static final int NUM_RUNS = 50; // Run multiple times for better statistical confidence

    /**
     * Helper method to get private fields using reflection for testing.
     */
    private Object getPrivateField(Object target, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true); // Allow access to private field
        return field.get(target);
    }

    @Test
    @DisplayName("should GodlikeBidderStrategy overwhelmingly outperform BalancedBidderStrategy (Medium Greed) over multiple runs")
    void shouldGodlikeOverwhelmBalancedMediumGreed() throws NoSuchFieldException, IllegalAccessException {
        int godlikeWinsCount = 0;
        int balancedWinsCount = 0;
        int tiesCount = 0;

        for (int i = 0; i < NUM_RUNS; i++) {
            GodlikeBidderStrategy godlikeStrategy = new GodlikeBidderStrategy(BidderStrategyParametersBuilder.defaultBuilder().build());
            BidderStrategy balancedStrategy = new BalancedBidderStrategy(
                    BidderStrategyParametersBuilder.defaultBuilder()
                            .build()
            );

            Auction auction = new Auction(TOTAL_QUANTITY, BASE_CASH, godlikeStrategy, balancedStrategy);
            auction.run();

            AuctionState finalState = (AuctionState) getPrivateField(auction, "auctionState");

            if (finalState.getOwnBidderQuantityWon() > finalState.getOtherBidderQuantityWon()) {
                godlikeWinsCount++;
            } else if (finalState.getOtherBidderQuantityWon() > finalState.getOwnBidderQuantityWon()) {
                balancedWinsCount++;
            } else {
                tiesCount++;
            }
        }

        System.out.printf("Godlike vs Balanced (Medium Greed): Godlike wins=%d, Balanced wins=%d, Ties=%d%n",
                godlikeWinsCount, balancedWinsCount, tiesCount);

        // Assert that Godlike wins a very high percentage of the time, and Balanced wins a low percentage
        assertTrue(godlikeWinsCount >= (int) (NUM_RUNS * 0.90),
                "Godlike strategy should win at least 90% of runs against Balanced (Medium Greed).");
        assertTrue(balancedWinsCount <= (int) (NUM_RUNS * 0.05),
                "Balanced (Medium Greed) strategy should win no more than 5% of runs against Godlike.");
    }

    @Test
    @DisplayName("should GodlikeBidderStrategy overwhelmingly outperform BalancedBidderStrategy (Strong Greed) over multiple runs")
    void shouldGodlikeOverwhelmBalancedStrongGreed() throws NoSuchFieldException, IllegalAccessException {
        int godlikeWinsCount = 0;
        int balancedWinsCount = 0;
        int tiesCount = 0;

        for (int i = 0; i < NUM_RUNS; i++) {
            GodlikeBidderStrategy godlikeStrategy = new GodlikeBidderStrategy(BidderStrategyParametersBuilder.defaultBuilder().build());
            BidderStrategy balancedStrategy = new BalancedBidderStrategy(
                    BidderStrategyParametersBuilder.defaultBuilder()
                            .withGreediness(BidderStrategyGreediness.STRONG) // Strong greed
                            .withRiskRewardRatio(1, 2) // More emphasis on reward
                            .build()
            );

            Auction auction = new Auction(TOTAL_QUANTITY, BASE_CASH, godlikeStrategy, balancedStrategy);
            auction.run();

            AuctionState finalState = (AuctionState) getPrivateField(auction, "auctionState");

            if (finalState.getOwnBidderQuantityWon() > finalState.getOtherBidderQuantityWon()) {
                godlikeWinsCount++;
            } else if (finalState.getOtherBidderQuantityWon() > finalState.getOwnBidderQuantityWon()) {
                balancedWinsCount++;
            } else {
                tiesCount++;
            }
        }

        System.out.printf("Godlike vs Balanced (Strong Greed): Godlike wins=%d, Balanced wins=%d, Ties=%d%n",
                godlikeWinsCount, balancedWinsCount, tiesCount);

        assertTrue(godlikeWinsCount >= (int) (NUM_RUNS * 0.90),
                "Godlike strategy should win at least 90% of runs against Balanced (Strong Greed).");
        assertTrue(balancedWinsCount <= (int) (NUM_RUNS * 0.05),
                "Balanced (Strong Greed) strategy should win no more than 5% of runs against Godlike.");
    }
}
