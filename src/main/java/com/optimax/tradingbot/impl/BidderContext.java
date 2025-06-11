package com.optimax.tradingbot.impl;

import com.optimax.tradingbot.bidder.BidderState;
import org.springframework.lang.NonNull;

import java.util.*;

/**
 * Shared state representing available for all bidders info
 * Contains the all party states and history.
 */
public final class BidderContext {
    private final Map<String, BidderState> states;
    private final List<BidderHistoryUnit> history;

    public BidderContext() {
        this.states = new HashMap<>();
        this.history = new LinkedList<>();
    }

    public void putState(@NonNull BidderState state) {
        states.put(state.getId(), state);
    }

    public void addHistoryUnit(@NonNull BidderHistoryUnit unit) {
        history.add(unit);
    }

    /**
     * @return The list of {@link BidderState} which doesn't include own BidderState
     */
    public List<BidderState> getAllStates() {
        return states.values().stream().toList();
    }

    /***
     * @return The list of all {@link String}s in the states
     */
    public List<String> getAllIds() {
        return states.keySet().stream().toList();
    }

    public List<BidderHistoryUnit> getHistory() {
        return history;
    }

    /**
     *
     * @param ownId id you want to exclude
     * @return all states except the id provided
     */
    public List<BidderState> getFilteredStates(@NonNull String ownId) {
        return states
                .entrySet()
                .stream()
                .filter(v -> !v.getKey().equals(ownId))
                .map(Map.Entry::getValue)
                .toList();
    }
}
