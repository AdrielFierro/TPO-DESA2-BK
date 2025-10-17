package com.uade.comedor.service;

import com.uade.comedor.entity.Product;
import com.uade.comedor.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public Optional<Product> getById(Long id) {
        return productRepository.findById(id);
    }

    public Product create(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> update(Long id, Product updates) {
        return productRepository.findById(id).map(existing -> {
            if (updates.getName() != null) existing.setName(updates.getName());
            if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
            if (updates.getPrice() != null) existing.setPrice(updates.getPrice());
            if (updates.getProductType() != null) existing.setProductType(updates.getProductType());
            if (updates.getImageUrl() != null) existing.setImageUrl(updates.getImageUrl());
            existing.setActive(updates.isActive());
            return productRepository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
}