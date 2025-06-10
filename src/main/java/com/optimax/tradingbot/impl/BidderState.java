package com.optimax.tradingbot.impl;

import com.optimax.tradingbot.utils.Pair;

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;

public class BidderState {
    private final int totalQuantity;

    private int quantity;
    private int cash;

    /**
     * stores all plays
     */
    private final List<Pair<Integer, Integer>> history;

    /**
     * @param initialQty
     *                  The initial Quantity of product provided by default
     * @param initialCash
     *                  The initial cash provided
     */
    public BidderState(int initialQty, int totalQuantity, int initialCash) throws InvalidParameterException {
        if (initialQty < 0 || initialCash < 0){
            throw new InvalidParameterException("Invalid initial Quantity or Cash provided");
        }
        this.totalQuantity = totalQuantity;
        cash = initialCash;
        quantity = initialQty;
        history = new LinkedList<>();
    }

    /**
     * @param ownBid
     *              the amount of cash needed to be decreased
     */
    public void decreaseCash(int ownBid) {
        cash -= ownBid;
    }

    /**
     * @param qty
     *           the amount of quantity needed to be increased
     */
    public void increaseQuantity(int qty) {
        quantity += qty;
    }

    /**
     * @param ownBid
     *              Amount of cash first party sacrificed
     * @param opponentBid
     *              Amount of cash second party sacrificed
     */
    public void addHistory(int ownBid, int opponentBid) {
        history.add(new Pair<>(ownBid, opponentBid));
    }

    /**
     * @return
     *        history of all plays
     */
    public List<Pair<Integer, Integer>> getHistory() {
        return history;
    }

    public int getRound() {
        return history.size();
    }

    /**
     * @return
     *        The acquired Quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @return
     *       Initial Total Quantity
     */
    public int getTotalQuantity() {
        return totalQuantity;
    }

    public int getRemainingQuantity() {
        return totalQuantity - quantity;
    }

    /**
     * @return
     *        cash left
     */
    public int getCash() {
        return cash;
    }
}
