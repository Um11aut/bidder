package com.optimax.tradingbot.impl;

import java.security.InvalidParameterException;

import com.optimax.tradingbot.bidder.BidderState;
import com.optimax.tradingbot.utils.RandomStringGenerator;
import org.springframework.lang.NonNull;
import com.optimax.tradingbot.bidder.Bidder;
import com.optimax.tradingbot.bidder.BidderWinEvaluator;
import com.optimax.tradingbot.exceptions.InternalStrategyException;
import com.optimax.tradingbot.bidder.BidderStrategy;

/**
 * Default implementation of the {@link Bidder} interface.
 * Represents a bidder in the auction who places bids using a specified strategy,
 * maintains its current state, and evaluates winning quantities.
 */
public class BidderImpl implements Bidder {

    private final BidderStrategy strategy;
    private final BidderWinEvaluator winnerEvaluator;
    private String ownId;

    private BidderStateImpl ownState;
    private final BidderContext context;

    /**
     * Constructs a new bidder with the specified initial quantity, cash, strategy, and context.
     * A unique ID is generated for this bidder to identify it in the auction context.
     *
     * @param quantity       initial quantity of items the bidder holds
     * @param cash           initial cash available for bidding
     * @param strategy       the bidding strategy to use when placing bids
     * @param winnerEvaluator evaluator to determine the winning quantity per bid round
     * @param context        shared context tracking all bidders' states
     */
    public BidderImpl(int quantity, int cash,
                      @NonNull BidderStrategy strategy,
                      @NonNull BidderWinEvaluator winnerEvaluator,
                      @NonNull BidderContext context
    ) {
        // Ensure the bidder has a unique identifier within the auction context
        do {
            ownId = RandomStringGenerator.generateRandomString(5);
        } while (context.getAllIds().contains(ownId));

        this.strategy = strategy;
        this.winnerEvaluator = winnerEvaluator;
        // Initialize own state with zero quantity won initially, given quantity and cash, and assigned ID
        this.ownState = new BidderStateImpl(0, quantity, cash, ownId);

        this.context = context;
        // Register own state in the shared context
        this.context.putState(ownState);
    }

    /**
     * Resets the bidder state with the specified quantity and cash.
     *
     * @param quantity new quantity to reset to (must be non-negative)
     * @param cash     new cash to reset to (must be non-negative)
     * @throws InvalidParameterException if quantity or cash is negative
     */
    @Override
    public void init(int quantity, int cash) throws InvalidParameterException {
        if (quantity < 0 || cash < 0) {
            throw new InvalidParameterException("Incorrect quantity or cash supplied");
        }
        // Reset the bidder's internal state with new values but keep the same ID
        this.ownState = new BidderStateImpl(0, quantity, cash, ownId);
        // Update context to reflect reset state
        this.context.putState(ownState);
    }

    /**
     * Requests the next bid amount from the bidding strategy.
     * Decreases the bidder's cash by the bid amount after validation.
     *
     * @return the bid amount to place (0 if strategy returns empty)
     * @throws InternalStrategyException if the strategy proposes a bid exceeding available cash
     */
    @Override
    public int placeBid() throws InternalStrategyException {
        var cashOpt = strategy.nextBid(ownState, context);
        if (cashOpt.isEmpty()) {
            // No bid possible (likely out of cash)
            return 0;
        }

        int cash = cashOpt.getAsInt();
        if (cash > ownState.cash()) { // Defensive check to avoid invalid bids
            throw new InternalStrategyException("Received by the strategy cash exceeds the left amount");
        }
        // Deduct the bid amount from the bidder's cash
        ownState.decreaseCash(cash);
        return cash;
    }

    /**
     * Updates the bidder's state after a bidding round, given own and opponent bids.
     * Validates input parameters, increases quantity won if applicable, and signals
     * the strategy to advance to the next round.
     *
     * @param own   the bid amount placed by this bidder
     * @param other the bid amount placed by the opponent
     * @throws InvalidParameterException if either bid is negative
     */
    @Override
    public void bids(int own, int other) throws InvalidParameterException {
        if (own < 0 || other < 0) {
            throw new InvalidParameterException("Either one or both of supplied quantities are incorrect");
        }
        // Evaluate and update quantity won based on bid comparison
        updateOwnState(own, other);

        // Notify the strategy that the round is finished (for internal state updates)
        strategy.finishRound();
    }

    /**
     * Returns the current state of this bidder, including remaining cash, quantity,
     * and total quantity won.
     *
     * @return the current bidder state
     */
    @Override
    public BidderState getState() {
        return this.ownState;
    }

    /**
     * Internal helper method to update bidder's quantity based on the outcome of the round.
     * The winner evaluator determines how many items were won in this bidding round.
     *
     * @param own   bid amount placed by this bidder
     * @param other bid amount placed by the opponent
     */
    private void updateOwnState(int own, int other) {
        // Calculate how many items were won this round based on bids
        var wonAmount = winnerEvaluator.evaluateWonQuantity(own, other);
        // Increase the quantity won accordingly in the bidder's state
        ownState.increaseQuantity(wonAmount);
    }
}
