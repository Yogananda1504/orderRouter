package com.example.order.order.services;

import java.util.ArrayList;
import java.util.HashMap;
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
    public synchronized List<Order> allocateOrders(List<User> users) {
        List<Order> allocatedOrders = new ArrayList<>();
        for (User user : List.copyOf(users)) {
            DeliveryStrategy<Center> deliveryStrategy = switch (user.getChoice()) {
                case COST -> costStrategy;
                case DISTANCE -> distanceStrategy;
            };
            for (Order order : List.copyOf(user.getOrders())) {
                if (order.getStatus() != OrderStatus.UNASSIGNED) {
                    continue;
                }
                Center center;
                try {
                    center = deliveryStrategy.calculate(order);
                } catch (NoAvailableCenterException exception) {
                    continue;
                }
                Map<Long, Integer> remainingStock = new HashMap<>(center.getItems());
                for (Map.Entry<Long, Integer> item : order.getItems().entrySet()) {
                    remainingStock.put(item.getKey(), remainingStock.get(item.getKey()) - item.getValue());
                }
                center.setItems(remainingStock);
                order.setAssignedCenter(center);
                order.setStatus(OrderStatus.ASSIGNED);
                allocatedOrders.add(order);
            }
        }
        return List.copyOf(allocatedOrders);
        
    }
}
