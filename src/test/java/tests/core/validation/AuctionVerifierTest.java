package tests.core.validation;

import com.optimax.tradingbot.bidder.BidderWinEvaluator;
import com.optimax.tradingbot.core.AuctionState;
import com.optimax.tradingbot.core.validation.AuctionRuleValidator;
import com.optimax.tradingbot.core.validation.AuctionVerifier;
import com.optimax.tradingbot.core.validation.CompositeAuctionValidator;
import com.optimax.tradingbot.exceptions.AuctionValidatorException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class AuctionVerifierTest {
    private static final int INITIAL_TOTAL_QUANTITY = 100;
    private static final int INITIAL_BASE_CASH = 500;
    private AuctionState auctionState;

    record TestBidderWinEvaluator(int quantityToReturn) implements BidderWinEvaluator {
        @Override
            public int evaluateWonQuantity(int ownBid, int otherBid) {
                return quantityToReturn;
            }
        }

    static class TestAuctionRuleValidator implements AuctionRuleValidator {
        private final boolean shouldThrowException;
        private final String exceptionMessage;
        private boolean validateCalled = false;

        public TestAuctionRuleValidator(boolean shouldThrowException, String exceptionMessage) {
            this.shouldThrowException = shouldThrowException;
            this.exceptionMessage = exceptionMessage;
        }

        public TestAuctionRuleValidator() {
            this(false, null);
        }

        @Override
        public void validate(AuctionState state) throws AuctionValidatorException {
            validateCalled = true;
            if (shouldThrowException) {
                throw new AuctionValidatorException(exceptionMessage);
            }
        }

        public boolean hasValidateBeenCalled() {
            return validateCalled;
        }

        public void resetValidationCallStatus() {
            validateCalled = false;
        }
    }

    @BeforeEach
    void setUp() {
        auctionState = new AuctionState(INITIAL_TOTAL_QUANTITY, INITIAL_BASE_CASH);
    }

    @Test
    @DisplayName("Should initialize AuctionVerifier with DefaultBidderWinEvaluator")
    void shouldInitializeWithDefaultEvaluators() {
        CompositeAuctionValidator roundValidator = new CompositeAuctionValidator(List.of());
        CompositeAuctionValidator finalValidator = new CompositeAuctionValidator(List.of());

        AuctionVerifier verifier = new AuctionVerifier(auctionState, roundValidator, finalValidator);

        assertNotNull(verifier, "AuctionVerifier should be initialized");
    }

    @Test
    @DisplayName("Should initialize AuctionVerifier with custom BidderWinEvaluators")
    void shouldInitializeWithCustomEvaluators() {
        CompositeAuctionValidator roundValidator = new CompositeAuctionValidator(List.of());
        CompositeAuctionValidator finalValidator = new CompositeAuctionValidator(List.of());
        BidderWinEvaluator customOwnEvaluator = new TestBidderWinEvaluator(1);
        BidderWinEvaluator customOtherEvaluator = new TestBidderWinEvaluator(0);

        AuctionVerifier verifier = new AuctionVerifier(auctionState, roundValidator, finalValidator,
                customOwnEvaluator, customOtherEvaluator);

        assertNotNull(verifier, "AuctionVerifier should be initialized with custom evaluators");
        try {
            verifier.verifyRound(10, 5);
            assertEquals(INITIAL_BASE_CASH - 10, auctionState.getOwnBidderCurrentCash());
            assertEquals(INITIAL_BASE_CASH - 5, auctionState.getOtherBidderCurrentCash());
            assertEquals(1, auctionState.getOwnBidderCurrentQuantityWon());
            assertEquals(0, auctionState.getOtherBidderCurrentQuantityWon());
            assertEquals(INITIAL_TOTAL_QUANTITY - 1, auctionState.getRemainingQuantity());
        } catch (AuctionValidatorException e) {
            fail("No exception expected for successful round validation: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("verifyRound should update state and call round validators on success")
    void verifyRoundShouldUpdateStateAndCallRoundValidatorsOnSuccess() throws AuctionValidatorException {
        TestAuctionRuleValidator singleRoundValidator = new TestAuctionRuleValidator();
        CompositeAuctionValidator roundValidator = new CompositeAuctionValidator(List.of(singleRoundValidator));
        CompositeAuctionValidator finalValidator = new CompositeAuctionValidator(List.of());
        BidderWinEvaluator ownWinEvaluator = new TestBidderWinEvaluator(1);
        BidderWinEvaluator otherWinEvaluator = new TestBidderWinEvaluator(1);

        AuctionVerifier verifier = new AuctionVerifier(auctionState, roundValidator, finalValidator,
                ownWinEvaluator, otherWinEvaluator);

        int ownBid = 10;
        int otherBid = 10;

        verifier.verifyRound(ownBid, otherBid);

        assertEquals(INITIAL_BASE_CASH - ownBid, auctionState.getOwnBidderCurrentCash());
        assertEquals(INITIAL_BASE_CASH - otherBid, auctionState.getOtherBidderCurrentCash());
        assertEquals(1, auctionState.getOwnBidderCurrentQuantityWon());
        assertEquals(1, auctionState.getOtherBidderCurrentQuantityWon());
        assertEquals(INITIAL_TOTAL_QUANTITY - 2, auctionState.getRemainingQuantity());

        assertTrue(singleRoundValidator.hasValidateBeenCalled(), "Round validator's validate method should have been called");
    }

    @Test
    @DisplayName("verifyRound should throw AuctionValidatorException if round validation fails")
    void verifyRoundShouldThrowExceptionOnRoundValidationFailure() {
        String expectedErrorMessage = "Round validation failed!";
        TestAuctionRuleValidator failingRoundValidator = new TestAuctionRuleValidator(true, expectedErrorMessage);
        CompositeAuctionValidator roundValidator = new CompositeAuctionValidator(List.of(failingRoundValidator));
        CompositeAuctionValidator finalValidator = new CompositeAuctionValidator(List.of());
        BidderWinEvaluator ownWinEvaluator = new TestBidderWinEvaluator(0);
        BidderWinEvaluator otherWinEvaluator = new TestBidderWinEvaluator(0);

        AuctionVerifier verifier = new AuctionVerifier(auctionState, roundValidator, finalValidator,
                ownWinEvaluator, otherWinEvaluator);

        int ownBid = 5;
        int otherBid = 5;

        AuctionValidatorException thrown = assertThrows(AuctionValidatorException.class,
                () -> verifier.verifyRound(ownBid, otherBid),
                "Expected AuctionValidatorException to be thrown");

        assertEquals(expectedErrorMessage, thrown.getMessage(), "Exception message should match");

        assertEquals(INITIAL_BASE_CASH - ownBid, auctionState.getOwnBidderCurrentCash());
        assertEquals(INITIAL_BASE_CASH - otherBid, auctionState.getOtherBidderCurrentCash());
        assertEquals(0, auctionState.getOwnBidderCurrentQuantityWon());
        assertEquals(0, auctionState.getOtherBidderCurrentQuantityWon());
        assertEquals(INITIAL_TOTAL_QUANTITY, auctionState.getRemainingQuantity());

        assertTrue(failingRoundValidator.hasValidateBeenCalled(), "shouldn't happen");
    }

    @Test
    @DisplayName("verifyFinalState should call final validators on success")
    void verifyFinalStateShouldCallFinalValidatorsOnSuccess() throws AuctionValidatorException {
        TestAuctionRuleValidator singleFinalValidator = new TestAuctionRuleValidator();
        CompositeAuctionValidator roundValidator = new CompositeAuctionValidator(List.of());
        CompositeAuctionValidator finalValidator = new CompositeAuctionValidator(List.of(singleFinalValidator));

        AuctionVerifier verifier = new AuctionVerifier(auctionState, roundValidator, finalValidator);

        verifier.verifyFinalState();

        assertTrue(singleFinalValidator.hasValidateBeenCalled(), "shouldn't happen");
    }

    @Test
    @DisplayName("verifyFinalState should throw AuctionValidatorException if final validation fails")
    void verifyFinalStateShouldThrowExceptionOnFinalValidationFailure() {
        String expectedErrorMessage = "Final validation failed!";
        TestAuctionRuleValidator failingFinalValidator = new TestAuctionRuleValidator(true, expectedErrorMessage);
        CompositeAuctionValidator roundValidator = new CompositeAuctionValidator(List.of());
        CompositeAuctionValidator finalValidator = new CompositeAuctionValidator(List.of(failingFinalValidator));

        AuctionVerifier verifier = new AuctionVerifier(auctionState, roundValidator, finalValidator);

        AuctionValidatorException thrown = assertThrows(AuctionValidatorException.class,
                verifier::verifyFinalState,
                "Expected AuctionValidatorException to be thrown");

        assertEquals(expectedErrorMessage, thrown.getMessage(), "Exception message should match");

        assertTrue(failingFinalValidator.hasValidateBeenCalled(), "Failing final validator's validate method should have been called");
    }

    @Test
    @DisplayName("Should process multiple rounds and then verify final state successfully")
    void shouldProcessMultipleRoundsAndVerifyFinalStateSuccessfully() throws AuctionValidatorException {
        TestAuctionRuleValidator roundValidatorRule1 = new TestAuctionRuleValidator(); // Pass
        TestAuctionRuleValidator roundValidatorRule2 = new TestAuctionRuleValidator(); // Pass
        CompositeAuctionValidator roundValidator = new CompositeAuctionValidator(List.of(roundValidatorRule1, roundValidatorRule2));

        TestAuctionRuleValidator finalValidatorRule = new TestAuctionRuleValidator(); // Pass
        CompositeAuctionValidator finalValidator = new CompositeAuctionValidator(List.of(finalValidatorRule));

        BidderWinEvaluator ownWinEvaluator = new TestBidderWinEvaluator(1);
        BidderWinEvaluator otherWinEvaluator = new TestBidderWinEvaluator(0);
        BidderWinEvaluator bothWinEvaluator = new TestBidderWinEvaluator(1);

        AuctionVerifier verifier = new AuctionVerifier(auctionState, roundValidator, finalValidator,
                ownWinEvaluator, bothWinEvaluator);

        // Round 1
        int ownBid1 = 10;
        int otherBid1 = 8;
        verifier.verifyRound(ownBid1, otherBid1);
        assertTrue(roundValidatorRule1.hasValidateBeenCalled());
        assertTrue(roundValidatorRule2.hasValidateBeenCalled());
        roundValidatorRule1.resetValidationCallStatus();
        roundValidatorRule2.resetValidationCallStatus();

        assertEquals(INITIAL_BASE_CASH - ownBid1, auctionState.getOwnBidderCurrentCash());
        assertEquals(INITIAL_BASE_CASH - otherBid1, auctionState.getOtherBidderCurrentCash());
        assertEquals(1, auctionState.getOwnBidderCurrentQuantityWon());
        assertEquals(1, auctionState.getOtherBidderCurrentQuantityWon());
        assertEquals(INITIAL_TOTAL_QUANTITY - 2, auctionState.getRemainingQuantity());

        // Round 2
        int ownBid2 = 5;
        int otherBid2 = 5;
        verifier.verifyRound(ownBid2, otherBid2);
        assertTrue(roundValidatorRule1.hasValidateBeenCalled());
        assertTrue(roundValidatorRule2.hasValidateBeenCalled());

        assertEquals(INITIAL_BASE_CASH - ownBid1 - ownBid2, auctionState.getOwnBidderCurrentCash());
        assertEquals(INITIAL_BASE_CASH - otherBid1 - otherBid2, auctionState.getOtherBidderCurrentCash());
        assertEquals(2, auctionState.getOwnBidderCurrentQuantityWon());
        assertEquals(2, auctionState.getOtherBidderCurrentQuantityWon());
        assertEquals(INITIAL_TOTAL_QUANTITY - 4, auctionState.getRemainingQuantity());

        // Final verification
        verifier.verifyFinalState();
        assertTrue(finalValidatorRule.hasValidateBeenCalled(), "Final validator should have been called");
    }
}
