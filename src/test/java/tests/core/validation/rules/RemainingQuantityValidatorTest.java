package tests.core.validation.rules;

import com.optimax.tradingbot.core.AuctionState;
import com.optimax.tradingbot.core.validation.rules.RemainingQuantityValidator;
import com.optimax.tradingbot.exceptions.AuctionValidatorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RemainingQuantityValidatorTest {
    private RemainingQuantityValidator validator;
    private AuctionState auctionState;

    @BeforeEach
    void setUp() {
        validator = new RemainingQuantityValidator();
        auctionState = new AuctionState(100, 500);
    }

    @Test
    @DisplayName("Should pass when remaining quantity is positive")
    void shouldPassWhenRemainingQuantityIsPositive() {
        auctionState.setRemainingQuantity(10);
        assertDoesNotThrow(() -> validator.validate(auctionState));
    }

    @Test
    @DisplayName("Should pass when remaining quantity is zero")
    void shouldPassWhenRemainingQuantityIsZero() {
        auctionState.setRemainingQuantity(0);
        assertDoesNotThrow(() -> validator.validate(auctionState));
    }

    @Test
    @DisplayName("Should throw exception when remaining quantity is negative")
    void shouldThrowExceptionWhenRemainingQuantityIsNegative() {
        auctionState.setRemainingQuantity(-5);
        AuctionValidatorException exception = assertThrows(AuctionValidatorException.class,
                () -> validator.validate(auctionState));
        assertEquals("Remaining quantity went below zero, indicating over-allocation.", exception.getMessage());
    }
}
