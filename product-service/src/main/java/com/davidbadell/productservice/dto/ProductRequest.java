package com.davidbadell.productservice.dto;

import com.davidbadell.productservice.entity.Category;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must be less than 255 characters")
    private String name;
    
    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;
    
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;
    
    private String sku;
    
    private Category category;
    
    private String imageUrl;
}
