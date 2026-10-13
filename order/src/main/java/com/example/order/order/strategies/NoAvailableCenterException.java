package com.example.order.order.strategies;

public class NoAvailableCenterException extends IllegalStateException {

    public NoAvailableCenterException() {
        super("No center has enough stock to fulfill the order");
    }
}
