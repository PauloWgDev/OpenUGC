package com.diversestudio.unityapi.util;


import com.diversestudio.unityapi.util.quearies.QueryProvider;
import org.springframework.beans.factory.annotation.Autowired;
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


    public String getFindAllContents() {
        return queryProvider.getFindAllContent();
    }

    public String getFindSingleContent() {
        return queryProvider.getFindSingleContent();
    }
}
