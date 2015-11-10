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
package gr.iit.demokritos.cru.cpserver;

import gr.iit.demokritos.cru.cps.api.CreateGroup;
import gr.iit.demokritos.cru.cps.api.CreateUser;
import gr.iit.demokritos.cru.cps.api.GetUserProfile;
import gr.iit.demokritos.cru.cps.api.SubmitCreativityExhibit;
import gr.iit.demokritos.cru.cps.api.SubmitExhibitPreferenceList;
import gr.iit.demokritos.cru.cps.api.SubmitUserFormalEvaluationList;
import gr.iit.demokritos.cru.cps.api.SubmitUserInformalEvaluationList;
import gr.iit.demokritos.cru.cps.scheduler.Bookeeper;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 *
 * @author George Panagopoulos
 */
public class CPServer extends Thread {

    /**
     * @param args the command line arguments
     */
    private int port;
    private InputStream input;
    private Map properties;

    public CPServer(int port, InputStream input) {
        this.port = port;
        this.input = input;
        this.properties = loadproperties();
    }

    public void httpHandler(BufferedReader input, DataOutputStream output) throws IOException, Exception {

        String request = input.readLine();
        request = request.toUpperCase();
        if (request.startsWith("POST")) {

            output.writeBytes(create_response_header(200));
            output.writeBytes(create_response_body(request));
        } else if (request.startsWith("GET")) {
            //ERROR MSG
            //output.writeBytes("ERROR 404");
            output.writeBytes(create_response_header(200));
            output.writeBytes(create_response_body(request));
        } else {
            //ERROR MSG
            output.writeBytes("404");
        }

        output.close();
    }

    public String create_response_header(int return_code) {

        String response_header = "HTTP/1.0 ";
        response_header = response_header + "200 OK";

        response_header = response_header + "\r\n"; //other header fields,
        response_header = response_header + "Connection: close\r\n"; //we can't handle persistent connections
        response_header = response_header + "Server: CPServer v0\r\n"; //server name

        response_header = response_header + "Content-Type: application/json\r\n";
        response_header = response_header + "Access-Control-Allow-Origin: *\r\n";
        response_header = response_header + "\r\n";

        return response_header;
    }

    public String create_response_body(String request) throws IOException, Exception {

        // String response_body = "Hello World";
        //  System.out.println(response_body);
        return parsePost(request);
    }

    private String parsePost(String request) throws IOException, Exception {

        //FIX THE ERRORS AS JSONS!!!!!!!!!!!!!!!!!!!!!S
        request = request.replaceAll("POST", "");
        JSONObject jso = new JSONObject();

        if (request.contains("HTTP/1.1")) {
            request = request.replaceAll("HTTP/1.1", "");
        } else {
            return " not HTTP/1.1 header";
            //error msg
        }
        request = request.trim();
        String[] MethodAndParams = request.split("\\?");
        if (MethodAndParams.length > 1) { //if the request has ? or parameters
            String method = MethodAndParams[0].replaceAll("/", "");
            String query = MethodAndParams[1];
            Map<String, String> params = parseQuery(query);
            System.out.println("method " + method);
            for (String s : params.keySet()) {
                System.out.println(s + " value=" + params.get(s));
            }
            if (method.equalsIgnoreCase("CreateUser")) {
                CreateUser cu = new CreateUser(params.get("application_key"), this.properties);
                jso = cu.processRequest();
            } else if (method.equalsIgnoreCase("GetUserProfile")) {
                GetUserProfile gup = new GetUserProfile(params.get("application_key"), params.get("user_id"), this.properties);
                jso = gup.processRequest();
            } else if (method.equalsIgnoreCase("CreateGroup")) {
                CreateGroup cg = new CreateGroup(params.get("application_key"), params.get("users_list"), this.properties);
                jso = cg.processRequest();
            } else if (method.equalsIgnoreCase("SubmitCreativityExhibit")) {
                SubmitCreativityExhibit sce = new SubmitCreativityExhibit(params.get("application_key"), params.get("user_id"), params.get("exhibit"), params.get("type"), params.get("language"), this.properties);
                jso = sce.processRequest();
            } else if (method.equalsIgnoreCase("SubmitExhibitPreferenceList")) {
                SubmitExhibitPreferenceList sepl = new SubmitExhibitPreferenceList(params.get("application_key"), params.get("user_id"), params.get("exhibit_list_id"), params.get("preference_list_id"), this.properties);
                jso = sepl.processRequest();
            } else if (method.equalsIgnoreCase("SubmitUserFormalEvaluationList")) {
                SubmitUserFormalEvaluationList sufel = new SubmitUserFormalEvaluationList(params.get("application_key"), params.get("users_list"), this.properties);
                jso = sufel.processRequest();
            } else if (method.equalsIgnoreCase("SubmitUserInformalEvaluationList")) {
                SubmitUserInformalEvaluationList suiel = new SubmitUserInformalEvaluationList(params.get("application_key"), params.get("evaluator_id"), params.get("users_list"), this.properties);
                jso = suiel.processRequest();
            } else {
                return "not known method";
            }

        } else {
            return "no parameters given";
        }
        return jso.toString(); //return the answer
    }

    private Map<String, String> parseQuery(String query)
            throws UnsupportedEncodingException {
        Map<String, String> parameters = new HashMap<String, String>();
        if (query != null) {
            String pairs[] = query.split("[&]");

            for (String pair : pairs) {
                String param[] = pair.split("[=]");

                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
                }

                if (param.length > 1) {//if the parameter is not given a value, the value stays null
                    value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
                }

                /*if (parameters.containsKey(key)) {
                 Object obj = parameters.get(key);
                 if(obj instanceof List) {
                 List values = (List)obj;
                 values.add(value);
                 } else if(obj instanceof String) {
                 List values = new ArrayList();
                 values.add((String)obj);
                 values.add(value);
                 parameters.put(key, values);
                 }
                 } else {*/
                parameters.put(key, value);

            }
        }
        return parameters;
    }

    public void StartCPServer() throws IOException, Exception {
        ServerSocket serversocket = new ServerSocket(port);
        System.out.println("Start CPServer");
        Bookeeper bk=new Bookeeper(this.properties);
        while (true) {
            System.out.println("Inside While");
            Socket connectionsocket = serversocket.accept();
            System.out.println("Inet");
            //InetAddress client = connectionsocket.getInetAddress();

            BufferedReader input = new BufferedReader(new InputStreamReader(connectionsocket.getInputStream()));
            DataOutputStream output = new DataOutputStream(connectionsocket.getOutputStream());

            httpHandler(input, output);
        }
    }

    public Map loadproperties() {

        Map properties = new HashMap();

        Properties prop = new Properties();

        String location = "";
        String username = "";
        String password = "";
        String database_name = "";

        String database_dir = "";
        String thesaurus_de = "";
        String thesaurus_el = "";
        String stopWordsEN = "";
        String stopWordsGR = "";
        String stopWordsDE = "";
        String offensiveWords = "";
        String time_window = "";

        try {

            // load a properties file
            prop.load(this.input);

            location = prop.getProperty("location");
            username = prop.getProperty("username");
            password = prop.getProperty("password");
            database_name = prop.getProperty("database_name");

            database_dir = prop.getProperty("wordnet.database.dir");
            thesaurus_de = prop.getProperty("openthesaurus_DE");
            thesaurus_el = prop.getProperty("openthesaurus_EL");
            stopWordsEN = prop.getProperty("stopWordsFile_EN");
            stopWordsGR = prop.getProperty("stopWordsFile_GR");
            stopWordsDE = prop.getProperty("stopWordsFile_DE");
            offensiveWords = prop.getProperty("offensiveWordsFile");
            time_window = prop.getProperty("time_window");

            properties.put("location", location);
            properties.put("database_name", database_name);
            properties.put("username", username);
            properties.put("password", password);

            properties.put("wordnet.database.dir", database_dir);
            properties.put("openthesaurus_DE", thesaurus_de);
            properties.put("openthesaurus_EL", thesaurus_el);
            properties.put("stopWordsFile_EN", stopWordsEN);
            properties.put("stopWordsFile_GR", stopWordsGR);
            properties.put("stopWordsFile_DE", stopWordsDE);
            properties.put("offensiveWordsFile", offensiveWords);
            properties.put("time_window", time_window);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (this.input != null) {
                try {
                    this.input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return properties;

    }

    public void run() {
        try {
            System.out.println("RUN");
            StartCPServer();
        } catch (IOException ex) {
            Logger.getLogger(CPServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
