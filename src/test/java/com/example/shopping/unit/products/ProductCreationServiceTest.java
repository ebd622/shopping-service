package com.example.shopping.unit.products;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.shopping.products.api.CreateProductRequest;
import com.example.shopping.products.api.ProductResponse;
import com.example.shopping.products.domain.ProductCreationService;
import com.example.shopping.products.infra.ProductEntity;
import com.example.shopping.products.infra.ProductRepository;
import com.example.shopping.shared.error.ConflictException;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductCreationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductCreationService productCreationService;

    @Test
    void shouldCreateProductWithDefaultCurrency() {
        when(productRepository.existsBySku("SKU-100")).thenReturn(false);
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productCreationService.create(new CreateProductRequest(
                "SKU-100",
                "Keyboard",
                new BigDecimal("25.00"),
                "Mechanical keyboard",
                null
        ));

        assertThat(response.sku()).isEqualTo("SKU-100");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.price()).isEqualByComparingTo("25.00");
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void shouldRejectDuplicateSku() {
        when(productRepository.existsBySku("SKU-100")).thenReturn(true);

        assertThatThrownBy(() -> productCreationService.create(new CreateProductRequest(
                "SKU-100",
                "Keyboard",
                new BigDecimal("25.00"),
                null,
                "USD"
        )))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("SKU-100");
    }
}

