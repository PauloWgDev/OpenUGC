package com.diversestudio.openugc.util.queries;

public interface QueryProvider {

    String getWhereFilter(String columnName);
    String getOrderBySimilarity(String columnName);
    // Content
    String getFindAllContent();
    String getFindSingleContent();
    String getFindAllContentGroupBy();
    String getCountContent();

    // User
    String getFindAllUsers();
    String getCountUsers();

    // Rating
    String getFindRatingsByContent();
    String getCountRatingsByContent();
    String getRatingDistributionByContent();
}
