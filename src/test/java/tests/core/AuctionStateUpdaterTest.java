package tests.core;

import com.optimax.tradingbot.bidder.BidderWinEvaluator;

import com.optimax.tradingbot.core.AuctionState;
import com.optimax.tradingbot.core.AuctionStateUpdater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuctionStateUpdaterTest {
    private static final int INITIAL_TOTAL_QUANTITY = 100;
    private static final int INITIAL_BASE_CASH = 500;

    private AuctionState auctionState;

    record TestBidderWinEvaluator(int quantityToReturn) implements BidderWinEvaluator {
        @Override
            public int evaluateWonQuantity(int ownBid, int otherBid) {
                return quantityToReturn;
            }
        }

    @BeforeEach
    void setUp() {
        auctionState = new AuctionState(INITIAL_TOTAL_QUANTITY, INITIAL_BASE_CASH);
    }

    @Test
    @DisplayName("Should update state when both bidders win 1 quantity each")
    void shouldUpdateStateWhenBothWinOneQuantity() {
        BidderWinEvaluator ownWinEvaluator = new TestBidderWinEvaluator(1);
        BidderWinEvaluator otherWinEvaluator = new TestBidderWinEvaluator(1);
        AuctionStateUpdater updater = new AuctionStateUpdater(ownWinEvaluator, otherWinEvaluator);

        int ownBid = 10;
        int otherBid = 10;

        updater.updateState(auctionState, ownBid, otherBid);

        assertEquals(INITIAL_BASE_CASH - ownBid, auctionState.getOwnBidderCurrentCash(),
                "Own bidder's cash should decrease by own bid");
        assertEquals(INITIAL_BASE_CASH - otherBid, auctionState.getOtherBidderCurrentCash(),
                "Other bidder's cash should decrease by other bid");
        assertEquals(1, auctionState.getOwnBidderCurrentQuantityWon(),
                "Own bidder should win 1 quantity");
        assertEquals(1, auctionState.getOtherBidderCurrentQuantityWon(),
                "Other bidder should win 1 quantity");
        assertEquals(INITIAL_TOTAL_QUANTITY - 2, auctionState.getRemainingQuantity(),
                "Remaining quantity should decrease by 2 (1+1)");
    }

    @Test
    @DisplayName("Should update state when only own bidder wins 2 quantity")
    void shouldUpdateStateWhenOnlyOwnWinsTwoQuantity() {
        BidderWinEvaluator ownWinEvaluator = new TestBidderWinEvaluator(2);
        BidderWinEvaluator otherWinEvaluator = new TestBidderWinEvaluator(0);
        AuctionStateUpdater updater = new AuctionStateUpdater(ownWinEvaluator, otherWinEvaluator);

        int ownBid = 20;
        int otherBid = 5;

        updater.updateState(auctionState, ownBid, otherBid);

        assertEquals(INITIAL_BASE_CASH - ownBid, auctionState.getOwnBidderCurrentCash(),
                "Own bidder's cash should decrease by own bid");
        assertEquals(INITIAL_BASE_CASH - otherBid, auctionState.getOtherBidderCurrentCash(),
                "Other bidder's cash should decrease by other bid");
        assertEquals(2, auctionState.getOwnBidderCurrentQuantityWon(),
                "Own bidder should win 2 quantities");
        assertEquals(0, auctionState.getOtherBidderCurrentQuantityWon(),
                "Other bidder should win 0 quantity");
        assertEquals(INITIAL_TOTAL_QUANTITY - 2, auctionState.getRemainingQuantity(),
                "Remaining quantity should decrease by 2 (only own won)");
    }

    @Test
    @DisplayName("Should update state when only other bidder wins 2 quantity")
    void shouldUpdateStateWhenOnlyOtherWinsTwoQuantity() {
        BidderWinEvaluator ownWinEvaluator = new TestBidderWinEvaluator(0);
        BidderWinEvaluator otherWinEvaluator = new TestBidderWinEvaluator(2);
        AuctionStateUpdater updater = new AuctionStateUpdater(ownWinEvaluator, otherWinEvaluator);

        int ownBid = 5;
        int otherBid = 20;

        updater.updateState(auctionState, ownBid, otherBid);

        assertEquals(INITIAL_BASE_CASH - ownBid, auctionState.getOwnBidderCurrentCash(),
                "Own bidder's cash should decrease by own bid");
        assertEquals(INITIAL_BASE_CASH - otherBid, auctionState.getOtherBidderCurrentCash(),
                "Other bidder's cash should decrease by other bid");
        assertEquals(0, auctionState.getOwnBidderCurrentQuantityWon(),
                "Own bidder should win 0 quantity");
        assertEquals(2, auctionState.getOtherBidderCurrentQuantityWon(),
                "Other bidder should win 2 quantities");
        assertEquals(INITIAL_TOTAL_QUANTITY - 2, auctionState.getRemainingQuantity(),
                "Remaining quantity should decrease by 2 (only other won)");
    }

    @Test
    @DisplayName("Should update cash but not quantity when neither bidder wins quantity")
    void shouldUpdateCashButNotQuantityWhenNeitherWins() {
        BidderWinEvaluator ownWinEvaluator = new TestBidderWinEvaluator(0);
        BidderWinEvaluator otherWinEvaluator = new TestBidderWinEvaluator(0);
        AuctionStateUpdater updater = new AuctionStateUpdater(ownWinEvaluator, otherWinEvaluator);

        int ownBid = 5;
        int otherBid = 5;

        updater.updateState(auctionState, ownBid, otherBid);

        assertEquals(INITIAL_BASE_CASH - ownBid, auctionState.getOwnBidderCurrentCash(),
                "Own bidder's cash should decrease by own bid");
        assertEquals(INITIAL_BASE_CASH - otherBid, auctionState.getOtherBidderCurrentCash(),
                "Other bidder's cash should decrease by other bid");
        assertEquals(0, auctionState.getOwnBidderCurrentQuantityWon(),
                "Own bidder should win 0 quantity");
        assertEquals(0, auctionState.getOtherBidderCurrentQuantityWon(),
                "Other bidder should win 0 quantity");
        assertEquals(INITIAL_TOTAL_QUANTITY, auctionState.getRemainingQuantity(),
                "Remaining quantity should remain unchanged as no quantity was won");
    }

    @Test
    @DisplayName("Should handle multiple update rounds cumulatively")
    void shouldHandleMultipleUpdateRoundsCumulatively() {
        BidderWinEvaluator round1OwnWinEvaluator = new TestBidderWinEvaluator(1);
        BidderWinEvaluator round1OtherWinEvaluator = new TestBidderWinEvaluator(1);
        AuctionStateUpdater updater1 = new AuctionStateUpdater(round1OwnWinEvaluator, round1OtherWinEvaluator);
        int ownBid1 = 10;
        int otherBid1 = 10;
        updater1.updateState(auctionState, ownBid1, otherBid1);

        assertEquals(INITIAL_BASE_CASH - ownBid1, auctionState.getOwnBidderCurrentCash());
        assertEquals(INITIAL_BASE_CASH - otherBid1, auctionState.getOtherBidderCurrentCash());
        assertEquals(1, auctionState.getOwnBidderCurrentQuantityWon());
        assertEquals(1, auctionState.getOtherBidderCurrentQuantityWon());
        assertEquals(INITIAL_TOTAL_QUANTITY - 2, auctionState.getRemainingQuantity());

        BidderWinEvaluator round2OwnWinEvaluator = new TestBidderWinEvaluator(2);
        BidderWinEvaluator round2OtherWinEvaluator = new TestBidderWinEvaluator(0);
        AuctionStateUpdater updater2 = new AuctionStateUpdater(round2OwnWinEvaluator, round2OtherWinEvaluator);
        int ownBid2 = 15;
        int otherBid2 = 8;
        updater2.updateState(auctionState, ownBid2, otherBid2);

        assertEquals(INITIAL_BASE_CASH - ownBid1 - ownBid2, auctionState.getOwnBidderCurrentCash(),
                "Own bidder's cash should be cumulative");
        assertEquals(INITIAL_BASE_CASH - otherBid1 - otherBid2, auctionState.getOtherBidderCurrentCash(),
                "Other bidder's cash should be cumulative");
        assertEquals(1 + 2, auctionState.getOwnBidderCurrentQuantityWon(),
                "Own bidder's quantity won should be cumulative (1+2)");
        assertEquals(1 + 0, auctionState.getOtherBidderCurrentQuantityWon(),
                "Other bidder's quantity won should be cumulative (1+0)");
        assertEquals(INITIAL_TOTAL_QUANTITY - 2 - 2, auctionState.getRemainingQuantity(),
                "Remaining quantity should be cumulative (100-2-2)");

        BidderWinEvaluator round3OwnWinEvaluator = new TestBidderWinEvaluator(0);
        BidderWinEvaluator round3OtherWinEvaluator = new TestBidderWinEvaluator(0);
        AuctionStateUpdater updater3 = new AuctionStateUpdater(round3OwnWinEvaluator, round3OtherWinEvaluator);
        int ownBid3 = 2;
        int otherBid3 = 3;
        updater3.updateState(auctionState, ownBid3, otherBid3);

        assertEquals(INITIAL_BASE_CASH - ownBid1 - ownBid2 - ownBid3, auctionState.getOwnBidderCurrentCash(),
                "Own bidder's cash should be cumulative after 3 rounds");
        assertEquals(INITIAL_BASE_CASH - otherBid1 - otherBid2 - otherBid3, auctionState.getOtherBidderCurrentCash(),
                "Other bidder's cash should be cumulative after 3 rounds");
        assertEquals(3, auctionState.getOwnBidderCurrentQuantityWon(),
                "Own bidder's quantity won remains 3");
        assertEquals(1, auctionState.getOtherBidderCurrentQuantityWon(),
                "Other bidder's quantity won remains 1");
        assertEquals(INITIAL_TOTAL_QUANTITY - 4, auctionState.getRemainingQuantity(),
                "Remaining quantity remains " + (INITIAL_TOTAL_QUANTITY - 4));
    }
}
