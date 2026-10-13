package com.example.order.order.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.example.order.order.models.Center;
import com.example.order.order.models.Order;
import com.example.order.order.models.User;
import com.example.order.order.strategies.DeliveryCostStrategy;
import com.example.order.order.strategies.DeliveryDistanceStrategy;

class ConcurrentOrderAllocationTests {

    private DeliveryDistanceStrategy competingStrategy(List<Center> centers) {
        CountDownLatch selected = new CountDownLatch(2);
        AtomicInteger calls = new AtomicInteger();
        return new DeliveryDistanceStrategy(centers) {
            @Override
            public Center calculate(Order order) {
                Center result = super.calculate(order);
                if (calls.incrementAndGet() <= 2) {
                    selected.countDown();
                    try {
                        if (!selected.await(5, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("Different orders could not select centers concurrently");
                        }
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(exception);
                    }
                }
                return result;
            }
        };
    }

    @Test
    void concurrentOrdersRetryAnotherCenterWhenSelectedStockIsTaken() throws Exception {
        Center near = new Center(1L, 0, 0, Map.of(100L, 1));
        Center far = new Center(2L, 10, 0, Map.of(100L, 1));
        List<Center> centers = List.of(near, far);
        OrderAllocationService service = new OrderAllocationService(
                new DeliveryCostStrategy(centers), competingStrategy(centers));
        Order first = new Order(1L, Map.of(100L, 1));
        Order second = new Order(2L, Map.of(100L, 1));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<List<Order>> a = pool.submit(() -> service.allocateOrders(List.of(new User(List.of(first)))));
            Future<List<Order>> b = pool.submit(() -> service.allocateOrders(List.of(new User(List.of(second)))));
            assertEquals(2, a.get(10, TimeUnit.SECONDS).size() + b.get(10, TimeUnit.SECONDS).size());
            assertNotEquals(first.getAssignedCenter(), second.getAssignedCenter());
            assertEquals(0, near.getItems().get(100L).intValue());
            assertEquals(0, far.getItems().get(100L).intValue());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void concurrentOrdersCannotOversellTheLastUnit() throws Exception {
        Center center = new Center(1L, 0, 0, Map.of(100L, 1));
        List<Center> centers = List.of(center);
        OrderAllocationService service = new OrderAllocationService(
                new DeliveryCostStrategy(centers), competingStrategy(centers));
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<List<Order>> a = pool.submit(() -> service.allocateOrders(
                    List.of(new User(List.of(new Order(1L, Map.of(100L, 1)))))));
            Future<List<Order>> b = pool.submit(() -> service.allocateOrders(
                    List.of(new User(List.of(new Order(2L, Map.of(100L, 1)))))));
            assertEquals(1, a.get(10, TimeUnit.SECONDS).size() + b.get(10, TimeUnit.SECONDS).size());
            assertEquals(0, center.getItems().get(100L).intValue());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void repeatedConcurrentSubmissionsAssignTheSameOrderOnlyOnce() throws Exception {
        Center center = new Center(1L, 0, 0, Map.of(100L, 10));
        Order order = new Order(1L, Map.of(100L, 1));
        List<User> users = List.of(new User(List.of(order)));
        OrderAllocationService service = new OrderAllocationService(
                new DeliveryCostStrategy(List.of(center)), new DeliveryDistanceStrategy(List.of(center)));
        ExecutorService pool = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<List<Order>>> results = new ArrayList<>();
        try {
            for (int i = 0; i < 12; i++) {
                results.add(pool.submit(() -> { start.await(); return service.allocateOrders(users); }));
            }
            start.countDown();
            int assigned = 0;
            for (Future<List<Order>> result : results) {
                assigned += result.get(5, TimeUnit.SECONDS).size();
            }
            assertEquals(1, assigned);
            assertEquals(9, center.getItems().get(100L).intValue());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void centerStockStaysSortedAndCannotBeChangedThroughASnapshot() {
        Center center = new Center(1L, 0, 0, Map.of(300L, 5, 100L, 5, 200L, 5));
        assertEquals(List.of(100L, 200L, 300L), new ArrayList<>(center.getItems().keySet()));
        assertThrows(UnsupportedOperationException.class, () -> center.getItems().put(100L, 0));
        OrderAllocationService service = new OrderAllocationService(
                new DeliveryCostStrategy(List.of(center)), new DeliveryDistanceStrategy(List.of(center)));
        service.allocateOrders(List.of(new User(List.of(new Order(1L, Map.of(300L, 1, 100L, 1))))));
        assertEquals(List.of(100L, 200L, 300L), new ArrayList<>(center.getItems().keySet()));
    }
}
