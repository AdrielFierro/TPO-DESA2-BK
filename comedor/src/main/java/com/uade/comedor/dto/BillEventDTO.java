package com.uade.comedor.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BillEventDTO {
    private Long billId;
    private String userId;
    private Long cartId;
    private Long reservationId;
    private BigDecimal subtotal;
    private BigDecimal totalWithDiscount;
    private BigDecimal totalWithoutDiscount;
    private LocalDateTime createdAt;
    private List<ProductEventDTO> products;

    // Constructors
    public BillEventDTO() {
    }

    public BillEventDTO(Long billId, String userId, Long cartId, Long reservationId, 
                        BigDecimal subtotal, BigDecimal totalWithDiscount, 
                        BigDecimal totalWithoutDiscount, LocalDateTime createdAt,
                        List<ProductEventDTO> products) {
        this.billId = billId;
        this.userId = userId;
        this.cartId = cartId;
        this.reservationId = reservationId;
        this.subtotal = subtotal;
        this.totalWithDiscount = totalWithDiscount;
        this.totalWithoutDiscount = totalWithoutDiscount;
        this.createdAt = createdAt;
        this.products = products;
    }

    // Getters and Setters
    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTotalWithDiscount() {
        return totalWithDiscount;
    }

    public void setTotalWithDiscount(BigDecimal totalWithDiscount) {
        this.totalWithDiscount = totalWithDiscount;
    }

    public BigDecimal getTotalWithoutDiscount() {
        return totalWithoutDiscount;
    }

    public void setTotalWithoutDiscount(BigDecimal totalWithoutDiscount) {
        this.totalWithoutDiscount = totalWithoutDiscount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ProductEventDTO> getProducts() {
        return products;
    }

    public void setProducts(List<ProductEventDTO> products) {
        this.products = products;
    }

    // Clase interna para productos en el evento
    public static class ProductEventDTO {
        private Long productId;
        private String name;
        private BigDecimal price;

        public ProductEventDTO() {
        }

        public ProductEventDTO(Long productId, String name, BigDecimal price) {
            this.productId = productId;
            this.name = name;
            this.price = price;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}
