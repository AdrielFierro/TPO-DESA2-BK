package com.uade.comedor.controller;

import com.uade.comedor.entity.Product;
import com.uade.comedor.service.AzureBlobStorageService;
import com.uade.comedor.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
	
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

	// Endpoint para crear producto con JSON (sin imagen)
	@PostMapping(consumes = {"application/json"})
	public ResponseEntity<?> createFromJson(@RequestBody Product product) {
		try {
			logger.info("📦 Creando producto desde JSON: {}", product.getName());
			// Si el JSON trae una data URL (base64), subirla y reemplazar por la URL del blob
			if (product.getImageUrl() != null && product.getImageUrl().startsWith("data:")) {
				logger.info("🔍 Se recibió imageUrl como data URL. Subiendo a Azure...");
				String imageUrl = azureBlobStorageService.uploadImageFromBase64(product.getImageUrl());
				product.setImageUrl(imageUrl);
				logger.info("✅ Data URL subida y reemplazada por: {}", imageUrl);
			}
			Product created = productService.create(product);
			logger.info("✅ Producto creado con ID: {}", created.getId());
			return ResponseEntity.status(201).body(created);
		} catch (Exception e) {
			logger.error("❌ Error al crear producto: ", e);
			return ResponseEntity.status(500)
					.body("Error al crear el producto: " + e.getMessage());
		}
	}

	// Endpoint para crear producto con multipart (con imagen)
	@PostMapping(consumes = {"multipart/form-data"})
	public ResponseEntity<?> create(
			@RequestParam("name") String name,
			@RequestParam(value = "description", required = false) String description,
			@RequestParam("price") BigDecimal price,
			@RequestParam("productType") Product.ProductType productType,
			@RequestParam(value = "isActive", defaultValue = "true") boolean isActive,
			@RequestParam(value = "image", required = false) MultipartFile image) {
		
		try {
			logger.info("📦 Creando producto: {}", name);
			logger.info("🔍 DEBUG - image parameter: {}", image);
			logger.info("🔍 DEBUG - image == null: {}", (image == null));
			if (image != null) {
				logger.info("🔍 DEBUG - image.isEmpty(): {}", image.isEmpty());
				logger.info("🔍 DEBUG - image.getSize(): {}", image.getSize());
			}
			
			// Crear el producto
			Product product = new Product();
			product.setName(name);
			product.setDescription(description);
			product.setPrice(price);
			product.setProductType(productType);
			product.setActive(isActive);

			// Si se envió una imagen, subirla a Azure
			if (image != null && !image.isEmpty()) {
				logger.info("📸 Imagen recibida: {} - Tamaño: {} bytes - Tipo: {}", 
					image.getOriginalFilename(), 
					image.getSize(), 
					image.getContentType());
				
				if (!azureBlobStorageService.isValidImage(image)) {
					logger.error("❌ Imagen no válida: {}", image.getContentType());
					return ResponseEntity.badRequest()
							.body("El archivo debe ser una imagen válida (JPG, PNG, WEBP)");
				}
				
				logger.info("⬆️ Subiendo imagen a Azure Blob Storage...");
				String imageUrl = azureBlobStorageService.uploadImage(image);
				logger.info("✅ Imagen subida exitosamente: {}", imageUrl);
				product.setImageUrl(imageUrl);
			} else {
				logger.info("ℹ️ No se envió imagen (image is null or empty)");
			}

			// Guardar el producto
			Product created = productService.create(product);
			logger.info("✅ Producto creado con ID: {} - ImageURL: {}", created.getId(), created.getImageUrl());
			return ResponseEntity.status(201).body(created);

		} catch (IOException e) {
			logger.error("❌ Error de IO al subir imagen: ", e);
			return ResponseEntity.status(500)
					.body("Error al subir la imagen: " + e.getMessage());
		} catch (Exception e) {
			logger.error("❌ Error al crear producto: ", e);
			return ResponseEntity.status(500)
					.body("Error al crear el producto: " + e.getMessage());
		}
	}

	// Endpoint para actualizar producto con JSON (sin imagen)
	@PatchMapping(value = "/{id}", consumes = {"application/json"})
	public ResponseEntity<?> updateFromJson(@PathVariable Long id, @RequestBody Product product) {
		try {
			logger.info("📝 Actualizando producto {} desde JSON", id);
			// Si el JSON incluye una data URL para la imagen, subirla y reemplazar
			if (product.getImageUrl() != null && product.getImageUrl().startsWith("data:")) {
				logger.info("🔍 Se recibió imageUrl como data URL en update. Subiendo a Azure...");
				String imageUrl = azureBlobStorageService.uploadImageFromBase64(product.getImageUrl());
				product.setImageUrl(imageUrl);
				logger.info("✅ Data URL subida y reemplazada por: {}", imageUrl);
			}
			Product updated = productService.update(id, product).orElse(null);
			if (updated == null) {
				return ResponseEntity.notFound().build();
			}
			logger.info("✅ Producto actualizado: {}", updated.getId());
			return ResponseEntity.ok(updated);
		} catch (Exception e) {
			logger.error("❌ Error al actualizar producto: ", e);
			return ResponseEntity.status(500)
					.body("Error al actualizar el producto: " + e.getMessage());
		}
	}

	// Endpoint para actualizar producto con multipart (con imagen)
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
