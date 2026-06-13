package com.retailcore.pos.product;

import com.retailcore.pos.product.dto.ProductActiveRequest;
import com.retailcore.pos.product.dto.ProductCreateRequest;
import com.retailcore.pos.product.dto.ProductResponse;
import com.retailcore.pos.product.dto.ProductUpdateRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Products", description = "Product catalog management endpoints")
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Create product", description = "Creates a product with unique SKU and optional unique barcode.")
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        ProductResponse response = productService.create(request);
        return ResponseEntity.created(URI.create("/api/products/" + response.id())).body(response);
    }

    @Operation(summary = "List products", description = "Lists all products.")
    @GetMapping
    public ResponseEntity<List<ProductResponse>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @Operation(summary = "Get product", description = "Returns one product by id.")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @Operation(summary = "Update product", description = "Updates product details, category, price, and active status.")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @Operation(summary = "Change product active status", description = "Enables or disables a product.")
    @PatchMapping("/{id}/active")
    public ResponseEntity<ProductResponse> changeActive(
            @PathVariable Long id,
            @Valid @RequestBody ProductActiveRequest request
    ) {
        return ResponseEntity.ok(productService.changeActive(id, request));
    }
}

