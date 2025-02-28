package com.spicep.cryptowallet.exception;

import com.spicep.cryptowallet.exception.apiclient.CoinCapAssetNotFoundException;
import com.spicep.cryptowallet.exception.apiclient.CoinCapException;
import com.spicep.cryptowallet.exception.apiclient.CoinCapServerException;
import com.spicep.cryptowallet.exception.wallet.WalletAlreadyExistsException;
import com.spicep.cryptowallet.exception.wallet.WalletException;
import com.spicep.cryptowallet.exception.wallet.WalletNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientResponseException() {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error communicating with the API."
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WalletException.class)
    public ResponseEntity<ErrorResponse> handleWalletExceptions(WalletException ex) {
        HttpStatus status;

        if (ex instanceof WalletAlreadyExistsException) {
            status = HttpStatus.CONFLICT;
        } else if (ex instanceof WalletNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }

        ErrorResponse error = new ErrorResponse(
                status.value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(CoinCapException.class)
    public ResponseEntity<ErrorResponse> handleCoinCapExceptions(CoinCapException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (ex instanceof CoinCapAssetNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof CoinCapServerException) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ErrorResponse error = new ErrorResponse(
                status.value(),
                ex.getMessage()
        );

        return new ResponseEntity<>(error, status);
    }

}
