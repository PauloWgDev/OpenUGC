package com.diversestudio.openugc.service;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TagNamesTest {

    @Test
    void trimsLowercasesAndKeepsFirstOccurrence() {
        assertThat(ContentService.normalizeTagNames(List.of("Racing", " racing ", "PUZZLE", "racing")))
                .containsExactly("racing", "puzzle");
    }

    @Test
    void dropsNullAndBlankNames() {
        assertThat(ContentService.normalizeTagNames(Arrays.asList("a", null, "   ", "")))
                .containsExactly("a");
    }

    @Test
    void nullOrEmptyInput_givesEmptyList() {
        assertThat(ContentService.normalizeTagNames(null)).isEmpty();
        assertThat(ContentService.normalizeTagNames(List.of())).isEmpty();
    }

    @Test
    void lowercasingDoesNotDependOnTheServerLocale() {
        // With a Turkish default locale, "I".toLowerCase() would give a dotless "ı".
        assertThat(ContentService.normalizeTagNames(List.of("INDIE"))).containsExactly("indie");
    }
}
