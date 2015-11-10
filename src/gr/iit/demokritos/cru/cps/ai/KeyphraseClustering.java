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
package gr.iit.demokritos.cru.cps.ai;

import gr.iit.demokritos.cru.cps.utilities.wordnet.WNAccess;
import gr.iit.demokritos.cru.cps.utilities.wordnet.WNDE;
import gr.iit.demokritos.cru.cps.utilities.wordnet.WNEL;
import gr.iit.demokritos.cru.cps.utilities.wordnet.WordNetDeDistance;
import gr.iit.demokritos.cru.cps.utilities.wordnet.WordNetENDistance;
import gr.iit.demokritos.cru.cps.utilities.wordnet.WordNetElDistance;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;
import weka.clusterers.EM;
import weka.clusterers.HierarchicalClusterer;
import weka.clusterers.SimpleKMeans;
import weka.clusterers.XMeans;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author Antonis Koukourikos
 */
public class KeyphraseClustering {

    private ArrayList<String> keys;
    private String language;
    private int clusters;
    private WordNetENDistance wd;
    private WordNetDeDistance wdde;
    private WordNetElDistance wdel;
    private WNAccess wn;
    private WNDE wnde;
    private WNEL wnel;
    private Instances data;
    //private int clusters;
    // private HashMap<String, String> tokensHashMap = new HashMap<String, String>();

    public KeyphraseClustering(ArrayList<String> k, int numberOfClust, String language,WNAccess wn,WNDE wnde,WNEL wnel) throws ClassNotFoundException {
        this.language = language;
        
        this.wn=wn;
        this.wd = new WordNetENDistance();
        this.wdde= new WordNetDeDistance(wnde);
        this.wdel= new WordNetElDistance(wnel);
        
        this.keys = new ArrayList<String>();
        for (int i = 0; i < k.size(); i++) {
            String[] tokenLine = k.get(i).split(";");
            String key = tokenLine[0];
            this.keys.add(key);
        }
        Attribute words = new Attribute("words", (FastVector) null);
        FastVector fvWekaAttributes = new FastVector();
        fvWekaAttributes.addElement(words);
        this.data = new Instances("words", fvWekaAttributes, 0);
        
        double sum = 0.0;
        for (String s : this.keys) {
            //keep the sum of the semantic distance between all the words
            for (String p : this.keys) {
                if (!p.equalsIgnoreCase(s)) {
                    sum += getDistance(s, p);
                }
            }
            //create new instance for every key and add it to the data
            Instance inst = new Instance(1);
            //System.out.println(fvWekaAttributes.get(0));   //System.out.println(data.attribute(0));
            inst.setValue(this.data.attribute(0), s);
            // data.add(new inst(1.0,vals));
            this.data.add(inst);
        }
        if (numberOfClust == 0) {
            //sum += keys.size();
            int numerator = (int) ceil(sum);
            int clust = (int) ceil(numerator / (double)(this.keys.size()));
            this.clusters = clust;
        } else {
            this.clusters = numberOfClust;
        }
    }

    public ArrayList<String> getClusters() throws Exception {
        System.out.println("Clustering......");
        // int[] clusters_size = new int[clusters];
        HierarchicalClusterer cl = new HierarchicalClusterer();
        // EM em=new EM();
        // XMeans xm = new XMeans();       no nominal attributes
        // DBSCAN db= new DBSCAN();        not our distance function
        // CascadeSimpleKMeans c = new CascadeSimpleKMeans(); not our distance function  
        
        cl.setNumClusters(this.clusters);
        if (language.equals("en")) {
            // cl.setDistanceFunction(wd);
            //xm.setDistanceF(wd);
            cl.setDistanceFunction(wd);
        } else if (language.equals("de")) {
            cl.setDistanceFunction(wdde);
            //c.setDistanceFunction(wdde);
            //xm.setDistanceF(wdde);
        } else if (language.equals("el")) {
            cl.setDistanceFunction(wdel);
            //c.setDistanceFunction(wdel);
            // xm.setDistanceF(wdel);
        }
        cl.buildClusterer(data);
        //xm.buildClusterer(data);
        //c.setMaxIterations(5);
        ArrayList<String> clustersList = new ArrayList<String>();
        for (int i = 0; i < cl.numberOfClusters(); i++) {
            clustersList.add("");
        }
        //cl.buildClusterer(data);
        //em.buildClusterer(data);
        // xm.buildClusterer(data);

        for (int j = 0; j < data.numInstances(); j++) {
            //double[] prob = c.distributionForInstance(data.instance(j));
            //double[] prob = cl.distributionForInstance(data.instance(j)); 
            String clusterLine = data.instance(j).stringValue(0);

            int clust = cl.clusterInstance(data.instance(j));
            clustersList.set(clust, clustersList.get(clust).concat(clusterLine + ";"));
            //take the probabilities prob[i] that it is in the coresponding cluster i
            /*for (int i = 0; i < prob.length; i++) {
             //keep the cluster that has prob>0.9, as this is the cluster that the word is in
             if (prob[i] > 0.9) {
             //keep for every cluster its terms
             clustersList.set(i, clustersList.get(i).concat(clusterLine + ";"));
             //keep the size of cluster i
             // clusters_size[i] = clusters_size[i] + 1;
             }
             }*/
        }
        return clustersList;
    }
    
    public double getDistance(String s1, String s2) {
        double sem = 0.0;
        if (this.language.equalsIgnoreCase("en")) {
            sem = 0.75 * wn.getDistance(s1, s2);
        } else if (this.language.equalsIgnoreCase("de")) {
            sem = 0.75 * this.wnde.getDistance(s1, s2);
        } else if (this.language.equalsIgnoreCase("el")) {
            sem = 0.75 * wnel.getDistance(s1, s2);
        }
        double lev = 0.25 * getLevenshteinDistance(s1, s2) / ((s1.length() + s2.length()) / 2);
        //System.out.println(w1 + "-" + w2 + ": " + d);
        return sem + lev;
    }
}
