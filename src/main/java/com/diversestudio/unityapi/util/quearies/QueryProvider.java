package com.diversestudio.unityapi.util.quearies;


public interface QueryProvider {

    String getWhereFilter(String columnName);
    String getOrderBySimilarity(String columnName);

    // Content
    String getFindAllContent();
    String getFindSingleContent();



    // User
    String getFindAllUsers();
}
