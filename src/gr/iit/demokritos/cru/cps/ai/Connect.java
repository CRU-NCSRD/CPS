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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

/**
 *
 * @author George Panagopoulos
 */
public class Connect {

    private String language;
    private WNAccess wn;
    private WNDE wnde;
    private WNEL wnel;
    private Set<String> stop;
    private Set<String> off;
    private Class stemCLass;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public WNAccess getWn() {
        return wn;
    }

    public void setWn(WNAccess wn) {
        this.wn = wn;
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

    public Set<String> getStop() {
        return stop;
    }

    public void setStop(Set<String> stop) {
        this.stop = stop;
    }

    public Set<String> getOff() {
        return off;
    }

    public void setOff(Set<String> off) {
        this.off = off;
    }

    public Class getStemCLass() {
        return stemCLass;
    }

    public void setStemCLass(Class stemCLass) {
        this.stemCLass = stemCLass;
    }

    public Connect(String language) throws ClassNotFoundException {
        this.language=language;
    }

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
}
