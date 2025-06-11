package com.optimax.tradingbot.core;

import com.optimax.tradingbot.impl.BidderContext;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import com.optimax.tradingbot.bidder.Bidder;
import com.optimax.tradingbot.bidder.BidderWinEvaluator;
import com.optimax.tradingbot.core.validation.AuctionVerifier;
import com.optimax.tradingbot.exceptions.AuctionValidatorException;
import com.optimax.tradingbot.exceptions.InternalStrategyException;
import com.optimax.tradingbot.impl.BidderImpl;
import com.optimax.tradingbot.bidder.BidderStrategy;
import com.optimax.tradingbot.impl.DefaultBidderWinEvaluator;
import com.optimax.tradingbot.core.validation.AuctionRuleValidator;
import com.optimax.tradingbot.core.validation.CompositeAuctionValidator;
import com.optimax.tradingbot.core.validation.rules.NegativeCashValidator;
import com.optimax.tradingbot.core.validation.rules.RemainingQuantityValidator;
import com.optimax.tradingbot.core.validation.rules.FinalQuantityExhaustionValidator;
import org.slf4j.Logger;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * Represents a base auction where two bidders compete by placing bids
 * over multiple rounds until all quantities are allocated or maximum rounds are reached.
 * <p>
 * This class manages the auction lifecycle, verifies each bidding round and final state
 * using defined validators, and maintains auction state and context.
 */
public class Auction implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Auction.class);

    private final BidderContext context;      // Shared context representing both bidders' states
    private final Bidder ownBidder;
    private final Bidder otherBidder;
    private final int maxRounds;                // Maximum rounds equal to half of total quantity

    private final AuctionVerifier verifier;    // Verifies each round and final auction state
    private final AuctionState auctionState;   // Maintains the current state of the auction

    /**
     * Constructs an Auction instance with initial parameters and strategies for both bidders.
     *
     * @param totalQuantity    the total quantity available to each bidder; must be positive and even
     * @param baseCash         the initial cash each bidder has; must be non-negative
     * @param ownStrategy      the bidding strategy for the own bidder; must not be null
     * @param opponentStrategy the bidding strategy for the opponent bidder; must not be null
     * @throws IllegalArgumentException if totalQuantity is not positive even number or baseCash is negative
     */
    public Auction(int totalQuantity, int baseCash, @NonNull BidderStrategy ownStrategy, @NonNull BidderStrategy opponentStrategy) throws IllegalArgumentException {
        if (totalQuantity % 2 != 0 || totalQuantity <= 0) {
            throw new IllegalArgumentException("Total Quantity must be evenly dividable by 2 and > 0");
        }
        if (baseCash < 0) {
            throw new IllegalArgumentException("Base Cash must be >= 0");
        }

        // Use Default evaluator
        BidderWinEvaluator defaultEvaluator = new DefaultBidderWinEvaluator();

        context = new BidderContext();

        // Create bidders with their respective strategies and the shared context
        ownBidder = new BidderImpl(totalQuantity, baseCash, ownStrategy, defaultEvaluator, context);
        otherBidder = new BidderImpl(totalQuantity, baseCash, opponentStrategy, defaultEvaluator, context);

        // Maximum number of rounds is half the total quantity (each round allocates two units)
        maxRounds = totalQuantity / 2;

        List<AuctionRuleValidator> roundValidators = List.of(
                new NegativeCashValidator(),           // Ensure no bidder has negative cash after bidding
                new RemainingQuantityValidator()       // Ensure bidders have remaining quantity to trade
        );

        List<AuctionRuleValidator> finalValidators = List.of(
                new FinalQuantityExhaustionValidator() // Ensure all quantity is exhausted properly
        );

        auctionState = new AuctionState(totalQuantity, baseCash);
        verifier = new AuctionVerifier(
                auctionState,
                new CompositeAuctionValidator(roundValidators),
                new CompositeAuctionValidator(finalValidators),
                defaultEvaluator,
                defaultEvaluator
        );
    }

    /**
     * Starts the auction process by running the auction loop
     * and verifying the final state after all rounds complete.
     * Logs errors if any verification fails.
     */
    @Override
    public void run() {
        auctionLoop(maxRounds);

        try {
            verifier.verifyFinalState();
        } catch (AuctionValidatorException e) {
            log.error("Auction Final State Verification Error: {}", e.getMessage());
        }
    }

    /**
     * Executes the auction rounds, where each bidder places bids and states are updated.
     * Verifies each round using configured validators and logs any errors encountered.
     *
     * @param iterations number of rounds to execute (typically maxRounds)
     */
    void auctionLoop(int iterations) {
        for (int i = 0; i < iterations; i++) {
            int ownBid;
            int otherBid;
            try {
                // Each bidder places their bid for this round
                ownBid = ownBidder.placeBid();
                otherBid = otherBidder.placeBid();
            } catch (InternalStrategyException e) {
                log.error("Caught internal strategy exception: {}", e.getMessage());
                return;  // Abort auction on strategy failure
            }

            try {
                // Update bidders with the bids placed by both sides
                ownBidder.bids(ownBid, otherBid);
                otherBidder.bids(otherBid, ownBid);
            } catch (InvalidParameterException e) {
                log.error("Caught invalid parameters: {}", e.getMessage());
                return;  // Abort auction on invalid bid parameters
            }

            log.debug("Bidding: {} against {}", ownBid, otherBid);

            try {
                // Validate round integrity (e.g., no negative cash, valid quantities)
                verifier.verifyRound(ownBid, otherBid);
            } catch (AuctionValidatorException e) {
                log.error("Auction Round Verification Error: {}", e.getMessage());
                return;  // Abort auction if round verification fails
            }

            // Update shared context state based on bids and auction results
            BidderContextUpdater.updateBidderContext(context, ownBidder, otherBidder, ownBid, otherBid);
        }

        // Log final auction results and declare the winner
        log.info("Winner: {}", auctionState.getOwnBidderCurrentQuantityWon() > auctionState.getOtherBidderCurrentQuantityWon() ? "Own" : "Other");
        log.info("Stats. Own: {} Other: {}", auctionState.getOwnBidderCurrentQuantityWon(), auctionState.getOtherBidderCurrentQuantityWon());
    }
}
