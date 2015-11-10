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
package gr.iit.demokritos.cru.cps.api;

import gr.iit.demokritos.cru.cps.validators.UserManager;
import gr.iit.demokritos.cru.database.MySQLConnector;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 *
 * @author Pythagoras Karampiperis
 */
public class CreateUser {

    private String application_key = "";
    private Map properties = new HashMap();

    public CreateUser(String application_key, Map properties) {
        this.application_key = application_key;
        this.properties = properties;
    }

    public JSONObject processRequest() throws IOException, Exception {

        String response_code = "e0";
        long user_id = 0;


        //String sha256hex = org.apache.commons.codec.digest.DigestUtils.sha256Hex(application_key);
        //String filename = "/WEB-INF/configuration.properties";
        

        String location = (String) properties.get("location");
        String database_name = (String) properties.get("database_name");
        String username = (String) properties.get("username");
        String password = (String) properties.get("password");

        MySQLConnector mysql = new MySQLConnector(location, database_name, username, password);
        Connection connection = mysql.connectToCPSDatabase();

        try {
            UserManager um = new UserManager(Long.parseLong(application_key));
            boolean isvalid = false;
            isvalid = um.validateClientApplication(mysql);
            if (isvalid == true) {
                um.createUser(mysql);
                user_id = um.getUser_id();
                response_code = "OK";
            } else {
                response_code = "e101";
            }

        } catch (NumberFormatException ex) {
            response_code = "e101";
            Logger.getLogger(CreateUser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(CreateUser.class.getName()).log(Level.SEVERE, null, ex);
        }

        JSONObject obj = new JSONObject();

        obj.put("application_key", application_key);
        obj.put("user_id", Long.toString(user_id));
        obj.put("response_code", response_code);
        return obj;
    }

}
