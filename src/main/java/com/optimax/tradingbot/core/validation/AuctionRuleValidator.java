package com.optimax.tradingbot.core.validation;

import com.optimax.tradingbot.core.AuctionState;
import com.optimax.tradingbot.exceptions.AuctionValidatorException;

/**
 * Interface for validating a specific rule within the auction.
 */
@FunctionalInterface
public interface AuctionRuleValidator {
    /**
     * Validates a specific rule against the current auction state.
     * @param state The current auction state.
     * @throws AuctionValidatorException if the rule is violated.
     */
    void validate(AuctionState state) throws AuctionValidatorException;
}