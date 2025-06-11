package tests.core;

import static org.junit.jupiter.api.Assertions.*;

import com.optimax.tradingbot.bidder.BidderStrategy;
import com.optimax.tradingbot.core.Auction;
import com.optimax.tradingbot.core.AuctionState;
import com.optimax.tradingbot.strategies.GodlikeBidderStrategy;
import com.optimax.tradingbot.strategies.RandomBidderStrategy;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParametersBuilder;
import org.junit.jupiter.api.*;

class AuctionIntegrationTests {

    private static final int TOTAL_QUANTITY = 10;
    private static final int BASE_CASH = 100;

    /**
     * Helper method to get private fields using reflection for testing.
     */
    private Object getPrivateField(Object target, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true); // Allow access to private field
        return field.get(target);
    }

    @Test
    @DisplayName("RandomStrategy vs ConservativeStrategy: Random should generally win more")
    void randomVsConservative() throws NoSuchFieldException, IllegalAccessException {
        BidderStrategy randomStrategy = new RandomBidderStrategy(BidderStrategyParametersBuilder.defaultBuilder().build());
        GodlikeBidderStrategy conservativeStrategy = new GodlikeBidderStrategy(BidderStrategyParametersBuilder.defaultBuilder().build());

        Auction auction = new Auction(TOTAL_QUANTITY, BASE_CASH, randomStrategy, conservativeStrategy);
        auction.run();

        AuctionState finalState = (AuctionState) getPrivateField(auction, "auctionState");

        int randomWins = finalState.getOwnBidderCurrentQuantityWon();
        int conservativeWins = finalState.getOtherBidderCurrentQuantityWon();

        System.out.printf("Random wins: %d, Conservative wins: %d%n", randomWins, conservativeWins);

        assertTrue(randomWins >= conservativeWins,
                "Random strategy should win at least as many units as Conservative");
    }
}
