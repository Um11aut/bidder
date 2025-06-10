package tests.core.validation.rules;

import com.optimax.tradingbot.core.AuctionState;
import com.optimax.tradingbot.core.validation.rules.FinalQuantityExhaustionValidator;
import com.optimax.tradingbot.exceptions.AuctionValidatorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FinalQuantityExhaustionValidatorTest {
    private static final int INITIAL_TOTAL_QUANTITY = 100;
    private static final int INITIAL_BASE_CASH = 500;
    private FinalQuantityExhaustionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FinalQuantityExhaustionValidator();
    }

    @Test
    @DisplayName("Should pass when all quantity is exhausted and total won matches initial")
    void shouldPassWhenQuantityExhaustedAndWonMatches() {
        AuctionState state = new AuctionState(INITIAL_TOTAL_QUANTITY, INITIAL_BASE_CASH);
        state.setRemainingQuantity(0);
        state.setOwnBidderCurrentQuantityWon(50);
        state.setOtherBidderCurrentQuantityWon(50);

        assertDoesNotThrow(() -> validator.validate(state),
                "Validation should pass when quantity is exhausted and total won matches initial.");
    }

    @Test
    @DisplayName("Should throw exception when remaining quantity is not zero")
    void shouldThrowExceptionWhenRemainingQuantityIsNotZero() {
        AuctionState state = new AuctionState(INITIAL_TOTAL_QUANTITY, INITIAL_BASE_CASH);
        state.setRemainingQuantity(10);
        state.setOwnBidderCurrentQuantityWon(45);
        state.setOtherBidderCurrentQuantityWon(45);

        AuctionValidatorException exception = assertThrows(AuctionValidatorException.class,
                () -> validator.validate(state),
                "Validation should throw exception when remaining quantity is not zero.");

        assertEquals("Auction did not fully auction all quantity. Remaining: 10",
                exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when total quantity won does not match initial, even if remaining is zero")
    void shouldThrowExceptionWhenTotalQuantityWonDoesNotMatchInitial() {
        AuctionState state = new AuctionState(INITIAL_TOTAL_QUANTITY, INITIAL_BASE_CASH);
        state.setRemainingQuantity(0);
        state.setOwnBidderCurrentQuantityWon(40);
        state.setOtherBidderCurrentQuantityWon(50);

        AuctionValidatorException exception = assertThrows(AuctionValidatorException.class,
                () -> validator.validate(state),
                "Validation should throw exception when total quantity won does not match initial quantity.");

        assertEquals("Total quantity won by bidders does not match initial total quantity.",
                exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when both remaining quantity is not zero and total won does not match")
    void shouldThrowExceptionWhenBothConditionsFail() {
        AuctionState state = new AuctionState(INITIAL_TOTAL_QUANTITY, INITIAL_BASE_CASH);
        state.setRemainingQuantity(5);
        state.setOwnBidderCurrentQuantityWon(45);
        state.setOtherBidderCurrentQuantityWon(45);

        AuctionValidatorException exception = assertThrows(AuctionValidatorException.class,
                () -> validator.validate(state),
                "Validation should throw exception when both conditions fail.");

        // Expect the first error message encountered (remaining quantity check)
        assertEquals("Auction did not fully auction all quantity. Remaining: 5",
                exception.getMessage());
    }

    @Test
    @DisplayName("Should pass with zero initial quantity if remaining and won quantities are zero")
    void shouldPassWithZeroInitialQuantity() {
        AuctionState state = new AuctionState(0, INITIAL_BASE_CASH);
        state.setRemainingQuantity(0);
        state.setOwnBidderCurrentQuantityWon(0);
        state.setOtherBidderCurrentQuantityWon(0);

        assertDoesNotThrow(() -> validator.validate(state),
                "Validation should pass for zero initial quantity when all conditions are met.");
    }

    @Test
    @DisplayName("Should throw exception if initial quantity is zero but remaining is not")
    void shouldThrowExceptionIfInitialQuantityZeroButRemainingNot() {
        AuctionState state = new AuctionState(0, INITIAL_BASE_CASH);
        state.setRemainingQuantity(10);
        state.setOwnBidderCurrentQuantityWon(0);
        state.setOtherBidderCurrentQuantityWon(0);

        AuctionValidatorException exception = assertThrows(AuctionValidatorException.class,
                () -> validator.validate(state),
                "Validation should throw exception if initial quantity is zero but remaining is not.");

        assertEquals("Auction did not fully auction all quantity. Remaining: 10",
                exception.getMessage());
    }
}
