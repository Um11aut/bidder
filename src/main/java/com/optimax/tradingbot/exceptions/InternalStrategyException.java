package com.optimax.tradingbot.exceptions;

public class InternalStrategyException extends RuntimeException {
    public InternalStrategyException(String message) {
        super(message);
    }

    public InternalStrategyException(String message, Throwable cause) {
        super(message, cause);
    }
}
