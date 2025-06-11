package com.optimax.tradingbot.strategies;

import com.optimax.tradingbot.bidder.BidderState;
import com.optimax.tradingbot.bidder.BidderStrategy;
import com.optimax.tradingbot.impl.BidderContext;
import com.optimax.tradingbot.strategies.builder.BidderStrategyParameters;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.OptionalInt;
import java.util.Scanner;

/**
 * A BidderStrategy implementation that prompts the user to input their bid manually each round via the console.
 * Provides information about the current context including other bidders' states to the user.
 */
public class ConsoleInputBidderStrategy implements BidderStrategy {

    private BidderStrategyParameters params;
    private int round;
    private final Scanner scanner;

    public ConsoleInputBidderStrategy() {
        this.scanner = new Scanner(System.in);
        this.round = 1;
    }

    @Override
    public void init(@NonNull BidderStrategyParameters params) {
        this.params = params;
        this.round = 1;
    }

    @NonNull
    @Override
    public OptionalInt nextBid(BidderState own, BidderContext ctx) {
        int maxBid = own.cash();

        if (maxBid <= 0) {
            System.out.println("No cash left to bid.");
            return OptionalInt.empty();
        }

        if (params.maxRounds().isPresent() && round > params.maxRounds().getAsInt()) {
            System.out.println("Maximum rounds reached. No more bids.");
            return OptionalInt.empty();
        }

        // Show user context info about other bidders
        List<BidderState> otherBidders = ctx.getFilteredStates(own.id());
        System.out.printf("Round %d - Your cash: %d, Your quantity: %d%n", round, own.cash(), own.getQuantity());
        System.out.println("Opponent bidders' status:");
        for (BidderState other : otherBidders) {
            System.out.printf(" - Bidder %s: Cash = %d, Quantity = %d%n", other.id(), other.cash(), other.getQuantity());
        }

        System.out.printf("Enter your bid (1 to %d): ", maxBid);

        int bid = -1;
        while (bid < 1 || bid > maxBid) {
            String input = scanner.nextLine();
            try {
                bid = Integer.parseInt(input);
                if (bid < 1 || bid > maxBid) {
                    System.out.printf("Invalid bid. Please enter a number between 1 and %d: ", maxBid);
                }
            } catch (NumberFormatException e) {
                System.out.printf("Invalid input. Please enter a valid integer between 1 and %d: ", maxBid);
            }
        }

        return OptionalInt.of(bid);
    }

    @Override
    public void finishRound() {
        round++;
    }
}
