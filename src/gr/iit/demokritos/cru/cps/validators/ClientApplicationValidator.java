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

import gr.iit.demokritos.cru.database.MySQLConnector;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Sotiris Konstantinidis
 */
public class ClientApplicationValidator {
    private long application_id;
    private String type;

    public ClientApplicationValidator(long application_id) {
        this.application_id = application_id;
    }
    
    public ClientApplicationValidator(String type) {
        this.type = type;
    }
    
    public boolean validateClientApplication(MySQLConnector mysql) throws SQLException{
        Connection connection=mysql.connectToCPSDatabase();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT application_key FROM applications WHERE application_key="+this.application_id);
        
        if(rs.next()){
            connection.close();
            return true;
        }else{
            connection.close();
            return false;
        }
        
    }    
    public boolean validateExhibitType(MySQLConnector mysql) throws SQLException{
        Connection connection=mysql.connectToCPSDatabase();
        Statement stmt = connection.createStatement();
        
        ResultSet rs = stmt.executeQuery("SELECT type_id FROM typeofexhibit WHERE name=\""+this.type+"\"");
        
        if(rs.next()){
            connection.close();
            return true;
        }else{
            connection.close();
            return false;
        }        
    }
}
