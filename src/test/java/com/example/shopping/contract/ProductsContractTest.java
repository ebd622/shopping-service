package com.example.shopping.contract;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shopping.products.api.ProductResponse;
import com.example.shopping.products.api.ProductsController;
import com.example.shopping.products.domain.ProductCreationService;
import com.example.shopping.products.domain.ProductQueryService;
import com.example.shopping.shared.error.ConflictException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ProductsController.class)
class ProductsContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductQueryService productQueryService;

    @MockitoBean
    private ProductCreationService productCreationService;

    @Test
    void shouldReturnProductList() throws Exception {
        ProductResponse product = new ProductResponse(UUID.randomUUID(), "SKU-1", "Phone", "Smart phone",
                new BigDecimal("10.00"), "USD", true);
        when(productQueryService.findAll()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU-1"));
    }

    @Test
    void shouldCreateProduct() throws Exception {
        ProductResponse response = new ProductResponse(UUID.randomUUID(), "SKU-10", "Keyboard", "Mechanical",
                new BigDecimal("25.00"), "USD", true);
        when(productCreationService.create(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SKU-10\",\"name\":\"Keyboard\",\"price\":25.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("SKU-10"));
    }

    @Test
    void shouldReturnValidationErrorWhenRequiredFieldMissing() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SKU-11\",\"name\":\"Mouse\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnConflictWhenSkuExists() throws Exception {
        when(productCreationService.create(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new ConflictException("Product already exists for sku: SKU-10"));

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"SKU-10\",\"name\":\"Keyboard\",\"price\":25.00}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }
}

