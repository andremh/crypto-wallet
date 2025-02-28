package com.spicep.cryptowallet.exception.wallet;

/**
 * General exception for wallet related issues
 */
public class WalletException extends RuntimeException{

    public WalletException(String message) {
        super(message);
    }

}
