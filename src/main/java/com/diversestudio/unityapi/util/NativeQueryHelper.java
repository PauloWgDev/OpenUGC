package com.diversestudio.unityapi.util;


import com.diversestudio.unityapi.util.quearies.QueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

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
    public String getFindSingleContent() {
        return queryProvider.getFindSingleContent();
    }

    // User
    public String getFindAllUsers(){ return queryProvider.getFindAllUsers(); }

    // Rating
    public String getFindRatingsByContent(){ return queryProvider.getFindRatingsByContent();}

    public String getRatingDistributionByContent() {
        return queryProvider.getRatingDistributionByContent();
    }
}
