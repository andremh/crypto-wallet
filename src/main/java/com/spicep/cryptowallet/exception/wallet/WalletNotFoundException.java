package com.spicep.cryptowallet.exception.wallet;

/**
 * Exception thrown when a wallet is not found
 */
public class WalletNotFoundException extends WalletException {

    public WalletNotFoundException(String message) {
        super(message);
    }

}
