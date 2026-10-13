package com.example.order.order.strategies;

import java.util.List;
import java.util.Comparator;

import org.springframework.stereotype.Component;

import com.example.order.order.models.Center;
import com.example.order.order.models.DeliveryOption;
import com.example.order.order.models.Order;

@Component
public class DeliveryDistanceStrategy extends DeliveryStrategy<Center> {

    public DeliveryDistanceStrategy(List<Center> centers) {
        super(centers);
    }

    @Override
    public Center calculate(Order order) {
        return findNearestCenter(order).center();
    }

    public List<DeliveryOption> findAvailableCenters(Order order) {
        return availableCenters(order).stream()
                .sorted(Comparator.comparingDouble(DeliveryOption::distanceKm))
                .toList();
    }

    public DeliveryOption findNearestCenter(Order order) {
        return availableCenters(order).stream()
                .min(Comparator.comparingDouble(DeliveryOption::distanceKm))
                .orElseThrow(NoAvailableCenterException::new);
    }
}
