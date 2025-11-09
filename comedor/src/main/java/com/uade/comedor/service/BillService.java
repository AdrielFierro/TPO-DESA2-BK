package com.uade.comedor.service;

import com.uade.comedor.entity.Bill;
import com.uade.comedor.entity.Cart;
import com.uade.comedor.repository.BillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BillService {
    private final BillRepository billRepository;

    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public Bill getBillById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Factura no encontrada"));
    }

    public boolean isReservationUsed(Long reservationId) {
        if (reservationId == null) return false;
        return billRepository.existsByReservationId(reservationId);
    }

    public List<Bill> getBills(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha inicial debe ser anterior a la fecha final");
            }
            return billRepository.findByCreatedAtBetween(startDate, endDate);
        }
        return billRepository.findAll();
    }

    @Transactional
    public Bill createBillFromCart(Cart cart) {
        Bill bill = new Bill();
        bill.setUserId(cart.getUserId());
        bill.setCartId(cart.getId());
        // Calculamos totals: original (sin descuento) y final (con descuento ya aplicado en cart.total)
        java.math.BigDecimal discount = cart.getReservationDiscount() == null ? java.math.BigDecimal.ZERO : cart.getReservationDiscount();
        java.math.BigDecimal finalTotal = cart.getTotal() == null ? java.math.BigDecimal.ZERO : cart.getTotal();
        java.math.BigDecimal originalTotal = finalTotal.add(discount);

        // Asociar reserva si existe y no fue usada ya en otra factura
        if (cart.getReservationId() != null && !isReservationUsed(cart.getReservationId())) {
            bill.setReservationId(cart.getReservationId());
        } else {
            bill.setReservationId(null);
        }

        // Guardar totales
        bill.setTotalWithDiscount(finalTotal);
        bill.setTotalWithoutDiscount(originalTotal);
        // Mantener 'subtotal' por compatibilidad (usamos subtotal = totalWithDiscount)
        bill.setSubtotal(finalTotal);

        bill.setCreatedAt(LocalDateTime.now());
        bill.setProducts(new java.util.ArrayList<>(cart.getProducts()));

        return billRepository.save(bill);
    }
}