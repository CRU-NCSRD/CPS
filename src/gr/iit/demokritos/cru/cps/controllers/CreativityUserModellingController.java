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
package gr.iit.demokritos.cru.cps.controllers;

import gr.iit.demokritos.cru.cps.UserProperty;
import gr.iit.demokritos.cru.cps.validators.ClientApplicationValidator;
import gr.iit.demokritos.cru.cps.validators.UserManager;
import gr.iit.demokritos.cru.database.MySQLConnector;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author Pythagoras Karampiperis
 */
public class CreativityUserModellingController {
    private long application_id;
    private long user_id;
    
    private ArrayList<UserProperty> userProperties = new ArrayList<UserProperty>();

    public CreativityUserModellingController(long application_id, long user_id) {
        this.application_id = application_id;
        this.user_id = user_id;
    }

    public ArrayList<UserProperty> getUserProperties() {
        return userProperties;
    }

    public void setUserProperties(ArrayList<UserProperty> userProperties) {
        this.userProperties = userProperties;
    }
    
    public boolean validateClientApplication(MySQLConnector mysql) throws SQLException{
        ClientApplicationValidator vac=new ClientApplicationValidator(this.application_id);
        boolean isvalid=vac.validateClientApplication(mysql);
        return isvalid;
    }
    
    public boolean validateUser(MySQLConnector mysql) throws SQLException{
        UserManager um=new UserManager(this.application_id,this.user_id);
        boolean isvalid=um.validateUser(mysql);
        return isvalid;
    }
    
    public ArrayList<UserProperty> retrieveUserProfile(MySQLConnector mysql) throws SQLException{
        Connection connection=mysql.connectToCPSDatabase();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT value,name,timestamp,window FROM userproperties JOIN properties ON userproperties.property_id=properties.property_id WHERE userproperties.user_id="+this.user_id+" AND window>-1 ORDER BY window,name");
        
        while (rs.next()) {
            double value=rs.getDouble(1);            
            String name=rs.getString(2);            
            long timestamp=rs.getLong(3);
            int window=rs.getInt(4);
            
            UserProperty up=new UserProperty(this.user_id,name,value,timestamp,window);
            userProperties.add(up);           
        }
        
        rs.close();
        
        return userProperties;
    }
    
    public void invokeMachineLearningComponents(){
        
    } 
    
}
