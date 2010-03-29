/*
 * Copyright (C) 2007-2008 GeoSolutions S.A.S.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. 
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
package it.geosolutions.geobatch.users.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.annotations.NaturalId;

/**
 * 
 */
@Entity
@Table
public class UserFlowAccess implements Serializable {

    @Id
    private String id;

    @NaturalId(mutable=false)
    private Long userId;

    @NaturalId(mutable=false)
    private String flowId;

    protected UserFlowAccess() {
    }

    public UserFlowAccess(Long userId, String flowId) {
        this.userId = userId;
        this.flowId = flowId;
        this.id = userId + "_" + flowId;
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public String getFlowId() {
        return flowId;
    }

    protected void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public Long getUserId() {
        return userId;
    }

    protected void setUserId(Long userId) {
        this.userId = userId;
    }
}
