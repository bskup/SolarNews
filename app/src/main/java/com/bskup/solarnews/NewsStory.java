package com.bskup.solarnews;

import android.net.Uri;

/**
 * Created on 12/7/2016.
 */

public class NewsStory {

    // NewsStory section name
    private String mSectionName;
    // NewsStory web title
    private String mWebTitle;
    // NewsStory publication date String
    private String mOutputDateString;
    // NewsStory publication time String
    private String mOutputTimeString;
    // NewsStory web url address String
    private String mWebUrl;

    // Constructor method
    public NewsStory (String sectionName, String webTitle, String outputDateString, String outputTimeString, String webUrl) {
        mSectionName = sectionName;
        mWebTitle = webTitle;
        mOutputDateString = outputDateString;
        mOutputTimeString = outputTimeString;
        mWebUrl = webUrl;
    }

    // Getter for section name
    public String getSectionName() { return mSectionName; }

    // Getter for web title
    public String getWebTitle() {
        return mWebTitle;
    }

    // Getter for publication date String
    public String getOutputDateString() { return mOutputDateString; }

    // Getter for publication time String
    public String getOutputTimeString() { return mOutputTimeString; }

    // Getter for web url String
    public String getWebUrl() { return mWebUrl; }

}
