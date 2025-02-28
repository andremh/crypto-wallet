package com.spicep.cryptowallet.exception.asset;

/**
 * Exception for asset not found in the wallet
 */
public class AssetNotFoundException extends AssetException {

    public AssetNotFoundException(String message) {
        super(message);
    }
}