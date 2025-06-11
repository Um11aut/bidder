package tests.core;

import com.optimax.tradingbot.bidder.Bidder;
import com.optimax.tradingbot.bidder.BidderState;
import com.optimax.tradingbot.bidder.BidderStrategy;
import com.optimax.tradingbot.bidder.BidderWinEvaluator;
import com.optimax.tradingbot.core.Auction;
import com.optimax.tradingbot.core.AuctionState;
import com.optimax.tradingbot.core.validation.AuctionVerifier;
import com.optimax.tradingbot.exceptions.AuctionValidatorException;
import com.optimax.tradingbot.exceptions.InternalStrategyException;
import com.optimax.tradingbot.impl.BidderContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.InvalidParameterException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionTest {
    @Mock
    private BidderStrategy mockOwnStrategy;
    @Mock
    private BidderStrategy mockOpponentStrategy;
    @Mock
    private Bidder mockOwnBidder;
    @Mock
    private Bidder mockOtherBidder;

    @Mock
    private BidderWinEvaluator mockBidderWinEvaluator;

    // Use @Captor to capture arguments passed to mocked methods if needed for inspection
    @Captor
    ArgumentCaptor<Integer> bidCaptor;


    @BeforeEach
    void setUp() {
        reset(mockOwnStrategy, mockOpponentStrategy, mockOwnBidder, mockOtherBidder, mockBidderWinEvaluator);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for odd total quantity")
    void constructor_shouldThrowExceptionForOddTotalQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                new Auction(15, 100, mockOwnStrategy, mockOpponentStrategy));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for zero total quantity")
    void constructor_shouldThrowExceptionForZeroTotalQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                new Auction(0, 100, mockOwnStrategy, mockOpponentStrategy));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for negative total quantity")
    void constructor_shouldThrowExceptionForNegativeTotalQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                new Auction(-10, 100, mockOwnStrategy, mockOpponentStrategy));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for negative base cash")
    void constructor_shouldThrowExceptionForNegativeBaseCash() {
        assertThrows(IllegalArgumentException.class, () ->
                new Auction(10, -50, mockOwnStrategy, mockOpponentStrategy));
    }

    @Test
    @DisplayName("Should initialize bidders and verifier correctly")
    void constructor_shouldInitializeCorrectly() throws Exception {
        int totalQuantity = 10;
        int baseCash = 100;
        Auction auction = new Auction(totalQuantity, baseCash, mockOwnStrategy, mockOpponentStrategy);

        AuctionState auctionStateCreatedByAuction = (AuctionState) getPrivateField(auction, "auctionState");
        assertNotNull(auctionStateCreatedByAuction);
        assertEquals(totalQuantity, auctionStateCreatedByAuction.getTotalInitialQuantity());
        assertEquals(baseCash, auctionStateCreatedByAuction.getInitialBaseCash());

        // why the hell java type introspection is so bad?
        AuctionVerifier auctionVerifierCreatedByAuction = (AuctionVerifier) getPrivateField(auction, "verifier");
        assertNotNull(auctionVerifierCreatedByAuction);
    }

    @Test
    @DisplayName("Auction loop should run correct number of rounds")
    void auctionLoop_shouldRunCorrectRounds() throws Exception {
        int totalQuantity = 4; // 2 rounds
        int baseCash = 100;
        Auction auction = new Auction(totalQuantity, baseCash, mockOwnStrategy, mockOpponentStrategy);

        setPrivateField(auction, "ownBidder", mockOwnBidder);
        setPrivateField(auction, "otherBidder", mockOtherBidder);
        setPrivateField(auction, "context", new BidderContext());

        AuctionVerifier spiedVerifier = spy((AuctionVerifier) getPrivateField(auction, "verifier"));
        setPrivateField(auction, "verifier", spiedVerifier);

        BidderState ownState = mock(BidderState.class);
        when(ownState.id()).thenReturn("own");
        when(mockOwnBidder.getState()).thenReturn(ownState);

        BidderState otherState = mock(BidderState.class);
        when(otherState.id()).thenReturn("opponent");
        when(mockOtherBidder.getState()).thenReturn(otherState);

        when(mockOwnBidder.placeBid()).thenReturn(10, 10);
        when(mockOtherBidder.placeBid()).thenReturn(5, 5);

        invokeAuctionLoop(auction, totalQuantity / 2);

        verify(mockOwnBidder, times(totalQuantity / 2)).placeBid();
        verify(mockOtherBidder, times(totalQuantity / 2)).placeBid();

        verify(mockOwnBidder, times(totalQuantity / 2)).bids(anyInt(), anyInt());
        verify(mockOtherBidder, times(totalQuantity / 2)).bids(anyInt(), anyInt());

        verify(spiedVerifier, times(totalQuantity / 2)).verifyRound(anyInt(), anyInt());

        AuctionState finalState = (AuctionState) getPrivateField(auction, "auctionState");
        assertEquals(0, finalState.getRemainingQuantity());
    }

    @Test
    @DisplayName("Auction should terminate early if both bidders place 0 bids")
    void auctionLoop_shouldTerminateOnZeroBids() throws Exception {
        int totalQuantity = 10; // Max 5 rounds
        int baseCash = 100;

        Auction auction = new Auction(totalQuantity, baseCash, mockOwnStrategy, mockOpponentStrategy);

        setPrivateField(auction, "ownBidder", mockOwnBidder);
        setPrivateField(auction, "otherBidder", mockOtherBidder);
        setPrivateField(auction, "context", new BidderContext());

        AuctionVerifier spiedVerifier = spy((AuctionVerifier) getPrivateField(auction, "verifier"));
        setPrivateField(auction, "verifier", spiedVerifier);

        BidderState ownState = mock(BidderState.class);
        when(ownState.id()).thenReturn("own");
        when(mockOwnBidder.getState()).thenReturn(ownState);

        BidderState otherState = mock(BidderState.class);
        when(otherState.id()).thenReturn("opponent");
        when(mockOtherBidder.getState()).thenReturn(otherState);

        when(mockOwnBidder.placeBid()).thenReturn(10, 0);
        when(mockOtherBidder.placeBid()).thenReturn(5, 0);

        invokeAuctionLoop(auction, totalQuantity / 2);

        verify(mockOwnBidder, times(5)).placeBid();
        verify(mockOtherBidder, times(5)).placeBid();

        verify(mockOwnBidder, times(5)).bids(anyInt(), anyInt());
        verify(mockOtherBidder, times(5)).bids(anyInt(), anyInt());

        verify(spiedVerifier, times(5)).verifyRound(anyInt(), anyInt());

        AuctionState finalState = (AuctionState) getPrivateField(auction, "auctionState");
        assertEquals(0, finalState.getRemainingQuantity());
        assertEquals(6, finalState.getOwnBidderCurrentQuantityWon());
        assertEquals(4, finalState.getOtherBidderCurrentQuantityWon());
    }

    @Test
    @DisplayName("Auction should handle InternalStrategyException from placeBid")
    void auctionLoop_shouldHandleInternalStrategyException() throws Exception {
        int totalQuantity = 10;
        int baseCash = 100;

        Auction auction = new Auction(totalQuantity, baseCash, mockOwnStrategy, mockOpponentStrategy);
        setPrivateField(auction, "ownBidder", mockOwnBidder);
        setPrivateField(auction, "otherBidder", mockOtherBidder);
        AuctionVerifier spiedVerifier = spy((AuctionVerifier) getPrivateField(auction, "verifier"));
        setPrivateField(auction, "verifier", spiedVerifier);

        when(mockOwnBidder.placeBid()).thenThrow(new InternalStrategyException("Strategy error"));

        invokeAuctionLoop(auction, totalQuantity / 2);

        verify(mockOwnBidder, times(1)).placeBid();
        verify(mockOtherBidder, never()).placeBid();
        verify(mockOwnBidder, never()).bids(anyInt(), anyInt());
        verify(mockOtherBidder, never()).bids(anyInt(), anyInt());
        verify(spiedVerifier, never()).verifyRound(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Auction should handle InvalidParameterException from bids")
    void auctionLoop_shouldHandleInvalidParameterException() throws Exception {
        int totalQuantity = 10;
        int baseCash = 100;

        Auction auction = new Auction(totalQuantity, baseCash, mockOwnStrategy, mockOpponentStrategy);
        setPrivateField(auction, "ownBidder", mockOwnBidder);
        setPrivateField(auction, "otherBidder", mockOtherBidder);
        AuctionVerifier spiedVerifier = spy((AuctionVerifier) getPrivateField(auction, "verifier"));
        setPrivateField(auction, "verifier", spiedVerifier);

        when(mockOwnBidder.placeBid()).thenReturn(10);
        when(mockOtherBidder.placeBid()).thenReturn(5);
        doThrow(new InvalidParameterException("Invalid bid parameter")).when(mockOwnBidder).bids(anyInt(), anyInt());

        invokeAuctionLoop(auction, totalQuantity / 2);

        verify(mockOwnBidder, times(1)).placeBid();
        verify(mockOtherBidder, times(1)).placeBid();

        verify(mockOwnBidder, times(1)).bids(anyInt(), anyInt());
        verify(mockOtherBidder, never()).bids(anyInt(), anyInt()); // Loop returns after first exception
        verify(spiedVerifier, never()).verifyRound(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Auction should handle AuctionValidatorException from verifyRound")
    void auctionLoop_shouldHandleAuctionValidatorException() throws Exception {
        int totalQuantity = 10;
        int baseCash = 100;

        Auction auction = new Auction(totalQuantity, baseCash, mockOwnStrategy, mockOpponentStrategy);
        setPrivateField(auction, "ownBidder", mockOwnBidder);
        setPrivateField(auction, "otherBidder", mockOtherBidder);
        AuctionVerifier spiedVerifier = spy((AuctionVerifier) getPrivateField(auction, "verifier"));
        setPrivateField(auction, "verifier", spiedVerifier);

        when(mockOwnBidder.placeBid()).thenReturn(1000); // Bid high enough to cause issue
        when(mockOtherBidder.placeBid()).thenReturn(1000);

        doThrow(new AuctionValidatorException("Validation error: cash went negative")).when(spiedVerifier).verifyRound(anyInt(), anyInt());

        invokeAuctionLoop(auction, totalQuantity / 2);

        verify(mockOwnBidder, times(1)).placeBid();
        verify(mockOtherBidder, times(1)).placeBid();
        verify(mockOwnBidder, times(1)).bids(anyInt(), anyInt());
        verify(mockOtherBidder, times(1)).bids(anyInt(), anyInt());
        verify(spiedVerifier, times(1)).verifyRound(anyInt(), anyInt());
    }

    @Test
    @DisplayName("run method should call auctionLoop and verifyFinalState")
    void run_shouldCallLoopAndFinalState() throws Exception {
        int totalQuantity = 4; // 2 rounds
        int baseCash = 100;

        Auction auction = new Auction(totalQuantity, baseCash, mockOwnStrategy, mockOpponentStrategy);
        Auction spiedAuction = spy(auction);

        setPrivateField(spiedAuction, "ownBidder", mockOwnBidder);
        setPrivateField(spiedAuction, "otherBidder", mockOtherBidder);
        setPrivateField(spiedAuction, "context", new BidderContext());

        when(mockOwnBidder.placeBid()).thenReturn(10, 10);
        when(mockOtherBidder.placeBid()).thenReturn(5, 5);

        BidderState ownState = mock(BidderState.class);
        when(ownState.id()).thenReturn("own");
        when(mockOwnBidder.getState()).thenReturn(ownState);

        BidderState otherState = mock(BidderState.class);
        when(otherState.id()).thenReturn("opponent");
        when(mockOtherBidder.getState()).thenReturn(otherState);

        AuctionVerifier spiedVerifier = spy((AuctionVerifier) getPrivateField(auction, "verifier"));
        setPrivateField(spiedAuction, "verifier", spiedVerifier); // Inject the spied verifier

        spiedAuction.run();

        this.invokeAuctionLoop(verify(spiedAuction, times(1)), totalQuantity / 2);
        verify(spiedVerifier, times(1)).verifyFinalState();
    }

    @Test
    @DisplayName("run method should handle AuctionValidatorException from verifyFinalState")
    void run_shouldHandleFinalStateVerificationError() throws Exception {
        int totalQuantity = 4;
        int baseCash = 100;

        Auction auction = new Auction(totalQuantity, baseCash, mockOwnStrategy, mockOpponentStrategy);
        Auction spiedAuction = spy(auction);

        setPrivateField(spiedAuction, "ownBidder", mockOwnBidder);
        setPrivateField(spiedAuction, "otherBidder", mockOtherBidder);
        setPrivateField(spiedAuction, "context", new BidderContext());

        when(mockOwnBidder.placeBid()).thenReturn(10, 10);
        when(mockOtherBidder.placeBid()).thenReturn(5, 5);

        BidderState ownState = mock(BidderState.class);
        when(ownState.id()).thenReturn("own");
        when(mockOwnBidder.getState()).thenReturn(ownState);

        BidderState otherState = mock(BidderState.class);
        when(otherState.id()).thenReturn("opponent");
        when(mockOtherBidder.getState()).thenReturn(otherState);

        AuctionVerifier spiedVerifier = spy((AuctionVerifier) getPrivateField(auction, "verifier"));
        setPrivateField(spiedAuction, "verifier", spiedVerifier);

        doThrow(new AuctionValidatorException("Final state check failed")).when(spiedVerifier).verifyFinalState();

        spiedAuction.run();

        this.invokeAuctionLoop(verify(spiedAuction, times(1)), totalQuantity / 2);
        verify(spiedVerifier, times(1)).verifyFinalState();
    }


    /**
     * Helper method to get private fields using reflection for testing.
     */
    private Object getPrivateField(Object target, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true); // Allow access to private field
        return field.get(target);
    }

    /**
     * Helper method to set private fields using reflection for testing.
     */
    private void setPrivateField(Object target, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true); // Allow access to private field
        field.set(target, value);
    }

    /**
     * Helper method to invoke private methods using reflection for testing.
     */
    private void invokeAuctionLoop(Object target, Object... args) throws Exception {
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].getClass();
            // Handle primitive types if necessary, e.g., for int, use int.class
            if (argTypes[i] == Integer.class) {
                argTypes[i] = int.class;
            }
        }
        java.lang.reflect.Method method = target.getClass().getDeclaredMethod("auctionLoop", argTypes);
        method.setAccessible(true); // Allow access to private method
        method.invoke(target, args);
    }
}