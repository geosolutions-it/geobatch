/*
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

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAttribute;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *
 * @author Etj (etj at geo-solutions.it)
 */
@XmlRootElement(name = "status")
@XmlType(propOrder = {"errorMessage", "latestAction", "task", "completed"})
public class RESTConsumerStatus implements  Serializable {

    public static enum Status {
        SUCCESS, FAIL, RUNNING
    }

    private String uuid;
    private Status status;

    private String errorMessage;

    private RESTActionShort latestAction;

    private String task;
    private String completed;


    public RESTConsumerStatus() {
    }

    @XmlAttribute(name = "uuid")
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @XmlAttribute(name = "type")
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public RESTActionShort getLatestAction() {
        return latestAction;
    }

    public void setLatestAction(RESTActionShort latestAction) {
        this.latestAction = latestAction;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }


    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "["
                + (uuid!=null? " uuid=" + uuid : "")
                + (status != null? " status=" + status : "")
                + (latestAction != null? " action=" + latestAction : "")
                + ']';
    }



}
