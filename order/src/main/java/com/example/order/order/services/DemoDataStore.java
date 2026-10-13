package com.example.order.order.services;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.order.order.models.Center;
import com.example.order.order.models.Order;
import com.example.order.order.models.User;

@Service
public class DemoDataStore {

    private final Map<Long, User> users = new LinkedHashMap<>();
    private final Map<Long, Center> centers = new LinkedHashMap<>();
    private final Map<Long, Order> orders = new LinkedHashMap<>();
    private final OrderAllocationService allocator;
    private int simulatedOrders;
    private long nextOrderId;
    public static final int SIMULATION_LIMIT = 30;

    public DemoDataStore(List<User> dummyUsers, List<Center> dummyCenters, OrderAllocationService allocator) {
        this.allocator = allocator;
        for (Center center : dummyCenters) {
            centers.put(center.getCenterId(), center);
        }
        long userId = 1;
        for (User user : dummyUsers) {
            users.put(userId, user);
            for (Order order : user.getOrders()) {
                orders.put(order.getId(), order);
            }
            userId++;
        }
        nextOrderId = orders.keySet().stream().mapToLong(Long::longValue).max().orElse(1000) + 1;
    }

    public synchronized DemoData getData() {
        // Copy mutable models so a simulation tick cannot change a response during serialization.
        Map<Long, Center> centerSnapshot = new LinkedHashMap<>();
        centers.forEach((id, center) -> centerSnapshot.put(id, new Center(id, center.getX(), center.getY(),
                new HashMap<>(center.getItems()), center.getGrade())));
        Map<Long, Order> orderSnapshot = new LinkedHashMap<>();
        orders.forEach((id, order) -> {
            Order copy = new Order(id, new HashMap<>(order.getItems()), order.getX(), order.getY());
            copy.setStatus(order.getStatus());
            if (order.getAssignedCenter() != null) {
                copy.setAssignedCenter(centerSnapshot.get(order.getAssignedCenter().getCenterId()));
            }
            orderSnapshot.put(id, copy);
        });
        Map<Long, User> userSnapshot = new LinkedHashMap<>();
        users.forEach((id, user) -> userSnapshot.put(id, new User(
                user.getOrders().stream().map(order -> orderSnapshot.get(order.getId())).toList(), user.getChoice())));
        return new DemoData(userSnapshot, centerSnapshot, orderSnapshot, simulatedOrders, SIMULATION_LIMIT);
    }

    public synchronized void simulateNextOrder() {
        if (simulatedOrders >= SIMULATION_LIMIT || users.isEmpty()) {
            return;
        }
        List<User> simulatedUsers = List.copyOf(users.values());
        User user = simulatedUsers.get(simulatedOrders % simulatedUsers.size());
        long itemId = simulatedOrders % 7 == 6 ? 999L : 101L + simulatedOrders % 3;
        int quantity = 1 + simulatedOrders % 3;
        Order order = new Order(nextOrderId++, new HashMap<>(Map.of(itemId, quantity)),
                simulatedOrders % 6, simulatedOrders % 4);
        user.getOrders().add(order);
        orders.put(order.getId(), order);
        simulatedOrders++;
        allocator.allocateOrders(simulatedUsers);
    }

    public record DemoData(Map<Long, User> users, Map<Long, Center> centers, Map<Long, Order> orders,
            int simulatedOrders, int simulationLimit) {
    }
}
