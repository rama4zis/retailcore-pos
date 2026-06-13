package com.retailcore.pos.payment;

public class CardCashTenderedException extends RuntimeException {

    public CardCashTenderedException() {
        super("Cash tendered must be empty for card payments");
    }
}
