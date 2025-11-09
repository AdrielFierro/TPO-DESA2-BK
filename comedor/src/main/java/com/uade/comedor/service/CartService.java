package com.uade.comedor.service;

import com.uade.comedor.dto.CartCreateRequest;
import com.uade.comedor.entity.Cart;
import com.uade.comedor.entity.Bill;
import com.uade.comedor.entity.Product;
import com.uade.comedor.entity.Reservation;
import com.uade.comedor.repository.CartRepository;
import com.uade.comedor.repository.ProductRepository;
import com.uade.comedor.repository.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final BillService billService;
    private final ReservationRepository reservationRepository;
    
    public CartService(CartRepository cartRepository, ProductRepository productRepository, 
                      BillService billService, ReservationRepository reservationRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.billService = billService;
        this.reservationRepository = reservationRepository;
    }
    

    @Transactional
    public Cart createCart(CartCreateRequest request) {
        List<Product> products = getProductsFromIds(request.getCart());
        BigDecimal subtotal = calculateTotal(products);
        BigDecimal discount = BigDecimal.ZERO;
        Long reservationId = null;
        // If a billId is provided and that bill already has a reservation associated,
        // do not reapply reservation discounts.
        boolean skipReservationDiscount = false;
        if (request.getBillId() != null) {
            try {
                com.uade.comedor.entity.Bill existing = billService.getBillById(request.getBillId());
                if (existing.getReservationId() != null) {
                    skipReservationDiscount = true;
                    // If request didn't include a reservationId, inherit from existing bill
                    if (request.getReservationId() == null) {
                        reservationId = existing.getReservationId();
                    }
                }
            } catch (Exception e) {
                // if bill not found, ignore and allow normal behavior (will error later if used)
            }
        }
    // Determinar reserva efectiva (puede venir en request o heredarse desde la factura existente)
    Long effectiveReservationId = request.getReservationId() != null ? request.getReservationId() : reservationId;

    // Si hay una reserva efectiva, validar y calcular descuento (permitir crear carrito aunque descuento ya fue usada)
    if (effectiveReservationId != null) {
            Reservation reservation = reservationRepository.findById(effectiveReservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Reserva no encontrada"));

            // Validar estado de la reserva
            if (reservation.getStatus() == Reservation.ReservationStatus.CANCELADA) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No puedes usar una reserva CANCELADA para obtener descuento");
            }

            if (reservation.getStatus() == Reservation.ReservationStatus.AUSENTE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No puedes usar una reserva AUSENTE para obtener descuento");
            }

            // Validar ventana de 24 horas
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reservationTime = reservation.getReservationDate();
            Duration timeDiff = Duration.between(reservationTime, now);
            long hoursDiff = Math.abs(timeDiff.toHours());

            if (hoursDiff > 24) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Solo puedes usar el descuento de la reserva dentro de las 24 horas. " +
                                 "Reserva: %s. Hora actual: %s. Diferencia: %d horas",
                                 reservationTime, now, hoursDiff));
            }

            // Determinar si la reserva ya fue usada en otra factura o si se pidió explícitamente skip por billId
            boolean used = billService.isReservationUsed(reservation.getId()) || skipReservationDiscount;
            if (used) {
                // Permitimos crear el carrito pero sin descuento
                discount = BigDecimal.ZERO;
                reservationId = reservation.getId();
            } else {
                // Aplicar descuento normalmente (no marcamos la reserva como "usada" aquí)
                discount = reservation.getCost();
                reservationId = reservation.getId();
            }
        }
        
        // Crear carrito
        Cart cart = new Cart();
        cart.setUserId(1L); // TODO: Obtener del contexto de seguridad
        cart.setPaymentMethod(request.getPaymentMethod());
        cart.setStatus(Cart.CartStatus.OPEN);
        cart.setProducts(products);
    // Asociar a factura si se pidió
    cart.setBillId(request.getBillId());
        cart.setReservationId(reservationId);
        cart.setReservationDiscount(discount);
        cart.setTotal(subtotal.subtract(discount)); // Total = Subtotal - Descuento
        cart.setCreatedAt(LocalDateTime.now());
        
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateCart(Long id, CartCreateRequest request) {
        Cart cart = cartRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito no encontrado"));
            
        if (cart.getStatus() != Cart.CartStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden actualizar carritos abiertos");
        }

        List<Product> products = getProductsFromIds(request.getCart());
        cart.setPaymentMethod(request.getPaymentMethod());
        cart.setProducts(products);
        cart.setTotal(calculateTotal(products));
        
        return cartRepository.save(cart);
    }

    @Transactional
    public void deleteCart(Long id) {
        Cart cart = cartRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito no encontrado"));
            
        cart.setStatus(Cart.CartStatus.CANCELLED);
        cartRepository.save(cart);
    }

    @Transactional
    public Bill confirmCart(Long id) {
        Cart cart = cartRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Carrito no encontrado"));
            
        if (cart.getStatus() != Cart.CartStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden confirmar carritos abiertos");
        }

        // Si el carrito tiene una reserva asociada, confirmarla automáticamente
        if (cart.getReservationId() != null) {
            Reservation reservation = reservationRepository.findById(cart.getReservationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Reserva no encontrada"));
            
            // Validar ventana de tiempo de 20 minutos para la confirmación
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reservationTime = reservation.getReservationDate();
            LocalDateTime earliestConfirmTime = reservationTime.minusMinutes(20);
            LocalDateTime latestConfirmTime = reservationTime.plusMinutes(20);
            
            if (now.isBefore(earliestConfirmTime) || now.isAfter(latestConfirmTime)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Solo puedes confirmar el carrito (y la reserva) entre 20 minutos antes y 20 minutos después del horario reservado. " +
                                 "Horario de reserva: %s. Ventana de confirmación: %s a %s. Hora actual: %s",
                                 reservationTime,
                                 earliestConfirmTime,
                                 latestConfirmTime,
                                 now));
            }
            
            // Confirmar la reserva automáticamente
            if (reservation.getStatus() != Reservation.ReservationStatus.CONFIRMADA) {
                reservation.setStatus(Reservation.ReservationStatus.CONFIRMADA);
                reservationRepository.save(reservation);
            }
        }

        // Si el carrito ya está ligado a una factura existing, retornamos dicha factura
        if (cart.getBillId() != null) {
            try {
                Bill existingBill = billService.getBillById(cart.getBillId());
                // marcar carrito como confirmado y devolver la factura existente (no reaplicar descuentos)
                cart.setStatus(Cart.CartStatus.CONFIRMED);
                cartRepository.save(cart);
                return existingBill;
            } catch (Exception e) {
                // si la factura no existe por alguna razón, continuamos y creamos una nueva
            }
        }

        // Crear factura con toda la información del carrito (incluyendo descuentos)
        Bill bill = billService.createBillFromCart(cart);
        
        // Asociar la factura al carrito
        cart.setBillId(bill.getId());

        // Actualizar estado del carrito
        cart.setStatus(Cart.CartStatus.CONFIRMED);
        cartRepository.save(cart);

        return bill;
    }

    private List<Product> getProductsFromIds(List<Long> productIds) {
        return productIds.stream()
            .map(id -> productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado: " + id)))
            .collect(Collectors.toList());
    }

    private BigDecimal calculateTotal(List<Product> products) {
        return products.stream()
            .map(Product::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}