package com.example.shopping.contract;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shopping.products.api.ProductResponse;
import com.example.shopping.products.api.ProductsController;
import com.example.shopping.products.domain.ProductQueryService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ProductsController.class)
class ProductsContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductQueryService productQueryService;

    @Test
    void shouldReturnProductList() throws Exception {
        ProductResponse product = new ProductResponse(UUID.randomUUID(), "SKU-1", "Phone", "Smart phone",
                new BigDecimal("10.00"), "USD", true);
        when(productQueryService.findAll()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU-1"));
    }
}

