package com.uade.comedor.service;

import com.uade.comedor.entity.Bill;
import com.uade.comedor.entity.Cart;
import com.uade.comedor.repository.BillRepository;
import com.uade.comedor.events.publisher.EventPublisher;
import com.uade.comedor.events.dto.BillCreatedPayload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BillService {
    private final BillRepository billRepository;
    private final EventPublisher eventPublisher;

    public BillService(BillRepository billRepository, EventPublisher eventPublisher) {
        this.billRepository = billRepository;
        this.eventPublisher = eventPublisher;
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
        
        Bill savedBill = billRepository.save(bill);
        
        // 🎉 Publicar evento de factura creada
        publishBillCreatedEvent(savedBill);
        
        return savedBill;
    }
    
    /**
     * Publica un evento de factura creada a RabbitMQ.
     */
    private void publishBillCreatedEvent(Bill bill) {
        // Convertir los productos a ProductInfo según AsyncAPI spec
        List<BillCreatedPayload.ProductInfo> productInfoList = bill.getProducts().stream()
            .map(product -> BillCreatedPayload.ProductInfo.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .productType(product.getProductType().name())
                .image(product.getImageUrl())
                .build())
            .collect(Collectors.toList());
        
        // Construir el payload del evento
        BillCreatedPayload payload = BillCreatedPayload.builder()
            .id(bill.getId().toString())
            .date(bill.getCreatedAt())
            .subtotal(bill.getSubtotal())
            .reservationId(bill.getReservationId() != null ? bill.getReservationId().toString() : null)
            .products(productInfoList)
            .build();
        
        // Publicar el evento
        eventPublisher.publishBillCreated(
            payload, 
            bill.getUserId().toString(), 
            "student"
        );
    }
}