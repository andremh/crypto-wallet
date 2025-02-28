package com.spicep.cryptowallet.exception.apiclient;

/**
 * Exception thrown when the server returns an error.
 */
public class CoinCapServerException extends CoinCapException {

    public CoinCapServerException(String message) {
        super(message);
    }
}