package com.davidbadell.productservice.service;

import com.davidbadell.productservice.dto.ProductRequest;
import com.davidbadell.productservice.dto.ProductResponse;
import com.davidbadell.productservice.entity.Category;
import com.davidbadell.productservice.entity.Product;
import com.davidbadell.productservice.repository.ProductRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .sku(request.getSku())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .active(true)
                .build();
        
        Product savedProduct = productRepository.save(product);
        log.info("Product created with id: {}", savedProduct.getId());
        return mapToResponse(savedProduct);
    }
    
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductByIdFallback")
    @Retry(name = "productService")
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        return mapToResponse(product);
    }
    
    public ProductResponse getProductByIdFallback(Long id, Exception e) {
        log.error("Fallback triggered for getProductById with id: {}", id, e);
        return ProductResponse.builder()
                .id(id)
                .name("Service Unavailable")
                .description("Product service is temporarily unavailable")
                .build();
    }
    
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(this::mapToResponse);
    }
    
    public List<ProductResponse> getProductsByCategory(Category category) {
        return productRepository.findByCategory(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());
        
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated with id: {}", updatedProduct.getId());
        return mapToResponse(updatedProduct);
    }
    
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        product.setActive(false);
        productRepository.save(product);
        log.info("Product soft deleted with id: {}", id);
    }
    
    @Transactional
    public void updateStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);
        log.info("Stock updated for product id: {}, new quantity: {}", id, product.getStockQuantity());
    }
    
    @Transactional
    public boolean reduceStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        if (product.getStockQuantity() < quantity) {
            log.warn("Insufficient stock for product id: {}", id);
            return false;
        }
        
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);
        log.info("Stock reduced for product id: {}, new quantity: {}", id, product.getStockQuantity());
        return true;
    }
    
    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .sku(product.getSku())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
