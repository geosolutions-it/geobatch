/*
 * ====================================================================
 *
 * GeoBatch - Intersection Engine
 *
 * Copyright (C) 2007 - 2011 GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 *
 * GPLv3 + Classpath exception
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
package it.geosolutions.geobatch.mail;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

/**
 * 
 * @author Tobia Di Pisa - tobia.dipisa@geo-solutions.it
 * 
 */

public class SendMailConfiguration extends ActionConfiguration implements Configuration {

    private String mailSubject = null;

    private String mailSmtpHost = null;

    private String mailSmtpStarttlsEnable;

    private String mailSmtpAuth = null;

    private String mailSmtpPort = null;

    private String mailAuthUsername = null;

    private String mailToAddress = null;

    private String mailFromAddress = null;

    private String mailAuthPassword = null;

    private String mailHeaderName = null;

    private String mailHeaderValue = null;

    private String mailContentHeader = null;

    public SendMailConfiguration(String id, String name, String description) {
        super(id, name, description);

        // TODO INITIALIZE MEMBERS
    }

    /**
     * @return the mailSubject
     */
    public String getMailSubject() {
        return mailSubject;
    }

    /**
     * @param mailSubject the mailSubject to set
     */
    public void setMailSubject(String mailSubject) {
        this.mailSubject = mailSubject;
    }

    /**
     * @return the mailSmtpHost
     */
    public String getMailSmtpHost() {
        return mailSmtpHost;
    }

    /**
     * @param mailSmtpHost the mailSmtpHost to set
     */
    public void setMailSmtpHost(String mailSmtpHost) {
        this.mailSmtpHost = mailSmtpHost;
    }

    /**
     * @return the mailSmtpStarttlsEnable
     */
    public String getMailSmtpStarttlsEnable() {
        return mailSmtpStarttlsEnable;
    }

    /**
     * @param mailSmtpStarttlsEnable the mailSmtpStarttlsEnable to set
     */
    public void setMailSmtpStarttlsEnable(String mailSmtpStarttlsEnable) {
        this.mailSmtpStarttlsEnable = mailSmtpStarttlsEnable;
    }

    /**
     * @return the mailSmtpAuth
     */
    public String getMailSmtpAuth() {
        return mailSmtpAuth;
    }

    /**
     * @param mailSmtpAuth the mailSmtpAuth to set
     */
    public void setMailSmtpAuth(String mailSmtpAuth) {
        this.mailSmtpAuth = mailSmtpAuth;
    }

    /**
     * @return the mailSmtpPort
     */
    public String getMailSmtpPort() {
        return mailSmtpPort;
    }

    /**
     * @param mailSmtpPort the mailSmtpPort to set
     */
    public void setMailSmtpPort(String mailSmtpPort) {
        this.mailSmtpPort = mailSmtpPort;
    }

    /**
     * @return the mailAuthUsername
     */
    public String getMailAuthUsername() {
        return mailAuthUsername;
    }

    /**
     * @param mailAuthUsername the mailAuthUsername to set
     */
    public void setMailAuthUsername(String mailAuthUsername) {
        this.mailAuthUsername = mailAuthUsername;
    }

    /**
     * @return the mailToAddress
     */
    public String getMailToAddress() {
        return mailToAddress;
    }

    /**
     * @param mailToAddress the mailToAddress to set
     */
    public void setMailToAddress(String mailToAddress) {
        this.mailToAddress = mailToAddress;
    }

    /**
     * @return the mailFromAddress
     */
    public String getMailFromAddress() {
        return mailFromAddress;
    }

    /**
     * @param mailFromAddress the mailFromAddress to set
     */
    public void setMailFromAddress(String mailFromAddress) {
        this.mailFromAddress = mailFromAddress;
    }

    /**
     * @return the mailAuthPassword
     */
    public String getMailAuthPassword() {
        return mailAuthPassword;
    }

    /**
     * @param mailAuthPassword the mailAuthPassword to set
     */
    public void setMailAuthPassword(String mailAuthPassword) {
        this.mailAuthPassword = mailAuthPassword;
    }

    /**
     * @return the mailHeaderName
     */
    public String getMailHeaderName() {
        return mailHeaderName;
    }

    /**
     * @param mailHeaderName the mailHeaderName to set
     */
    public void setMailHeaderName(String mailHeaderName) {
        this.mailHeaderName = mailHeaderName;
    }

    /**
     * @return the mailHeaderValue
     */
    public String getMailHeaderValue() {
        return mailHeaderValue;
    }

    /**
     * @param mailHeaderValue the mailHeaderValue to set
     */
    public void setMailHeaderValue(String mailHeaderValue) {
        this.mailHeaderValue = mailHeaderValue;
    }

    /**
     * @return the mailContentHeader
     */
    public String getMailContentHeader() {
        return mailContentHeader;
    }

    /**
     * @param mailContentHeader the mailContentHeader to set. 
     * The mail content will starts with this message, then wil be append the content of the file writed by the previous action.
     */
    public void setMailContentHeader(String mailContentHeader) {
        this.mailContentHeader = mailContentHeader;
    }

    @Override
    public SendMailConfiguration clone() {
        final SendMailConfiguration ret = new SendMailConfiguration(this.getId(), this.getName(),
                this.getDescription());

        ret.setMailSubject(mailSubject);

        ret.setMailSmtpHost(mailSmtpHost);
        ret.setMailSmtpStarttlsEnable(mailSmtpStarttlsEnable);
        ret.setMailSmtpAuth(mailSmtpAuth);
        ret.setMailSmtpPort(mailSmtpPort);

        ret.setMailAuthUsername(mailAuthUsername);
        ret.setMailAuthPassword(mailAuthPassword);

        ret.setMailSubject(mailSubject);

        ret.setMailToAddress(mailToAddress);
        ret.setMailFromAddress(mailFromAddress);

        ret.setMailHeaderName(mailHeaderName);
        ret.setMailHeaderValue(mailHeaderValue);
        ret.setMailContentHeader(mailContentHeader);

        // TODO CLONE YOUR MEMBERS
        ret.setServiceID(this.getServiceID());
        ret.setListenerConfigurations(ret.getListenerConfigurations());

        return ret;
    }

}
