/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.iit.demokritos.cru.cps.utilities.wordnet;

import java.util.Enumeration;
import java.util.concurrent.Callable;
import weka.core.DistanceFunction;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.PerformanceStats;

/**
 *
 * @author antonis
 */
public class WordNetElDistance implements Callable, DistanceFunction {
    private WNEL wn;

    public WordNetElDistance(WNEL wn){
        this.wn = wn;
    }
    
    @Override
    public Object call() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setInstances(Instances i) {
        
    }

    @Override
    public Instances getInstances() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setAttributeIndices(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getAttributeIndices() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setInvertSelection(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getInvertSelection() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double distance(Instance instnc, Instance instnc1) {
        String w1 = instnc.stringValue(0);
        String w2 = instnc1.stringValue(0);
        
        double d = wn.getDistance(w1, w2);
        //System.out.println(w1 + "-" + w2 + ": " + d);
        return d;
    }

    @Override
    public double distance(Instance instnc, Instance instnc1, PerformanceStats ps) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double distance(Instance instnc, Instance instnc1, double d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double distance(Instance instnc, Instance instnc1, double d, PerformanceStats ps) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void postProcessDistances(double[] doubles) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void update(Instance instnc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Enumeration listOptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setOptions(String[] strings) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String[] getOptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clean() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
