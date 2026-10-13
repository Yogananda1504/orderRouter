package com.example.order.order.services;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.example.order.order.models.Center;
import com.example.order.order.models.Order;
import com.example.order.order.models.OrderStatus;
import com.example.order.order.models.User;
import com.example.order.order.strategies.DeliveryStrategy;
import com.example.order.order.strategies.DeliveryCostStrategy;
import com.example.order.order.strategies.DeliveryDistanceStrategy;
import com.example.order.order.strategies.NoAvailableCenterException;

@Service
public class OrderAllocationService {

    private final DeliveryCostStrategy costStrategy;
    private final DeliveryDistanceStrategy distanceStrategy;

    public OrderAllocationService(DeliveryCostStrategy costStrategy, DeliveryDistanceStrategy distanceStrategy) {
        this.costStrategy = Objects.requireNonNull(costStrategy);
        this.distanceStrategy = Objects.requireNonNull(distanceStrategy);
    }

    // Users and their orders retain input order; choice controls center selection.
    // Orders without sufficient stock remain unassigned for a later attempt.
    public List<Order> allocateOrders(List<User> users) {
        List<Order> allocatedOrders = new ArrayList<>();
        for (User user : List.copyOf(users)) {
            DeliveryStrategy<Center> deliveryStrategy = switch (user.getChoice()) {
                case COST -> costStrategy;
                case DISTANCE -> distanceStrategy;
            };
            for (Order order : List.copyOf(user.getOrders())) {
                if (allocateOrder(order, deliveryStrategy)) {
                    allocatedOrders.add(order);
                }
            }
        }
        return List.copyOf(allocatedOrders);
    }

    private boolean allocateOrder(Order order, DeliveryStrategy<Center> deliveryStrategy) {
        // Always acquire the order before a center. Never hold two center locks.
        synchronized (order) {
            if (order.getStatus() != OrderStatus.UNASSIGNED) {
                return false;
            }
            while (true) {
                Center center;
                try {
                    center = deliveryStrategy.calculate(order);
                } catch (NoAvailableCenterException exception) {
                    return false;
                }
                synchronized (center) {
                    Map<Long, Integer> remainingStock = new TreeMap<>(center.getItems());
                    boolean hasStock = order.getItems().entrySet().stream().allMatch(item -> {
                        Integer available = remainingStock.get(item.getKey());
                        return available != null && available >= item.getValue();
                    });
                    if (!hasStock) {
                        // Release this center before selecting again using current stock.
                        continue;
                    }
                    for (Map.Entry<Long, Integer> item : order.getItems().entrySet()) {
                        remainingStock.put(item.getKey(), remainingStock.get(item.getKey()) - item.getValue());
                    }
                    center.setItems(remainingStock);
                    order.setAssignedCenter(center);
                    order.setStatus(OrderStatus.ASSIGNED);
                    return true;
                }
            }
        }
    }
}
