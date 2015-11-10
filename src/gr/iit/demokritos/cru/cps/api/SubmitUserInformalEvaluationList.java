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
import gr.iit.demokritos.cru.cps.controllers.CreativityExhibitModelController;
import gr.iit.demokritos.cru.cps.validators.UserManager;
import gr.iit.demokritos.cru.database.MySQLConnector;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
 * @author Pythagoras Karampiperis, Sotiris Konstantinidis
 */
public class SubmitUserInformalEvaluationList {

    private String application_key = "";
    private String evaluator_id = "";
    private String users_list = "";
    private Map properties = new HashMap();

    public SubmitUserInformalEvaluationList(String application_key, String evaluator_id, String users_list, Map properties) {
        this.application_key = application_key;
        this.evaluator_id = evaluator_id;
        this.users_list = users_list;
        this.properties = properties;
    }

    public JSONObject processRequest() throws IOException, Exception {

        String response_code = "e0";
        long user_id = 0;

        String[] users = users_list.split(";");
        ArrayList<Long> users_id = new ArrayList<Long>();

        for (int i = 0; i < users.length; i++) {
            users_id.add(Long.parseLong(users[i]));
        }

        String location = (String) properties.get("location");
        String database_name = (String) properties.get("database_name");
        String username = (String) properties.get("username");
        String password = (String) properties.get("password");

        MySQLConnector mysql = new MySQLConnector(location, database_name, username, password);

        try {
            user_id = Long.parseLong(evaluator_id);
            Connection connection = mysql.connectToCPSDatabase();
            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT window FROM windows WHERE current_window=1");
            int window = -1;
            while (rs.next()) {
                window = rs.getInt(1);
            }
            rs.close();

            UserManager um = new UserManager(Long.parseLong(application_key), user_id, users_id);
            CreativityExhibitModelController cemc = new CreativityExhibitModelController(window, Long.parseLong(application_key), user_id, users_id);

            boolean isvalid = false;
            isvalid = um.validateClientApplication(mysql);
            if (isvalid == true) {
                isvalid = um.validateUser(mysql);
                if (isvalid == true) {
                    Iterator it = users_id.iterator();
                    while (it.hasNext()) {
                        um.setUser_id((Long) it.next());
                        isvalid = um.validateUser(mysql);
                        if (isvalid == false) {
                            break;
                        }
                    }
                } else {
                    response_code = "e102";
                }

                if (isvalid == true) {
                    cemc.storeEvaluation(mysql);
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
            Logger.getLogger(CreateUser.class.getName()).log(Level.SEVERE, null, ex);
        }

        JSONArray list = new JSONArray();

        for (Long temp_user : users_id) {
            list.add(Long.toString(temp_user));
        }

        JSONObject obj = new JSONObject();

        obj.put("application_key", application_key);
        obj.put("user_id", Long.toString(user_id));
        obj.put("users_id", list);
        obj.put("response_code", response_code);

        return obj;
    }

}
