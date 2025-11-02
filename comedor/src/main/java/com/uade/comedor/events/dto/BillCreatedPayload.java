package com.uade.comedor.events.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Payload del evento "bill.created"
 * 
 * Contiene la información de una factura recién generada.
 * Se emite cada vez que un usuario completa una compra en el comedor.
 * 
 * Según AsyncAPI spec: BillCreatedPayload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillCreatedPayload {
    
    // ID de la factura
    private String id;
    
    // Fecha/hora en que se generó la factura
    private LocalDateTime date;
    
    // Subtotal de la factura (suma de todos los productos)
    private BigDecimal subtotal;
    
    // ID de la reserva asociada (puede ser null si es compra sin reserva)
    private String reservationId;
    
    // Lista de productos incluidos en la factura
    private List<ProductInfo> products;
    
    /**
     * Información de cada producto en la factura
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private Long id;
        private String name;
        private BigDecimal price;
        private String productType;  // PLATO, BEBIDA, POSTRE
        private String image;
    }
}
