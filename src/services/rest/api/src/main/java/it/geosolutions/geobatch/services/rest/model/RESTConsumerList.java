/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2013 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.services.rest.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author DamianoG
 *
 */
@XmlRootElement(name = "consumers")
@XmlType(propOrder = {"flow", "consumerList"})
public class RESTConsumerList implements Iterable<RESTConsumerShort>{

    private RESTFlowShort flow;
    private List<RESTConsumerShort> consumerList;
    
    public RESTConsumerList(){
        flow = new RESTFlowShort();
        consumerList = new ArrayList<RESTConsumerShort>();
    }
    
    public RESTFlowShort getFlow() {
        return flow;
    }
    
    public void setFlow(RESTFlowShort flow) {
        this.flow = flow;
    }
    
    @XmlElement(name = "consumer")
    public List<RESTConsumerShort> getConsumerList() {
        return consumerList;
    }
    
    public void setConsumerList(List<RESTConsumerShort> consumerList) {
        this.consumerList = consumerList;
    }
    
    public void add(RESTConsumerShort consumer) {
        this.consumerList.add(consumer);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
                .append("[id:").append(flow.getId()).append(" [  the flow contains:  ").append(consumerList.size()).append(" consumers ]");
        return  sb.toString();
    }

    /**
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<RESTConsumerShort> iterator() {
        return consumerList.iterator();
    }
    
    
}
