package com.example.order.order.services;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "demo.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class UserOrderSimulation {

    private final DemoDataStore dataStore;

    public UserOrderSimulation(DemoDataStore dataStore) {
        this.dataStore = dataStore;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 5000)
    public void createOrder() {
        dataStore.simulateNextOrder();
    }
}
