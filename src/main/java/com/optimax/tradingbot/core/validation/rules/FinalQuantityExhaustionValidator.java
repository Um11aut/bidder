package com.optimax.tradingbot.core.validation.rules;

import com.optimax.tradingbot.core.AuctionState;
import com.optimax.tradingbot.core.validation.AuctionRuleValidator;
import com.optimax.tradingbot.exceptions.AuctionValidatorException;

/**
 * Validates the final state of the auction, ensuring all quantity has been auctioned.
 */
public class FinalQuantityExhaustionValidator implements AuctionRuleValidator {

    @Override
    public void validate(AuctionState state) throws AuctionValidatorException  {
        // Rule: Bidding on each 2 QU is repeated until the supply of x QU is fully auctioned.
        if (state.getRemainingQuantity() != 0) {
            throw new AuctionValidatorException("Auction did not fully auction all quantity. Remaining: " + state.getRemainingQuantity());
        }
        // Additional check: total quantity won must match initial total quantity
        if (state.getOwnBidderQuantityWon() + state.getOtherBidderQuantityWon() != state.getTotalInitialQuantity()) {
            throw new AuctionValidatorException("Total quantity won by bidders does not match initial total quantity.");
        }
    }
}