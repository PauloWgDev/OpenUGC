package com.diversestudio.unityapi.util.quearies;


public interface QueryProvider {
    String getFindAllContent();
    String getFindSingleContent();
    String getWhereFilter();
    String getOrderBySimilarity();
}
