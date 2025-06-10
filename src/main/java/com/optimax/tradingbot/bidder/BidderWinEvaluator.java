package com.optimax.tradingbot.bidder;

import java.security.InvalidParameterException;

/**
 * Functional interface representing a strategy for determining
 * the quantity won in a bidding scenario.
 */
@FunctionalInterface
public interface BidderWinEvaluator {
    /**
     * Calculate the won quantity based on some finite algorithm
     * @param own
     *                  own cash offer
     * @param other
     *                  opponent cash offer
     * @return
     *                  quantity own has won
     * @throws InvalidParameterException
     *                  if own or opponent amount < 0
     */
    int evaluateWonQuantity(int own, int other) throws InvalidParameterException;
}
