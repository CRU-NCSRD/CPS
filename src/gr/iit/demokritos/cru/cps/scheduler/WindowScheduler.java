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
package gr.iit.demokritos.cru.cps.scheduler;

import com.mathworks.toolbox.javabuilder.MWException;
import gr.iit.demokritos.cru.cps.User;
import gr.iit.demokritos.cru.cps.UserProperty;
import gr.iit.demokritos.cru.cps.ai.MachineLearningComponents;
import gr.iit.demokritos.cru.cps.ai.MachineLearningComponents.Vec;
import gr.iit.demokritos.cru.database.MySQLConnector;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sotiris Konstantinidis
 */
public class WindowScheduler {
    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    private static String location;
    private static String database_name;    
    private static String username;
    private static String password;  
    private static String time_window;
    
    
    public WindowScheduler(String location, String database_name, String username, String password,String time_window) throws FileNotFoundException, FileNotFoundException, FileNotFoundException, IOException {
        WindowScheduler.location = location;
        WindowScheduler.database_name = database_name;
        WindowScheduler.username = username;
        WindowScheduler.password = password;
        WindowScheduler.time_window=time_window;
    }    
    public WindowScheduler(Map properties) {

        location=(String)properties.get("location");
        database_name=(String)properties.get("database_name");
        password=(String)properties.get("password");
        time_window=(String)properties.get("time_window");

    }
    public WindowScheduler() {
        try {
            loadproperties();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WindowScheduler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WindowScheduler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
 
    public void startExecutor(){        
        startdrill();        
    }
    
    public static double[] convertDoubles(List<Double> doubles)
    {
        double[] ret = new double[doubles.size()];
        Iterator<Double> iterator = doubles.iterator();
        int i=0;
        while(iterator.hasNext())
        {
            ret[i] = iterator.next().doubleValue();
            i++;
        }
        return ret;
    }
    public static int updateWindow() throws SQLException{
            MySQLConnector mysql=new MySQLConnector(WindowScheduler.location,WindowScheduler.database_name,WindowScheduler.username,WindowScheduler.password);
            Connection connection=mysql.connectToCPSDatabase(); 
            connection.setAutoCommit(false);
            Statement stmt= connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT window_id,window FROM windows WHERE current_window=1");
            int window_id=-1;
            int window=0;
            if(rs.next()) {
                window_id = rs.getInt(1);
                window=rs.getInt(2);
                stmt.executeUpdate("UPDATE windows SET current_window=0 WHERE window_id="+window_id);                
            }    
            window_id=window_id+1;
            window=window+1;
            long timestamp=System.currentTimeMillis()/1000L;   
            int result = stmt.executeUpdate("INSERT INTO windows (window_id,timestamp,current_window,window) VALUES ("+window_id+","+timestamp+",1,"+window+")");
            connection.commit();
            return window;
    }
    
    public static double[] updateWeights(int window,long application_key) throws SQLException{
            MySQLConnector mysql=new MySQLConnector(WindowScheduler.location,WindowScheduler.database_name,WindowScheduler.username,WindowScheduler.password);
            Connection connection=mysql.connectToCPSDatabase(); 
            connection.setAutoCommit(false);
            
            Statement stmt= connection.createStatement();
            Statement stmt2= connection.createStatement();
            Statement stmt3= connection.createStatement();            
            
                
            double[] novelty=null;
            double[] surprise=null;
            double[] rarity=null;
            double[] recreational=null;
            double[] weights=new double[3];
            
            ResultSet rs_2 = stmt2.executeQuery("SELECT exhibitinstance.exhibit_id FROM exhibitinstance JOIN users ON exhibitinstance.user_id = users.user_id WHERE users.application_key="+application_key);
            ResultSet rs_3= stmt3.executeQuery("SELECT metrics.metric_id, metrics.name FROM metrics JOIN typeofexhibit ON metrics.type_id = typeofexhibit.type_id WHERE typeofexhibit.name=\"semantic\" ORDER BY metric_id");
            ArrayList<Double> temp=new ArrayList<Double>();

            while(rs_3.next()){
                    int metric_id=rs_3.getInt(1);
                    String name=rs_3.getString(2);

                    temp.clear();
                    while(rs_2.next()){
                        long exhibit_id=rs_2.getLong(1);
                        ResultSet rs_4=stmt.executeQuery("SELECT calculation FROM metricscalculations WHERE metric_id="+metric_id+" AND exhibit_id="+exhibit_id);
                        if(rs_4.next()){
                            double calculation=rs_4.getDouble(1);
                            temp.add(calculation);
                        }                        
                        
                    }
                    rs_2.beforeFirst();
                    if(name.equalsIgnoreCase("novelty")){  

                        novelty=new double[temp.size()];
                        novelty=WindowScheduler.convertDoubles(temp);


                    }else if(name.equalsIgnoreCase("surprise")){
                        surprise=new double[temp.size()];
                        surprise=WindowScheduler.convertDoubles(temp);

                        ResultSet rs_5=stmt.executeQuery("SELECT value FROM weights WHERE metric_id="+metric_id+" AND application_id="+application_key+" AND window="+(window-1));
                        if(rs_5.next()){
                            weights[0]=rs_5.getDouble(1);                            
                        }
                        

                    }else if(name.equalsIgnoreCase("rarity")){

                        rarity=new double[temp.size()];
                        rarity=WindowScheduler.convertDoubles(temp);

                        ResultSet rs_5=stmt.executeQuery("SELECT value FROM weights WHERE metric_id="+metric_id+" AND application_id="+application_key+" AND window="+(window-1));                             
                         if(rs_5.next()){
                            weights[1]=rs_5.getDouble(1);                            
                        }

                    }else if(name.equalsIgnoreCase("recreational")){
                        recreational=new double[temp.size()];
                        recreational=WindowScheduler.convertDoubles(temp);

                        ResultSet rs_5=stmt.executeQuery("SELECT value FROM weights WHERE metric_id="+metric_id+" AND application_id="+application_key+" AND window="+(window-1));
                         if(rs_5.next()){
                            weights[2]=rs_5.getDouble(1);                            
                        }
                    }
                }

            System.out.println("Old Weights "+weights[0]+" "+weights[1]+" "+weights[2]);
            System.out.println("Optimization Invoked...");            
            double[] updated_weights=weights;  
            try {
                updated_weights = MachineLearningComponents.CalculateWeights(novelty,surprise,rarity,recreational,weights);
            } catch (MWException ex) {
                System.out.println("Exception...");
                updated_weights=weights;
                Logger.getLogger(WindowScheduler.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Optimization Completed...");
            
            ResultSet weight_rs = stmt.executeQuery("SELECT MAX(weight_id) FROM weights");
            int weight_id=-1;
            if(weight_rs.next()){
                weight_id=weight_rs.getInt(1);    
            }
                        
            long timestamp=System.currentTimeMillis()/1000L;   
            int result = stmt.executeUpdate("INSERT INTO weights (weight_id,application_id,metric_id,value,timestamp,window) VALUES ("+(weight_id+1)+","+application_key+",2,"+updated_weights[0]+","+timestamp+","+window+")");
            result = stmt.executeUpdate("INSERT INTO weights (weight_id,application_id,metric_id,value,timestamp,window) VALUES ("+(weight_id+2)+","+application_key+",3,"+updated_weights[1]+","+timestamp+","+window+")");
            result = stmt.executeUpdate("INSERT INTO weights (weight_id,application_id,metric_id,value,timestamp,window) VALUES ("+(weight_id+3)+","+application_key+",4,"+updated_weights[2]+","+timestamp+","+window+")");
                                           
            connection.commit();
            
            return updated_weights;
        
    }
    public static void updateGroupProfile(long application_key,int window,double[] updated_weights) throws SQLException{
        
            MySQLConnector mysql=new MySQLConnector(WindowScheduler.location,WindowScheduler.database_name,WindowScheduler.username,WindowScheduler.password);
            Connection connection=mysql.connectToCPSDatabase(); 
            connection.setAutoCommit(false);
            Statement stmt= connection.createStatement();
            ResultSet properties_rs=stmt.executeQuery("SELECT property_id FROM properties ORDER BY property_id");
            ArrayList<Integer> properties=new ArrayList<Integer>();
            
            while(properties_rs.next()){
                properties.add(properties_rs.getInt(1));
            }
            Statement stmt0= connection.createStatement();
            ResultSet users_rs=stmt0.executeQuery("SELECT user_id FROM users WHERE application_key="+application_key+" AND isgroup="+1);
            while(users_rs.next()){
                long user_id=users_rs.getLong(1);
                Statement stmt2= connection.createStatement();
                ResultSet property_value_rs=stmt2.executeQuery("SELECT value FROM userproperties WHERE user_id="+user_id+" AND property_id="+properties.get(0)+" AND window="+(window-1));
                double visionary=0.0;
                while(property_value_rs.next()){
                    visionary=property_value_rs.getDouble(1);
                }
                Statement stmt3= connection.createStatement();
                property_value_rs=stmt3.executeQuery("SELECT value FROM userproperties WHERE user_id="+user_id+" AND property_id="+properties.get(1)+" AND window="+(window-1));
                double constructive=0.0;
                while(property_value_rs.next()){
                    constructive=property_value_rs.getDouble(1);
                }
                
                ResultSet user_metrics_rs=stmt.executeQuery("SELECT metricscalculations.calculation, metrics.name  FROM metricscalculations JOIN exhibitinstance ON metricscalculations.exhibit_id = exhibitinstance.exhibit_id \n" +
                        "JOIN metrics ON metricscalculations.metric_id=metrics.metric_id\n" +
                        "WHERE exhibitinstance.user_id="+user_id+" AND exhibitinstance.window="+(window));
                        
                ArrayList<Double> user_novelty=new ArrayList<Double>();
                ArrayList<Double> user_rarity=new ArrayList<Double>();
                ArrayList<Double> user_surprise=new ArrayList<Double>();
                ArrayList<Double> user_recreational=new ArrayList<Double>();
                
                while(user_metrics_rs.next()){
                    String metric_name=user_metrics_rs.getString(2);
                    if(metric_name.equalsIgnoreCase("novelty")){
                        user_novelty.add(user_metrics_rs.getDouble(1));
                    }else if(metric_name.equalsIgnoreCase("surprise")){
                        user_rarity.add(user_metrics_rs.getDouble(1));
                    }else if(metric_name.equalsIgnoreCase("rarity")){
                        user_surprise.add(user_metrics_rs.getDouble(1));
                    }else if(metric_name.equalsIgnoreCase("recreational")){
                        user_recreational.add(user_metrics_rs.getDouble(1));
                    }
                }
                double updated_visionary=-1;
                double updated_constructive=-1;
                ArrayList<User> user=new ArrayList<User>();
                Vec creativity=MachineLearningComponents.CalculateUserProfile(convertDoubles(user_novelty), convertDoubles(user_surprise), convertDoubles(user_rarity), convertDoubles(user_recreational), updated_weights, 8,visionary,constructive,user);
                updated_visionary=creativity.getX();
                updated_constructive=creativity.getY();
                
                long timestamp=System.currentTimeMillis()/1000L;   
                
                if(((updated_visionary>=0)&&(updated_constructive>=0))){
                    Statement stmt4= connection.createStatement();
                    Statement stmt5= connection.createStatement();
                    
                    int result = stmt4.executeUpdate("INSERT INTO userproperties (user_id,property_id,value,window,timestamp) VALUES ("+user_id+",1,"+updated_visionary+","+window+","+timestamp+")");
                    result = stmt5.executeUpdate("INSERT INTO userproperties (user_id,property_id,value,window,timestamp) VALUES ("+user_id+",2,"+updated_constructive+","+window+","+timestamp+")");
                }else{
                    Statement stmt7= connection.createStatement();
                    int result = stmt7.executeUpdate("INSERT INTO userproperties (user_id,property_id,value,window,timestamp) VALUES ("+user_id+",1,"+visionary+","+window+","+timestamp+")");
                    Statement stmt8= connection.createStatement();
                    result = stmt8.executeUpdate("INSERT INTO userproperties (user_id,property_id,value,window,timestamp) VALUES ("+user_id+",2,"+constructive+","+window+","+timestamp+")");
                }
               
                connection.commit();

            }        
    }
    
    public static void updateUserProfile(long application_key,int window,double[] updated_weights) throws SQLException{
            MySQLConnector mysql=new MySQLConnector(WindowScheduler.location,WindowScheduler.database_name,WindowScheduler.username,WindowScheduler.password);
            Connection connection=mysql.connectToCPSDatabase(); 
            connection.setAutoCommit(false);
            Statement stmt0= connection.createStatement();
            ResultSet properties_rs=stmt0.executeQuery("SELECT property_id FROM properties ORDER BY property_id");
            ArrayList<Integer> properties=new ArrayList<Integer>();
            while(properties_rs.next()){
                properties.add(properties_rs.getInt(1));
            }
            Statement stmt1= connection.createStatement();
            ResultSet users_rs=stmt1.executeQuery("SELECT user_id FROM users WHERE application_key="+application_key+" AND isgroup="+0);
            while(users_rs.next()){
                long user_id=users_rs.getLong(1);
                
                Statement stmt2= connection.createStatement();
                ResultSet property_value_rs=stmt2.executeQuery("SELECT value FROM userproperties WHERE user_id="+user_id+" AND property_id="+properties.get(0)+" AND window="+(window-1));
                double visionary=0.0;
                while(property_value_rs.next()){
                    visionary=property_value_rs.getDouble(1);
                }

                Statement stmt3= connection.createStatement();
                property_value_rs=stmt3.executeQuery("SELECT value FROM userproperties WHERE user_id="+user_id+" AND property_id="+properties.get(1)+" AND window="+(window-1));
                double constructive=0.0;
                while(property_value_rs.next()){
                    constructive=property_value_rs.getDouble(1);
                }
                Statement stmt4= connection.createStatement();
                ResultSet user_metrics_rs=stmt4.executeQuery("SELECT metricscalculations.calculation, metrics.name  FROM metricscalculations JOIN exhibitinstance ON metricscalculations.exhibit_id = exhibitinstance.exhibit_id \n" +
                        "JOIN metrics ON metricscalculations.metric_id=metrics.metric_id\n" +
                        "WHERE exhibitinstance.user_id="+user_id+" AND exhibitinstance.window="+(window-1));
                        
                ArrayList<Double> user_novelty=new ArrayList<Double>();
                ArrayList<Double> user_rarity=new ArrayList<Double>();
                ArrayList<Double> user_surprise=new ArrayList<Double>();
                ArrayList<Double> user_recreational=new ArrayList<Double>();
                
                while(user_metrics_rs.next()){
                    String metric_name=user_metrics_rs.getString(2);
                    if(metric_name.equalsIgnoreCase("novelty")){
                        user_novelty.add(user_metrics_rs.getDouble(1));
                    }else if(metric_name.equalsIgnoreCase("surprise")){
                        user_rarity.add(user_metrics_rs.getDouble(1));
                    }else if(metric_name.equalsIgnoreCase("rarity")){
                        user_surprise.add(user_metrics_rs.getDouble(1));
                    }else if(metric_name.equalsIgnoreCase("recreational")){
                        user_recreational.add(user_metrics_rs.getDouble(1));
                    }
                }
                Statement stmt5= connection.createStatement();                               
                ResultSet group_ids=stmt5.executeQuery("SELECT group_id FROM groups WHERE user_id="+user_id);
                ArrayList<User> ups=new ArrayList<User>();
                while(group_ids.next()){
                    long group_id=group_ids.getLong(1);
                    Statement stmt6= connection.createStatement();
                    System.out.println("SELECT userproperties.value, properties.name  FROM userproperties JOIN properties ON userproperties.property_id = properties.property_id "
                            + "WHERE userproperties.user_id="+group_id+" AND userproperties.window="+window);
                    ResultSet group_metrics_rs=stmt6.executeQuery("SELECT userproperties.value, properties.name  FROM userproperties JOIN properties ON userproperties.property_id = properties.property_id "
                            + "WHERE userproperties.user_id="+group_id+" AND userproperties.window="+window);
                    
                    ArrayList<UserProperty> props=new ArrayList<UserProperty>();
                    while(group_metrics_rs.next()){
                        double value=group_metrics_rs.getDouble(1);
                        String property_name=group_metrics_rs.getString(2);                        
                        UserProperty up = new UserProperty(property_name,value);                        
                        props.add(up);
                    }
                    User us=new User(props);
                    props.clear();
                    ups.add(us);
                    
                }    
                System.out.println("UPS "+ups.size());
                System.out.println(ups.get(0).getUps().get(0));
                
                Vec creativity=MachineLearningComponents.CalculateUserProfile(convertDoubles(user_novelty), convertDoubles(user_surprise), convertDoubles(user_rarity), convertDoubles(user_recreational), updated_weights, 8,visionary,constructive,ups);
                double updated_visionary=creativity.getX();
                double updated_constructive=creativity.getY();
                
                long timestamp=System.currentTimeMillis()/1000L;
                if(((updated_visionary>=0)&&(updated_constructive>=0))){
                    Statement stmt7= connection.createStatement();
                    int result = stmt7.executeUpdate("INSERT INTO userproperties (user_id,property_id,value,window,timestamp) VALUES ("+user_id+",1,"+updated_visionary+","+window+","+timestamp+")");
                    Statement stmt8= connection.createStatement();
                    result = stmt8.executeUpdate("INSERT INTO userproperties (user_id,property_id,value,window,timestamp) VALUES ("+user_id+",2,"+updated_constructive+","+window+","+timestamp+")");
                }else{
                    Statement stmt7= connection.createStatement();
                    int result = stmt7.executeUpdate("INSERT INTO userproperties (user_id,property_id,value,window,timestamp) VALUES ("+user_id+",1,"+visionary+","+window+","+timestamp+")");
                    Statement stmt8= connection.createStatement();
                    result = stmt8.executeUpdate("INSERT INTO userproperties (user_id,property_id,value,window,timestamp) VALUES ("+user_id+",2,"+constructive+","+window+","+timestamp+")");
                }
                connection.commit();

            }
    
    
    }
    
    public void loadproperties() throws FileNotFoundException, IOException{
        URL resource = getClass().getResource("/");
        String path = resource.getPath();
        path = path.replace("classes/", "configuration.properties");
        Properties prop = new Properties();
        InputStream in = new FileInputStream(path);  
        
        prop.load(in);
        
        location=prop.getProperty("location");
        username=prop.getProperty("username");
        password=prop.getProperty("password");
        database_name=prop.getProperty("database_name");
    }
    
    public static void quartzJob() throws SQLException{   
            
        
            int new_window= WindowScheduler.updateWindow();
            System.out.println("Executor Initiated in window="+new_window);
            MySQLConnector mysql=new MySQLConnector(WindowScheduler.location,WindowScheduler.database_name,WindowScheduler.username,WindowScheduler.password);
            Connection connection=mysql.connectToCPSDatabase(); 
            Statement stmt= connection.createStatement();
        
        
            ResultSet rs=stmt.executeQuery("SELECT application_key FROM applications");
            while(rs.next()){
                long application_key=rs.getLong(1);
                System.out.println("Calculate new Weights...");                
                double[] updated_weights=updateWeights(new_window,application_key);
                System.out.println("New Weights:"+updated_weights[0]+" "+updated_weights[1]+" "+updated_weights[2]);
                System.out.println("Update Group Profile...");
                updateGroupProfile(application_key,new_window,updated_weights);
                System.out.println("Update User Profile...");
                updateUserProfile(application_key,new_window,updated_weights);

            }
        
    }
    
    
    public static void startdrill() {
        
        final Runnable update = new Runnable() {
        public void run() {
            try {
                
                int new_window= WindowScheduler.updateWindow();
                System.out.println("Executor Initiated in window="+new_window);
                MySQLConnector mysql=new MySQLConnector(WindowScheduler.location,WindowScheduler.database_name,WindowScheduler.username,WindowScheduler.password);
                Connection connection=mysql.connectToCPSDatabase(); 
                Statement stmt= connection.createStatement();
                
                ResultSet rs=stmt.executeQuery("SELECT application_key FROM applications");
                while(rs.next()){
                    long application_key=rs.getLong(1);
                    System.out.println("Calculate new Weights...");
                    double[] updated_weights=updateWeights(new_window-1,application_key);
                    System.out.println("New Weights:"+updated_weights[0]+" "+updated_weights[1]+" "+updated_weights[2]);
                    System.out.println("Update Group Profile...");
                    updateGroupProfile(application_key,new_window-1,updated_weights);
                    System.out.println("Update User Profile...");
                    updateUserProfile(application_key,new_window-1,updated_weights);
                
                }
            } catch (SQLException ex) {
                Logger.getLogger(WindowScheduler.class.getName()).log(Level.SEVERE, null, ex);
            } 
            
        }
        };
        //WindowScheduler.time_window="10";
        System.out.println(WindowScheduler.time_window);
        
        final ScheduledFuture updateHandle = scheduler.scheduleAtFixedRate(update,10,Integer.parseInt(WindowScheduler.time_window),TimeUnit.MINUTES);
    }
}
