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
package gr.iit.demokritos.cru.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;

    
/**
 *
 * @author Pythagoras Karampiperis
 */
public class MySQLConnector {
    
    private String location;
    private String database_name;    
    private String username;
    private String password;
    
    private final String driver="com.mysql.jdbc.Driver";    


    public MySQLConnector(String location, String database_name, String username, String password) {
        this.location = location;
        this.database_name = database_name;
        this.username = username;
        this.password = password;
    }
    
    public Connection connectToCPSDatabase(){
        Connection connection=null;
        
        String connection_string="jdbc:mysql://"+this.location+"/"+this.database_name;
       
        
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(connection_string,this.username,this.password);
        } catch (SQLException ex) {           
            Logger.getLogger(MySQLConnector.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MySQLConnector.class.getName()).log(Level.SEVERE, null, ex);
        }        
        
        return connection;
    }   
        
}
