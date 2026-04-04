package com.marcosdias.miniifood.product.service;

import com.marcosdias.miniifood.product.domain.Product;
import com.marcosdias.miniifood.product.ProductCacheConstants;
import com.marcosdias.miniifood.product.repository.ProductRepository;
import com.marcosdias.miniifood.product.web.ProductMapper;
import com.marcosdias.miniifood.product.web.dto.ProductPageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@CacheConfig(cacheNames = ProductCacheConstants.PRODUCTS_CACHE)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public ProductPageResponse findAll(Pageable pageable) {
        log.debug("Finding all products with pageable: {}", pageable);
        Page<Product> page = productRepository.findAll(pageable);

        return new ProductPageResponse(
            page.getContent().stream().map(productMapper::toResponse).toList(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.isEmpty()
        );
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        log.debug("Finding product with id: {}", id);
        return productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public Product create(Product product) {
        if (productRepository.existsByNameIgnoreCase(product.getName())) {
            log.warn("Product name already exists: {}", product.getName());
            throw new ProductAlreadyExistsException("Product with name '" + product.getName() + "' already exists");
        }

        log.info("Creating product: {}", product.getName());
        return productRepository.save(product);
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public Product update(Long id, Product productData) {
        log.debug("Updating product with id: {}", id);
        Product product = findById(id);

        if (!product.getName().equalsIgnoreCase(productData.getName()) &&
            productRepository.existsByNameIgnoreCase(productData.getName())) {
            log.warn("Product name already exists: {}", productData.getName());
            throw new ProductAlreadyExistsException("Product with name '" + productData.getName() + "' already exists");
        }

        product.setName(productData.getName());
        product.setDescription(productData.getDescription());
        product.setPrice(productData.getPrice());
        product.setQuantityAvailable(productData.getQuantityAvailable());

        log.info("Product updated: {}", id);
        return productRepository.save(product);
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public void delete(Long id) {
        log.debug("Deleting product with id: {}", id);
        Product product = findById(id);
        productRepository.delete(product);
        log.info("Product deleted: {}", id);
    }
}

