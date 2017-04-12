package com.bskup.solarnews;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class NewsStoryAdapter extends ArrayAdapter<NewsStory> {

    private Context mContext;

    // Because we are extending ArrayAdapter, our constructor was expected to match
    // one of the formats of the constructors for ArrayAdapter which all include
    // int resource id (for a single TextView) as a 2nd param. We don't need a resource id
    // passed in, because our custom getView already knows what ids we're using.
    // So to correct the code errors, our constructor calls super in the format expected
    // by ArrayAdapter, passing in a useless placeholder value of 0 (could be anything)
    public NewsStoryAdapter(Context context, List<NewsStory> newsStoryList) {
        super(context, 0, newsStoryList);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Store the passed in convertView param in a View variable
        View listItemView = convertView;

        // If the View isn't being reused, aka the contents of convertView are null,
        // inflate the layout we want to use
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        // Find the NewsStory at (position) in the ArrayList
        final NewsStory currentNewsStory = getItem(position);

        if (currentNewsStory != null) {
            // Find the section name text view in our list item layout
            TextView sectionNameTextView = (TextView) listItemView.findViewById(R.id.section_name_text_view);
            if (currentNewsStory.getSectionName() != null) {
                // Set the section name text view to display section name of current NewsStory
                sectionNameTextView.setText(currentNewsStory.getSectionName());
            } else {
                // If current NewsStory's section name is empty, set empty state text
                sectionNameTextView.setText(R.string.no_section_listed);
            }

            // Find the title text view in our list item layout
            TextView titleTextView = (TextView) listItemView.findViewById(R.id.title_text_view);
            // Set the title text view to display title of current NewsStory
            titleTextView.setText(currentNewsStory.getWebTitle());

            // Find the date text view in our list item layout
            TextView dateTextView = (TextView) listItemView.findViewById(R.id.date_text_view);
            if (currentNewsStory.getOutputDateString() != null) {
                // Set the date text view to display publication date of current NewsStory
                dateTextView.setText(currentNewsStory.getOutputDateString());
            } else {
                // If current NewsStory's date is empty, set empty state text
                dateTextView.setText(R.string.no_date_listed);
            }

            // Find the time text view in our list item layout
            TextView timeTextView = (TextView) listItemView.findViewById(R.id.time_text_view);
            ImageView clockImageView = (ImageView) listItemView.findViewById(R.id.clock_image_view);
            if (currentNewsStory.getOutputTimeString() != null) {
                // Set the date text view to display publication date of current NewsStory
                timeTextView.setText(currentNewsStory.getOutputTimeString());
            } else {
                // If current NewsStory's time is somehow empty, hide stuff
                clockImageView.setVisibility(View.GONE);
                timeTextView.setVisibility(View.GONE);
            }

            // Find the share image view in our list item layout
            ImageView shareImageView = (ImageView) listItemView.findViewById(R.id.share_image_view);
            // Set a click listener on share image view so it does stuff when clicked
            shareImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Do this stuff when share image view is clicked
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    String currentShareMessage = getContext().getString(R.string.share_message, currentNewsStory.getWebTitle(), currentNewsStory.getWebUrl());
                    shareIntent.putExtra(Intent.EXTRA_TEXT, currentShareMessage);
                    shareIntent.setType("text/plain");
                    mContext.startActivity(shareIntent);
                }
            });
        }
        return listItemView;
    }
}
