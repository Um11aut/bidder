package org.tradingbot;

import org.tradingbot.core.Auction;
import org.tradingbot.strategies.BalancedBidderStrategy;
import org.tradingbot.bidder.BidderStrategy;
import org.tradingbot.strategies.builder.enums.BidderStrategyGreediness;
import org.tradingbot.strategies.builder.BidderStrategyParametersBuilder;

public class TradingBotApp {
    public static void main(String[] args) {
        BidderStrategy ownStrat = new BalancedBidderStrategy(BidderStrategyParametersBuilder.defaultBuilder()
                .withGreediness(BidderStrategyGreediness.WEAK)
                .withRiskRewardRatio(3, 10)
                .build());
        BidderStrategy opponentStrat = new BalancedBidderStrategy(BidderStrategyParametersBuilder.defaultBuilder()
                .withGreediness(BidderStrategyGreediness.STRONG)
                .build());

        Auction auction = new Auction(10, ownStrat, opponentStrat);
    }
}
