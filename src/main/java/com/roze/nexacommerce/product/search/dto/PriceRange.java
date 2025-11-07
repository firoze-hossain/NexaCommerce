package com.roze.nexacommerce.product.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceRange {
    private BigDecimal min;
    private BigDecimal max;
    private BigDecimal selectedMin;
    private BigDecimal selectedMax;
}