package tests.impl;

import com.optimax.tradingbot.bidder.BidderState;
import com.optimax.tradingbot.exceptions.InternalStrategyException;
import com.optimax.tradingbot.impl.BidderContext;
import com.optimax.tradingbot.impl.BidderImpl;
import com.optimax.tradingbot.utils.RandomStringGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BidderImplTest {

    @Mock
    private com.optimax.tradingbot.bidder.BidderStrategy mockStrategy;
    @Mock
    private com.optimax.tradingbot.bidder.BidderWinEvaluator mockWinnerEvaluator;
    @Mock
    private BidderContext mockContext;

    private BidderImpl bidder;

    @Test
    @DisplayName("should initialize with correct parameters and register state")
    void shouldInitializeWithCorrectParametersAndRegisterState() {
        try (MockedStatic<RandomStringGenerator> mockedStatic = mockStatic(RandomStringGenerator.class)) {
            mockedStatic.when(() -> RandomStringGenerator.generateRandomString(5))
                    .thenReturn("test1"); // Ensure unique ID for this test

            when(mockContext.getAllIds()).thenReturn(Collections.emptyList()); // Simulate no existing IDs

            bidder = new BidderImpl(100, 500, mockStrategy, mockWinnerEvaluator, mockContext);

            assertNotNull(bidder.getState());
            assertEquals("test1", bidder.getState().id());
            assertEquals(100, bidder.getState().totalQuantity());
            assertEquals(500, bidder.getState().cash());

            verify(mockContext).putState(argThat(state ->
                    state.id().equals("test1") &&
                            state.totalQuantity() == 100 &&
                            state.cash() == 500
            ));
        }
    }

    @Test
    @DisplayName("should generate unique ID if existing IDs conflict")
    void shouldGenerateUniqueIdIfExistingIdsConflict() {
        try (MockedStatic<RandomStringGenerator> mockedStatic = mockStatic(RandomStringGenerator.class)) {
            mockedStatic.when(() -> RandomStringGenerator.generateRandomString(5))
                    .thenReturn("dup", "unique");

            when(mockContext.getAllIds()).thenReturn(List.of("dup"), Collections.emptyList());

            bidder = new BidderImpl(100, 500, mockStrategy, mockWinnerEvaluator, mockContext);

            assertNotNull(bidder.getState());
            assertEquals("unique", bidder.getState().id()); // Assert that the unique ID was used
            verify(mockContext).putState(argThat(state -> state.id().equals("unique")));
        }
    }

    @Test
    @DisplayName("should reset state with new quantity and cash")
    void shouldResetStateWithNewQuantityAndCash() {
        try (MockedStatic<RandomStringGenerator> mockedStatic = mockStatic(RandomStringGenerator.class)) {
            mockedStatic.when(() -> RandomStringGenerator.generateRandomString(5)).thenReturn("initialId");
            when(mockContext.getAllIds()).thenReturn(Collections.emptyList());
            bidder = new BidderImpl(100, 500, mockStrategy, mockWinnerEvaluator, mockContext);
            reset(mockContext);
        }

        bidder.init(200, 600);

        BidderState currentState = bidder.getState();
        assertNotNull(currentState);
        assertEquals(200, currentState.totalQuantity());
        assertEquals(600, currentState.cash());
        assertEquals("initialId", currentState.id()); // ID should remain the same after init

        // Verify that the updated state was put into the context
        verify(mockContext).putState(argThat(state ->
                state.id().equals("initialId") &&
                        state.totalQuantity() == 200 &&
                        state.cash() == 600
        ));
    }

    @Test
    @DisplayName("should throw InvalidParameterException for negative quantity in init")
    void shouldThrowInvalidParameterExceptionForNegativeQuantityInInit() {
        try (MockedStatic<RandomStringGenerator> mockedStatic = mockStatic(RandomStringGenerator.class)) {
            mockedStatic.when(() -> RandomStringGenerator.generateRandomString(5)).thenReturn("testId");
            when(mockContext.getAllIds()).thenReturn(Collections.emptyList());
            bidder = new BidderImpl(100, 500, mockStrategy, mockWinnerEvaluator, mockContext);
        }
        assertThrows(InvalidParameterException.class, () -> bidder.init(-10, 500));
    }

    @Test
    @DisplayName("should throw InvalidParameterException for negative cash in init")
    void shouldThrowInvalidParameterExceptionForNegativeCashInInit() {
        try (MockedStatic<RandomStringGenerator> mockedStatic = mockStatic(RandomStringGenerator.class)) {
            mockedStatic.when(() -> RandomStringGenerator.generateRandomString(5)).thenReturn("testId");
            when(mockContext.getAllIds()).thenReturn(Collections.emptyList());
            bidder = new BidderImpl(100, 500, mockStrategy, mockWinnerEvaluator, mockContext);
        }
        assertThrows(InvalidParameterException.class, () -> bidder.init(100, -50));
    }

    @Test
    @DisplayName("should return 0 if strategy returns empty optional for bid")
    void shouldReturnZeroIfStrategyReturnsEmptyOptionalForBid() {
        try (MockedStatic<RandomStringGenerator> mockedStatic = mockStatic(RandomStringGenerator.class)) {
            mockedStatic.when(() -> RandomStringGenerator.generateRandomString(5)).thenReturn("testId");
            when(mockContext.getAllIds()).thenReturn(Collections.emptyList());
            bidder = new BidderImpl(100, 500, mockStrategy, mockWinnerEvaluator, mockContext);
        }

        when(mockStrategy.nextBid(any(BidderState.class), any(BidderContext.class))).thenReturn(OptionalInt.empty());

        int bid = bidder.placeBid();
        assertEquals(0, bid);
        assertEquals(500, bidder.getState().cash()); // Cash should not change if no bid
    }

    @Test
    @DisplayName("should deduct bid amount from cash if strategy returns valid bid")
    void shouldDeductBidAmountFromCashIfStrategyReturnsValidBid() {
        try (MockedStatic<RandomStringGenerator> mockedStatic = mockStatic(RandomStringGenerator.class)) {
            mockedStatic.when(() -> RandomStringGenerator.generateRandomString(5)).thenReturn("testId");
            when(mockContext.getAllIds()).thenReturn(Collections.emptyList());
            bidder = new BidderImpl(100, 500, mockStrategy, mockWinnerEvaluator, mockContext);
        }

        when(mockStrategy.nextBid(any(BidderState.class), any(BidderContext.class))).thenReturn(OptionalInt.of(100));

        int bid = bidder.placeBid();
        assertEquals(100, bid);
        assertEquals(400, bidder.getState().cash()); // 500 - 100 = 400
    }

    @Test
    @DisplayName("should throw InternalStrategyException if strategy proposes bid exceeding available cash")
    void shouldThrowInternalStrategyExceptionIfStrategyProposesBidExceedingAvailableCash() {
        try (MockedStatic<RandomStringGenerator> mockedStatic = mockStatic(RandomStringGenerator.class)) {
            mockedStatic.when(() -> RandomStringGenerator.generateRandomString(5)).thenReturn("testId");
            when(mockContext.getAllIds()).thenReturn(Collections.emptyList());
            bidder = new BidderImpl(100, 500, mockStrategy, mockWinnerEvaluator, mockContext);
        }

        when(mockStrategy.nextBid(any(BidderState.class), any(BidderContext.class))).thenReturn(OptionalInt.of(600)); // Bid more than current cash

        assertThrows(InternalStrategyException.class, () -> bidder.placeBid());
        assertEquals(500, bidder.getState().cash()); // Cash should remain unchanged due to exception
    }

    @Test
    @DisplayName("should throw InvalidParameterException if own bid is negative in bids")
    void shouldThrowInvalidParameterExceptionIfOwnBidIsNegativeInBids() {
        try (MockedStatic<RandomStringGenerator> mockedStatic = mockStatic(RandomStringGenerator.class)) {
            mockedStatic.when(() -> RandomStringGenerator.generateRandomString(5)).thenReturn("testId");
            when(mockContext.getAllIds()).thenReturn(Collections.emptyList());
            bidder = new BidderImpl(100, 500, mockStrategy, mockWinnerEvaluator, mockContext);
        }
        assertThrows(InvalidParameterException.class, () -> bidder.bids(-10, 50));
    }

    @Test
    @DisplayName("should throw InvalidParameterException if other bid is negative in bids")
    void shouldThrowInvalidParameterExceptionIfOtherBidIsNegativeInBids() {
        try (MockedStatic<RandomStringGenerator> mockedStatic = mockStatic(RandomStringGenerator.class)) {
            mockedStatic.when(() -> RandomStringGenerator.generateRandomString(5)).thenReturn("testId");
            when(mockContext.getAllIds()).thenReturn(Collections.emptyList());
            bidder = new BidderImpl(100, 500, mockStrategy, mockWinnerEvaluator, mockContext);
        }
        assertThrows(InvalidParameterException.class, () -> bidder.bids(10, -50));
    }

    @Test
    @DisplayName("should update own state based on winner evaluator and finish round")
    void shouldUpdateOwnStateBasedOnWinnerEvaluatorAndFinishRound() {
        try (MockedStatic<RandomStringGenerator> mockedStatic = mockStatic(RandomStringGenerator.class)) {
            mockedStatic.when(() -> RandomStringGenerator.generateRandomString(5)).thenReturn("testId");
            when(mockContext.getAllIds()).thenReturn(Collections.emptyList());
            bidder = new BidderImpl(100, 500, mockStrategy, mockWinnerEvaluator, mockContext);
        }

        when(mockWinnerEvaluator.evaluateWonQuantity(50, 40)).thenReturn(2);

        bidder.bids(50, 40);

        BidderState currentState = bidder.getState();
        assertEquals(2, currentState.getQuantity());
        verify(mockStrategy).finishRound(); // Verify that strategy.finishRound() was called
    }

    @Test
    @DisplayName("should return the current bidder state")
    void shouldReturnTheCurrentBidderState() {
        try (MockedStatic<RandomStringGenerator> mockedStatic = mockStatic(RandomStringGenerator.class)) {
            mockedStatic.when(() -> RandomStringGenerator.generateRandomString(5)).thenReturn("testId");
            when(mockContext.getAllIds()).thenReturn(Collections.emptyList());
            bidder = new BidderImpl(100, 500, mockStrategy, mockWinnerEvaluator, mockContext);
        }

        BidderState state = bidder.getState();
        assertNotNull(state);
        assertEquals("testId", state.id());
        assertEquals(100, state.totalQuantity());
        assertEquals(500, state.cash());
    }
}
