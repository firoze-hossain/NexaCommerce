package com.roze.nexacommerce.common.address.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDataResponse {
    private List<String> dhakaMetroAreas;
    private List<String> dhakaSuburbanAreas;
    private List<String> otherCities;
    private Map<String, List<String>> cityAreas;

    @Data
    @Builder
    public static class ShippingRate {
        private String zone;
        private BigDecimal rate;
        private String deliveryTime;
    }

    private List<ShippingRate> shippingRates;
}