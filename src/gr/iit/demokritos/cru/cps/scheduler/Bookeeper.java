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
package gr.iit.demokritos.cru.cps.scheduler;

import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author Sotiris Konstantinidis
 */
public class Bookeeper implements  Job {
    private Map properties;

    public Bookeeper(Map properties) {
        this.properties = properties;
    }
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        try {
            System.out.println("Execute ML Components");
            WindowScheduler ws=new WindowScheduler(properties);
            WindowScheduler.quartzJob();
        } catch (SQLException ex) {
            Logger.getLogger(Bookeeper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
    
    
}
