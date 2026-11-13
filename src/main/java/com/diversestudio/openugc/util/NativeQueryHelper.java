package com.diversestudio.openugc.util;


import com.diversestudio.openugc.exception.InvalidRequestException;
import com.diversestudio.openugc.util.queries.QueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Map;

// This script should contain all the native queries in order to easily manage
// the most complex queries
@Component
public class NativeQueryHelper {
    private final QueryProvider queryProvider;
    @Autowired
    public NativeQueryHelper(QueryProvider queryProvider) {
        this.queryProvider = queryProvider;
    }

    /**
     * Converts a string representation of sort parameters into a Spring Data {@link Sort} object.
     *
     * @param s the sort string in the format "property,direction" (e.g., "createdAt,desc")
     * @return a {@link Sort} object based on the given input; defaults to ascending if direction is not specified
     */
    public Sort StringToSort(String s) {
        String[] sortParams = s.split(",");
        String property = sortParams[0];

        if (sortParams.length == 2 && "desc".equalsIgnoreCase(sortParams[1])) {
            return Sort.by(property).descending();
        }
        return Sort.by(property).ascending();
    }

    /**
     * Resolves a client-supplied sort field to a safe, known column name.
     * <p>
     * Only fields present in {@code allowed} are accepted; anything else is rejected
     * before it can reach the SQL string. This is what prevents SQL injection through
     * the {@code sort} parameter: the column placed in ORDER BY can now only ever be
     * one of a fixed set of literals defined in code.
     *
     * @param allowed  map of accepted API field name -> real column/alias
     * @param property the field name requested by the client
     * @return the safe column/alias to place in the ORDER BY clause
     * @throws InvalidRequestException if the field is not whitelisted (maps to HTTP 400)
     */
    public String resolveSortColumn(Map<String, String> allowed, String property) {
        String column = allowed.get(property);
        if (column == null) {
            throw new InvalidRequestException(
                    "Invalid sort field: '" + property + "'. Allowed values: " + allowed.keySet());
        }
        return column;
    }

    // query building helpers
    public String getWhereFilter(String columnName) {
        return queryProvider.getWhereFilter(columnName);
    }
    public String getOrderBySimilarity(String columnName) {
        return queryProvider.getOrderBySimilarity(columnName);
    }
    public String getFindAllContentGroupBy() {return queryProvider.getFindAllContentGroupBy(); }

    // Content
    public String getFindAllContents() { return queryProvider.getFindAllContent(); }
    public String getCountContent() { return queryProvider.getCountContent(); }
    public String getFindSingleContent() {
        return queryProvider.getFindSingleContent();
    }

    // User
    public String getFindAllUsers(){ return queryProvider.getFindAllUsers(); }
    public String getCountUsers(){ return queryProvider.getCountUsers(); }

    // Rating
    public String getFindRatingsByContent(){ return queryProvider.getFindRatingsByContent();}
    public String getCountRatingsByContent(){ return queryProvider.getCountRatingsByContent();}

    public String getRatingDistributionByContent() {
        return queryProvider.getRatingDistributionByContent();
    }
}
