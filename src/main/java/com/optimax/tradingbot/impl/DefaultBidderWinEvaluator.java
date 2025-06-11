package com.optimax.tradingbot.impl;

import com.optimax.tradingbot.bidder.BidderWinEvaluator;

import java.security.InvalidParameterException;

public class DefaultBidderWinEvaluator implements BidderWinEvaluator {
    /**
     * Desired default win algorithm
     * If own bid more MU than other, own won more 2 QU
     * In a tie get 1
     */
    @Override
    public int evaluateWonQuantity(int own, int other) throws InvalidParameterException {
        if (own < 0 || other < 0) {
            throw new InvalidParameterException("Invalid own or opponent parameters provided");
        }

        if (own > other) {
            return 2;
        } else if (own == other) {
            return 1;
        }
        return 0;
    }
}
