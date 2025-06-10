package com.optimax.tradingbot.core;

import com.optimax.tradingbot.utils.Pair;

/**
 * Represents the current state of the auction at any given point.
 */
public class AuctionState {
    private Pair<Integer, Integer> currentCash;          // (ownBidderCash, otherBidderCash)
    private Pair<Integer, Integer> quantityWon;          // (ownBidderQuantityWon, otherBidderQuantityWon)
    private int remainingQuantity;
    private final int totalInitialQuantity;
    private final int initialBaseCash;

    public AuctionState(int totalInitialQuantity, int initialBaseCash) {
        this.totalInitialQuantity = totalInitialQuantity;
        this.initialBaseCash = initialBaseCash;
        this.currentCash = new Pair<>(initialBaseCash, initialBaseCash);
        this.quantityWon = new Pair<>(0, 0);
        this.remainingQuantity = totalInitialQuantity;
    }

    // Getters
    public int getOwnBidderCurrentCash() {
        return currentCash.getFirst();
    }

    public int getOtherBidderCurrentCash() {
        return currentCash.getSecond();
    }

    public int getOwnBidderCurrentQuantityWon() {
        return quantityWon.getFirst();
    }

    public int getOtherBidderCurrentQuantityWon() {
        return quantityWon.getSecond();
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public int getTotalInitialQuantity() {
        return totalInitialQuantity;
    }

    public int getInitialBaseCash() {
        return initialBaseCash;
    }

    // Setters for mutable fields
    public void setOwnBidderCurrentCash(int cash) {
        this.currentCash = new Pair<>(cash, currentCash.getSecond());
    }

    public void setOtherBidderCurrentCash(int cash) {
        this.currentCash = new Pair<>(currentCash.getFirst(), cash);
    }

    public void setOwnBidderCurrentQuantityWon(int quantity) {
        this.quantityWon = new Pair<>(quantity, quantityWon.getSecond());
    }

    public void setOtherBidderCurrentQuantityWon(int quantity) {
        this.quantityWon = new Pair<>(quantityWon.getFirst(), quantity);
    }

    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }
}
