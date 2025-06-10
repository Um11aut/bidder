package tests.core.validation.rules;

import com.optimax.tradingbot.core.AuctionState;
import com.optimax.tradingbot.core.validation.rules.NegativeCashValidator;
import com.optimax.tradingbot.exceptions.AuctionValidatorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NegativeCashValidatorTest {
    private static final int INITIAL_TOTAL_QUANTITY = 100;
    private static final int INITIAL_BASE_CASH = 500;
    private NegativeCashValidator validator;
    private AuctionState auctionState;

    @BeforeEach
    void setUp() {
        validator = new NegativeCashValidator();
        auctionState = new AuctionState(INITIAL_TOTAL_QUANTITY, INITIAL_BASE_CASH);
    }

    @Test
    @DisplayName("Should pass when both bidders have positive cash")
    void shouldPassWhenBothHavePositiveCash() {
        auctionState.setOwnBidderCurrentCash(450);
        auctionState.setOtherBidderCurrentCash(400);

        assertDoesNotThrow(() -> validator.validate(auctionState),
                "Validation should pass when both bidders have positive cash.");
    }

    @Test
    @DisplayName("Should pass when both bidders have zero cash")
    void shouldPassWhenBothHaveZeroCash() {
        auctionState.setOwnBidderCurrentCash(0);
        auctionState.setOtherBidderCurrentCash(0);

        assertDoesNotThrow(() -> validator.validate(auctionState),
                "Validation should pass when both bidders have zero cash.");
    }

    @Test
    @DisplayName("Should throw exception when own bidder's cash is negative")
    void shouldThrowExceptionWhenOwnBidderCashIsNegative() {
        auctionState.setOwnBidderCurrentCash(-10);
        auctionState.setOtherBidderCurrentCash(400);

        AuctionValidatorException exception = assertThrows(AuctionValidatorException.class,
                () -> validator.validate(auctionState),
                "Expected AuctionValidatorException when own bidder's cash is negative.");

        assertEquals("Own bidder's cash went below zero.", exception.getMessage(),
                "Exception message should indicate own bidder's negative cash.");
    }

    @Test
    @DisplayName("Should throw exception when other bidder's cash is negative")
    void shouldThrowExceptionWhenOtherBidderCashIsNegative() {
        auctionState.setOwnBidderCurrentCash(450);
        auctionState.setOtherBidderCurrentCash(-5);

        AuctionValidatorException exception = assertThrows(AuctionValidatorException.class,
                () -> validator.validate(auctionState),
                "Expected AuctionValidatorException when other bidder's cash is negative.");

        assertEquals("Other bidder's cash went below zero.", exception.getMessage(),
                "Exception message should indicate other bidder's negative cash.");
    }

    @Test
    @DisplayName("Should throw exception for own bidder if both bidders' cash is negative (prioritized)")
    void shouldThrowExceptionWhenBothBidderCashIsNegative() {
        auctionState.setOwnBidderCurrentCash(-20);
        auctionState.setOtherBidderCurrentCash(-10);

        AuctionValidatorException exception = assertThrows(AuctionValidatorException.class,
                () -> validator.validate(auctionState),
                "Expected AuctionValidatorException when both bidders' cash is negative.");

        assertEquals("Own bidder's cash went below zero.", exception.getMessage(),
                "Exception message should indicate own bidder's negative cash as it's checked first.");
    }

    @Test
    @DisplayName("Should pass if initial cash is zero and remains zero after bid")
    void shouldPassWithZeroInitialCashAndZeroRemaining() {
        AuctionState stateWithZeroInitialCash = new AuctionState(INITIAL_TOTAL_QUANTITY, 0);
        stateWithZeroInitialCash.setOwnBidderCurrentCash(0);
        stateWithZeroInitialCash.setOtherBidderCurrentCash(0);

        assertDoesNotThrow(() -> validator.validate(stateWithZeroInitialCash),
                "Validation should pass if initial cash is zero and remains zero.");
    }
}
