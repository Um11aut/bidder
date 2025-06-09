package org.tradingbot.utils;

import org.tradingbot.bidder.BidderWinEvaluator;

import java.util.LinkedList;
import java.util.List;

public class BidderHistory {
    private final List<Pair<Integer, Integer>> history;
    private final BidderWinEvaluator winningFunction;

    public BidderHistory(BidderWinEvaluator winningFunction) {
        this.history = new LinkedList<>();
        this.winningFunction = winningFunction;
    }

    public void add(int ownBid, int otherBid) {
        history.add(new Pair<>(ownBid, otherBid));
    }

    public int getOwnQuantity() {
        return history.stream()
                .mapToInt(entry -> winningFunction.evaluateWonQuantity(entry.getFirst(), entry.getSecond()))
                .sum();
    }

    public int getOpponentQuantity() {
        return history.stream()
                .mapToInt(entry -> winningFunction.evaluateWonQuantity(entry.getFirst(), entry.getSecond()))
                .sum();
    }
}
