/*
 * Copyright (C) 2015 Computational Systems & Human Mind Research Unit
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.iit.demokritos.cru.cps;

import java.util.ArrayList;

/**
 *
 * @author Pythagoras Karampiperis
 */
public class UserProperty {
    private long user_id;
    private String property_name;    
    private double property_value;
    private long timestamp;
    private int window;
    private ArrayList<String> property_value_timeline;
    
   public UserProperty(long user_id) {
       this.user_id = user_id;
   }

    public UserProperty(long user_id, String property_name, double property_value,long timestamp, int window) {
        this.user_id = user_id;
        this.property_name = property_name;
        this.property_value = property_value;
        this.timestamp=timestamp;
        this.window = window;
    }

    public UserProperty(String property_name, double property_value) {
        this.property_name = property_name;
        this.property_value = property_value;
    }
    
    public String getProperty_name() {
        return property_name;
    }

    public void setProperty_name(String property_name) {
        this.property_name = property_name;
    }

    public double getProperty_value() {
        return property_value;
    }

    public void setProperty_value(double property_value) {
        this.property_value = property_value;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }   

    public int getWindow() {
        return window;
    }

    public void setWindow(int window) {
        this.window = window;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<String> getProperty_value_timeline() {
        return property_value_timeline;
    }

    public void setProperty_value_timeline(ArrayList<String> property_value_timeline) {
        this.property_value_timeline = property_value_timeline;
    }
    
    
}
