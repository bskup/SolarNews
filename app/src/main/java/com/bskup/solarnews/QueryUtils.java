package com.bskup.solarnews;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class QueryUtils {

    // Tag for log messages
    public static final String LOG_TAG = QueryUtils.class.getSimpleName();

    // Private constructor because an object instance of query utils is never needed
    // This class only holds static variables and methods
    private QueryUtils() {
    }

    // Return list of NewsStory objects that has been built up by parsing a JSON response
    public static List<NewsStory> extractNewsStories(String requestUrl) {
        // Temp log message
        Log.v(LOG_TAG, "extractNewsStories called from QueryUtils");

        // Create URL
        URL passedInRequestUrl = createUrl(requestUrl);

        // Create an empty List that we can start adding NewsStory objects to
        List<NewsStory> newsStories = new ArrayList<>();

        // Date format to match the format we receive the date in from the server
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        // Date formats to convert to and use in our ui
        SimpleDateFormat outputFormatDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        SimpleDateFormat outputFormatTime = new SimpleDateFormat("h:mma", Locale.US);

        // Try to parse the json response. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {
            // Parse the response given by the SAMPLE_JSON_RESPONSE string and
            // build up a list of NewsStory objects with the corresponding data.
            String jsonResponseString = makeHttpRequest(passedInRequestUrl);
            JSONObject jsonRootObject = new JSONObject(jsonResponseString);

            JSONObject jsonObjectResponse = jsonRootObject.optJSONObject("response");
            JSONArray jsonArrayResults = jsonObjectResponse.optJSONArray("results");
            // If json array of results exists, do the rest of this
            if (jsonArrayResults != null) {
                // For each object in the array, extract the data we need
                for (int i = 0; i < jsonArrayResults.length(); i++) {
                    // Use getJSONObject here because we know there are objects there based on length
                    JSONObject currentJsonObject = jsonArrayResults.getJSONObject(i);

                    // Get the json string sectionName from the current result
                    String sectionName = currentJsonObject.optString("sectionName");
                    // Get the json string webTitle from the current result
                    String webTitle = currentJsonObject.optString("webTitle");
                    // Get the json string WebPublicationDate from the current result
                    String webPublicationDate = currentJsonObject.optString("webPublicationDate");
                    // Get json string webUrl from the current result
                    String webUrlString = currentJsonObject.optString("webUrl");

                    // Parse date and convert to our preferred format
                    Date inputDate = null;
                    try {
                        inputDate = inputFormat.parse(webPublicationDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    String outputDateString = outputFormatDate.format(inputDate);
                    String outputTimeString = outputFormatTime.format(inputDate);

                    // Add new NewsStory to list using data obtained during
                    // this iteration of the loop
                    newsStories.add(new NewsStory(sectionName, webTitle, outputDateString, outputTimeString, webUrlString));
                }
            }
        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the Guardian JSON results", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        // Return the list of NewsStory objects
        return newsStories;
    }

    // Returns new URL object from the given string URL
    public static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    // Make an HTTP request to the given URL and return a String as the response
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(20000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the Google Books JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    // Convert the InputStream into a String which contains the whole JSON response from the server
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }
}
