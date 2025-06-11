package com.optimax.tradingbot.core.validation.rules;

import com.optimax.tradingbot.core.AuctionState;
import com.optimax.tradingbot.core.validation.AuctionRuleValidator;
import com.optimax.tradingbot.exceptions.AuctionValidatorException;

/**
 * Validates that no bidder's cash goes below zero.
 */
public class NegativeCashValidator implements AuctionRuleValidator {

    @Override
    public void validate(AuctionState state) throws AuctionValidatorException  {
        if (state.getOwnBidderCurrentCash() < 0) {
            throw new AuctionValidatorException("Own bidder's cash went below zero.");
        }
        if (state.getOtherBidderCurrentCash() < 0) {
            throw new AuctionValidatorException("Other bidder's cash went below zero.");
        }
    }
}