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

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import gr.iit.demokritos.cru.cps.ai.InfoSummarization;
import gr.iit.demokritos.cru.cps.ai.KeyphraseClustering;
import gr.iit.demokritos.cru.cps.utilities.wordnet.WNAccess;
import gr.iit.demokritos.cru.cps.utilities.wordnet.WNDE;
import gr.iit.demokritos.cru.cps.utilities.wordnet.WNEL;
import gr.iit.demokritos.cru.cps.utilities.wordnet.WordNetENDistance;
import gr.iit.demokritos.cru.cps.Metric;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;
import weka.core.Instances;

/**
 *
 * @author Giorgos Panagopoulos, Pythagoras Karampiperis, Antonis Koukourikos
 */
public class ComputationalCreativityMetrics {

    private Instances data;
    private String language;
    private WNAccess wn;
    private WNDE wnde;
    private WNEL wnel;
    private InfoSummarization inf;
    private Set<String> stop;
    private Set<String> off;
    private Class stemCLass;

    public ComputationalCreativityMetrics(String language,WNAccess wn,WNDE wnde,WNEL wnel,InfoSummarization inf,Set<String> stop,Set<String> off) throws ClassNotFoundException {
        this.language = language;
        
        this.wn=wn;
        this.wnde=wnde;
        this.wnel=wnel;
        this.inf=inf;
        this.stop=stop;
        this.off=off;
        
        this.stemCLass = Class.forName("gr.iit.demokritos.cru.cps.utilities.snowball.ext.englishStemmer");
        
        if (language.equalsIgnoreCase("de")) {
            this.stemCLass = Class.forName("gr.iit.demokritos.cru.cps.utilities.snowball.ext.dutchStemmer");          
        }
       
    }

    public WNDE getWnde() {
        return wnde;
    }

    public void setWnde(WNDE wnde) {
        this.wnde = wnde;
    }

    public WNEL getWnel() {
        return wnel;
    }

    public void setWnel(WNEL wnel) {
        this.wnel = wnel;
    }

    public ArrayList<Metric> ComputationalCreativityMetricsCalculator(String NewStory, ArrayList<String> stories, String type) throws ClassNotFoundException, IllegalAccessException, Exception {

         //ArrayList<String> stories = new ArrayList<String>();//get stories
        stories.add(NewStory);
        if (type.equalsIgnoreCase("semantic")) {
                Map<String, String> com = ComputeRar_Eff(stories);
                Map<String, Double> nov = Novelty(stories);
                Map<String, Double> sur = new HashMap<String, Double>();
                String delims = "[\\.\\s;?!():\"]+";
                for (String s : stories) {
                    String h = s;
                    String[] e = h.split(delims);
                    ArrayList<String> fragments = new ArrayList<String>();
                    if (e.length > 30) { //surprise is calculated for every  30 words sentence
                        while (e.length > 30) {//30
                            String temp1 = h.substring(0, h.indexOf(e[29]));//the sentence before 30
                            String temp2 = h.substring(h.indexOf(e[29]));//the sentence after
                            String[] g = temp2.split("[\\.?;!]+", 2);//keep the words before the setence ended
                            fragments.add(temp1 + g[0]);//the fragment
                            if (g.length < 2) {
                                break;
                            }
                            e = g[1].split(delims);//the next fragments
                            h = g[1];
                        }

                        double surp = Surprise("", fragments);
                        sur.put(s, surp);  
                    } else {
                    //if the story has less then thirty words it has NO surprise
                        sur.put(s, 0.0);  
                    }
                }
            ArrayList<Metric> values = new ArrayList<Metric>();
            values.add(new Metric("Novelty", nov.get(NewStory)));
            values.add(new Metric("Surprise",sur.get(NewStory) ));
            String impr = com.get(NewStory);
           
            values.add(new Metric("Rarity",Double.parseDouble(impr.split(":::")[0])));
            values.add(new Metric("Recreational",Double.parseDouble(impr.split(":::")[1])));
            return values;
        } else {
            System.out.println("name not defined");
            return null;
        }
    }

    

    

    //compute rarity and recreational effort as defined in the paper

    public Map<String, String> ComputeRar_Eff(ArrayList<String> stories) throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
        ArrayList<Double> WeiOfClust = new ArrayList();
        ArrayList<Double> NoOfClust = new ArrayList();
        for (int i = 0; i < stories.size(); i++) {
            // System.out.println("new story " + stories.get(i));
            //take the top terms of story
            HashMap<ArrayList<String>, Double> top = inf.TopTerms(stories.get(i).replace("---", " "), true);
            //  if (top.isEmpty()) {
            //      WeiOfClust.add(0.0);
            //  }
            ArrayList<String> terms = new ArrayList<String>();
            for (ArrayList<String> stems : top.keySet()) {
                //if it is in compact form , there is only one term for each stem 
                for (int j = 0; j < stems.size(); j++) {
                    //terms and the stem's tf
                    //stems.get(i)= foo {fooing,fooed ...}
                    terms.add(stems.get(j).split("\\{")[0] + ";" + top.get(stems));
                    //System.out.println("term "+stems.get(j).split("\\{")[0]);////////////////////////////////////todo
                    // System.out.println(terms.get(j));
                }
            }
            ArrayList<String> clusters = new ArrayList<String>();
            //find the term clusters for this story
            if (terms.size()>1) {                
                KeyphraseClustering kl = new KeyphraseClustering(terms, 0, this.language,this.wn,this.wnde,this.wnel);
                clusters = kl.getClusters();               
            }

         
            //for every cluster
            double min = 10000;
            double maxclosure = 0.0;
            //System.out.println(stories.get(i));
            for (String s : clusters) {
                String p = s.replace(";", " ");
                //count the maximum possible closure the cluster may have, which is (#of words in cluster -1)*1.0, meaning the max possible distances
                maxclosure += (s.split(";").length - 1) * 1.0;
                if (!p.equalsIgnoreCase("")) {
                    //get the minclosure of the terms' graph
                    //double wei = 0.0;
                    // System.out.println(k.replace(";", ""));
                    //the clusters that have only one word have a closure of 0
                    double wei = MinClosure(p, stories.get(i));
                    //we keep the minimum minweight closure of the graph
                    if (wei < min && wei != 0) { ///todo update the online with wei!=0
                        min = wei;
                    }
                }
            }
            //System.out.println(clusters.size()+" "+min);
            if (min != 0.0 && min != 10000) {
                WeiOfClust.add(min);
            } else {
                WeiOfClust.add(maxclosure);
            }
            //the number of clusters the story had
            NoOfClust.add((double) clusters.size());
            //  System.out.println(stories.get(i) + " " + WeiOfClust.get(i) + " " + NoOfClust.get(i));
        }
        ArrayList<Double> rar = Rar_Eff(WeiOfClust);
        //we make the NoOfClust arraylist <double> to use it with the Metric method
        ArrayList<Double> recr = Rar_Eff(NoOfClust);
        //map each story with its rarity and its recreational value
        Map<String, String> raef = new HashMap<String, String>();
        for (int i = 0; i < stories.size(); i++) {
            //put in the respective story its rarity+reacreational effort /
            //story tallies to the respective number
            raef.put(stories.get(i), rar.get(i) + ":::" + recr.get(i)); //(rar.get(i) + recr.get(i)) / 2);
            //System.out.println(stories.get(i) + rar.get(i) + recr.get(i));
        }
        return raef;
    }

    //compute the formula of rarity and recreational effort with the same method
    public ArrayList<Double> Rar_Eff(ArrayList<Double> SomethingOfClust) {
        double max = Collections.max(SomethingOfClust);
        ArrayList<Double> score = new ArrayList<Double>();
        //the formula is 2*#Clusters/max(#Clusters) 
        for (int i = 0; i < SomethingOfClust.size(); i++) {
            if (max == 0) {
                score.add(0.0);
            } else {
                score.add(2.0 * SomethingOfClust.get(i) / max);
            }
        }
        return score;
    }

    //measuring the creativity points of a new phrase given old phrases
    public Double CreativityPoints(String new_phrase, ArrayList<String> phrases) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        double creat = 0.0;
        // InfoSummarization inf = new InfoSummarization(language);
        // if (this.language.equalsIgnoreCase("en")) {
        HashMap<String, Double> dict = new HashMap<String, Double>();
        for (String s : phrases) {
            //get the minclosure of the graph of those terms
            dict.put(s, MinClosure(s, ""));
            // System.out.println(s + " " + dict.get(s));
        }
        //get the minclosure of the graph of this term
        Double neo = MinClosure(new_phrase, "");
        //for each time the neo closure is bigger than one of the olds, creat++
        for (Double val : dict.values()) {
            if (neo > val) {
                creat++;
            }
        }
        //laize creat to 10
        creat = creat * 10 / phrases.size();
        //  }
        return creat;
    }

    //the closure of the graph of words based on our distance measure
    public double MinClosure(String phrase, String story) {
        double closure = 0.0;
        //in case minclosue is not called by ComputeRar_Eff
        if (story.equalsIgnoreCase("")) {
            story = phrase;
        }
        //hashmap of the terms and their index
        HashMap<String, Double> termIndex = new HashMap<String, Double>();
        //take the top terms of the phrase by their stems tf
           // HashMap<ArrayList<String>, Double> termsTf = inf.TopTerms(story.toLowerCase(), true);
        
        for (String s : phrase.split(" ")) {
            termIndex.put(s, 1.0 * story.indexOf(s));
        }
       
        //sort the hashamp (descending) and traverse it reversely, to start from the first word in the phrase
        LinkedHashMap<String, Double> sorted = inf.sortHashMapByValues(termIndex);
        ListIterator iter = new ArrayList(sorted.keySet()).listIterator(sorted.size());

        HashMap<String, Double> graph = new HashMap<String, Double>();
        //store the first word in the phrase, in order to be found in the first iteration
        graph.put(sorted.keySet().toArray()[sorted.keySet().size() - 1].toString(), 0.0);
        //for each word that comes next in the phrase
        while (iter.hasPrevious()) {
            String s = iter.previous().toString();
            //find the shortest distance from it to the root (first word)
            double min = 1.0;
            //looking through every word that has already defined its min distance to the root
            for (String k : graph.keySet()) {
                double dist = getDistance(s, k); //+ graph.get(k);
                if (dist < min) {
                    min = dist;
                }
            }
            graph.put(s, min);
            //keep the overal sum of weights of the edges
            closure += min;
        }
        return closure;

    }

    //distance of two words as defined in the paper
    public double getDistance(String s1, String s2) {
        double sem = 0.0;
        if (this.language.equalsIgnoreCase("en")) {
            sem = 0.75 * wn.getDistance(s1, s2);
        } else if (this.language.equalsIgnoreCase("de")) {
            sem = 0.75 * wnde.getDistance(s1, s2);
        } else if (this.language.equalsIgnoreCase("el")) {
            sem = 0.75 * wnel.getDistance(s1, s2);
        }
        double lev = 0.25 * getLevenshteinDistance(s1, s2) / ((s1.length() + s2.length()) / 2);
        //System.out.println(w1 + "-" + w2 + ": " + d);
        return sem + lev;
    }

    //novelty of a set of stories as defined in the paper
    public Map<String, Double> Novelty(ArrayList<String> stories) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        String AllStories = "";
        Map<String, Double> novelties = new HashMap<String, Double>();
        //count average sem distance for every story
        Map<String, Double> stoped = new HashMap<String, Double>();
        for (int i = 0; i < stories.size(); i++) {
            HashMap<ArrayList<String>, Double> top = inf.TopTerms(stories.get(i).replace("---", " "), false);
            if (top.isEmpty()) {
                stoped.put(stories.get(i), -1.0);
            }
            //cumpute the average semantic distance of the top terms in this story
            Set<String> terms = new HashSet<String>();
            for (ArrayList<String> stems : top.keySet()) {
                //if it is in compact form , there is only one term for each stem 
                for (int j = 0; j < stems.size(); j++) {
                    terms.add(stems.get(j));
                }
            }
            // System.out.println("terms" + terms);
            double nov = AvgSemDist(terms);
            novelties.put(stories.get(i), nov);
            AllStories += "  " + stories.get(i);
        }
        //count avg semantic distance for all the stories together
        HashMap<ArrayList<String>, Double> bigtop = inf.TopTerms(AllStories, false);
        Set<String> termsAll = new HashSet<String>();
        for (ArrayList<String> stems : bigtop.keySet()) {
            for (int j = 0; j < stems.size(); j++) {
                termsAll.add(stems.get(j));
            }
        }
        //the novelty of all stories combined
        double novBig = AvgSemDist(termsAll);
        for (Map.Entry<String, Double> nov : novelties.entrySet()) {
            double newNov = 2 * abs(nov.getValue() - novBig);// / bigtop.size();
            novelties.put(nov.getKey(), newNov);
        }
        novelties.putAll(stoped);
        return novelties;
    }

    //suprise of a story as defined in the paper
    public double Surprise(String new_frag, ArrayList<String> frags) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
                //split the story in fragments (sentences)
        if (frags.isEmpty()) {
            return 0.0;
        }
        double dist = 0;
        //calculate AvgSemDist for the first fragment
        HashMap<ArrayList<String>, Double> top = inf.TopTerms(frags.get(0), false);
        double older = 0.0;
        Set<String> terms = new HashSet<String>();
        if (!top.isEmpty()) {
            //cumpute the average semantic distance of this fragment
            for (ArrayList<String> stems : top.keySet()) {
                //if it is in compact form , there is only one term for each stem
                for (int j = 0; j < stems.size(); j++) {
                    terms.add(stems.get(j));
                }
            }
            older = AvgSemDist(terms);
        }
        double newer = 0.0;
        //put the new story as the last fragment
        if (new_frag.length() > 0) {
            frags.add(new_frag);
        }
        //frags.remove(0);//fragment 0 is already calculated
        for (int i = 1; i < frags.size(); i++) {
            //calculate AvgSemDist for every fragment
            top = inf.TopTerms(frags.get(i), false);
            if (top.isEmpty()) {
                //if the framgent has only stopwords, it has 0.0 avg sem distance
                //newer = 0.0;
                //if the fragment had only stopoff words, step it
                continue;
            } else {
                terms = new HashSet<String>();
                //cumpute the average semantic distance of this story
                for (ArrayList<String> stems : top.keySet()) {
                    //if it is in compact form , there is only one term for each stem
                    for (int j = 0; j < stems.size(); j++) {
                        terms.add(stems.get(j));
                    }
                }
                newer = AvgSemDist(terms);
                //System.out.println(newer);
            }
            //and abstract it with the previous
            dist += abs(older - newer);// / top.size();
            //the new becomes the older to be abstracted with the next fragment
            older = newer;
        }
        //calculate the final formula
        double sur = 0.0;
        if (dist != 0) {
            sur = dist * 2 / (frags.size() - 1);
        }
        return sur;

    }

    //average semantic distance between a set of strings as defined in the paper
    public double AvgSemDist(Set<String> top) {
        float dist = 0;
        int i = 0;
        ArrayList<String> examined = new ArrayList<String>();
        if (language.equalsIgnoreCase("el")) {
            for (String key : top) {
                for (String other : top) {
                    if (!key.equalsIgnoreCase(other)) {
                        if (!key.equalsIgnoreCase("") && !other.equalsIgnoreCase("") && !examined.contains(other)) {
                            dist += wnel.getDistance(key, other);
                            i++;
                        }
                        // System.out.println(key + " " + other + " " + wnel.getDistance(key, other));
                    }
                }
                examined.add(key);
            }
        } else if (language.equalsIgnoreCase("de")) {
            for (String key : top) {
                for (String other : top) {
                    // System.out.println(key+" "+other+" "+pos+" ");
                    if (!key.equalsIgnoreCase(other) && !examined.contains(other)) {
                        dist += wnde.getDistance(key, other);
                        i++;
                    }
                }
                examined.add(key);
            }
        } else {
            for (String key : top) {
                String pos = wn.getCommonPos(key);
                for (String other : top) {
                    if (!key.equalsIgnoreCase(other) && pos != null && !examined.contains(other)) {
                        dist += wn.getDistance(key, other);
                        //System.out.println(dist+" "+top.size());
                        i++;
                    }
                }
                //System.out.println("examined "+key);
                examined.add(key);
            }
            //System.out.println(i);
        }
        //get the distance of one term to every other term in the document
        if (dist == 0) {
            return 0;
        }
        return dist / i;
    }

}
