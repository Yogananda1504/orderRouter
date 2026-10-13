package com.example.order.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.order.order.models.Center;
import com.example.order.order.models.User;
import com.example.order.order.strategies.DeliveryCostStrategy;
import com.example.order.order.strategies.DeliveryDistanceStrategy;
import com.example.order.order.strategies.NoAvailableCenterException;

@SpringBootTest(properties = "demo.simulation.enabled=false")
class OrderApplicationTests {

	@Autowired
	private List<Center> centers;

	@Autowired
	private List<User> users;

	@Autowired
	private DeliveryDistanceStrategy distanceStrategy;

	@Autowired
	private DeliveryCostStrategy costStrategy;

	@Test
	void contextLoads() {
		assertEquals(3, centers.size());
		assertEquals(2, users.size());
		assertEquals(1L, distanceStrategy.calculate(users.get(0).getOrders().get(0)).getCenterId());
		assertEquals(2L, costStrategy.calculate(users.get(1).getOrders().get(0)).getCenterId());
		assertThrows(NoAvailableCenterException.class,
				() -> costStrategy.calculate(users.get(1).getOrders().get(1)));
	}

}
