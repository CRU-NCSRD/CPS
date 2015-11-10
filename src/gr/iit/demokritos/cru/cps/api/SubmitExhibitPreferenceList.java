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

import gr.iit.demokritos.cru.cps.ai.InfoSummarization;
import gr.iit.demokritos.cru.cps.utilities.wordnet.WNAccess;
import gr.iit.demokritos.cru.cps.utilities.wordnet.WNDE;
import gr.iit.demokritos.cru.cps.utilities.wordnet.WNEL;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
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
public class SubmitExhibitPreferenceList {

    private String application_key = "";
    private String user_id = "";
    private String exhibit_list_id = "";
    private String preference_list_id = "";
    private Map properties = new HashMap();

    public SubmitExhibitPreferenceList(String application_key, String user_id, String exhibit_list_id, String preference_list_id, Map properties) {
        this.application_key = application_key;
        this.user_id = user_id;
        this.exhibit_list_id = exhibit_list_id;
        this.preference_list_id = preference_list_id;
        this.properties = properties;
    }

    public JSONObject processRequest() throws IOException, ClassNotFoundException, IllegalAccessException, Exception {

        String response_code = "e0";

        String[] exhibit_list_temp = exhibit_list_id.split(";");
        ArrayList<String> exhibit_list = new ArrayList<String>();

        for (int i = 0; i < exhibit_list_temp.length; i++) {
            exhibit_list.add(exhibit_list_temp[i]);
        }

        String location = (String) properties.get("location");
        String database_name = (String) properties.get("database_name");
        String username = (String) properties.get("username");
        String password = (String) properties.get("password");

        String database_dir = (String) properties.get("wordnet.database.dir");
        String thesaurus_de = (String) properties.get("openthesaurus_DE");
        String thesaurus_el = (String) properties.get("openthesaurus_EL");
        String stopWordsEN = (String) properties.get("stopWordsFile_EN");
        String stopWordsGR = (String) properties.get("stopWordsFile_GR");
        String stopWordsDE = (String) properties.get("stopWordsFile_DE");
        String offensiveWords = (String) properties.get("offensiveWordsFile");

        MySQLConnector mysql = new MySQLConnector(location, database_name, username, password);

        try {
            Connection connection = mysql.connectToCPSDatabase();

            Statement stmt = connection.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT window FROM windows WHERE current_window=1");
            int window = -1;
            while (rs.next()) {
                window = rs.getInt(1);
            }
            rs.close();

            CreativityExhibitModelController cemc = new CreativityExhibitModelController(Long.parseLong(application_key), Long.parseLong(user_id), exhibit_list, window);
            boolean isvalid = cemc.validateClientApplication(mysql);
            UserManager um = new UserManager(Long.parseLong(application_key), Long.parseLong(user_id));
            if (isvalid == true) {
                isvalid = cemc.validateUser(mysql);
                System.out.println("");
                if (isvalid == true) {
                    Iterator it = exhibit_list.iterator();

                    while (it.hasNext()) {
                        um.setExhibit_id(Long.parseLong((String) it.next()));
                        isvalid = um.validateExhibit(mysql);
                        if (isvalid == false) {
                            break;
                        }
                    }
                    if (isvalid == true) {
                        cemc.storeExhbitPreferenceList(mysql);
                        preference_list_id = Long.toString(cemc.getPreference_id());
                        response_code = "OK";

                    } else {
                        response_code = "e105";
                    }

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
            Logger.getLogger(SubmitExhibitPreferenceList.class.getName()).log(Level.SEVERE, null, ex);
        }

        JSONObject obj = new JSONObject();

        obj.put("application_key", application_key);
        obj.put("user_id", user_id);
        obj.put("preference_list_id", preference_list_id);
        obj.put("response_code", response_code);
        return obj;
    }

}
