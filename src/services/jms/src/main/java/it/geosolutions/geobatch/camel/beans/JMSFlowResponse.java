/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geobatch.camel.beans;

import java.io.Serializable;
import java.util.List;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class JMSFlowResponse implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -700784659986505238L;
    
    private JMSFlowStatus status;
    private List<String> responses;
    

    public JMSFlowResponse(JMSFlowStatus status, List<String> responses) {
        this.status = status;
        this.responses = responses;
    }
    
    public JMSFlowStatus getStatus() {
        return status;
    }
    public void setStatus(final JMSFlowStatus status) {
        this.status = status;
    }
    public List<String> getResponses() {
        return this.responses;
    }
    public void setResponses(final List<String> responses) {
        this.responses = responses;
    }

}
