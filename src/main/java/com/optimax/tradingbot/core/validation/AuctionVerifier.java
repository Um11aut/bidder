package com.optimax.tradingbot.core.validation;

import com.optimax.tradingbot.bidder.BidderWinEvaluator;
import com.optimax.tradingbot.core.AuctionState;
import com.optimax.tradingbot.core.AuctionStateUpdater;
import com.optimax.tradingbot.exceptions.AuctionValidatorException;
import com.optimax.tradingbot.impl.DefaultBidderWinEvaluator;

/**
 * Verifies the bid data from the rounds
 */
public class AuctionVerifier {
    private final AuctionStateUpdater stateUpdater;
    private CompositeAuctionValidator roundValidators;
    private CompositeAuctionValidator finalValidators;
    private AuctionState auctionState;

    /**
     * Helper to follow DRY on constructors
     */
    private void init(AuctionState state,
                           CompositeAuctionValidator roundValidators,
                           CompositeAuctionValidator finalValidators) {
        this.auctionState = state;
        this.roundValidators = roundValidators;
        this.finalValidators = finalValidators;
    }

    /**
     * Utilizes the DefaultBidderWinEvaluator by default for both of the evaluators
     */
    public AuctionVerifier(AuctionState state,
                           CompositeAuctionValidator roundValidators,
                           CompositeAuctionValidator finalValidators) {
        init(state, roundValidators, finalValidators);
        this.stateUpdater = new AuctionStateUpdater(new DefaultBidderWinEvaluator(), new DefaultBidderWinEvaluator());
    }

    /**
     * Initialize with your own win evaluators
     */
    public AuctionVerifier(AuctionState state,
                           CompositeAuctionValidator roundValidators,
                           CompositeAuctionValidator finalValidators,
                           BidderWinEvaluator ownWinEvaluator,
                           BidderWinEvaluator otherWinEvaluator) {
        init(state, roundValidators, finalValidators);
        this.stateUpdater = new AuctionStateUpdater(ownWinEvaluator, otherWinEvaluator);
    }

    /**
     * Processes and validates a single round of bids.
     * @param ownBid The bid of the "own" bidder.
     * @param otherBid The bid of the "other" bidder.
     * @throws AuctionValidatorException if any round rule is violated.
     */
    public void verifyRound(int ownBid, int otherBid) throws AuctionValidatorException {
        stateUpdater.updateAuctionState(auctionState, ownBid, otherBid);
        roundValidators.validate(auctionState);
    }

    /**
     * Validates the final state of the auction.
     * @throws AuctionValidatorException if any final rule is violated.
     */
    public void verifyFinalState() throws AuctionValidatorException {
        finalValidators.validate(auctionState);
    }
}