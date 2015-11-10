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

import gr.iit.demokritos.cru.cps.Metric;
import gr.iit.demokritos.cru.cps.UserProperty;
import gr.iit.demokritos.cru.cps.controllers.CreativityUserModellingController;
import gr.iit.demokritos.cru.database.MySQLConnector;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Pythagoras Karampiperis
 */
public class GetUserProfile {

    private String application_key = "";
    private String user_id = "";
    private Map properties = new HashMap();

    public GetUserProfile(String application_key,String user_id,Map properties) {
        this.application_key = application_key;
        this.user_id=user_id;
        this.properties = properties;
    }
    
    public JSONObject processRequest() throws IOException {
        String response_code = "e0";

        ArrayList<UserProperty> up = new ArrayList<UserProperty>();

       // String filename = "/WEB-INF/configuration.properties";
        String location = (String) properties.get("location");
        String database_name = (String) properties.get("database_name");
        String username = (String) properties.get("username");
        String password = (String) properties.get("password");

        MySQLConnector mysql = new MySQLConnector(location, database_name, username, password);
        Connection connection = mysql.connectToCPSDatabase();

        try {
            CreativityUserModellingController cumc = new CreativityUserModellingController(Long.parseLong(application_key), Long.parseLong(user_id.trim()));
            boolean isvalid = cumc.validateClientApplication(mysql);
            if (isvalid == true) {
                isvalid = cumc.validateUser(mysql);
                if (isvalid == true) {
                    cumc.retrieveUserProfile(mysql);
                    up = cumc.getUserProperties();
                    response_code = "OK";
                } else {
                    response_code = "e102";
                }
            } else {
                response_code = "e101";
            }
        } catch (NumberFormatException ex) {
            response_code = "e101";
            Logger.getLogger(CreateUser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(GetUserProfile.class.getName()).log(Level.SEVERE, null, ex);
        } 

        JSONArray list = new JSONArray();

        Iterator it = up.iterator();
        int window = 0;
        JSONObject obj = new JSONObject();

        JSONObject obj_temp = new JSONObject();
        while (it.hasNext()) {

            JSONObject obj_window = new JSONObject();
            UserProperty temp_up = (UserProperty) it.next();

            obj_window.put("window", temp_up.getWindow());
            obj_window.put(temp_up.getProperty_name(), temp_up.getProperty_value());
            obj_window.put("timestamp", temp_up.getTimestamp());

            list.add(obj_window);
            //System.out.println(obj_temp.toJSONString());
            //obj_window.clear();
        }

        //obj_temp.put("properties",list);
        JSONObject main_obj = new JSONObject();

        main_obj.put("application_key", application_key);
        main_obj.put("user_id", user_id);
        main_obj.put("profile", list);
        main_obj.put("response_code", response_code);

        return main_obj;
    }

}
