package org.tradingbot.impl;

import org.tradingbot.utils.Pair;

import java.util.LinkedList;
import java.util.List;

public class BidderState {
    private int quantity;
    private int cash;

    private List<Pair<Integer, Integer>> history;

    public BidderState(int initialQty, int initialCash) {
        cash = initialCash;
        quantity = initialQty;
        history = new LinkedList<>();
    }

    public void decreaseCash(int ownBid) {
        cash -= ownBid;
    }

    public void increaseQuantity(int qty) {
        quantity += qty;
    }

    public void addHistory(int ownBid, int opponentBid) {
        history.add(new Pair<>(ownBid, opponentBid));
    }

    public List<Pair<Integer, Integer>> getAuctions() {
        return history;
    }

    public int getRound() {
        return history.size();
    }

    public int getQuantity() {
        return quantity;
    }

    public int getCash() {
        return cash;
    }
}
