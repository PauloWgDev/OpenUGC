package com.diversestudio.openugc.util;

import com.diversestudio.openugc.config.PaginationProperties;
import com.diversestudio.openugc.exception.InvalidRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Turns the {@code page}, {@code size} and {@code sort} query parameters of the list endpoints
 * into a {@link Pageable}, rejecting bad values with 400 instead of letting them reach the query.
 */
@Component
public class PageRequests {

    private final PaginationProperties paginationProperties;
    private final NativeQueryHelper nativeQueryHelper;

    public PageRequests(PaginationProperties paginationProperties, NativeQueryHelper nativeQueryHelper) {
        this.paginationProperties = paginationProperties;
        this.nativeQueryHelper = nativeQueryHelper;
    }

    /**
     * @throws InvalidRequestException (400) if {@code page} is negative, {@code size} is outside
     *                                 1..{@code pagination.max-page-size}, or {@code sort} is empty
     */
    public Pageable of(int page, int size, String sort) {
        int maxPageSize = paginationProperties.getMaxPageSize();
        if (page < 0) {
            throw new InvalidRequestException("page must be 0 or more, got " + page);
        }
        if (size < 1 || size > maxPageSize) {
            throw new InvalidRequestException("size must be between 1 and " + maxPageSize + ", got " + size);
        }
        if (sort == null || sort.isBlank() || sort.split(",")[0].isBlank()) {
            throw new InvalidRequestException("sort must be in the format field,direction (e.g. createdAt,desc)");
        }
        return PageRequest.of(page, size, nativeQueryHelper.StringToSort(sort.trim()));
    }
}
