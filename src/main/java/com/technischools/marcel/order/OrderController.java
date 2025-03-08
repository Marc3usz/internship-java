package com.technischools.marcel.order;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        if (order.getCustomerId() == null || order.getProducts() == null || order.getProducts().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid order data");
        }
        Order createdOrder = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping(path = {"","/{id}"})
    public ResponseEntity<?> getOrderById(@PathVariable(required = false) Long id, @RequestParam(required = false) String status) {
        List<Order> orders = orderService.getOrders(Optional.ofNullable(id), OrderStatus.toValidStatus(status));
        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(orders);
        }
    }

    @PatchMapping(path = "/{id}")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestParam(required = false) String status, @RequestParam(required = false) List<String> products) {
        Order order = orderService.patchOrder(id, OrderStatus.toValidStatus(status), Optional.ofNullable(products));
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(order);
        }
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Order deleted successfully");
    }

}

