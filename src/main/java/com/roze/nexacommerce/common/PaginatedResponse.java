package com.roze.nexacommerce.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {
    private List<T> items;
    private Long totalItems;
    private int currentPage;
    private int pageSize;
    private int totalPages;

    public static <T> PaginatedResponse<T> of(List<T> items, long totalItems, int currentPage, int pageSize, int totalPages) {
        return PaginatedResponse.<T>builder()
                .items(items)
                .totalItems(totalItems)
                .currentPage(currentPage)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .build();
    }

    public static <T> PaginatedResponse<T> fromPage(Page<T> page) {
        return PaginatedResponse.<T>builder()
                .items(page.getContent())
                .totalItems(page.getTotalElements())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .build();
    }
}
