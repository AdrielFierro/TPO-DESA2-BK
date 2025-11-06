package com.uade.comedor.controller;

import com.uade.comedor.entity.Product;
import com.uade.comedor.service.AzureBlobStorageService;
import com.uade.comedor.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
	@Autowired
	private ProductService productService;

	@Autowired
	private AzureBlobStorageService azureBlobStorageService;

	@GetMapping
	public List<Product> getAll() {
		return productService.getAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Product> getById(@PathVariable Long id) {
		return productService.getById(id)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping(consumes = {"multipart/form-data"})
	public ResponseEntity<?> create(
			@RequestParam("name") String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam("price") BigDecimal price,
			@RequestParam("productType") Product.ProductType productType,
			@RequestParam(value = "isActive", defaultValue = "true") boolean isActive,
			@RequestParam(value = "image", required = false) MultipartFile image) {
		
		try {
			// Crear el producto
			Product product = new Product();
			product.setName(name);
			product.setDescription(description);
			product.setPrice(price);
			product.setProductType(productType);
			product.setActive(isActive);

			// Si se envió una imagen, subirla a Azure
			if (image != null && !image.isEmpty()) {
				if (!azureBlobStorageService.isValidImage(image)) {
					return ResponseEntity.badRequest()
							.body("El archivo debe ser una imagen válida (JPG, PNG, WEBP)");
				}
				
				String imageUrl = azureBlobStorageService.uploadImage(image);
				product.setImageUrl(imageUrl);
			}

			// Guardar el producto
			Product created = productService.create(product);
			return ResponseEntity.status(201).body(created);

		} catch (IOException e) {
			return ResponseEntity.status(500)
					.body("Error al subir la imagen: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(500)
					.body("Error al crear el producto: " + e.getMessage());
		}
	}

	@PatchMapping(value = "/{id}", consumes = {"multipart/form-data"})
	public ResponseEntity<?> update(
			@PathVariable Long id,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam(value = "price", required = false) BigDecimal price,
			@RequestParam(value = "productType", required = false) Product.ProductType productType,
			@RequestParam(value = "isActive", required = false) Boolean isActive,
			@RequestParam(value = "image", required = false) MultipartFile image) {
		
		try {
			// Buscar el producto existente
			Product existing = productService.getById(id).orElse(null);
			if (existing == null) {
				return ResponseEntity.notFound().build();
			}

			// Actualizar solo los campos enviados
			if (name != null) existing.setName(name);
			if (description != null) existing.setDescription(description);
			if (price != null) existing.setPrice(price);
			if (productType != null) existing.setProductType(productType);
			if (isActive != null) existing.setActive(isActive);

			// Si se envió una nueva imagen
			if (image != null && !image.isEmpty()) {
				if (!azureBlobStorageService.isValidImage(image)) {
					return ResponseEntity.badRequest()
							.body("El archivo debe ser una imagen válida (JPG, PNG, WEBP)");
				}

				// Eliminar la imagen anterior si existe
				if (existing.getImageUrl() != null && !existing.getImageUrl().isEmpty()) {
					azureBlobStorageService.deleteImage(existing.getImageUrl());
				}

				// Subir la nueva imagen
				String imageUrl = azureBlobStorageService.uploadImage(image);
				existing.setImageUrl(imageUrl);
			}

			// Guardar cambios
			Product updated = productService.update(id, existing).orElse(null);
			return ResponseEntity.ok(updated);

		} catch (IOException e) {
			return ResponseEntity.status(500)
					.body("Error al subir la imagen: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(500)
					.body("Error al actualizar el producto: " + e.getMessage());
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		// Buscar el producto para eliminar su imagen
		productService.getById(id).ifPresent(product -> {
			if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
				azureBlobStorageService.deleteImage(product.getImageUrl());
			}
		});

		if (productService.delete(id)) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
