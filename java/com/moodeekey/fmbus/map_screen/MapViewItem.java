package com.moodeekey.fmbus.map_screen;

/**
 * Class-container for RouteSelector that contains Route, Color and Boolean Value(selection state)
 */
public class MapViewItem {
    String route;
    String color;
    boolean selected = false;


    public MapViewItem(String route, String color_code, boolean selected) {
        super();
        this.route = route;
        this.color = color_code;
        this.selected = selected;
    }

    public String getColor() {
        return color;
    }
    public String getName() {
        return route;
    }
    public boolean isSelected() {
        return selected;
    }
    public int switch_state(){
        int result = 0;

        if(this.isSelected())
            result = 1;
        return result;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
