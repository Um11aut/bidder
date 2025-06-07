package org.tradingbot.impl;

import java.security.InvalidParameterException;

import org.tradingbot.bidder.Bidder;
import org.tradingbot.exceptions.InternalStrategyException;
import org.tradingbot.strategies.BidderStrategy;

public class BidderImpl implements Bidder {
    private BidderStrategy strategy;
    private BidderState state;

    public BidderImpl(int quantity, int cash, BidderStrategy strategy) throws InvalidParameterException {
        this.strategy = strategy;
        init(quantity, cash);
    }

    @Override
    public void init(int quantity, int cash) throws InvalidParameterException {
        if (quantity <= 0 || cash <= 0) {
            throw new InvalidParameterException("Incorrect quantity or cash supplied");
        }
        state = new BidderState(quantity, cash);
    }

    @Override
    public int placeBid() throws InternalStrategyException {
        var qty = strategy.getQuantity();
        if (qty <= 0) {
            throw new InternalStrategyException("Received by the strategy quantity is not valid");
        }
        state.decreaseCash(qty);
        return qty;
    }

    @Override
    public void bids(int own, int other) throws InvalidParameterException {
        if (own <= 0 || other <= 0) {
            throw new InvalidParameterException("Either one or both of supplied quantities are incorrect");
        }
        state.addHistory(own, other);
        increaseIfWon(own, other);
    }

    private void increaseIfWon(int own, int other) {
        if (own > other) {
            state.increaseQuantity(2);
        } else if (own == other) { // cover edge case described in the task
            state.increaseQuantity(1);
        }
    }
}
