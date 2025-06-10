package com.optimax.tradingbot.exceptions;

/**
 * Custom exception for verification failures.
 */
public class AuctionValidatorException extends Exception {
    public AuctionValidatorException(String message) {
        super(message);
    }
}