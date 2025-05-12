package com.farm.delivery.farmapi.controller;

import com.farm.delivery.farmapi.dto.ProductDTOs.*;
import com.farm.delivery.farmapi.service.FileStorageService;
import com.farm.delivery.farmapi.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;

    public ProductController(ProductService productService, FileStorageService fileStorageService) {
        this.productService = productService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDto>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        if (name != null && !name.isEmpty()) {
            return ResponseEntity.ok(productService.getProductsByName(name));
        } else if (minPrice != null && maxPrice != null) {
            return ResponseEntity.ok(productService.getProductsByPriceRange(minPrice, maxPrice));
        } else {
            return ResponseEntity.ok(productService.getAllProducts());
        }
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<ProductResponseDto>> getProductsByFarmerId(@PathVariable Long farmerId) {
        return ResponseEntity.ok(productService.getProductsByFarmerId(farmerId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('FARMER', 'ADMIN')")
    public ResponseEntity<ProductResponseDto> createProduct(@RequestBody ProductRequestDto productDto) {
        return ResponseEntity.ok(productService.createProduct(productDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FARMER', 'ADMIN')")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequestDto productDto) {
        return ResponseEntity.ok(productService.updateProduct(id, productDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('FARMER', 'ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/image")
    @PreAuthorize("hasAnyRole('FARMER', 'ADMIN')")
    public ResponseEntity<UpdateProductImageDto> uploadProductImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);
        String imageUrl = "/images/" + fileName;
        UpdateProductImageDto response = productService.updateProductImage(id, imageUrl);
        return ResponseEntity.ok(response);
    }
}
