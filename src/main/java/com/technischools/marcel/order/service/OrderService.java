package com.technischools.marcel.order.service;

import com.technischools.marcel.order.model.Order;
import com.technischools.marcel.order.model.OrderStatus;
import com.technischools.marcel.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order createOrder(Order order) {
        LocalDateTime now = LocalDateTime.now();
        OrderStatus default_status = OrderStatus.NOWE;
        if (order.getStatus() == null) {
            order.setStatus(default_status);
        }
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(now);
        }
        return orderRepository.save(order);
    }

    public List<Order> getOrders(Optional<Long> id, Optional<OrderStatus> status) {
        List<Order> orders = new ArrayList<>();
        if (id.isPresent()) {
            orderRepository.findById(id.get()).ifPresent(order -> {
                if (order.getStatus() == status.orElse(null) || status.isEmpty()) {
                    orders.add(order);
                }
            });
        } else {
            orderRepository.findAll().forEach(order -> {
                if (order.getStatus() == status.orElse(null) || status.isEmpty()) {
                    orders.add(order);
                }
            });
        }
        return orders;
    }

    @Transactional
    public Order patchOrder(Long id, Optional<OrderStatus> orderStatus, Optional<List<String>> products) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {return null;}
        orderStatus.ifPresent(order::setStatus);
        products.ifPresent(order::setProducts);
        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

}

