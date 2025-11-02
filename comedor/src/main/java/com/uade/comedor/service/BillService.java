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
        bill.setSubtotal(cart.getTotal());
        bill.setCreatedAt(LocalDateTime.now());
        bill.setProducts(new java.util.ArrayList<>(cart.getProducts()));
        
        return billRepository.save(bill);
    }
}