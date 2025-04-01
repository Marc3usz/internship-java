package com.technischools.marcel.order.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "orders")
@Schema(description = "Entity representing a customer order")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the order", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "ID of the customer who placed the order", example = "1001")
    private Integer customerId;

    @NotNull
    @ElementCollection
    @Schema(description = "List of product identifiers in the order", example = "['product-123', 'product-456']")
    private List<String> products;

    @Schema(description = "Timestamp when the order was created", example = "2024-03-31T12:30:00")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Current status of the order", example = "NOWE")
    private OrderStatus status;
}
