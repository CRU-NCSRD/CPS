/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iit.demokritos.cru.cps.utilities.wordnet;

import rita.wordnet.RiWordnet;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author antonis
 */
public class WNAccess {

    private RiWordnet wn;
    private WordNetDatabase database;

    public WNAccess(String database_dir) {
        wn = new RiWordnet();
        database = WordNetDatabase.getFileInstance();

        System.setProperty("wordnet.database.dir", database_dir);
    }

    public WNAccess() {
        wn = new RiWordnet();
        database = WordNetDatabase.getFileInstance();

        System.setProperty("wordnet.database.dir", "C:\\WordNet\\2.1\\dict\\");
    }

    public String getCommonPos(String word) {
        String pos = wn.getBestPos(word);
        return pos;
    }

    public String[] getPos(String word) {
        String[] pos = wn.getPos(word);
        return pos;
    }

    public String[] getWordsInSynsets(String word) {
        String pos = wn.getBestPos(word);
        if (pos == null) {
            return null;
        }
        String[] synsets = wn.getAllSynsets(word, this.getCommonPos(word));
        return synsets;
    }

    public String[] getWordsInAllSynsets(String word) {
        /*String pos = wn.getBestPos(word);
        if (pos == null) {
            System.out.println("den vrhke tetoio");
            return null;
        }
        return wn.getAllSynsets(word, pos);*/
        
        Synset[] a= database.getSynsets(word);
        if(a.length==0||a==null){
            return null;
        }
        String b="";
        for (int j = 0; j < a.length; j++) {
            String[] l = a[j].getWordForms();
            for(int k = 0; k < l.length; k++){
                b+=":"+l[k];
            }
        }
        return b.split(":");
    }

    public String[] getTermsOfLargestSynset(String word){
        Synset[] a=database.getSynsets(word);
        int max=a[0].getWordForms().length;
        String[] terms=a[0].getWordForms();
        for(int i=1;i<a.length;i++){
            String[] b=a[i].getWordForms();
            if(b.length>max){
                max=b.length;
                terms=b;
            }
        }
        return terms;
    }
    
    public int getCount(String word) {
        return database.getSynsets(word)[0].getTagCount(word);
    }

    public int getSynsetCount(String word) {
        String pos=this.getCommonPos(word);
        if(pos==null){
            return 0;
        }
        String[] synsets = wn.getSynset(word,pos , true);
        if (synsets == null) {
            return 0;
        }
        return synsets.length;
    }

    public double getDistance(String w1, String w2) {
        double d = 0.0;
        boolean common = false;

        String p1[] = wn.getPos(w1);
        String p2[] = wn.getPos(w2);

        if (p1 == null || p2 == null) {
            d = 1.0;
            return d;
        }

        for (int i = 0; i < p1.length; i++) {
            for (int j = 0; j < p2.length; j++) {
                if (p1[i].equals(p2[j])) {
                    common = true;//System.out.println("both words are " + p1[i]);
                    if (wn.getDistance(w1, w2, p1[i]) > d) {
                        d = wn.getDistance(w1, w2, p1[i]);
                       // System.out.println("no"+d);
                    }
                }
            }
        }
        if (!common) {
            d = 1.0;
        }

        return d;
    }
}
