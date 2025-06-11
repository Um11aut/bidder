package com.optimax.tradingbot.impl;

import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Optional;

public record BidderHistoryUnit(Map<String, Integer> bids) {

    /**
     * Find appropriate bid by associated id
     */
    public Optional<Integer> getById(String id) {
        return Optional.ofNullable(bids.get(id));
    }

    /**
     * Get the maximum bid in the current round
     * @param idToExclude
     *                  id to be excluded from the map
     */
    public Optional<Integer> getMaxBidInRound(@NonNull String idToExclude) {
        return bids.entrySet()
                .stream()
                .filter(v -> !v.getKey().equals(idToExclude))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getValue);
    }
}
