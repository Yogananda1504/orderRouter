package com.example.order.order.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.order.order.models.Center;
import com.example.order.order.models.DeliveryOption;
import com.example.order.order.models.Order;

class DeliveryDistanceStrategyTests {

    @Test
    void listsOnlyFullyStockedCentersWithDistancesInAscendingOrder() {
        Center far = new Center(1L, 20, 0, Map.of(100L, 5, 200L, 1));
        Center insufficient = new Center(2L, 0, 0, Map.of(100L, 1, 200L, 1));
        Center missingItem = new Center(3L, 1, 0, Map.of(100L, 5));
        Center near = new Center(4L, -3, 4, Map.of(100L, 2, 200L, 1));
        DeliveryDistanceStrategy service = new DeliveryDistanceStrategy(
                List.of(far, insufficient, missingItem, near));
        Order order = new Order(1L, Map.of(100L, 2, 200L, 1), 0, 0);

        assertEquals(List.of(new DeliveryOption(near, 7), new DeliveryOption(far, 20)),
                service.findAvailableCenters(order));
        assertEquals(new DeliveryOption(near, 7), service.findNearestCenter(order));
        assertEquals(2, near.getItems().get(100L).intValue());
    }

    @Test
    void returnsEmptyOptionsAndRejectsNearestSelectionWhenStockIsUnavailable() {
        DeliveryDistanceStrategy service = new DeliveryDistanceStrategy(
                List.of(new Center(1L, 0, 0, Map.of(100L, 1))));
        Order order = new Order(1L, Map.of(100L, 2), 0, 0);

        assertTrue(service.findAvailableCenters(order).isEmpty());
        assertThrows(IllegalStateException.class, () -> service.findNearestCenter(order));
    }

    @Test
    void keepsConfiguredOrderForEquallyDistantCenters() {
        Center first = new Center(1L, 3, 4, Map.of(100L, 1));
        Center second = new Center(2L, -3, -4, Map.of(100L, 1));
        DeliveryDistanceStrategy service = new DeliveryDistanceStrategy(List.of(first, second));

        assertEquals(first, service.findNearestCenter(
                new Order(1L, Map.of(100L, 1), 0, 0)).center());
    }

    @Test
    void rejectsInvalidDeliveryCoordinatesAndOrders() {
        DeliveryDistanceStrategy service = new DeliveryDistanceStrategy(List.of());

        assertThrows(IllegalArgumentException.class, () -> service.findAvailableCenters(null));
        assertThrows(IllegalArgumentException.class, () -> service.findAvailableCenters(
                new Order(1L, Map.of(100L, 1), Double.POSITIVE_INFINITY, 0)));
        assertThrows(IllegalArgumentException.class, () -> service.findAvailableCenters(
                new Order(1L, Map.of(100L, -1), 0, 0)));
    }
}
