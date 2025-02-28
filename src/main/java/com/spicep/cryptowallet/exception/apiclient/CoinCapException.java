package com.spicep.cryptowallet.exception.apiclient;

/**
 * Generic exception thrown when an error occurs while interacting with the CoinCap API.
 */
public class CoinCapException extends RuntimeException {

    public CoinCapException(String message) {
        super(message);
    }
}
