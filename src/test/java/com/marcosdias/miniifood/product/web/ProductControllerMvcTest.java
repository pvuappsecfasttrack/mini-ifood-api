package com.marcosdias.miniifood.product.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcosdias.miniifood.product.domain.Product;
import com.marcosdias.miniifood.product.service.ProductService;
import com.marcosdias.miniifood.security.JwtAuthenticationFilter;
import com.marcosdias.miniifood.product.web.dto.CreateProductRequest;
import com.marcosdias.miniifood.product.web.dto.ProductPageResponse;
import com.marcosdias.miniifood.product.web.dto.ProductResponse;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = ProductController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
    },
    excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private ProductMapper productMapper;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    void shouldListProductsWithPagination() throws Exception {
        ProductResponse item = new ProductResponse(
            1L,
            "Burger",
            "Delicious burger",
            BigDecimal.valueOf(25.90),
            10,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );
        ProductPageResponse response = new ProductPageResponse(List.of(item), 0, 10, 1, 1, true, true, false);

        when(productService.findAll(any())).thenReturn(response);

        mockMvc.perform(get("/api/products?page=0&size=10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(1L))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.empty").value(false));
    }

    @Test
    void shouldCreateProduct() throws Exception {
        CreateProductRequest request = new CreateProductRequest("Burger", "Delicious burger", BigDecimal.valueOf(25.90), 10);
        Product input = Product.builder().name("Burger").description("Delicious burger").price(BigDecimal.valueOf(25.90)).quantityAvailable(10).build();
        Product created = Product.builder().id(1L).name("Burger").description("Delicious burger").price(BigDecimal.valueOf(25.90)).quantityAvailable(10).build();
        ProductResponse response = new ProductResponse(1L, "Burger", "Delicious burger", BigDecimal.valueOf(25.90), 10, OffsetDateTime.now(), OffsetDateTime.now());

        when(productMapper.toEntity(any(CreateProductRequest.class))).thenReturn(input);
        when(productService.create(any(Product.class))).thenReturn(created);
        when(productMapper.toResponse(eq(created))).thenReturn(response);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/api/products/1"))
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Burger"));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        CreateProductRequest request = new CreateProductRequest("Updated Burger", "New desc", BigDecimal.valueOf(29.90), 20);
        Product input = Product.builder().name("Updated Burger").description("New desc").price(BigDecimal.valueOf(29.90)).quantityAvailable(20).build();
        Product updated = Product.builder().id(1L).name("Updated Burger").description("New desc").price(BigDecimal.valueOf(29.90)).quantityAvailable(20).build();
        ProductResponse response = new ProductResponse(1L, "Updated Burger", "New desc", BigDecimal.valueOf(29.90), 20, OffsetDateTime.now(), OffsetDateTime.now());

        when(productMapper.toEntity(any(CreateProductRequest.class))).thenReturn(input);
        when(productService.update(eq(1L), any(Product.class))).thenReturn(updated);
        when(productMapper.toResponse(eq(updated))).thenReturn(response);

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Updated Burger"));
    }
}

