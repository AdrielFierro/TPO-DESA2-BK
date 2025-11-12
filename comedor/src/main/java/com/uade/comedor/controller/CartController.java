package com.uade.comedor.controller;

import com.uade.comedor.entity.Cart;
import com.uade.comedor.entity.Bill;
import com.uade.comedor.service.CartService;
import com.uade.comedor.dto.CartCreateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carts")
public class CartController {
    
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<Cart> createCart(@RequestBody CartCreateRequest request) {
        Cart createdCart = cartService.createCart(request);
        return new ResponseEntity<>(createdCart, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cart> getCartById(@PathVariable Long id) {
        Cart cart = cartService.getCartById(id);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cart> updateCart(@PathVariable Long id, @RequestBody CartCreateRequest request) {
        Cart updatedCart = cartService.updateCart(id, request);
        return ResponseEntity.ok(updatedCart);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable Long id) {
        cartService.deleteCart(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirmation/{id}")
    public ResponseEntity<Bill> confirmCart(@PathVariable Long id) {
        Bill bill = cartService.confirmCart(id);
        return new ResponseEntity<>(bill, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Cart>> getAllCarts() {
        List<Cart> carts = cartService.getAllCarts();
        return ResponseEntity.ok(carts);
    }
}
