package com.example.lifetune;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

import java.util.Locale;

public class CurrentTimeMarker extends MarkerView {
    private final TextView markerText;

    public CurrentTimeMarker(Context context, int layoutResource) {
        super(context, layoutResource);
        markerText = findViewById(R.id.markerText);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        float hour = e.getX();
        int minutes = (int) ((hour - (int) hour) * 60);
        markerText.setText(String.format(Locale.getDefault(), "%02d:%02d", (int) hour, minutes));
        super.refreshContent(e, highlight);
    }
}