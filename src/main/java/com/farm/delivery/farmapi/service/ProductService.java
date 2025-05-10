package com.farm.delivery.farmapi.service;

import com.farm.delivery.farmapi.dto.ProductDTOs.*;
import com.farm.delivery.farmapi.model.Product;
import com.farm.delivery.farmapi.model.User;
import com.farm.delivery.farmapi.repository.ProductRepository;
import com.farm.delivery.farmapi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public ProductService(
            ProductRepository productRepository,
            UserRepository userRepository,
            UserService userService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public List<ProductResponseDto> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToProductResponseDto)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDto> getProductsByFarmerId(Long farmerId) {
        return productRepository.findByOwnerId(farmerId).stream()
                .map(this::convertToProductResponseDto)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDto> getProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToProductResponseDto)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }
        return productRepository.findByPriceBetween(minPrice, maxPrice).stream()
                .map(this::convertToProductResponseDto)
                .collect(Collectors.toList());
    }

    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return convertToProductResponseDto(product);
    }

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto productDto) {
        logger.debug("Creating new product: {}", productDto.getName());
        
        User currentUser = userService.getCurrentUser();

        if (currentUser.getRole() != User.Role.FARMER && currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Only farmers and admins can create products");
        }

        // Validate product data
        validateProductData(productDto);

        // Check for duplicate product name for the same farmer
        if (productRepository.existsByNameAndOwnerId(productDto.getName(), currentUser.getId())) {
            throw new RuntimeException("You already have a product with this name");
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setAvailableQuantity(productDto.getAvailableQuantity());
        product.setOwner(currentUser);  // This sets both owner and farmer_id

        Product savedProduct = productRepository.save(product);
        logger.info("Product created successfully with ID: {}", savedProduct.getId());
        
        return convertToProductResponseDto(savedProduct);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductRequestDto productDto) {
        logger.debug("Updating product with ID: {}", id);
        
        User currentUser = userService.getCurrentUser();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Check if the current user is the owner or an admin
        if (!product.getOwner().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("You can only update your own products");
        }

        // Validate product data
        validateProductData(productDto);

        // Check for duplicate product name (excluding current product)
        if (!product.getName().equals(productDto.getName()) &&
                productRepository.existsByNameAndOwnerId(productDto.getName(), currentUser.getId())) {
            throw new RuntimeException("You already have a product with this name");
        }

        product.setName(productDto.getName());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setAvailableQuantity(productDto.getAvailableQuantity());

        Product updatedProduct = productRepository.save(product);
        logger.info("Product updated successfully with ID: {}", updatedProduct.getId());
        
        return convertToProductResponseDto(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        logger.debug("Deleting product with ID: {}", id);
        
        User currentUser = userService.getCurrentUser();
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Check if the current user is the owner or an admin
        if (!product.getOwner().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("You can only delete your own products");
        }

        productRepository.deleteById(id);
        logger.info("Product deleted successfully with ID: {}", id);
    }

    private void validateProductData(ProductRequestDto productDto) {
        if (productDto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if (productDto.getAvailableQuantity() < 0) {
            throw new IllegalArgumentException("Available quantity cannot be negative");
        }
        if (productDto.getName() == null || productDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (productDto.getDescription() == null || productDto.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Product description is required");
        }
    }

    private ProductResponseDto convertToProductResponseDto(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getOwner().getId(),
                product.getOwner().getName(),
                product.getAvailableQuantity());
    }
}
