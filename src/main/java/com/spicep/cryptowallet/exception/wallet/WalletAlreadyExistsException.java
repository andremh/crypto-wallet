package com.spicep.cryptowallet.exception.wallet;

/**
 * Exception thrown when a wallet already exists
 */
public class WalletAlreadyExistsException extends WalletException{

    public WalletAlreadyExistsException(String message) {
        super(message);
    }

}
