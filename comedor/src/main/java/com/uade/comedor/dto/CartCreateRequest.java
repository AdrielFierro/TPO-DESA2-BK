package com.uade.comedor.dto;

import com.uade.comedor.entity.Cart;
import java.util.List;

public class CartCreateRequest {
    private Cart.PaymentMethod paymentMethod;
    private List<Long> cart;
    private Long reservationId; // Opcional: ID de la reserva para aplicar descuento

    // Getters and Setters
    public Cart.PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Cart.PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<Long> getCart() {
        return cart;
    }

    public void setCart(List<Long> cart) {
        this.cart = cart;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }
}