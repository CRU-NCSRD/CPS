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

import gr.iit.demokritos.cru.cps.ai.ComputationalCreativityMetrics;
import gr.iit.demokritos.cru.cps.ai.InfoSummarization;
import gr.iit.demokritos.cru.cps.utilities.wordnet.WNAccess;
import gr.iit.demokritos.cru.cps.utilities.wordnet.WNDE;
import gr.iit.demokritos.cru.cps.utilities.wordnet.WNEL;
import gr.iit.demokritos.cru.cps.validators.ClientApplicationValidator;
import gr.iit.demokritos.cru.cps.Metric;
import gr.iit.demokritos.cru.cps.validators.UserManager;
import gr.iit.demokritos.cru.database.MySQLConnector;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import javax.sql.rowset.serial.SerialBlob;

/**
 *
 * @author Pythagoras Karampiperis
 */
public class CreativityExhibitModelController {
    
    private long application_id;
    private long user_id;
    private long exhibit_id;
    private int window;
    private long preference_id;
    private long evaluation_id;
    
    private String exhibit;
    private String language;
    private String type;
    private int type_id;
    
    private ArrayList<String> exhibitsList = new ArrayList<String>();
    private ArrayList<Metric> ccm=new ArrayList<Metric>();
    private ArrayList<String> exhibitsPreferenceList = new ArrayList<String>();
    ArrayList<Long> preferredUsersList=new ArrayList<Long>();
    
    public CreativityExhibitModelController(long application_id, long user_id, String exhibit, String type, int window,String language) {
        this.application_id = application_id;
        this.user_id = user_id;
        this.exhibit = exhibit;
        this.type = type.trim();
        this.window=window;
        this.language=language;
    }

    public CreativityExhibitModelController(int window,long application_id, long user_id, ArrayList<Long> preferredUsersList) {
        this.application_id = application_id;
        this.user_id = user_id;
        this.preferredUsersList = preferredUsersList;
        this.window=window;
    }
    
    
    public CreativityExhibitModelController(long application_id, long user_id, ArrayList<String> exhibitsPreferenceList, int window) {
        this.application_id = application_id;
        this.user_id = user_id;
        this.exhibitsPreferenceList = exhibitsPreferenceList;
        this.window = window;
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
    
    public boolean validateType(MySQLConnector mysql) throws SQLException{
        ClientApplicationValidator vac=new ClientApplicationValidator(this.type);
        boolean isvalid=vac.validateExhibitType(mysql);
        return isvalid;        
    }

    public long getPreference_id() {
        return preference_id;
    }

    public void setPreference_id(long preference_id) {
        this.preference_id = preference_id;
    }
    
    public boolean retrieveApplicationExhibits(MySQLConnector mysql) throws SQLException{
        
        ArrayList<Long> userList = new ArrayList<Long>();
        Connection connection=mysql.connectToCPSDatabase();
        
        //retrieve User IDs
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT user_id FROM users WHERE application_key="+this.application_id);
        
        while (rs.next()) {
            long user_id = rs.getLong(1);
            userList.add(user_id);           
        }
        rs.close();
        
        rs = stmt.executeQuery("SELECT type_id FROM typeofexhibit WHERE name=\""+this.type+"\"");
        
        while (rs.next()) {
            this.type_id = Integer.parseInt(rs.getString(1));               
        }
        rs.close();
        
        //retrieveEvidences
        Iterator iterator = userList.iterator();
        while (iterator.hasNext()) {
            long user_id=(Long)iterator.next();
            rs = stmt.executeQuery("SELECT artifact FROM exhibitinstance WHERE user_id="+user_id+" AND type_id="+this.type_id+" AND window="+(this.window-1)+" AND language_code=\""+this.language+"\"");
            while (rs.next()) {  
                String artifact = rs.getString(1);                
                exhibitsList.add(artifact);  
            }
            rs.close();            
        }
        connection.close();
        if(exhibitsList.size()>0){
            return true;
        }else{
            return false;
        }
        
    }
    
    public ArrayList<Metric> invokeComputationalCreativityMetricsCalculator(MySQLConnector mysql,String database_dir,ArrayList<String> thesaurus,String stopWordsFile,String offensiveWordsFile) throws ClassNotFoundException, IllegalAccessException, Exception{
        System.out.println("CCM Preparation");        
       
        
        WNAccess wn = new WNAccess(database_dir);
        WNDE wnde = new WNDE(thesaurus.get(0));        
        WNEL wnel = new WNEL(thesaurus.get(1));
        
        
        Set<String> stop = new HashSet<String>();
        Set<String> offensive = new HashSet<String>();
        
       
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(stopWordsFile), "UTF8"));
        String stopWord = new String();
        while ((stopWord = br.readLine()) != null) {
            if (stopWord.trim().compareToIgnoreCase("") == 0) {
                continue;
            }
            stop.add(stopWord.trim());
        }
        br.close();
        
        BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(offensiveWordsFile), "UTF8"));
            String offWord = new String();
            while ((offWord = br2.readLine()) != null) {
                if (offWord.trim().compareToIgnoreCase("") == 0) {
                    continue;
                }
            offensive.add(offWord.trim());
            }
        br2.close();
        
        InfoSummarization inf = new InfoSummarization(this.language,stop,offensive,wn,wnde,wnel);
        
        System.out.println("CCM Invoked");
        boolean hasexhibits=retrieveApplicationExhibits(mysql);
        
        if(hasexhibits==true){
            ComputationalCreativityMetrics m = new ComputationalCreativityMetrics(this.language,wn,wnde,wnel,inf,stop,offensive);
            this.ccm.clear();
            this.ccm = m.ComputationalCreativityMetricsCalculator(this.exhibit, this.exhibitsList,this.type);
        }else{
            ComputationalCreativityMetrics m = new ComputationalCreativityMetrics(this.language,wn,wnde,wnel,inf,stop,offensive);
            this.ccm.clear();
            this.exhibitsList.add(this.exhibit);
            this.ccm = m.ComputationalCreativityMetricsCalculator(this.exhibit, this.exhibitsList,this.type);
        }
        
        return this.ccm;
    }
    
    public void storeEvidenceandMetrics(MySQLConnector mysql) throws SQLException{
        
        Connection connection=mysql.connectToCPSDatabase();
        connection.setAutoCommit(false);
        
        Statement stmt = connection.createStatement();
        ResultSet rs = null;
        
        UUID uid=UUID.randomUUID();
        this.exhibit_id=uid.getMostSignificantBits();
        
        if(this.exhibit_id<0){
            this.exhibit_id=this.exhibit_id*-1;
        }
        
        boolean isuuidvalid=checkIfUUIDisValid(mysql,"exhibit_id");
        
        while(!isuuidvalid){
            this.exhibit_id=uid.getMostSignificantBits();
            isuuidvalid=checkIfUUIDisValid(mysql,"exhibit_id");
        }
        
        String artifact=this.exhibit;             
        
        long timestamp=System.currentTimeMillis()/1000L;        
        String sql="INSERT INTO exhibitinstance (exhibit_id,user_id,type_id,timestamp,artifact,window,language_code) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement insert_exhibit = connection.prepareStatement(sql);
        
        insert_exhibit.setLong(1, this.exhibit_id);
        insert_exhibit.setLong(2, this.user_id);
        insert_exhibit.setInt(3, this.type_id);
        insert_exhibit.setLong(4, timestamp);
        insert_exhibit.setString(5, artifact);
        insert_exhibit.setInt(6, this.window);
        insert_exhibit.setString(7, this.language);
        
        insert_exhibit.executeUpdate();
        
     
        for (Metric metr : ccm) {
            String name=metr.getName();
            double calculation=metr.getValue();
            
            String query="SELECT metric_id FROM metrics where name=\""+name+"\"";
            rs=stmt.executeQuery(query);
            
            int metric_id=-1;
            while (rs.next()) {
                metric_id = rs.getInt(1);
            } 
            rs.close();
            
            rs = stmt.executeQuery("SELECT MAX(calculation_id) FROM metricscalculations");
            int calculation_id=-1;
            while (rs.next()) {
                calculation_id = rs.getInt(1)+1;
            }       
            rs.close();
            
            sql="INSERT INTO metricscalculations VALUES("+calculation_id+","+exhibit_id+","+metric_id+","+calculation+")";
            stmt.executeUpdate(sql);
        }
        
        connection.commit();
        connection.close();
    }

    public long getExhibit_id() {
        return exhibit_id;
    }

    public void setExhibit_id(long exhibit_id) {
        this.exhibit_id = exhibit_id;
    }
    
    private boolean checkIfUUIDisValid(MySQLConnector mysql,String type) throws SQLException{
        
        Connection connection=mysql.connectToCPSDatabase();
        Statement stmt = connection.createStatement();
        ResultSet rs=null;
        if(type.equalsIgnoreCase("exhibit_id")){
            rs= stmt.executeQuery("SELECT exhibit_id FROM exhibitinstance WHERE exhibit_id="+this.exhibit_id);
        }else if(type.equals("preference_id")){
            rs= stmt.executeQuery("SELECT preference_id FROM exhibitpreferencelists WHERE preference_id="+this.preference_id);
        }else if(type.equals("evaluation_id")){
            rs= stmt.executeQuery("SELECT evaluation_id FROM informalevaluationlists WHERE evaluation_id="+this.evaluation_id);
        }else{
            rs= stmt.executeQuery("SELECT fevaluation_id FROM formalevaluationlists WHERE fevaluation_id="+this.evaluation_id);
        }       
        
        if(!rs.next()){            
            return true;
            
        }else{
            return false;
        }
    }
    
    public void storeExhbitPreferenceList(MySQLConnector mysql) throws SQLException{
        
        Connection connection=mysql.connectToCPSDatabase();
        Statement stmt = connection.createStatement();
        connection.setAutoCommit(false);
        
        UUID uid=UUID.randomUUID();
        this.preference_id=uid.getMostSignificantBits();
        
        if(this.preference_id<0){
            this.preference_id=this.preference_id*-1;
        }
        
        boolean isuuidvalid=checkIfUUIDisValid(mysql,"preference_id");
        
        while(!isuuidvalid){
            this.preference_id=uid.getMostSignificantBits();
            isuuidvalid=checkIfUUIDisValid(mysql,"preference_id");
        }
        Iterator it=this.exhibitsPreferenceList.iterator();
        long timestamp=System.currentTimeMillis()/1000L;
        int order=0;
        while(it.hasNext()){
            String exhibit_id=(String)it.next();
                    
            String sql="INSERT INTO exhibitpreferencelists VALUES("+this.preference_id+","+this.user_id+","+this.application_id+","+Long.parseLong(exhibit_id)+","+timestamp+","+this.window+","+order+")";
            stmt.executeUpdate(sql);
            order++;
        }
        connection.commit();
    }
    
     public void storeEvaluation(MySQLConnector mysql) throws SQLException{
        
        Connection connection=mysql.connectToCPSDatabase();
        Statement stmt = connection.createStatement();
        connection.setAutoCommit(false);
        
        UUID uid=UUID.randomUUID();
        this.evaluation_id=uid.getMostSignificantBits();
        
        if(this.evaluation_id<0){
            this.evaluation_id=this.evaluation_id*-1;
        }
        
        boolean isuuidvalid=checkIfUUIDisValid(mysql,"evaluation_id");
        
        while(!isuuidvalid){
            this.evaluation_id=uid.getMostSignificantBits();
            isuuidvalid=checkIfUUIDisValid(mysql,"evaluation_id");
        }
        Iterator it=this.preferredUsersList.iterator();
        long timestamp=System.currentTimeMillis()/1000L;
        int order=0;
        while(it.hasNext()){
            Long user_id=(Long)it.next();
                    
            String sql="INSERT INTO informalevaluationlists VALUES("+this.evaluation_id+","+this.user_id+","+user_id+","+order+","+timestamp+","+this.window+","+this.application_id+")";
            System.out.println(sql);
            stmt.executeUpdate(sql);
            order++;
        }
        connection.commit();
    }
    public void storeFormalEvaluation(MySQLConnector mysql) throws SQLException{
        
        Connection connection=mysql.connectToCPSDatabase();
        Statement stmt = connection.createStatement();
        connection.setAutoCommit(false);
        
        UUID uid=UUID.randomUUID();
        this.evaluation_id=uid.getMostSignificantBits();
        
        if(this.evaluation_id<0){
            this.evaluation_id=this.evaluation_id*-1;
        }
        
        boolean isuuidvalid=checkIfUUIDisValid(mysql,"fevaluation_id");
        
        while(!isuuidvalid){
            this.evaluation_id=uid.getMostSignificantBits();
            isuuidvalid=checkIfUUIDisValid(mysql,"fevaluation_id");
        }
        Iterator it=this.preferredUsersList.iterator();
        long timestamp=System.currentTimeMillis()/1000L;
        int order=0;
        while(it.hasNext()){
            Long user_id=(Long)it.next();
            
            String sql="INSERT INTO formalevaluationlists VALUES("+this.evaluation_id+","+this.application_id+","+user_id+","+order+","+timestamp+","+this.window+")";        
            System.out.println(sql);
            stmt.executeUpdate(sql);
            order++;
        }
        connection.commit();
    }
}
