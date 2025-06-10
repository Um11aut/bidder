package com.optimax.tradingbot.impl;

/**
 * Contains the both party states for bidding.
 * @param own
 *           the first party Bidder state
 * @param other
 *           the second party Bidder state
 */
public record BidderContext(BidderState own, BidderState other) {
}
