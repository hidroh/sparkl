package io.github.hidroh.sparkl.data;

import android.content.SearchRecentSuggestionsProvider;

public class QueryRecentSuggestionsProvider extends SearchRecentSuggestionsProvider {
    public static final String PROVIDER_AUTHORITY = "io.github.hidroh.sparkl.recentprovider";
    public static final int MODE = DATABASE_MODE_QUERIES;

    public QueryRecentSuggestionsProvider() {
        setupSuggestions(PROVIDER_AUTHORITY, MODE);
    }
}
