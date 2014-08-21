package com.moodeekey.fmbus.saved_routes;

/**
 * Custom Item Class, Contains two variables - ID used for data handling, text is for display only
 */
public class ListViewItem {
    private int itemId;
    private String itemText;
    public ListViewItem(int id, String text) {
        itemId = id;
        itemText = text;
    }
    public String toString() {
        return itemText;
    }

    public int getItemId(){
        return itemId;
    }
}
