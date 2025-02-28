package com.spicep.cryptowallet.exception.asset;

/**
 * Exception for asset related errors
 */
public class AssetException extends RuntimeException {

    public AssetException(String message) {
        super(message);
    }
}