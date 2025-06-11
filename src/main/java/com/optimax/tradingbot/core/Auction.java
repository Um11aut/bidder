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
 * Base Auction
 */
public class Auction implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Auction.class);

    private final BidderContext context;
    private final Bidder ownBidder;
    private final Bidder otherBidder;
    private final int maxRounds;

    private final AuctionVerifier verifier;
    private final AuctionState auctionState;

    public Auction(int totalQuantity, int baseCash, @NonNull BidderStrategy ownStrategy, @NonNull BidderStrategy opponentStrategy) throws IllegalArgumentException {
        if (totalQuantity % 2 != 0 || totalQuantity <= 0) {
            throw new IllegalArgumentException("Total Quantity must be evenly dividable by 2 and <= 0");
        }
        if (baseCash < 0) {
            throw new IllegalArgumentException("Base Cash must be >= 0");
        }
        BidderWinEvaluator defaultEvaluator = new DefaultBidderWinEvaluator();

        context = new BidderContext();
        ownBidder = new BidderImpl(totalQuantity, baseCash, ownStrategy, defaultEvaluator, context);
        otherBidder = new BidderImpl(totalQuantity, baseCash, opponentStrategy, defaultEvaluator, context);

        maxRounds = totalQuantity / 2;

        // validators for rounds and final state
        List<AuctionRuleValidator> roundValidators = List.of(
                new NegativeCashValidator(),
                new RemainingQuantityValidator()
        );
        List<AuctionRuleValidator> finalValidators = List.of(
                new FinalQuantityExhaustionValidator()
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

    @Override
    public void run() {
        auctionLoop(maxRounds);
        try {
            verifier.verifyFinalState();
        } catch (AuctionValidatorException e) {
            log.error("Auction Final State Verification Error: {}", e.getMessage());
        }
    }

    void auctionLoop(int iterations) {
        for (int i = 0; i < iterations; i++) {
            int ownBid;
            int otherBid;
            try {
                ownBid = ownBidder.placeBid();
                otherBid = otherBidder.placeBid();
            } catch (InternalStrategyException e) {
                log.error("Caught internal strategy exception: {}", e.getMessage());
                return;
            }

            try {
                ownBidder.bids(ownBid, otherBid);
                otherBidder.bids(otherBid, ownBid);
            } catch (InvalidParameterException e) {
                log.error("Caught invalid parameters: {}", e.getMessage());
                return;
            }
            log.debug("Bidding: {} against {}", ownBid, otherBid);

            try {
                verifier.verifyRound(ownBid, otherBid);
            } catch (AuctionValidatorException e) {
                log.error("Auction Round Verification Error: {}", e.getMessage());
                return;
            }

            BidderContextUpdater.updateBidderContext(context, ownBidder, otherBidder, ownBid, otherBid);
            log.info("{}", context.getHistory());
        }

        log.info("Winner: {}", auctionState.getOwnBidderCurrentQuantityWon() > auctionState.getOtherBidderCurrentQuantityWon() ? "Own" : "Other");
        log.info("Stats. Own: {} Other: {}", auctionState.getOwnBidderCurrentQuantityWon(), auctionState.getOtherBidderCurrentQuantityWon());
    }
}