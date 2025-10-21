package com.uade.comedor.service;

import com.uade.comedor.dto.CartCreateRequest;
import com.uade.comedor.entity.Cart;
import com.uade.comedor.entity.Bill;
import com.uade.comedor.entity.Product;
import com.uade.comedor.repository.CartRepository;
import com.uade.comedor.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final BillService billService;

    public CartService(CartRepository cartRepository, ProductRepository productRepository, BillService billService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.billService = billService;
    }

    @Transactional
    public Cart createCart(CartCreateRequest request) {
        List<Product> products = getProductsFromIds(request.getCart());
        Cart cart = new Cart();
        cart.setUserId(1L); // TODO: Get from security context
        cart.setPaymentMethod(request.getPaymentMethod());
        cart.setStatus(Cart.CartStatus.OPEN);
        cart.setProducts(products);
        cart.setTotal(calculateTotal(products));
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

        // Crear factura
        Bill bill = billService.createBillFromCart(cart);

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