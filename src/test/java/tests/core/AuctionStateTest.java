package tests.core;

import com.optimax.tradingbot.core.AuctionState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuctionStateTest {
    private static final int INITIAL_QUANTITY = 100;
    private static final int INITIAL_CASH = 500;
    private AuctionState auctionState;

    @BeforeEach
    void setUp() {
        auctionState = new AuctionState(INITIAL_QUANTITY, INITIAL_CASH);
    }

    @Test
    @DisplayName("Should initialize AuctionState with correct default values")
    void shouldInitializeWithCorrectDefaultValues() {
        assertNotNull(auctionState, "AuctionState should be initialized");

        assertEquals(INITIAL_QUANTITY, auctionState.getTotalInitialQuantity(),
                "Total initial quantity should match constructor parameter");
        assertEquals(INITIAL_CASH, auctionState.getInitialBaseCash(),
                "Initial base cash should match constructor parameter");

        assertEquals(INITIAL_CASH, auctionState.getOwnBidderCash(),
                "Own bidder's initial cash should be base cash");
        assertEquals(INITIAL_CASH, auctionState.getOtherBidderCash(),
                "Other bidder's initial cash should be base cash");
        assertEquals(0, auctionState.getOwnBidderQuantityWon(),
                "Own bidder's initial quantity won should be 0");
        assertEquals(0, auctionState.getOtherBidderQuantityWon(),
                "Other bidder's initial quantity won should be 0");
        assertEquals(INITIAL_QUANTITY, auctionState.getRemainingQuantity(),
                "Remaining quantity should initially be total initial quantity");
    }

    @Test
    @DisplayName("Should correctly set own bidder's current cash")
    void shouldSetOwnBidderCurrentCash() {
        int newCash = 450;
        auctionState.setOwnBidderCurrentCash(newCash);

        assertEquals(newCash, auctionState.getOwnBidderCash(),
                "Own bidder's cash should be updated to " + newCash);
        assertEquals(INITIAL_CASH, auctionState.getOtherBidderCash(),
                "Other bidder's cash should remain unchanged");
    }

    @Test
    @DisplayName("Should correctly set other bidder's current cash")
    void shouldSetOtherBidderCurrentCash() {
        int newCash = 300;
        auctionState.setOtherBidderCurrentCash(newCash);

        assertEquals(newCash, auctionState.getOtherBidderCash(),
                "Other bidder's cash should be updated to " + newCash);
        assertEquals(INITIAL_CASH, auctionState.getOwnBidderCash(),
                "Own bidder's cash should remain unchanged");
    }

    @Test
    @DisplayName("Should correctly set own bidder's current quantity won")
    void shouldSetOwnBidderCurrentQuantityWon() {
        int newQuantity = 10;
        auctionState.setOwnBidderCurrentQuantityWon(newQuantity);

        assertEquals(newQuantity, auctionState.getOwnBidderQuantityWon(),
                "Own bidder's quantity won should be updated to " + newQuantity);
        assertEquals(0, auctionState.getOtherBidderQuantityWon(),
                "Other bidder's quantity won should remain unchanged");
    }

    @Test
    @DisplayName("Should correctly set other bidder's current quantity won")
    void shouldSetOtherBidderCurrentQuantityWon() {
        int newQuantity = 15;
        auctionState.setOtherBidderCurrentQuantityWon(newQuantity);

        assertEquals(newQuantity, auctionState.getOtherBidderQuantityWon(),
                "Other bidder's quantity won should be updated to " + newQuantity);
        assertEquals(0, auctionState.getOwnBidderQuantityWon(),
                "Own bidder's quantity won should remain unchanged");
    }

    @Test
    @DisplayName("Should correctly set remaining quantity")
    void shouldSetRemainingQuantity() {
        int newRemainingQuantity = 75;
        auctionState.setRemainingQuantity(newRemainingQuantity);

        assertEquals(newRemainingQuantity, auctionState.getRemainingQuantity(),
                "Remaining quantity should be updated to " + newRemainingQuantity);
    }

    @Test
    @DisplayName("Should handle multiple state updates correctly")
    void shouldHandleMultipleStateUpdatesCorrectly() {
        int bid1OwnCash = 490;
        int bid1OtherCash = 490;
        int bid1OwnQuantity = 5;
        int bid1OtherQuantity = 0;
        int bid1Remaining = 95;

        // First round of updates
        auctionState.setOwnBidderCurrentCash(bid1OwnCash);
        auctionState.setOtherBidderCurrentCash(bid1OtherCash);
        auctionState.setOwnBidderCurrentQuantityWon(bid1OwnQuantity);
        auctionState.setRemainingQuantity(bid1Remaining);

        assertEquals(bid1OwnCash, auctionState.getOwnBidderCash());
        assertEquals(bid1OtherCash, auctionState.getOtherBidderCash());
        assertEquals(bid1OwnQuantity, auctionState.getOwnBidderQuantityWon());
        assertEquals(bid1OtherQuantity, auctionState.getOtherBidderQuantityWon()); // Still 0
        assertEquals(bid1Remaining, auctionState.getRemainingQuantity());

        int bid2OwnCash = 480;
        int bid2OtherCash = 485;
        int bid2OtherQuantity = 10;
        int bid2Remaining = 85;

        // Second round of updates
        auctionState.setOwnBidderCurrentCash(bid2OwnCash);
        auctionState.setOtherBidderCurrentCash(bid2OtherCash);
        auctionState.setOtherBidderCurrentQuantityWon(bid2OtherQuantity);
        auctionState.setRemainingQuantity(bid2Remaining);

        assertEquals(bid2OwnCash, auctionState.getOwnBidderCash());
        assertEquals(bid2OtherCash, auctionState.getOtherBidderCash());
        assertEquals(bid1OwnQuantity, auctionState.getOwnBidderQuantityWon()); // Still 5
        assertEquals(bid2OtherQuantity, auctionState.getOtherBidderQuantityWon());
        assertEquals(bid2Remaining, auctionState.getRemainingQuantity());
    }
}
