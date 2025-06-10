package com.optimax.tradingbot.impl;

import java.security.InvalidParameterException;

import org.springframework.lang.NonNull;
import com.optimax.tradingbot.bidder.Bidder;
import com.optimax.tradingbot.bidder.BidderWinEvaluator;
import com.optimax.tradingbot.exceptions.InternalStrategyException;
import com.optimax.tradingbot.bidder.BidderStrategy;

public class BidderImpl implements Bidder {
    private final BidderStrategy strategy;
    private final BidderWinEvaluator winnerEvaluator;
    private BidderState ownState;

    // Keep track of both of the bidder states for strategies
    private BidderContext bidderContext;

    /**
     * Bidder implementation using default win evaluator
     * @param quantity
     *                Initial quantity
     * @param cash
     *                Initial cash
     * @param strategy
     *                The wished strategy
     * @throws InvalidParameterException
     *                if quantity, cash are incorrect, or strategy is null
     */
    public BidderImpl(int quantity, int cash, @NonNull BidderStrategy strategy, @NonNull BidderWinEvaluator winnerEvaluator) {
        this.strategy = strategy;
        this.winnerEvaluator = winnerEvaluator;
        init(quantity, cash);
    }

    @Override
    public void init(int quantity, int cash) throws InvalidParameterException {
        if (quantity < 0 || cash < 0) {
            throw new InvalidParameterException("Incorrect quantity or cash supplied");
        }
        ownState = new BidderState(0, cash, quantity);
        BidderState otherState = new BidderState(0, cash, quantity); // to keep track of the other bidder state
        bidderContext = new BidderContext(ownState, otherState);
    }

    @Override
    public int placeBid() throws InternalStrategyException {
        var cashOpt = strategy.nextBid(bidderContext);
        if (cashOpt.isEmpty()) {
            return 0; // no cash left: bid 0
        }

        int cash = cashOpt.getAsInt();
        if (cash > ownState.getCash()) { // should not happen
            throw new InternalStrategyException("Received by the strategy cash exceeds the left amount");
        }
        ownState.decreaseCash(cash);
        return cash;
    }

    @Override
    public void bids(int own, int other) throws InvalidParameterException {
        if (own < 0 || other < 0) {
            throw new InvalidParameterException("Either one or both of supplied quantities are incorrect");
        }
        updateOwnState(own, other);
        updateOpponentState(other, own);

        strategy.finishRound();
        ownState.addHistory(own, other);
    }

    /**
     * Increases qty if win conditions are met
     * @param own
     *            first bidder cash
     * @param other
     *            second bidder cash
     */
    private void updateOwnState(int own, int other) {
        var wonAmount = winnerEvaluator.evaluateWonQuantity(own, other);
        ownState.increaseQuantity(wonAmount);
    }

    /**
     * Calculate the opponents won quantity and cash
     */
    private void updateOpponentState(int other, int own) {
        var wonQuantity = winnerEvaluator.evaluateWonQuantity(other, own);
        BidderState otherState = bidderContext.other();
        otherState.increaseQuantity(wonQuantity);
        otherState.decreaseCash(other);
    }
}
