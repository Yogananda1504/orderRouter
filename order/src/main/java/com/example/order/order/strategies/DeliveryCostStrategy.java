package com.example.order.order.strategies;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.order.order.models.Center;
import com.example.order.order.models.DeliveryOption;
import com.example.order.order.models.Order;

@Component
public class DeliveryCostStrategy extends DeliveryStrategy<Center> {

    private static final double BASE_PRICE = 10;
    private static final double PRICE_PER_BLOCK = 10;
    private static final double BLOCK_DISTANCE_KM = 10;

    public DeliveryCostStrategy(List<Center> centers) {
        super(centers);
    }

    @Override
    public Center calculate(Order order) {
        return findCheapestCenter(order);
    }

    public Center findCheapestCenter(Order order) {
        return cheapestOption(order).center();
    }

    public double calculateDeliveryCost(Order order) {
        return costForOption(cheapestOption(order));
    }

    private DeliveryOption cheapestOption(Order order) {
        DeliveryOption cheapest = null;
        double cheapestCost = Double.POSITIVE_INFINITY;
        for (DeliveryOption option : availableCenters(order)) {
            double cost = costForOption(option);
            if (cheapest == null || cost < cheapestCost) {
                cheapest = option;
                cheapestCost = cost;
            }
        }
        if (cheapest == null) {
            throw new NoAvailableCenterException();
        }
        return cheapest;
    }

    private double costForOption(DeliveryOption option) {
        return option.center().getGrade() * (BASE_PRICE
                + Math.ceil(option.distanceKm() / BLOCK_DISTANCE_KM) * PRICE_PER_BLOCK);
    }

}

