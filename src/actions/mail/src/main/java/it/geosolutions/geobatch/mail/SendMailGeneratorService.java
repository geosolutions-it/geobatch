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

import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;

import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Tobia Di Pisa - tobia.dipisa@geo-solutions.it
 * 
 */
public class SendMailGeneratorService extends BaseService implements
        ActionService<EventObject, SendMailConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendMailGeneratorService.class);

    /**
     * @param id
     * @param name
     * @param description
     */
    public SendMailGeneratorService(String id, String name, String description) {
        super(id, name, description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.geosolutions.geobatch.flow.event.action.ActionService#createAction(it.geosolutions.geobatch.configuration.event.action.ActionConfiguration)
     */
    public SendMailAction createAction(SendMailConfiguration configuration) {
        try {
            SendMailAction glidersSendMailAction = new SendMailAction(configuration);
            return glidersSendMailAction;
        } catch (Exception e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(e.getLocalizedMessage(), e);
            }

            return null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.geosolutions.geobatch.flow.event.action.ActionService#canCreateAction(it.geosolutions.geobatch.configuration.event.action.ActionConfiguration
     * )
     */
    public boolean canCreateAction(SendMailConfiguration configuration) {
        LOGGER.info("------------------->Checking setting parameters");
        try {
            String mailSubject = configuration.getMailSubject();

            String mailSmtpHost = configuration.getMailSmtpHost();
            String mailSmtpStarttlsEnable = configuration.getMailSmtpStarttlsEnable();
            String mailSmtpAuth = configuration.getMailSmtpAuth();
            String mailSmtpPort = configuration.getMailSmtpPort();

            String mailAuthUsername = configuration.getMailAuthUsername();
            String mailAuthPassword = configuration.getMailAuthPassword();

            String mailToAddress = configuration.getMailToAddress();
            String mailFromAddress = configuration.getMailFromAddress();

            String mailMessageText = configuration.getMailContentHeader();

            if (mailSubject != null) {
                LOGGER.info("mailSubject is " + mailSubject);

            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("TracksIngestGeneratorService::canCreateAction(): "
                            + "unable to create action, it's not possible to get the mailSubject.");
                }

                return false;
            }
            if (mailSmtpHost != null) {
                LOGGER.info("mailSmtpHost value is " + mailSmtpHost);
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("TracksIngestGeneratorService::canCreateAction(): "
                            + "unable to create action, it's not possible to get the mailSmtpHost.");
                }

                return false;
            }
            if (mailSmtpStarttlsEnable != null) {
                LOGGER.info("mailSmtpStarttlsEnable value is " + mailSmtpStarttlsEnable);
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("TracksIngestGeneratorService::canCreateAction(): "
                            + "unable to create action, it's not possible to get the mailSmtpStarttlsEnable.");
                }

                return false;
            }
            if (mailSmtpAuth != null) {
                LOGGER.info("mailSmtpAuth value is " + mailSmtpAuth);
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("TracksIngestGeneratorService::canCreateAction(): "
                            + "unable to create action, it's not possible to get the mailSmtpAuth.");
                }

                return false;
            }
            if (mailSmtpPort != null) {
                LOGGER.info("mailSmtpPort value is " + mailSmtpPort);
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("TracksIngestGeneratorService::canCreateAction(): "
                            + "unable to create action, it's not possible to get the mailSmtpPort.");
                }

                return false;
            }
            if (mailAuthUsername != null) {
                LOGGER.info("mailAuthUsername value is " + mailAuthUsername);
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("TracksIngestGeneratorService::canCreateAction(): "
                            + "unable to create action, it's not possible to get the mailAuthUsername.");
                }

                return false;
            }
            if (mailAuthPassword != null) {
                LOGGER.info("mailAuthPassword value is " + mailAuthPassword);
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("TracksIngestGeneratorService::canCreateAction(): "
                            + "unable to create action, it's not possible to get the mailAuthPassword.");
                }

                return false;
            }
            if (mailFromAddress != null) {
                LOGGER.info("mailFromAddress value is " + mailFromAddress);
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("TracksIngestGeneratorService::canCreateAction(): "
                            + "unable to create action, it's not possible to get the mailFromAddress.");
                }

                return false;
            }
            if (mailToAddress != null) {
                LOGGER.info("mailToAddress value is " + mailToAddress);
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("TracksIngestGeneratorService::canCreateAction(): "
                            + "unable to create action, it's not possible to get the mailToAddress.");
                }

                return false;
            }
            if (mailMessageText != null) {
                LOGGER.info("mailMessageText value is " + mailMessageText);
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("TracksIngestGeneratorService::canCreateAction(): "
                            + "unable to create action, it's not possible to get the mailMessageText.");
                }

                return false;
            }
        } catch (Throwable e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }

            return false;
        }

        return true;
    }
}
