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
package gr.iit.demokritos.cps.ai;

import java.util.Comparator;

/**
 *
 * @author Sotiris Konstantinidis
 */
public class TagComparator implements Comparator<String>{
    
    public int compare(String tag1,String tag2){
        String tag1_elements[]=tag1.split(";");
        String tag2_elements[]=tag2.split(";");
        
        
        Double tc1_temp=Double.parseDouble(tag1_elements[1]);
        Double tc2_temp=Double.parseDouble(tag2_elements[1]);
        
        Integer tc1=tc1_temp.intValue();
        Integer tc2=tc2_temp.intValue();               
        
        return tc1.compareTo(tc2);        
        
    }
    
}
