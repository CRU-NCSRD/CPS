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
package gr.iit.demokritos.cru.cps.validators;

import gr.iit.demokritos.cru.cps.User;
import gr.iit.demokritos.cru.cps.UserProperty;
import gr.iit.demokritos.cru.database.MySQLConnector;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

/**
 *
 * @author Sotiris Konstantinidis
 */
public class UserManager {
    private long user_id;
    private long application_id;
    private ArrayList<Long> users_id=new ArrayList<Long>();
    private long exhibit_id;
    private ArrayList<String> exhibits_list=new ArrayList<String>();

    public UserManager(long application_id) {        
        this.application_id = application_id;        
    }
    
    public UserManager(long application_id,long user_id) {        
        this.application_id = application_id;        
        this.user_id = user_id;        
    }
    
    public UserManager(long application_id,ArrayList<Long> users_id) {        
        this.application_id = application_id; 
        this.users_id=users_id;
    }
     public UserManager(long application_id,long user_id,ArrayList<Long> users_id) {        
        this.application_id = application_id; 
        this.users_id=users_id;
        this.user_id=user_id;
    }
    
     public boolean validateClientApplication(MySQLConnector mysql) throws SQLException{
        ClientApplicationValidator vac=new ClientApplicationValidator(this.application_id);
        boolean isvalid=vac.validateClientApplication(mysql);
        return isvalid;
    }
    
    public boolean validateUser(MySQLConnector mysql) throws SQLException{        
        Connection connection=mysql.connectToCPSDatabase();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT user_id FROM users WHERE user_id="+this.user_id+" AND application_key="+this.application_id);
        
        if(rs.next()){
            connection.close();
            return true;
        }else{
            connection.close();
            return false;
        }
        
    }

    
    public boolean validateExhibit(MySQLConnector mysql) throws SQLException{
        
        Connection connection=mysql.connectToCPSDatabase();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT exhibit_id FROM exhibitinstance WHERE exhibit_id="+this.exhibit_id);
        
        if(rs.next()){
            connection.close();
            return true;
        }else{
            connection.close();
            return false;
        }
        
    }
    
    public long getExhibit_id() {
        return exhibit_id;
    }

    public void setExhibit_id(long exhibit_id) {
        this.exhibit_id = exhibit_id;
    }

    public long getUser_id() {
        return user_id;
    }

    public void setUser_id(long user_id) {
        this.user_id = user_id;
    }
    
    @SuppressWarnings("empty-statement")
    public void createUser(MySQLConnector mysql)throws SQLException, Exception{       
        
        UUID uid=UUID.randomUUID();
        this.user_id=uid.getMostSignificantBits();
        
        if(this.user_id<0){
            this.user_id=this.user_id*-1;
        }
        
        boolean isuuidvalid=checkIfUUIDisValid(mysql);
        
        while(!isuuidvalid){
            this.user_id=uid.getMostSignificantBits();
            isuuidvalid=checkIfUUIDisValid(mysql);
        }
        Connection connection=mysql.connectToCPSDatabase();
        connection.setAutoCommit(false);
        Statement stmt = connection.createStatement();
        
        stmt.executeUpdate("INSERT INTO users (user_id,application_key,isgroup) VALUES ("+this.user_id+","+this.application_id+",0)");
        ResultSet properties_rs=stmt.executeQuery("SELECT property_id FROM properties");
        long timestamp=System.currentTimeMillis()/1000L;   
        
        while(properties_rs.next()){            
            int property_id=properties_rs.getInt(1); 
            
            Statement stmt2 = connection.createStatement();
            stmt2.executeUpdate("INSERT INTO userproperties (user_id,property_id,value,window,timestamp) VALUES ("+this.user_id+","+property_id+","+0+",-1,"+timestamp+")");
        }
        connection.commit();
        connection.close();
    }
    
    private boolean checkIfUUIDisValid(MySQLConnector mysql) throws SQLException{
        
        Connection connection=mysql.connectToCPSDatabase();
        Statement stmt = connection.createStatement();
        
        ResultSet rs = stmt.executeQuery("SELECT user_id FROM users WHERE user_id="+this.user_id+" AND application_key="+this.application_id);
        
        if(!rs.next()){            
            return true;
            
        }else{
            return false;
        }
    }
    
    public void createGroup(MySQLConnector mysql) throws SQLException, Exception{ 
        UUID uid=UUID.randomUUID();
        this.user_id=uid.getMostSignificantBits();
        if(this.user_id<0){
            this.user_id=this.user_id*-1;
        }
        boolean isuuidvalid=checkIfUUIDisValid(mysql);        
        while(!isuuidvalid){
            this.user_id=uid.getMostSignificantBits();
            isuuidvalid=checkIfUUIDisValid(mysql);
        }
        Connection connection=mysql.connectToCPSDatabase();
        connection.setAutoCommit(false);        
        Statement stmt = connection.createStatement();         
        
        //System.out.println("INSERT INTO users (user_id,application_key) VALUES ("+this.user_id+","+this.application_id+")");
        stmt.executeUpdate("INSERT INTO users (user_id,application_key,isgroup) VALUES ("+this.user_id+","+this.application_id+",1)");         
                  
        Iterator it=users_id.iterator();
        while(it.hasNext()){
            //System.out.println("INSERT INTO groups (group_id,user_id) VALUES ("+this.user_id+","+(Long)it.next()+")");
            stmt.executeUpdate("INSERT INTO groups (group_id,user_id) VALUES ("+this.user_id+","+(Long)it.next()+")");
        }
        
        ResultSet properties_rs=stmt.executeQuery("SELECT property_id FROM properties");
        long timestamp=System.currentTimeMillis()/1000L;   
        
        while(properties_rs.next()){            
            int property_id=properties_rs.getInt(1); 
            
            Statement stmt2 = connection.createStatement();
            stmt2.executeUpdate("INSERT INTO userproperties (user_id,property_id,value,window,timestamp) VALUES ("+this.user_id+","+property_id+","+0+",-1,"+timestamp+")");
        }
        
        connection.commit();
        connection.close();
    }
    public void destroyGroup(MySQLConnector mysql) throws SQLException, Exception{ 
        Connection connection=mysql.connectToCPSDatabase();
        connection.setAutoCommit(false);        
        Statement stmt = connection.createStatement();         
        
        //System.out.println("INSERT INTO users (user_id,application_key) VALUES ("+this.user_id+","+this.application_id+")");
        
        ResultSet group_users=stmt.executeQuery("SELECT user_id FROM groups WHERE groupd_id="+this.user_id);
      
        ArrayList<UserProperty> al=new ArrayList<UserProperty>();
        
        while(group_users.next()){  
            Long user=group_users.getLong(1);
            UserProperty up=new UserProperty(user);
        }
        
        
        stmt.executeUpdate("DELETE * FROM  users WHERE  ("+this.user_id+","+this.application_id+",1)");         
                  
        
        long timestamp=System.currentTimeMillis()/1000L;   
        
        
        
        connection.commit();
        connection.close();
    }
    
}
