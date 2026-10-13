package com.example.order.order.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.order.order.OrderApplication;
import com.example.order.order.models.Center;
import com.example.order.order.models.OrderStatus;
import com.example.order.order.services.DemoDataStore.DemoData;
import com.example.order.order.strategies.DeliveryCostStrategy;
import com.example.order.order.strategies.DeliveryDistanceStrategy;

class UserOrderSimulationTests {

    private DemoDataStore store() {
        OrderApplication app = new OrderApplication();
        List<Center> centers = List.of(app.nearbyCenter(), app.economicalCenter(), app.thirdCenter());
        return new DemoDataStore(app.dummyUsers(), centers, new OrderAllocationService(
                new DeliveryCostStrategy(centers), new DeliveryDistanceStrategy(centers)));
    }

    @Test
    void userOrdersAreCreatedAndAutomaticallyAllocatedUsingTheirChoices() {
        DemoDataStore store = store();
        UserOrderSimulation simulation = new UserOrderSimulation(store);
        simulation.createOrder();
        simulation.createOrder();
        DemoData data = store.getData();

        assertEquals(6, data.orders().size());
        assertEquals(2, data.simulatedOrders());
        assertEquals(1L, data.orders().get(1005L).getAssignedCenter().getCenterId());
        assertEquals(2L, data.orders().get(1006L).getAssignedCenter().getCenterId());
        assertEquals(7, data.centers().get(1L).getItems().get(101L).intValue());
        assertEquals(OrderStatus.UNASSIGNED, data.orders().get(1004L).getStatus());
    }

    @Test
    void simulationStopsAtItsLimitWithoutNegativeStockOrDuplicateIds() {
        DemoDataStore store = store();
        for (int tick = 0; tick < 40; tick++) {
            store.simulateNextOrder();
        }
        DemoData data = store.getData();
        assertEquals(30, data.simulatedOrders());
        assertEquals(34, data.orders().size());
        assertEquals(17, data.users().get(1L).getOrders().size());
        assertEquals(17, data.users().get(2L).getOrders().size());
        assertTrue(data.centers().values().stream()
                .flatMap(center -> center.getItems().values().stream()).allMatch(quantity -> quantity >= 0));
    }

    @Test
    void snapshotsStayConsistentWhileSimulationChangesTheLiveModels() {
        DemoDataStore store = store();
        DemoData before = store.getData();
        store.simulateNextOrder();
        assertEquals(4, before.orders().size());
        assertEquals(2, before.users().get(1L).getOrders().size());
        assertEquals(10, before.centers().get(1L).getItems().get(101L).intValue());
        assertEquals(OrderStatus.UNASSIGNED, before.orders().get(1001L).getStatus());
    }
}
