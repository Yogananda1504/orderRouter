package com.example.order.order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.order.order.models.Center;
import com.example.order.order.models.DeliveryChoice;
import com.example.order.order.models.Order;
import com.example.order.order.models.OrderItem;
import com.example.order.order.models.User;

@SpringBootApplication
public class OrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderApplication.class, args);
	}

	@Bean
	public Center nearbyCenter() {
		return new Center(1L, 1, 0, new HashMap<>(Map.of(101L, 10, 102L, 8, 103L, 5)), 5);
	}

	@Bean
	public Center economicalCenter() {
		return new Center(2L, 11, 0, new HashMap<>(Map.of(101L, 20, 102L, 15, 103L, 10)), 1);
	}

	@Bean
	public Center thirdCenter() {
		return new Center(3L, 5, 5, new HashMap<>(Map.of(101L, 5, 102L, 10)), 3);
	}

	@Bean
	public List<User> dummyUsers() {
		// Each order's items share the same delivery location.
		Order firstOrder = createOrder(1001L,
				new OrderItem(101L, 2, 0, 0), new OrderItem(102L, 1, 0, 0));
		Order secondOrder = createOrder(1002L, new OrderItem(103L, 2, 2, 1));
		Order thirdOrder = createOrder(1003L,
				new OrderItem(101L, 3, 0, 0), new OrderItem(102L, 2, 0, 0));
		// No center stocks item 999, so this order stays unassigned during allocation.
		Order unavailableOrder = createOrder(1004L, new OrderItem(999L, 1, 3, 3));

		User distanceUser = new User(new ArrayList<>(List.of(firstOrder, secondOrder)), DeliveryChoice.DISTANCE);
		User costUser = new User(new ArrayList<>(List.of(thirdOrder, unavailableOrder)), DeliveryChoice.COST);
		return new ArrayList<>(List.of(distanceUser, costUser));
	}

	private Order createOrder(Long id, OrderItem... orderItems) {
		Map<Long, Integer> quantities = new HashMap<>();
		for (OrderItem item : orderItems) {
			quantities.merge(item.getId(), item.getQuantity(), Integer::sum);
		}
		return new Order(id, quantities, orderItems[0].getX(), orderItems[0].getY());
	}

}
