package com.marcosdias.miniifood.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marcosdias.miniifood.product.domain.Product;
import com.marcosdias.miniifood.product.repository.ProductRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProductWhenNameIsAvailable() {
        Product input = Product.builder()
            .name("Burger")
            .description("Delicious burger")
            .price(BigDecimal.valueOf(25.90))
            .quantityAvailable(100)
            .build();

        when(productRepository.existsByNameIgnoreCase("Burger")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(input);

        Product created = productService.create(input);

        assertEquals("Burger", created.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowExceptionWhenProductNameAlreadyExists() {
        Product input = Product.builder()
            .name("Burger")
            .build();

        when(productRepository.existsByNameIgnoreCase("Burger")).thenReturn(true);

        assertThrows(ProductAlreadyExistsException.class, () -> productService.create(input));
    }

    @Test
    void shouldUpdateProductSuccessfully() {
        Product existing = Product.builder()
            .id(1L)
            .name("Burger")
            .description("Old description")
            .price(BigDecimal.valueOf(25.90))
            .quantityAvailable(100)
            .build();

        Product input = Product.builder()
            .name("Premium Burger")
            .description("New description")
            .price(BigDecimal.valueOf(35.90))
            .quantityAvailable(50)
            .build();

        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(existing));
        when(productRepository.existsByNameIgnoreCase("Premium Burger")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.update(1L, input);

        assertEquals("Premium Burger", updated.getName());
        assertEquals("New description", updated.getDescription());
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.findById(999L));
    }

    @Test
    void shouldThrowWhenUpdatingToDuplicatedName() {
        Product existing = Product.builder()
            .id(1L)
            .name("Burger")
            .description("Old description")
            .price(BigDecimal.valueOf(25.90))
            .quantityAvailable(100)
            .build();

        Product input = Product.builder()
            .name("Pizza")
            .description("New description")
            .price(BigDecimal.valueOf(35.90))
            .quantityAvailable(50)
            .build();

        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(existing));
        when(productRepository.existsByNameIgnoreCase("Pizza")).thenReturn(true);

        assertThrows(ProductAlreadyExistsException.class, () -> productService.update(1L, input));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldDeleteExistingProduct() {
        Product existing = Product.builder().id(1L).name("Burger").build();
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(existing));

        productService.delete(1L);

        verify(productRepository).delete(existing);
    }

    @Test
    void shouldThrowWhenDeletingNonExistingProduct() {
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.delete(1L));
    }
}

