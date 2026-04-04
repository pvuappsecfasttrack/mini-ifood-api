package com.marcosdias.miniifood.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.reset;

import com.marcosdias.miniifood.product.domain.Product;
import com.marcosdias.miniifood.product.ProductCacheConstants;
import com.marcosdias.miniifood.product.repository.ProductRepository;
import com.marcosdias.miniifood.product.web.dto.ProductPageResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProductService Cache Tests")
class ProductServiceCacheIntegrationTest {

    @Autowired
    private ProductService productService;

    @MockitoSpyBean
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        var productsCache = cacheManager.getCache(ProductCacheConstants.PRODUCTS_CACHE);
        if (productsCache != null) {
            productsCache.clear();
        }
        productRepository.deleteAll();
        reset(productRepository);
    }

    @Test
    @DisplayName("Should cache product listings for the same pageable")
    void shouldCacheProductListing() {
        Product product = Product.builder()
                .name("Cached Burger")
                .description("Burger used for cache test")
                .price(BigDecimal.valueOf(29.90))
                .quantityAvailable(12)
                .build();
        productRepository.save(product);
        reset(productRepository);

        Pageable pageable = PageRequest.of(0, 10);

        ProductPageResponse firstCall = productService.findAll(pageable);
        ProductPageResponse secondCall = productService.findAll(pageable);

        assertThat(firstCall.content()).hasSize(1);
        assertThat(secondCall.content()).hasSize(1);
        verify(productRepository, times(1)).findAll(pageable);
    }
}



