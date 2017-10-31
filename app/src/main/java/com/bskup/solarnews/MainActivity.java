package com.bskup.solarnews;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderCallbacks<List<NewsStory>> {

    // Tag for log messages
    public static final String LOG_TAG = MainActivity.class.getName();
    // Constant value for Loader ID
    private static final int NEWS_STORY_LOADER_ID = 1;
    // Constant request URL
    private static final String GUARDIAN_REQUEST_URL = "http://content.guardianapis.com/search";

    // News story adapter
    private NewsStoryAdapter mAdapter;
    // Swipe refresh layout
    private SwipeRefreshLayout mSwipeRefreshLayout;
    // Empty state TextView
    private TextView mEmptyStateTextView;
    // Empty state image view
    private ImageView mEmptyStateImageView;
    // Empty state image view and text linear layout
    private LinearLayout mEmptyStateLinearLayout;
    // List view
    private ListView mNewsStoryListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Theme change based on preference
        String themeName = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("theme", "default");
        Log.v(LOG_TAG, "value for theme String in mainactivity oncreate: " + themeName);
        if (themeName.equals("AppThemeLight")) {
            setTheme(R.style.AppThemeLight);
        } else if (themeName.equals("AppThemeDark")) {
            setTheme(R.style.AppThemeDark);
        }

        setContentView(R.layout.activity_main);

        // Find the ListView, find its empty view components and set empty view
        mNewsStoryListView = (ListView) findViewById(R.id.list);
        mEmptyStateLinearLayout = (LinearLayout) findViewById(R.id.empty_state_linear_layout);
        mEmptyStateImageView = (ImageView) findViewById(R.id.empty_state_image_view);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_state_text_view);
        mNewsStoryListView.setEmptyView(mEmptyStateLinearLayout);

        // Assign starting value to mAdapter
        mAdapter = new NewsStoryAdapter(this, new ArrayList<NewsStory>());

        // Find swipe refresh layout and set on refresh listener to handle swipe down refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Do stuff when user swipes down to refresh
                Log.v(LOG_TAG, "onRefresh called from SwipeRefreshLayout");

                restartNewsLoader();
            }
        });

        // Check network connection before initializing loader which
        // will attempt to connect to network to get Google Books data
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            // Start a Loader
            getLoaderManager().initLoader(NEWS_STORY_LOADER_ID, null, this);
            // Temp log message
            Log.v(LOG_TAG, "initLoader called, Network check OK, uriBuilder query string: " + getUriStringWithUpdatedPreferences());
        } else {
            // Display network error (hide loading indicator and change empty state text)
            mSwipeRefreshLayout.setRefreshing(false);
            mEmptyStateTextView.setVisibility(View.VISIBLE);
            mEmptyStateTextView.setText(R.string.no_internet_connection);
            mEmptyStateImageView.setVisibility(View.VISIBLE);
            mEmptyStateImageView.setImageResource(R.drawable.ic_no_internet);
        }
    }

    // Show empty state layout
    public void showEmptyStateLayout() {
        mEmptyStateLinearLayout.setVisibility(View.VISIBLE);
        mEmptyStateImageView.setVisibility(View.VISIBLE);
        mEmptyStateTextView.setVisibility(View.VISIBLE);
    }

    // Hide empty state layout
    public void hideEmptyStateLayout() {
        mEmptyStateLinearLayout.setVisibility(View.GONE);
        mEmptyStateImageView.setVisibility(View.GONE);
        mEmptyStateTextView.setVisibility(View.GONE);
    }

    @Override
    public Loader<List<NewsStory>> onCreateLoader(int id, Bundle args) {
        // Temp log message
        Log.v(LOG_TAG, "onCreateLoader called, uriBuilder query string: " + getUriStringWithUpdatedPreferences());

        // Testing set refreshing true here so we can use this loading animation
        // instead of a progress bar
        mSwipeRefreshLayout.setRefreshing(true);

        // Note: When importing support loader manager instead, this was incompatible type
        return new NewsStoryLoader(this, getUriStringWithUpdatedPreferences());
    }

    // Get Uri String with updated preferences
    public String getUriStringWithUpdatedPreferences() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String pageSize = sharedPrefs.getString(
                getString(R.string.settings_page_size_key),
                getString(R.string.settings_page_size_default));

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );
        Uri baseUri = Uri.parse(GUARDIAN_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("q", "solar");
        uriBuilder.appendQueryParameter("page-size", pageSize);
        uriBuilder.appendQueryParameter("order-by", orderBy);
        uriBuilder.appendQueryParameter("api-key", "test");

        return uriBuilder.toString();
    }

    @Override
    public void onLoadFinished(Loader<List<NewsStory>> loader, List<NewsStory> newsStoryList) {
        // Temp log message
        Log.v(LOG_TAG, "onLoadFinished called");

        // Set swipeRefreshLayout refreshing animation to false
        mSwipeRefreshLayout.setRefreshing(false);

        // Clear adapter data
        mAdapter.clear();

        // Do same thing we did in onPostExecute before switching to Loaders
        // If there's no result, do nothing and inform user
        if (newsStoryList != null && !newsStoryList.isEmpty()) {
            hideEmptyStateLayout();
            mNewsStoryListView.setVisibility(View.VISIBLE);
            updateUi(newsStoryList);
        } else if (newsStoryList != null && newsStoryList.isEmpty()){
            // Set empty state text view and image view to indicate no results
            mEmptyStateTextView.setText(R.string.no_results_found);
            mEmptyStateImageView.setImageResource(R.drawable.ic_sad_face);
            showEmptyStateLayout();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsStory>> loader) {
        // Temp log message
        Log.v(LOG_TAG, "onLoaderReset called");

        // Clear out existing News data
        mAdapter.clear();
    }

    // Update the UI with the given NewsStory information
    private void updateUi(final List<NewsStory> newsStoryList) {

        // Add data in newsStoryList to our adapter
        // Note: Comment this out to test empty state
        mAdapter.addAll(newsStoryList);

        // Find the ListView
        mNewsStoryListView = (ListView) findViewById(R.id.list);

        // Set the adapter on the ListView
        // so the list can be populated in the ui
        mNewsStoryListView.setAdapter(mAdapter);
        // Make list view items do stuff when clicked
        mNewsStoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Do this stuff when list view item clicked
                // Get current NewsStory object
                NewsStory currentNewsStory = newsStoryList.get(position);

                // Get web url from current news story
                String currentNewsStoryWebUrl = currentNewsStory.getWebUrl();
                // Parse url string to uri
                Uri webUrlUri = Uri.parse(currentNewsStoryWebUrl);
                // Open uri in browser with intent
                // Create intent to open url converted to uri
                Intent newsStoryIntent = new Intent(Intent.ACTION_VIEW, webUrlUri);
                // If there's an app available that can open the url, do it
                if (newsStoryIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(newsStoryIntent);
                }
            }
        });
    }

    // Restart the loader
    public void restartNewsLoader() {

        // Check network connection before initializing loader which
        // will attempt to connect to network to get Guardian data
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            // Hide empty state views first
            mEmptyStateTextView.setVisibility(View.GONE);
            mEmptyStateImageView.setVisibility(View.GONE);
            // Restart Loader
            getLoaderManager().restartLoader(NEWS_STORY_LOADER_ID, null, this);
            // Play the refreshing animation since we'll be attempting to fetch data
            mSwipeRefreshLayout.setRefreshing(true);
            // Temp log message
            Log.v(LOG_TAG, "restartLoader called from restartNewsLoader");

        } else {
            // Display network error (hide refreshing indicator and change empty state text)
            mNewsStoryListView.setVisibility(View.GONE);
            mSwipeRefreshLayout.setRefreshing(false);
            mEmptyStateLinearLayout.setVisibility(View.VISIBLE);
            mEmptyStateTextView.setVisibility(View.VISIBLE);
            mEmptyStateTextView.setText(R.string.no_internet_connection);
            mEmptyStateImageView.setVisibility(View.VISIBLE);
            mEmptyStateImageView.setImageResource(R.drawable.ic_no_internet);
            Log.v(LOG_TAG, "aosdinoliahsfg");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "onResume called");
        Log.v(LOG_TAG, "NewsPrefFragment.mPreferencesChanged: " + SettingsActivity.NewsPreferenceFragment.mPreferencesChanged);
        if (SettingsActivity.NewsPreferenceFragment.mPreferencesChanged != null) {
            if (SettingsActivity.NewsPreferenceFragment.mPreferencesChanged) {
                restartNewsLoader();
                recreate();
                Log.v(LOG_TAG, "recreate() called from onResume, setting mPreferencesChanged to false");
                SettingsActivity.NewsPreferenceFragment.mPreferencesChanged = false;
            }
        }

    }
}
