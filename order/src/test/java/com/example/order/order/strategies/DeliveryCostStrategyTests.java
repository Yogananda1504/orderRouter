package com.example.order.order.strategies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.order.order.models.Center;
import com.example.order.order.models.Order;
import com.example.order.order.utils.ManhattanDistance;

class DeliveryCostStrategyTests {

    @Test
    void manhattanDistanceWorksAcrossNegativeCoordinates() {
        assertEquals(10, new ManhattanDistance().calculateDistance(-2, 3, 4, -1));
    }

    @ParameterizedTest
    @CsvSource({"0,10", "0.1,20", "9.9,20", "10,20", "10.1,30", "20,30"})
    void chargesForEachStartedTenKilometerBlock(double distance, double expectedCost) {
        Center center = new Center(1L, 0, 0, Map.of(100L, 5));
        DeliveryCostStrategy service = new DeliveryCostStrategy(List.of(center));

        assertEquals(expectedCost, service.calculateDeliveryCost(
                new Order(1L, Map.of(100L, 2), distance, 0)));
        assertEquals(5, center.getItems().get(100L).intValue());
    }

    @Test
    void choosesCheapestCenterThatCanSupplyEveryItem() {
        List<Center> centers = List.of(
                new Center(1L, 0, 0, Map.of(100L, 1, 200L, 5)),
                new Center(2L, 30, 0, Map.of(100L, 5, 200L, 5)),
                new Center(3L, 3, 4, Map.of(100L, 5, 200L, 5)));

        Order order = new Order(1L, Map.of(100L, 2, 200L, 1), 0, 0);
        DeliveryCostStrategy strategy = new DeliveryCostStrategy(centers);
        assertEquals(centers.get(2), strategy.findCheapestCenter(order));
        assertEquals(20, strategy.calculateDeliveryCost(order));
    }

    @Test
    void doesNotCombineStockFromDifferentCenters() {
        DeliveryCostStrategy service = new DeliveryCostStrategy(List.of(
                new Center(1L, 0, 0, Map.of(100L, 5)),
                new Center(2L, 0, 0, Map.of(200L, 5))));

        assertThrows(IllegalStateException.class, () -> service.calculateDeliveryCost(
                new Order(1L, Map.of(100L, 2, 200L, 1), 0, 0)));
    }

    @ParameterizedTest
    @CsvSource({"1,30", "2,60", "3,90", "4,120", "5,150"})
    void multipliesDeliveryPriceByCenterGrade(int grade, double expectedCost) {
        Center center = new Center(1L, 11, 0, Map.of(100L, 2), grade);
        DeliveryCostStrategy strategy = new DeliveryCostStrategy(List.of(center));

        assertEquals(expectedCost, strategy.calculateDeliveryCost(
                new Order(1L, Map.of(100L, 1), 0, 0)));
    }

    @Test
    void choosesFartherCenterWhenItsGradeMakesItCheaper() {
        Center near = new Center(1L, 1, 0, Map.of(100L, 2), 5);
        Center far = new Center(2L, 11, 0, Map.of(100L, 2), 1);
        List<Center> centers = List.of(near, far);
        Order order = new Order(1L, Map.of(100L, 1), 0, 0);
        DeliveryCostStrategy strategy = new DeliveryCostStrategy(centers);

        assertEquals(far, strategy.calculate(order));
        assertEquals(30, strategy.calculateDeliveryCost(order));
        assertEquals(near, new DeliveryDistanceStrategy(centers).calculate(order));
    }

    @Test
    void defaultsGradeToOneAndRejectsGradesOutsideTheRange() {
        Center center = new Center();
        assertEquals(1, center.getGrade());
        assertThrows(IllegalArgumentException.class, () -> center.setGrade(0));
        assertThrows(IllegalArgumentException.class, () -> center.setGrade(6));
        assertThrows(IllegalArgumentException.class,
                () -> new Center(1L, 0, 0, Map.of(), -1));
        assertEquals(1, center.getGrade());
    }

    @Test
    void rejectsInvalidOrders() {
        DeliveryCostStrategy service = new DeliveryCostStrategy(List.of());

        assertThrows(IllegalArgumentException.class, () -> service.calculateDeliveryCost(new Order()));
        assertThrows(IllegalArgumentException.class, () -> service.calculateDeliveryCost(
                new Order(1L, Map.of(100L, 0), 0, 0)));
        assertThrows(IllegalArgumentException.class, () -> service.calculateDeliveryCost(
                new Order(1L, Map.of(100L, 1), Double.NaN, 0)));
        assertThrows(IllegalStateException.class, () -> service.calculateDeliveryCost(
                new Order(1L, Map.of(100L, 1), 0, 0)));
    }
}
