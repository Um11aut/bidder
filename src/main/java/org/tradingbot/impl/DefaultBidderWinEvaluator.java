package org.tradingbot.impl;

import org.tradingbot.bidder.BidderWinEvaluator;

import java.security.InvalidParameterException;

public class DefaultBidderWinEvaluator implements BidderWinEvaluator {
    /**
     * The described in the task win algorithm
     */
    @Override
    public int evaluateWonQuantity(int own, int other) throws InvalidParameterException {
        if (own < 0 || other < 0) {
            throw new InvalidParameterException("Invalid own or opponent parameters provided");
        }

        if (own > other) {
            return 2;
        } if (own == other) {
            return 1;
        }
        return 0;
    }
}
