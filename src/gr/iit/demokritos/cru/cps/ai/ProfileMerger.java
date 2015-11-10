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
package gr.iit.demokritos.cru.cps.ai;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import gr.iit.demokritos.cru.cps.User;
import gr.iit.demokritos.cru.cps.UserProperty;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 *
 * @author Pythagoras Karampiperis
 */
public class ProfileMerger {

    private ArrayList<Double> group_profile;
    private HashMap<Long,ArrayList<Double>> user_profiles;
    
    public void JoinProfiles(ArrayList<User> users,ArrayList<String> features) throws ParseException{
        group_profile=new ArrayList<Double>();
        for(int i=0;i<features.size();i++){
            ArrayList<Double> f_values=new ArrayList<Double>();
            double f_trend = 0;
            
            for(int j=0;j<users.size();j++){
                UserProperty up=users.get(j).getUps().get(i);            
                ArrayList<Double> res=CalculateUserTrends(up.getProperty_value_timeline());
                f_trend+=res.get(0);
                f_values.add(res.get(1));
            }
            double group_feat_sum=0.0;
            
            for (Double v : f_values) {
                group_feat_sum += v/f_trend;
            }
             
            group_profile.add(group_feat_sum);
        }
            
     }
    
    public void DisjoinProfiles(User group,ArrayList<String> features,ArrayList<User> users) throws ParseException{
        for(int i=0;i<features.size();i++){
            UserProperty up=group.getUps().get(i);
            ArrayList<String> f_timeline=up.getProperty_value_timeline();
            
            String[] first=f_timeline.get(0).split(",");
            double first_f_value=Double.parseDouble(first[0]);
            double first_f_time=Double.parseDouble(first[1]);
            
            double last_f_value=Double.parseDouble(f_timeline.get(f_timeline.size()-1).split(",")[0]);
            double diff_feature_value=last_f_value-first_f_value;
           
            for(int j=0;j<users.size();j++){
                UserProperty up_user=users.get(j).getUps().get(i); 
                ArrayList<Double> res=CalculateUserTrends(up_user.getProperty_value_timeline(),first_f_time);
                
                ArrayList<Double> user_features=user_profiles.get(users.get(j).getUps().get(0).getUser_id());
                user_features.add(res.get(1)*diff_feature_value);
                user_profiles.put(users.get(j).getUps().get(0).getUser_id(), user_features);
                        
            }
        }        
        
    }
    
    
    public ArrayList<Double> CalculateUserTrends(ArrayList<String> users_features) throws ParseException {

        SimpleDateFormat sdfu = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        ArrayList<Double> result=new ArrayList<Double>();
    
            for (int i = 0; i < users_features.size(); i++) {
                SimpleRegression regression = new SimpleRegression();
                String[] features = ((String) users_features.get(i)).split(";");

                double average = 0.0;
                double f=0.0;
                for (String s : features) {

                    String[] inside_feature = s.split(",");

                    //make timestamp secs
                    Date udate = sdfu.parse(inside_feature[1]);
                    double sec = udate.getTime();
                    average += Double.parseDouble(inside_feature[0]);
                    //fix mls regr

                    regression.addData(sec, Double.parseDouble(inside_feature[0]));

                    average = average / features.length;

                    f = Math.atan(regression.getSlope());// atan of slope is the angle of the regression in rad
                    if (Double.isNaN(f)) {
                        f = 0;
                    }
                    if (f != 0 && (Math.toDegrees(f) > 90 || Math.toDegrees(f) < -90)) {
                        if ((Math.toDegrees(f) / 90) % 2 == 0) {//make angles in [-90,90]
                            f = Math.toDegrees(f) % Math.toDegrees(Math.PI / 2);
                        } else {
                            f = -Math.toDegrees(f) % Math.toDegrees(Math.PI / 2);
                        }
                    }
                    f = f + Math.PI / 2;//refrain trend=0                    
                    
                }
                
                result.add(f);
                result.add(f*average);
                
            }
            return result;
            
    }
    
    public ArrayList<Double> CalculateUserTrends(ArrayList<String> users_features, Double start_time) throws ParseException {

        SimpleDateFormat sdfu = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
        ArrayList<Double> result=new ArrayList<Double>();
    
            for (int i = 0; i < users_features.size(); i++) {
                SimpleRegression regression = new SimpleRegression();
                String[] features = ((String) users_features.get(i)).split(";");

                double average = 0.0;
                double f=0.0;
                for (String s : features) {

                    String[] inside_feature = s.split(",");

                    //make timestamp secs
                    Date udate = sdfu.parse(inside_feature[1]);
                    double sec = udate.getTime();
                    
                    if(sec>start_time){
                        continue;
                    }
                    average += Double.parseDouble(inside_feature[0]);
                    //fix mls regr

                    regression.addData(sec, Double.parseDouble(inside_feature[0]));

                    average = average / features.length;

                    f = Math.atan(regression.getSlope());// atan of slope is the angle of the regression in rad
                    if (Double.isNaN(f)) {
                        f = 0;
                    }
                    if (f != 0 && (Math.toDegrees(f) > 90 || Math.toDegrees(f) < -90)) {
                        if ((Math.toDegrees(f) / 90) % 2 == 0) {//make angles in [-90,90]
                            f = Math.toDegrees(f) % Math.toDegrees(Math.PI / 2);
                        } else {
                            f = -Math.toDegrees(f) % Math.toDegrees(Math.PI / 2);
                        }
                    }
                    f = f + Math.PI / 2;//refrain trend=0                    
                    
                }
                
                result.add(f);
                result.add(f*average);
                
            }
            return result;
            
    }
    
}

  
