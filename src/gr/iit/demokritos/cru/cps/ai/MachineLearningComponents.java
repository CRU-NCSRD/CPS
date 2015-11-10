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

import NonLinearMinimization.Minimize;
import com.google.common.collect.ArrayListMultimap;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import gr.iit.demokritos.cru.cps.User;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

/**
 *
 * @author Pythagoras Karampiperis, George Panagopoulos
 */
public class MachineLearningComponents {

    /**
     * @param args the command line arguments
     */
 

    public static class TextualCreativityPrincipalComponents {

        double[] N;  //Novelty vector
        double[] S;  //Surprise vector
        double[] R;  //Rarity vector
        double[] E;  //R.Effort vector
        double WS;
        double WR;
        double WE;
        double V;  // Value at Inital point [IWS,IWR,IWE]
        double OV; // Optimum Value
        boolean Initialized = false;

        //-------------------------------------------
        public TextualCreativityPrincipalComponents(double[] N, double[] S, double[] R, double[] E, double IWS, double IWR, double IWE) throws MWException {
            if ((N.length == S.length && S.length == R.length && R.length == E.length) && (IWS + IWR + IWE) != 0) {//vectors must have same size
                this.N = N;
                this.S = S;
                this.R = R;
                this.E = E;
                //---------
                double[] AT = new double[S.length];  // Atypicallity
                for (int i = 0; i < S.length; i++) {
                    AT[i] = (IWS * S[i]
                            + IWR * R[i]
                            + IWE * E[i])
                            / (IWS + IWR + IWE);
                }
                if((N.length<2)||(AT.length<2)){
                    return;
                }else{
                    this.V = new PearsonsCorrelation().correlation(N, AT);
                }
                
                
                //-------------------------------
                Object[] result = null;
                Minimize lm = new Minimize();
                result = lm.NonLinearMinimization(2, N, S, R, E);
                MWNumericArray OptimalWeights = (MWNumericArray) result[0];//the weights
                System.out.println(result[0] + " " + result[1]);
                double[] ow = OptimalWeights.getDoubleData();
                this.WS = ow[0];
                this.WR = ow[1];
                this.WE = ow[2];
                MWNumericArray OptimalCorrelation = (MWNumericArray) result[1];
                double OV = OptimalCorrelation.getDouble();
                //-------------------------------
                this.Initialized = true;
            } else {
                return;
            }
        }
    }

    public static double[] CalculateWeights(double[] AllN, double[] AllS, double[] AllR, double[] AllE, double[] W) throws MWException {// all with current values or with the starting values
        TextualCreativityPrincipalComponents tcpc = new TextualCreativityPrincipalComponents(AllN, AllS, AllR, AllE, W[0], W[1], W[2]);
        double[] Weights;
        if (tcpc.Initialized) {//check if the constraints were satisfied
            if ((tcpc.V - tcpc.OV) / tcpc.V >= 0.05) {//check if the update in correlation is crucial enough to change the weights
                Weights = new double[]{tcpc.WE, tcpc.WR, tcpc.WS};
            } else {
                Weights = W;
            }
            return Weights;
        } else {            
            return W;
        }
    }

    public static Vec UserCreativity(double[] LastN, double[] LastS, double[] LastR, double[] LastE, double[] W, double D) throws MWException {//ArrayListMultimap<String, Double> LastEvids, ArrayListMultimap<String, Double> AllEvids) {//all evids have current values
        double x = 0.0;
        double y = 0.0;
        for (int i = 0; i < LastN.length; i++) {
            x += LastN[i];//add constructive novelty  LastN[i] / D + (D - 1) * x / D
            y += ((LastS[i] * W[0] + LastR[i] * W[1] + LastE[i] * W[2]) / (W[0] + W[1] + W[2]));//add visionary novelty
        }
        x = x / LastN.length;//average vector representing creativity of the user the past time window
        y = y / LastN.length;
        Vec C = new Vec(x, y);
        return C;
    }

    public static class Vec {

        double x;
        double y;
        double length;
        boolean right = true;

        public Vec(double x, double y) {
            if (x != 0 & y != 0) {
                this.x = x;
                this.y = y;
                this.length = sqrt(pow(x, 2) + pow(y, 2));
            } else {
                this.right = false;
            }
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getLength() {
            return length;
        }

        public void setLength(double length) {
            this.length = length;
        }

        public boolean isRight() {
            return right;
        }

        public void setRight(boolean right) {
            this.right = right;
        }
        
    }

    public static Vec CalculateUserProfile(double[] LastN, double[] LastS, double[] LastR, double[] LastE, double[] w, int D,double visionary,double constructive,ArrayList<User> group) {
        double OldCreativityX = visionary; //!!!!!!!!!!!!!!get old user's X,Y 
        double OldCreativityY = constructive;
        
        try {
            Vec UserVec = UserCreativity(LastN, LastS, LastR, LastE, w, D);
            Vec AverageGroupVec = new Vec(0, 0);
            int NumberOfGroups=group.size();
            for (int i = 0; i < NumberOfGroups; i++) {
                //!!!!!!!!!!!!!!!get last metrics of each group
                double x=(Double)group.get(i).getUps().get(0).getProperty_value();
                double y=(Double)group.get(i).getUps().get(1).getProperty_value();
                
                
                
                AverageGroupVec.x += x / NumberOfGroups;
                AverageGroupVec.y += y/ NumberOfGroups;
            }
            double x=UserVec.x;
            double y=UserVec.y;
            //if the group's metrics are greater, change the UserVector to include the group's achievments
            if (UserVec.x < AverageGroupVec.x) {
                double k = 1 / 2 + 1 / 2 + Math.tanh(2 * ((AverageGroupVec.x - UserVec.x) - 1));
                x = UserVec.x + k * (AverageGroupVec.x - UserVec.x);
            }
            if (UserVec.y< AverageGroupVec.y) {
                double k = 1 / 2 + 1 / 2 + Math.tanh(2 * ((AverageGroupVec.x - UserVec.x) - 1));
                y = UserVec.y + k * (AverageGroupVec.y - UserVec.y);
            }
             UserVec = new Vec(x, y);
            Vec Creativity = new Vec(UserVec.x / D + (D - 1) * OldCreativityX / D, UserVec.y / D + (D - 1) * OldCreativityY / D);
            return Creativity;
        } catch (MWException ex) {
            Logger.getLogger(MachineLearningComponents.class.getName()).log(Level.SEVERE, null, ex);
            return new Vec(0, 0);
        }
    }
}
