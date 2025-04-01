package com.technischools.marcel.order;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.technischools.marcel.order.model.Order;
import com.technischools.marcel.order.model.OrderStatus;
import com.technischools.marcel.order.repository.OrderRepository;
import com.technischools.marcel.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = new Order();
        sampleOrder.setId(1L);
        sampleOrder.setStatus(OrderStatus.NOWE);
        sampleOrder.setCreatedAt(LocalDateTime.now());
        sampleOrder.setProducts(Arrays.asList("Product1", "Product2"));
    }

    @Test
    void testCreateOrder_WithNullValues_SetsDefaults() {
        Order order = new Order(); // Empty order (null values)
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        Order createdOrder = orderService.createOrder(order);

        assertNotNull(createdOrder);
        assertEquals(OrderStatus.NOWE, createdOrder.getStatus());
        assertNotNull(createdOrder.getCreatedAt());

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCreateOrder_WithCustomValues() {
        Order order = new Order();
        order.setStatus(OrderStatus.W_TRAKCIE);
        order.setCreatedAt(LocalDateTime.of(2023, 1, 1, 10, 0));

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order createdOrder = orderService.createOrder(order);

        assertNotNull(createdOrder);
        assertEquals(OrderStatus.W_TRAKCIE, createdOrder.getStatus());
        assertEquals(LocalDateTime.of(2023, 1, 1, 10, 0), createdOrder.getCreatedAt());

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void testGetOrders_ByStatus_ReturnsFilteredOrders() {
        Order order2 = new Order();
        order2.setId(2L);
        order2.setStatus(OrderStatus.W_TRAKCIE);
        order2.setCreatedAt(LocalDateTime.now());
        order2.setProducts(List.of("Product3"));

        when(orderRepository.findAll()).thenReturn(List.of(sampleOrder, order2));

        List<Order> result = orderService.getOrders(Optional.empty(), Optional.of(OrderStatus.W_TRAKCIE));

        assertEquals(1, result.size());
        assertEquals(OrderStatus.W_TRAKCIE, result.get(0).getStatus());

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testGetOrders_NonExistingId_ReturnsEmptyList() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        List<Order> result = orderService.getOrders(Optional.of(99L), Optional.empty());

        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findById(99L);
    }

    @Test
    void testGetOrders_ByIdAndStatus_MatchingStatus_ReturnsOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        List<Order> result = orderService.getOrders(Optional.of(1L), Optional.of(OrderStatus.NOWE));

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(OrderStatus.NOWE, result.get(0).getStatus());

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testGetOrders_ByIdAndStatus_NonMatchingStatus_ReturnsEmptyList() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        List<Order> result = orderService.getOrders(Optional.of(1L), Optional.of(OrderStatus.ZAKONCZONE));

        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testPatchOrder_UpdateOnlyStatus() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        Order patchedOrder = orderService.patchOrder(1L, Optional.of(OrderStatus.ZAKONCZONE), Optional.empty());

        assertEquals(OrderStatus.ZAKONCZONE, patchedOrder.getStatus());
        assertEquals(2, patchedOrder.getProducts().size()); // Products remain unchanged

        verify(orderRepository, times(1)).save(sampleOrder);
    }

    @Test
    void testPatchOrder_UpdateOnlyProducts() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(sampleOrder);

        List<String> newProducts = List.of("New Product A", "New Product B");
        Order patchedOrder = orderService.patchOrder(1L, Optional.empty(), Optional.of(newProducts));

        assertEquals(OrderStatus.NOWE, patchedOrder.getStatus()); // Status remains unchanged
        assertEquals(2, patchedOrder.getProducts().size());
        assertEquals("New Product A", patchedOrder.getProducts().get(0));

        verify(orderRepository, times(1)).save(sampleOrder);
    }

    @Test
    void testPatchOrder_OrderNotFound_ReturnsNull() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        Order result = orderService.patchOrder(99L, Optional.of(OrderStatus.ZAKONCZONE), Optional.empty());

        assertNull(result);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testDeleteOrder_NonExistingId_NoError() {
        doNothing().when(orderRepository).deleteById(99L);

        assertDoesNotThrow(() -> orderService.deleteOrder(99L));

        verify(orderRepository, times(1)).deleteById(99L);
    }

    @Test
    void testGetOrders_NoOrders_ReturnsEmptyList() {
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        List<Order> result = orderService.getOrders(Optional.empty(), Optional.empty());

        assertTrue(result.isEmpty());
        verify(orderRepository, times(1)).findAll();
    }
}