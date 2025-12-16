package com.uade.comedor.controller;

import com.uade.comedor.entity.Cart;
import com.uade.comedor.entity.Bill;
import com.uade.comedor.service.CartService;
import com.uade.comedor.dto.CartCreateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/carts")
public class CartController {
    
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<Cart> createCart(@RequestBody String rawJson) {
        System.out.println("📥 [CartController.createCart] Raw JSON recibido:");
        System.out.println(rawJson);
        
        try {
            // Parsear manualmente para debugging
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            CartCreateRequest request = mapper.readValue(rawJson, CartCreateRequest.class);
            
            System.out.println("📥 [CartController.createCart] Request parseado exitosamente");
            System.out.println("   Payment Method: " + request.getPaymentMethod());
            System.out.println("   Products count: " + (request.getCart() != null ? request.getCart().size() : 0));
            System.out.println("   Reservation ID: " + request.getReservationId());
            System.out.println("   Bill ID: " + request.getBillId());
            
            Cart createdCart = cartService.createCart(request);
            
            System.out.println("📤 [CartController.createCart] Cart creado con ID: " + createdCart.getId());
            System.out.println("   Payment Method guardado: " + createdCart.getPaymentMethod());
            
            return new ResponseEntity<>(createdCart, HttpStatus.CREATED);
        } catch (Exception e) {
            System.err.println("❌ [CartController.createCart] Error al parsear JSON: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al parsear el request: " + e.getMessage(), e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cart> getCartById(@PathVariable Long id) {
        Cart cart = cartService.getCartById(id);
        
        System.out.println("📤 [CartController.getCartById] Devolviendo cart ID: " + id);
        System.out.println("   Payment Method: " + cart.getPaymentMethod());
        System.out.println("   Status: " + cart.getStatus());
        System.out.println("   Total: " + cart.getTotal());
        
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cart> updateCart(@PathVariable Long id, @RequestBody String rawJson) {
        System.out.println("📥 [CartController.updateCart] Raw JSON recibido para cart ID: " + id);
        System.out.println(rawJson);
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            CartCreateRequest request = mapper.readValue(rawJson, CartCreateRequest.class);
            
            System.out.println("📥 [CartController.updateCart] Request parseado exitosamente");
            System.out.println("   Payment Method: " + request.getPaymentMethod());
            System.out.println("   Products count: " + (request.getCart() != null ? request.getCart().size() : 0));
            
            Cart updatedCart = cartService.updateCart(id, request);
            
            System.out.println("📤 [CartController.updateCart] Cart actualizado");
            System.out.println("   Payment Method guardado: " + updatedCart.getPaymentMethod());
            
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            System.err.println("❌ [CartController.updateCart] Error al parsear JSON: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error al parsear el request: " + e.getMessage(), e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable Long id) {
        cartService.deleteCart(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirmation/{id}")
    public ResponseEntity<Bill> confirmCart(
            @PathVariable Long id,
            @org.springframework.web.bind.annotation.RequestHeader("Authorization") String authorizationHeader) {
        // Obtener el walletId del contexto de autenticación
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String walletId = null;
        
        if (authentication instanceof com.uade.comedor.security.UserAuthenticationToken) {
            walletId = ((com.uade.comedor.security.UserAuthenticationToken) authentication).getWalletId();
        }
        
        // Extraer el token (remover "Bearer " si existe)
        String token = authorizationHeader.startsWith("Bearer ") 
            ? authorizationHeader.substring(7) 
            : authorizationHeader;
        
        Bill bill = cartService.confirmCart(id, walletId, token);
        return new ResponseEntity<>(bill, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Cart>> getAllCarts() {
        List<Cart> carts = cartService.getAllCarts();
        return ResponseEntity.ok(carts);
    }
}
