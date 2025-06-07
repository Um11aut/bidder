package org.tradingbot.strategies;

import org.tradingbot.strategies.builder.BidderParametersBuilder;

public interface BidderStrategy {
    void init(BidderParametersBuilder params);

    int getQuantity();
}
