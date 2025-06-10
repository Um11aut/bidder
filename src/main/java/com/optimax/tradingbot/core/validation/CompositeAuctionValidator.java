package com.optimax.tradingbot.core.validation;

import com.optimax.tradingbot.core.AuctionState;
import com.optimax.tradingbot.exceptions.AuctionValidatorException;

import java.util.ArrayList;
import java.util.List;

/**
 * A composite validator that aggregates multiple AuctionRuleValidators.
 */
public record CompositeAuctionValidator(List<AuctionRuleValidator> validators) implements AuctionRuleValidator {
    public CompositeAuctionValidator(List<AuctionRuleValidator> validators) {
        this.validators = new ArrayList<>(validators); // Defensive copy
    }

    @Override
    public void validate(AuctionState state) throws AuctionValidatorException {
        for (AuctionRuleValidator validator : validators) {
            validator.validate(state);
        }
    }
}