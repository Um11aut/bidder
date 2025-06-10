package com.optimax.tradingbot.core.validation.rules;

import com.optimax.tradingbot.core.AuctionState;
import com.optimax.tradingbot.core.validation.AuctionRuleValidator;
import com.optimax.tradingbot.exceptions.AuctionValidatorException;

/**
 * Validates that the remaining quantity does not go below zero during a round.
 */
public class RemainingQuantityValidator implements AuctionRuleValidator {
    @Override
    public void validate(AuctionState state) throws AuctionValidatorException {
        if (state.getRemainingQuantity() < 0) {
            throw new AuctionValidatorException("Remaining quantity went below zero, indicating over-allocation.");
        }
    }
}