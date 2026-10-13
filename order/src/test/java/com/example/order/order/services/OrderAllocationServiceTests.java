package com.example.order.order.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.order.order.models.Center;
import com.example.order.order.models.DeliveryChoice;
import com.example.order.order.models.Order;
import com.example.order.order.models.OrderStatus;
import com.example.order.order.models.User;
import com.example.order.order.strategies.DeliveryCostStrategy;
import com.example.order.order.strategies.DeliveryDistanceStrategy;

class OrderAllocationServiceTests {

    private OrderAllocationService allocator(List<Center> centers) {
        return new OrderAllocationService(new DeliveryCostStrategy(centers),
                new DeliveryDistanceStrategy(centers));
    }

    @Test
    void allocatesLimitedStockInInputOrderRegardlessOfChoice() {
        Center center = new Center(1L, 0, 0, Map.of(100L, 2));
        Order first = new Order(1L, Map.of(100L, 2));
        Order second = new Order(2L, Map.of(100L, 2));

        assertEquals(List.of(first), allocator(List.of(center)).allocateOrders(List.of(
                new User(List.of(first), DeliveryChoice.COST),
                new User(List.of(second), DeliveryChoice.DISTANCE))));
        assertEquals(OrderStatus.ASSIGNED, first.getStatus());
        assertEquals(center, first.getAssignedCenter());
        assertEquals(OrderStatus.UNASSIGNED, second.getStatus());
        assertNull(second.getAssignedCenter());
        assertEquals(0, center.getItems().get(100L).intValue());
    }

    @Test
    void preservesOrderListOrderAndDoesNotAllocateAssignedOrdersAgain() {
        Center center = new Center(1L, 0, 0, Map.of(100L, 2));
        Order first = new Order(1L, Map.of(100L, 1));
        Order second = new Order(2L, Map.of(100L, 1));
        Order third = new Order(3L, Map.of(100L, 1));
        OrderAllocationService service = allocator(List.of(center));
        List<User> users = List.of(new User(List.of(first, second)), new User(List.of(third)));

        assertEquals(List.of(first, second), service.allocateOrders(users));
        assertEquals(OrderStatus.UNASSIGNED, third.getStatus());
        assertTrue(service.allocateOrders(users).isEmpty());
        assertEquals(0, center.getItems().get(100L).intValue());
    }

    @Test
    void unavailableOrderDoesNotPreventOtherOrdersFromBeingAllocated() {
        Center center = new Center(1L, 0, 0, Map.of(100L, 2));
        Order unavailable = new Order(1L, Map.of(200L, 1));
        Order available = new Order(2L, Map.of(100L, 1));

        assertEquals(List.of(available), allocator(List.of(center)).allocateOrders(List.of(
                new User(List.of(unavailable)), new User(List.of(available)))));
        assertEquals(OrderStatus.UNASSIGNED, unavailable.getStatus());
    }

    @Test
    void eachUserChoosesTheirOwnDeliveryStrategyWithinTheSameBatch() {
        Center near = new Center(1L, 1, 0, Map.of(100L, 2), 5);
        Center cheap = new Center(2L, 11, 0, Map.of(100L, 2), 1);
        Order costOrder = new Order(1L, Map.of(100L, 1));
        Order distanceOrder = new Order(2L, Map.of(100L, 1));

        assertEquals(List.of(costOrder, distanceOrder), allocator(List.of(near, cheap)).allocateOrders(
                List.of(new User(List.of(costOrder), DeliveryChoice.COST),
                        new User(List.of(distanceOrder), DeliveryChoice.DISTANCE))));

        assertEquals(cheap, costOrder.getAssignedCenter());
        assertEquals(near, distanceOrder.getAssignedCenter());
        assertEquals(1, cheap.getItems().get(100L).intValue());
        assertEquals(1, near.getItems().get(100L).intValue());
    }

    @Test
    void defaultsToDistanceAndRejectsNullChoice() {
        User user = new User();
        assertEquals(DeliveryChoice.DISTANCE, user.getChoice());
        assertThrows(IllegalArgumentException.class, () -> user.setChoice(null));
        assertThrows(IllegalArgumentException.class, () -> new User(List.of(), null));
        assertEquals(DeliveryChoice.DISTANCE, user.getChoice());
    }

    @Test
    void changingUserChoiceChangesTheNextAllocation() {
        Center near = new Center(1L, 1, 0, Map.of(100L, 2), 5);
        Center cheap = new Center(2L, 11, 0, Map.of(100L, 2), 1);
        Order order = new Order(1L, Map.of(100L, 1));
        User user = new User(List.of(order));
        user.setChoice(DeliveryChoice.COST);

        allocator(List.of(near, cheap)).allocateOrders(List.of(user));

        assertEquals(cheap, order.getAssignedCenter());
    }
}