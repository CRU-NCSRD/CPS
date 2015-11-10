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
package gr.iit.demokritos.cru.test;

import gr.iit.demokritos.cru.cpserver.CPServer;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 *
 * @author George Panagopoulos
 */
public class Test {

    public static void main(String[] args) throws FileNotFoundException {
        // TODO code application logic here
        CPServer cps = new CPServer(5000, new FileInputStream(("C:\\Users\\George\\Documents\\NetBeansProjects\\CPServer\\web\\WEB-INF\\configuration.properties")));

        cps.start();
    }

}
