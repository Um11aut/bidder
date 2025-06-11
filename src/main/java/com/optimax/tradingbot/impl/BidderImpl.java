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
 * The default implementation of Bidder interface
 */
public class BidderImpl implements Bidder {

    private final BidderStrategy strategy;
    private final BidderWinEvaluator winnerEvaluator;
    private String ownId;

    private BidderStateImpl ownState;
    private final BidderContext context;

    public BidderImpl(int quantity, int cash,
                      @NonNull BidderStrategy strategy,
                      @NonNull BidderWinEvaluator winnerEvaluator,
                      @NonNull BidderContext context
    ) {
        // Ensure unique id
        do {
            ownId = RandomStringGenerator.generateRandomString(5);
        } while (context.getAllIds().contains(ownId));

        this.strategy = strategy;
        this.winnerEvaluator = winnerEvaluator;
        this.ownState = new BidderStateImpl(0, quantity, cash, ownId);

        this.context = context;
        this.context.putState(ownState);
    }

    @Override
    public void init(int quantity, int cash) throws InvalidParameterException {
        if (quantity < 0 || cash < 0) {
            throw new InvalidParameterException("Incorrect quantity or cash supplied");
        }
        this.ownState = new BidderStateImpl(0, quantity, cash, ownId);
        this.context.putState(ownState);
    }

    @Override
    public int placeBid() throws InternalStrategyException {
        var cashOpt = strategy.nextBid(ownState, context);
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

        strategy.finishRound();
    }

    @Override
    public BidderState getState() {
        return this.ownState;
    }

    /**
     * Increases qty if win conditions are met
     *
     * @param own   first bidder cash
     * @param other second bidder cash
     */
    private void updateOwnState(int own, int other) {
        var wonAmount = winnerEvaluator.evaluateWonQuantity(own, other);
        ownState.increaseQuantity(wonAmount);
    }
}
