package com.bskup.solarnews;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;


public class NewsStoryLoader extends AsyncTaskLoader<List<NewsStory>> {

    // Tag for log messages
    private static final String LOG_TAG = NewsStoryLoader.class.getName();

    // Store url passed in via constructor here
    private String mUrl;

    // Constructor with String Url as parameter
    public NewsStoryLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    public List<NewsStory> loadInBackground() {
        // Temp log message
        Log.v(LOG_TAG, "loadInBackground called from NewsStoryLoader class");

        // If no urls or first url is null, don't parse anything
        if (mUrl == null) {
            return null;
        }

        // Perform the HTTP request for Book data and process the response.
        List<NewsStory> newsStoryList = QueryUtils.extractNewsStories(mUrl);
        return newsStoryList;
    }

    @Override
    public void onCanceled(List<NewsStory> data) {
        super.onCanceled(data);
    }

    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();
    }

    @Override
    protected void onStartLoading() {
        // Temp log message
        Log.v(LOG_TAG, "onStartLoading called from NewsStoryLoader class");

        // Starts loading, ignores previously loaded data set and loads a new one
        // TODO: DeliverResult, if takeContentChanged, or some way to ensure this doesn't fire
        // TODO: automatically after backing out of child activity (like settings) to parent activity
        forceLoad();

    }
}
