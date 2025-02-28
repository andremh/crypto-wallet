package com.spicep.cryptowallet.exception.apiclient;

/**
 * Exception thrown when an asset is not found in CoinCap API.
 */
public class CoinCapAssetNotFoundException extends CoinCapException {

    public CoinCapAssetNotFoundException(String message) {
        super(message);
    }
}