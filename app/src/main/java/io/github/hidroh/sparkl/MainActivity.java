package io.github.hidroh.sparkl;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import io.github.hidroh.sparkl.data.Photo;
import io.github.hidroh.sparkl.data.PhotoManager;
import io.github.hidroh.sparkl.data.QueryRecentSuggestionsProvider;

public class MainActivity extends AppCompatActivity {

    private static final int GRID_COLUMNS = 3;
    private final PhotoManager.Observer mObserver = new PhotoManager.Observer() {
        @Override
        public void onStart() {
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        public void onComplete(@NonNull List<Photo> results) {
            mProgress.setVisibility(View.GONE);
            if (results.isEmpty()) {
                Toast.makeText(MainActivity.this, R.string.no_results, Toast.LENGTH_SHORT).show();
            }
        }
    };
    private RecyclerView mRecyclerView;
    private View mProgress;
    private View mEmpty;
    private GridAdapter mAdapter;
    private PhotoManager mPhotoManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgress = findViewById(android.R.id.progress);
        mEmpty = findViewById(android.R.id.empty);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, GRID_COLUMNS));
        Application application = (Application) getApplication();
        mPhotoManager = application.createPhotoManager();
        mAdapter = new GridAdapter(getLayoutInflater(),
                mPhotoManager,
                application.createPhotoUtil());
        mRecyclerView.setAdapter(mAdapter);
        mPhotoManager.subscribe(mObserver);
        if (savedInstanceState == null) {
            handleIntent(getIntent());
        } else {
            mAdapter.restoreState(savedInstanceState);
            if (!TextUtils.isEmpty(mAdapter.getQuery())) {
                setTitle(mAdapter.getQuery());
                mEmpty.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
        supportInvalidateOptionsMenu();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.saveState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem menuSearch = menu.findItem(R.id.menu_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuSearch);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(
                new ComponentName(this, MainActivity.class)));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));
        searchView.setQuery("", false);
        searchView.clearFocus();
        searchView.setIconified(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPhotoManager.unsubscribe(mObserver);
        mRecyclerView.setAdapter(null); // force detaching existing adapter
    }

    private void handleIntent(Intent intent) {
        if (TextUtils.equals(intent.getAction(), Intent.ACTION_SEARCH)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (!TextUtils.isEmpty(query)) {
                mEmpty.setVisibility(View.GONE);
                saveRecent(query);
                mAdapter.setQuery(query);
                setTitle(query);
            }
        }
    }

    private void saveRecent(String query) {
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                QueryRecentSuggestionsProvider.PROVIDER_AUTHORITY,
                QueryRecentSuggestionsProvider.MODE);
        suggestions.saveRecentQuery(query, null);
    }
}
