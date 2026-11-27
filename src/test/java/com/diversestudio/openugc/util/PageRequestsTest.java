package com.diversestudio.openugc.util;

import com.diversestudio.openugc.config.PaginationProperties;
import com.diversestudio.openugc.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** {@code pagination.max-page-size} decides the largest page; bad values are 400. */
class PageRequestsTest {

    private static PageRequests withMax(int maxPageSize) {
        PaginationProperties properties = new PaginationProperties();
        properties.setMaxPageSize(maxPageSize);
        // The query provider isn't used to parse the sort.
        return new PageRequests(properties, new NativeQueryHelper(null));
    }

    @Test
    void defaultMax_is100() {
        assertThat(new PaginationProperties().getMaxPageSize()).isEqualTo(100);
    }

    @Test
    void validRequest_becomesPageable() {
        Pageable pageable = withMax(100).of(2, 20, "createdAt,desc");

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void configuredMax_isTheLimit() {
        PageRequests pageRequests = withMax(5);

        assertThat(pageRequests.of(0, 5, "name,asc").getPageSize()).isEqualTo(5);
        assertThatThrownBy(() -> pageRequests.of(0, 6, "name,asc"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("between 1 and 5");
    }

    @Test
    void badValues_areRejected() {
        PageRequests pageRequests = withMax(100);

        assertThatThrownBy(() -> pageRequests.of(0, 0, "name,asc")).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> pageRequests.of(-1, 10, "name,asc")).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> pageRequests.of(0, 10, "")).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> pageRequests.of(0, 10, " ,desc")).isInstanceOf(InvalidRequestException.class);
    }
}
