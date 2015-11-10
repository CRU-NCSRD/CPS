/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iit.demokritos.cru.cps.utilities.tokenizers;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import gr.iit.demokritos.cru.cps.ai.InfoSummarization;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 *
 * @author admin
 */
public class HTMLPages implements Runnable {

    String url;
    //  Set<String> stopWordSet;
    Thread runner;
    // ConcurrentHashMap<String, double[]> tokensHashMap;
    // ConcurrentHashMap<String, Double> tokensHashMap;
    Set<String> pages;
    // ConcurrentHashMap<String, Double> sentenceHashMap;
    String language;

    public HTMLPages(String url, Set<String> pages, String language) {
        this.url = url;
        this.language = language;
        //  this.stopWordSet = stopWordSet;
        runner = new Thread(this, this.url);
        // this.tokensHashMap = tokensHashMap;
        // this.sentenceHashMap = sentenceHashMap;
        this.pages = pages;
        runner.start();
    }

    public void run() {
        // getTokensList(tokensHashMap);//, sentenceHashMap);
        getPagesList(pages);
    }

    public void getPagesList(Set<String> pages) {//ConcurrentHashMap<String, Double> tokensHashMap, ConcurrentHashMap<String, Double> sentenceHashMap) {

        try {
            String html = getHtml(url);
            String cleanHtml = ArticleExtractor.INSTANCE.getText(html);
        //    Detector detector = DetectorFactory.create();
            if (!cleanHtml.equalsIgnoreCase("")) {
           //     detector.append(cleanHtml);
             //   try {
               //     String lang = detector.detect();
                 //   if (lang.equalsIgnoreCase(this.language) || lang.equalsIgnoreCase("en")) {
                        //System.out.println(url);
                        pages.add(cleanHtml);
             //   } catch (Exception e) {

            //    }
            }
         } catch (IOException ex) {
            Logger.getLogger(HTMLPages.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BoilerpipeProcessingException ex) {
            Logger.getLogger(HTMLPages.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
            //keep the page only if it is in this language or in english

            ////cal inf.top terms here

            /*  int nGramOrder = 1;
             //take the tokens of the page
             NGramTokenizer tokenizer = new NGramTokenizer(true, stopWordSet, nGramOrder);
             Vector<String> tokenList = tokenizer.tokenize(cleanHtml);

             String phrase = "";
             for (int t = 0; t < tokenList.size(); t++) {
             phrase += tokenList.get(t);
             }
            
            
             HashMap<ArrayList<String>, Double> top = new HashMap<ArrayList<String>, Double>();
             try {
             InfoSummarization inf = new InfoSummarization(this.language);
             top = inf.TopTerms(true, phrase);
             for (ArrayList<String> stems : top.keySet()) {
             //if it is in compact form , there is only one term for each stem 
             for (int j = 0; j < stems.size(); j++) {
             tokensHashMap.put(stems.get(j), top.get(stems));
             }
             }
             } catch (ClassNotFoundException ex) {
             Logger.getLogger(HTMLPages.class.getName()).log(Level.SEVERE, null, ex);
             } catch (InstantiationException ex) {
             Logger.getLogger(HTMLPages.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
             Logger.getLogger(HTMLPages.class.getName()).log(Level.SEVERE, null, ex);
             }
             /* String token;
             String ngram;
             //for every word in the page
             for (int t = 0; t < tokenList.size(); t++) {
             token = tokenList.get(t);
             token = token.trim().toLowerCase();
             //ignore it if it's too small
             if (token.length() < 4) {
             continue;
             }
             ngram = token;

             sentenceHashMap.putIfAbsent(ngram, new Double(0.0));
             //keep the count of the token occurences in the sentence
             Double sentenceTokenCount = (Double) sentenceHashMap.get(ngram);
             double temp = sentenceTokenCount.doubleValue() + 1.0;
             sentenceHashMap.put(ngram, new Double(temp));
             }
             //go through the tokens of the sentence
             Iterator<String> it = sentenceHashMap.keySet().iterator();
             String key;
             while (it.hasNext()) {
             key = (String) it.next();
             //put their frequency to the overall frequensy of the token
             Double stc = (Double) sentenceHashMap.get(key);
             double tc, sc;
             double[] newTokenValues = new double[2];

             newTokenValues[0] = stc.doubleValue();
             newTokenValues[1] = 1.0;

             double[] check = tokensHashMap.putIfAbsent(key, newTokenValues);

             if (check != null) {
             double[] tokenValues = tokensHashMap.get(key);
             tc = tokenValues[0] + stc.doubleValue();
             sc = tokenValues[1] + 1.0;

             newTokenValues[0] = tc;
             newTokenValues[1] = sc;

             tokensHashMap.put(key, newTokenValues);
             }
             }
             sentenceHashMap.clear();*/
       

    public String getHtml(String url) throws IOException {
        HttpGet request = null;
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        StringBuilder html = new StringBuilder("");
        try {
            HttpClient client = new DefaultHttpClient();
            request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            // Get the responserequest.releaseConnection();
            inputStreamReader = new InputStreamReader(response.getEntity().getContent(), "UTF8");
            bufferedReader = new BufferedReader(inputStreamReader);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                html.append(line);
            }
            //request.releaseConnection();
            //bufferedReader.close();
            //inputStreamReader.close();
            return html.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "";
        } finally {
            if (request != null) {
                request.releaseConnection();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
        }
    }

}
