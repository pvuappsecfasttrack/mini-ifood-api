package com.marcosdias.miniifood.product.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ProductPageResponse(
    @Schema(description = "Products in the current page")
    List<ProductResponse> content,

    @Schema(description = "Current page number", example = "0")
    int page,

    @Schema(description = "Page size", example = "10")
    int size,

    @Schema(description = "Total number of products", example = "25")
    long totalElements,

    @Schema(description = "Total number of pages", example = "3")
    int totalPages,

    @Schema(description = "Whether this is the first page")
    boolean first,

    @Schema(description = "Whether this is the last page")
    boolean last,

    @Schema(description = "Whether the page is empty")
    boolean empty
) {
}

