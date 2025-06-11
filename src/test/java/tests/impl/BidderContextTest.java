package tests.impl;

import com.optimax.tradingbot.bidder.BidderState;
import com.optimax.tradingbot.impl.BidderContext;
import com.optimax.tradingbot.impl.BidderHistoryUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BidderContextTest {

    private BidderContext bidderContext;
    private BidderState mockBidderState1;
    private BidderState mockBidderState2;
    private BidderHistoryUnit mockBidderHistoryUnit1;
    private BidderHistoryUnit mockBidderHistoryUnit2;

    @BeforeEach
    void setUp() {
        bidderContext = new BidderContext();
        mockBidderState1 = mock(BidderState.class);
        mockBidderState2 = mock(BidderState.class);
        when(mockBidderState1.id()).thenReturn("bidder1");
        when(mockBidderState2.id()).thenReturn("bidder2");

        mockBidderHistoryUnit1 = new BidderHistoryUnit(Map.of("bidder1", 10, "bidder2", 5));
        mockBidderHistoryUnit2 = new BidderHistoryUnit(Map.of("bidder1", 12, "bidder2", 8));
    }

    @Test
    @DisplayName("should add state correctly")
    void shouldAddStateCorrectly() {
        bidderContext.putState(mockBidderState1);
        List<BidderState> allStates = bidderContext.getAllStates();
        assertEquals(1, allStates.size());
        assertTrue(allStates.contains(mockBidderState1));
    }

    @Test
    @DisplayName("should add history unit correctly")
    void shouldAddHistoryUnitCorrectly() {
        bidderContext.addHistoryUnit(mockBidderHistoryUnit1);
        List<BidderHistoryUnit> history = bidderContext.getHistory();
        assertEquals(1, history.size());
        assertTrue(history.contains(mockBidderHistoryUnit1));
    }

    @Test
    @DisplayName("should return all added states")
    void shouldReturnAllAddedStates() {
        bidderContext.putState(mockBidderState1);
        bidderContext.putState(mockBidderState2);
        List<BidderState> allStates = bidderContext.getAllStates();
        assertEquals(2, allStates.size());
        assertTrue(allStates.contains(mockBidderState1));
        assertTrue(allStates.contains(mockBidderState2));
    }

    @Test
    @DisplayName("should return all added IDs")
    void shouldReturnAllAddedIds() {
        bidderContext.putState(mockBidderState1);
        bidderContext.putState(mockBidderState2);
        List<String> allIds = bidderContext.getAllIds();
        assertEquals(2, allIds.size());
        assertTrue(allIds.contains("bidder1"));
        assertTrue(allIds.contains("bidder2"));
    }

    @Test
    @DisplayName("should return all added history units")
    void shouldReturnAllAddedHistoryUnits() {
        bidderContext.addHistoryUnit(mockBidderHistoryUnit1);
        bidderContext.addHistoryUnit(mockBidderHistoryUnit2);
        List<BidderHistoryUnit> history = bidderContext.getHistory();
        assertEquals(2, history.size());
        assertTrue(history.contains(mockBidderHistoryUnit1));
        assertTrue(history.contains(mockBidderHistoryUnit2));
    }

    @Test
    @DisplayName("should exclude own ID when filtering states")
    void shouldExcludeOwnIdWhenFilteringStates() {
        bidderContext.putState(mockBidderState1);
        bidderContext.putState(mockBidderState2);
        List<BidderState> filteredStates = bidderContext.getFilteredStates("bidder1");
        assertEquals(1, filteredStates.size());
        assertFalse(filteredStates.contains(mockBidderState1));
        assertTrue(filteredStates.contains(mockBidderState2));
    }

    @Test
    @DisplayName("should return all states if own ID does not exist when filtering")
    void shouldReturnAllStatesIfOwnIdDoesNotExistWhenFiltering() {
        bidderContext.putState(mockBidderState1);
        bidderContext.putState(mockBidderState2);
        List<BidderState> filteredStates = bidderContext.getFilteredStates("nonExistentBidder");
        assertEquals(2, filteredStates.size());
        assertTrue(filteredStates.contains(mockBidderState1));
        assertTrue(filteredStates.contains(mockBidderState2));
    }

    @Test
    @DisplayName("should overwrite existing state when adding duplicate ID")
    void shouldOverwriteExistingStateWhenAddingDuplicateId() {
        BidderState mockBidderState1Updated = mock(BidderState.class);
        when(mockBidderState1Updated.id()).thenReturn("bidder1");
        bidderContext.putState(mockBidderState1);
        bidderContext.putState(mockBidderState1Updated);
        List<BidderState> allStates = bidderContext.getAllStates();
        assertEquals(1, allStates.size());
        assertTrue(allStates.contains(mockBidderState1Updated));
        assertFalse(allStates.contains(mockBidderState1));
    }
}