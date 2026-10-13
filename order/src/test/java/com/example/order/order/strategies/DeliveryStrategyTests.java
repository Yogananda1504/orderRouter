package com.example.order.order.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.order.order.models.Center;
import com.example.order.order.models.Order;

class DeliveryStrategyTests {

    @Test
    void bothStrategiesSelectCentersThroughTheCommonContract() {
        Center center = new Center(1L, 3, 4, Map.of(100L, 2));
        Order order = new Order(1L, Map.of(100L, 2), 0, 0);
        DeliveryStrategy<Center> cost = new DeliveryCostStrategy(List.of(center));
        DeliveryStrategy<Center> distance = new DeliveryDistanceStrategy(List.of(center));

        assertEquals(center, cost.calculate(order));
        assertEquals(center, distance.calculate(order));
    }

    @Test
    void costStrategyUsesPriceWhileDistanceStrategyUsesDistance() {
        Center first = new Center(1L, 9, 0, Map.of(100L, 2));
        Center nearer = new Center(2L, 1, 0, Map.of(100L, 2));
        Order order = new Order(1L, Map.of(100L, 2), 0, 0);
        List<Center> centers = List.of(first, nearer);

        // Both cost 20, so the cost strategy keeps the first configured center.
        assertEquals(first, new DeliveryCostStrategy(centers).calculate(order));
        assertEquals(nearer, new DeliveryDistanceStrategy(centers).calculate(order));
    }
}
