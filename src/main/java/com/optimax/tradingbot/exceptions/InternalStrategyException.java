package com.optimax.tradingbot.exceptions;

/**
 * Custom exception for the internal exceptions happen in the strategy
 */
public class InternalStrategyException extends RuntimeException {
    public InternalStrategyException(String message) {
        super(message);
    }

    public InternalStrategyException(String message, Throwable cause) {
        super(message, cause);
    }
}
