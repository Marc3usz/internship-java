package com.technischools.marcel.order.controller;

import com.technischools.marcel.order.model.Order;
import com.technischools.marcel.order.model.OrderStatus;
import com.technischools.marcel.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new order with provided details. If status is not provided, it will be set to NOWE. Creation timestamp will be set automatically if not provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "400", description = "Invalid order data",
                    content = @Content( examples = @ExampleObject(value = "Invalid order data")))
    })
    public ResponseEntity<?> createOrder(
            @Parameter(description = "Order object to be created", required = true)
            @RequestBody Order order) {
        if (order.getCustomerId() == null || order.getProducts() == null || order.getProducts().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid order data");
        }
        Order createdOrder = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping(path = "/{id}")
    @Operation(summary = "Get order by ID", description = "Returns an order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(examples = @ExampleObject(value = "Order not found"))),
    })
    public ResponseEntity<?> getOrderById(
            @Parameter(description = "ID of the order to retrieve", required = true)
            @PathVariable() Long id) {
        List<Order> orders = orderService.getOrders(Optional.ofNullable(id), Optional.empty());
        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(orders);
        }
    }

    @GetMapping(path = "/get-by-status")
    @Operation(summary = "Get orders by status", description = "Returns all orders with the specified status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Order.class)))),
            @ApiResponse(responseCode = "404", description = "No orders found with given status",
                    content = @Content(examples = @ExampleObject(value = "Order not found")))
    })
    public ResponseEntity<?> getOrders(
            @Parameter(description = "Status to filter orders by", required = true)
            @RequestParam() String status) {
        List<Order> orders = orderService.getOrders(Optional.empty(), OrderStatus.toValidStatus(status));
        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(orders);
        }
    }

    @GetMapping(path = "")
    @Operation(summary = "Get all orders", description = "Returns all orders in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders found",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Order.class)))),
            @ApiResponse(responseCode = "404", description = "No orders found",
                    content = @Content(examples = @ExampleObject(value = "Order not found")))
    })
    public ResponseEntity<?> getAllOrders() {
        List<Order> orders = orderService.getOrders(Optional.empty(), Optional.empty());
        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(orders);
        }
    }

    @PatchMapping(path = "/{id}")
    @Operation(summary = "Update an order", description = "Updates an existing order's status and/or products")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order updated successfully",
                    content = @Content(schema = @Schema(implementation = Order.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(examples = @ExampleObject(value = "Order not found")))})
    public ResponseEntity<?> updateOrder(
            @Parameter(description = "ID of the order to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "New status for the order")
            @RequestParam(required = false) String status,
            @Parameter(description = "New product list for the order")
            @RequestParam(required = false) List<String> products) {
        Order order = orderService.patchOrder(id, OrderStatus.toValidStatus(status), Optional.ofNullable(products));
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(order);
        }
    }

    @DeleteMapping(path = "/{id}")
    @Operation(summary = "Delete an order", description = "Deletes an order by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order deleted successfully",
                    content = @Content(examples = {@ExampleObject(value = "Order deleted successfully")}))
    })
    public ResponseEntity<?> deleteOrder(
            @Parameter(description = "ID of the order to delete", required = true)
            @PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Order deleted successfully");
    }
}