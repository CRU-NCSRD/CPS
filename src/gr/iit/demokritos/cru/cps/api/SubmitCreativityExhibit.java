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
public class SubmitCreativityExhibit {

    private String application_key = "";
    private String user_id ="";
    private String exhibit = "";
    private String type = "";
    private String language = "";
    private Map properties = new HashMap();
    
    public SubmitCreativityExhibit(String application_key,String user_id,String exhibit,String type,String language,Map properties) {
        this.application_key = application_key;
        this.user_id=user_id;
        this.exhibit=exhibit;
        this.type=type;
        this.language=language;
        this.properties = properties;
    }

    public JSONObject processRequest() throws IOException, ClassNotFoundException, IllegalAccessException, Exception {
        String response_code = "e0";
        long exhibit_id = 0;

        exhibit = java.net.URLDecoder.decode(exhibit, "UTF-8");
        if (exhibit.isEmpty() || exhibit.length() < 2) {
            response_code = "e104";
        }
        if (!exhibit.endsWith(".")) {
            exhibit = exhibit + ".";
        }
        
        if (!((language == "en") || (language == "de") || (language == "el"))) {
            response_code = "e107";
        }
        ArrayList<Metric> metric = new ArrayList<Metric>();

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

            CreativityExhibitModelController cemc = new CreativityExhibitModelController(Long.parseLong(application_key), Long.parseLong(user_id), exhibit, type, window, language);
            boolean isvalid = false;
            isvalid = cemc.validateClientApplication(mysql);
            if (!(response_code.equalsIgnoreCase("e104")) || (response_code.equalsIgnoreCase("e107"))) {
                if (isvalid == true) {
                    isvalid = cemc.validateUser(mysql);
                    if (isvalid == true) {
                        isvalid = cemc.validateType(mysql);
                        if (isvalid == false) {
                            response_code = "e103";
                        }
                    } else {
                        response_code = "e102";
                    }
                } else {
                    response_code = "e101";
                }
            }
            if (isvalid == true) {
                ArrayList<String> thesaurus = new ArrayList<String>();
                thesaurus.add(thesaurus_de);
                thesaurus.add(thesaurus_el);

                String stopWordsFile = "";
                if (language.equalsIgnoreCase("en")) {
                    stopWordsFile = stopWordsEN;
                } else if (language.equalsIgnoreCase("de")) {
                    stopWordsFile = stopWordsDE;
                } else {
                    stopWordsFile = stopWordsGR;
                }

                metric = cemc.invokeComputationalCreativityMetricsCalculator(mysql, database_dir, thesaurus, stopWordsFile, offensiveWords);
                if (metric.isEmpty()) {
                    response_code = "e301";
                } else {
                    cemc.storeEvidenceandMetrics(mysql);
                    exhibit_id = cemc.getExhibit_id();
                    response_code = "OK";
                }
            }

        } catch (NumberFormatException ex) {
            response_code = "e101";
            Logger.getLogger(CreateUser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(SubmitCreativityExhibit.class.getName()).log(Level.SEVERE, null, ex);
        }

        JSONObject obj_metric = new JSONObject();
        JSONArray list = new JSONArray();
        if (!metric.isEmpty()) {
            for (Metric temp_metric : metric) {
                obj_metric.put(temp_metric.getName(), temp_metric.getValue());
            }
        }

        list.add(obj_metric);

        JSONObject obj = new JSONObject();

        obj.put("application_key", application_key);
        obj.put("user_id", user_id);
        obj.put("exhibit_id", Long.toString(exhibit_id));
        obj.put("exhibit_type", type);
        obj.put("metrics", list);
        obj.put("response_code", response_code);

        return obj;
    }
}
