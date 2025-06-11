package com.optimax.tradingbot.impl;

import com.optimax.tradingbot.bidder.BidderState;

import java.security.InvalidParameterException;

/**
 * Default bidder state
 */
public class BidderStateImpl implements BidderState {

    private final int totalQuantity;

    private int quantity;
    private int cash;
    private final String id;

    /**
     * @param initialQty  The initial Quantity of product provided by default
     * @param initialCash The initial cash provided
     */
    public BidderStateImpl(int initialQty, int totalQuantity, int initialCash, String id) throws InvalidParameterException {
        if (initialQty < 0 || initialCash < 0) {
            throw new InvalidParameterException("Invalid initial Quantity or Cash provided");
        }
        if (id == null || id.isEmpty()) {
            throw new InvalidParameterException("Invalid id provided");
        }
        this.id = id;
        this.totalQuantity = totalQuantity;
        cash = initialCash;
        quantity = initialQty;
    }

    /**
     * @param ownBid the amount of cash needed to be decreased
     */
    public void decreaseCash(int ownBid) {
        cash -= ownBid;
    }

    /**
     * @param qty the amount of quantity needed to be increased
     */
    public void increaseQuantity(int qty) {
        quantity += qty;
    }

    public String getId() {
        return id;
    }

    /**
     * @return The acquired Quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @return Initial Total Quantity
     */
    public int getTotalQuantity() {
        return totalQuantity;
    }

    public int getRemainingQuantity() {
        return totalQuantity - quantity;
    }

    /**
     * @return cash left
     */
    public int getCash() {
        return cash;
    }
}
