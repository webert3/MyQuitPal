package com.example.webert3.imagedisplayapp.Helpers;

import com.github.mikephil.charting.data.Entry;

import java.util.Comparator;

/**
 * Written by TR4Android
 *
 * Modification to MPAndroid library to ensure that entry arrays are ordered. This prevents data
 * points from not being drawn on the canvas.
 */
public class EntryComparator implements Comparator<Entry> {
    @Override
    public int compare(Entry entry1, Entry entry2) {
        return entry1.getXIndex() - entry2.getXIndex();
    }
}
