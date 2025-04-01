package com.technischools.marcel.order;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.technischools.marcel.order.controller.OrderController;
import com.technischools.marcel.order.model.Order;
import com.technischools.marcel.order.model.OrderStatus;
import com.technischools.marcel.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = new Order();
        sampleOrder.setId(1L);
        sampleOrder.setCustomerId(123);
        sampleOrder.setStatus(OrderStatus.NOWE);
        sampleOrder.setCreatedAt(LocalDateTime.now());
        sampleOrder.setProducts(Arrays.asList("Product1", "Product2"));
    }

    @Test
    void testCreateOrder_ValidRequest_ReturnsCreated() throws Exception {
        when(orderService.createOrder(any(Order.class))).thenReturn(sampleOrder);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleOrder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerId").value(123))
                .andExpect(jsonPath("$.status").value("NOWE"));

        verify(orderService, times(1)).createOrder(any(Order.class));
    }

    @Test
    void testCreateOrder_InvalidData_ReturnsBadRequest() throws Exception {
        Order invalidOrder = new Order(); // Missing required fields

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrder)))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).createOrder(any(Order.class));
    }

    @Test
    void testGetOrderById_ExistingOrder_ReturnsOrder() throws Exception {
        when(orderService.getOrders(Optional.of(1L), Optional.empty())).thenReturn(List.of(sampleOrder));

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].customerId").value(123));

        verify(orderService, times(1)).getOrders(Optional.of(1L), Optional.empty());
    }

    @Test
    void testGetOrderById_NonExistingOrder_ReturnsNotFound() throws Exception {
        when(orderService.getOrders(Optional.of(99L), Optional.empty())).thenReturn(List.of());

        mockMvc.perform(get("/orders/99"))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).getOrders(Optional.of(99L), Optional.empty());
    }

    @Test
    void testGetOrdersByStatus_ExistingOrders_ReturnsOrders() throws Exception {
        when(orderService.getOrders(Optional.empty(), Optional.of(OrderStatus.NOWE))).thenReturn(List.of(sampleOrder));

        mockMvc.perform(get("/orders/get-by-status")
                        .param("status", "NOWE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("NOWE"));

        verify(orderService, times(1)).getOrders(Optional.empty(), Optional.of(OrderStatus.NOWE));
    }

    @Test
    void testGetOrdersByStatus_NoOrders_ReturnsNotFound() throws Exception {
        when(orderService.getOrders(Optional.empty(), Optional.of(OrderStatus.ZAKONCZONE))).thenReturn(List.of());

        mockMvc.perform(get("/orders/get-by-status")
                        .param("status", "ZAKONCZONE"))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).getOrders(Optional.empty(), Optional.of(OrderStatus.ZAKONCZONE));
    }

    @Test
    void testGetAllOrders_ExistingOrders_ReturnsAllOrders() throws Exception {
        Order anotherOrder = new Order();
        anotherOrder.setId(2L);
        anotherOrder.setCustomerId(456);
        anotherOrder.setStatus(OrderStatus.W_TRAKCIE);
        anotherOrder.setCreatedAt(LocalDateTime.now());
        anotherOrder.setProducts(Arrays.asList("Product3", "Product4"));

        when(orderService.getOrders(Optional.empty(), Optional.empty())).thenReturn(List.of(sampleOrder, anotherOrder));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(orderService, times(1)).getOrders(Optional.empty(), Optional.empty());
    }

    @Test
    void testGetAllOrders_NoOrders_ReturnsNotFound() throws Exception {
        when(orderService.getOrders(Optional.empty(), Optional.empty())).thenReturn(List.of());

        mockMvc.perform(get("/orders"))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).getOrders(Optional.empty(), Optional.empty());
    }

    @Test
    void testUpdateOrder_Success() throws Exception {
        sampleOrder.setStatus(OrderStatus.ZAKONCZONE);
        when(orderService.patchOrder(eq(1L), any(), any())).thenReturn(sampleOrder);

        mockMvc.perform(patch("/orders/1")
                        .param("status", "ZAKONCZONE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ZAKONCZONE"));

        verify(orderService, times(1)).patchOrder(eq(1L), any(), any());
    }

    @Test
    void testUpdateOrder_WithProducts_Success() throws Exception {
        sampleOrder.setProducts(Arrays.asList("UpdatedProduct1", "UpdatedProduct2"));
        when(orderService.patchOrder(eq(1L), eq(Optional.empty()), any())).thenReturn(sampleOrder);

        mockMvc.perform(patch("/orders/1")
                        .param("products", "UpdatedProduct1", "UpdatedProduct2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0]").value("UpdatedProduct1"))
                .andExpect(jsonPath("$.products[1]").value("UpdatedProduct2"));

        verify(orderService, times(1)).patchOrder(eq(1L), eq(Optional.empty()), any());
    }

    @Test
    void testUpdateOrder_WithStatusAndProducts_Success() throws Exception {
        sampleOrder.setStatus(OrderStatus.ZAKONCZONE);
        sampleOrder.setProducts(Arrays.asList("UpdatedProduct1", "UpdatedProduct2"));
        when(orderService.patchOrder(eq(1L), any(), any())).thenReturn(sampleOrder);

        mockMvc.perform(patch("/orders/1")
                        .param("status", "ZAKONCZONE")
                        .param("products", "UpdatedProduct1", "UpdatedProduct2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ZAKONCZONE"))
                .andExpect(jsonPath("$.products[0]").value("UpdatedProduct1"))
                .andExpect(jsonPath("$.products[1]").value("UpdatedProduct2"));

        verify(orderService, times(1)).patchOrder(eq(1L), any(), any());
    }

    @Test
    void testUpdateOrder_OrderNotFound_ReturnsNotFound() throws Exception {
        when(orderService.patchOrder(eq(99L), any(), any())).thenReturn(null);

        mockMvc.perform(patch("/orders/99")
                        .param("status", "ZAKONCZONE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).patchOrder(eq(99L), any(), any());
    }

    @Test
    void testDeleteOrder_Success() throws Exception {
        doNothing().when(orderService).deleteOrder(1L);

        mockMvc.perform(delete("/orders/1"))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).deleteOrder(1L);
    }

    @Test
    void testDeleteOrder_NonExistingOrder_NoError() throws Exception {
        doNothing().when(orderService).deleteOrder(99L);

        mockMvc.perform(delete("/orders/99"))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).deleteOrder(99L);
    }
}