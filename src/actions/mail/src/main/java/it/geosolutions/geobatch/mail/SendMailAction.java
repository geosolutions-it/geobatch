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

import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.io.File;
import java.io.FileInputStream;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Tobia Di Pisa - tobia.dipisa@geo-solutions.it
 * 
 */
public class SendMailAction extends BaseAction<EventObject> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendMailAction.class);

    /**
     * configuration
     */
    private final SendMailConfiguration conf;

    public SendMailAction(SendMailConfiguration configuration) {
        super(configuration);
        conf = configuration;
    }

    public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {
        final Queue<EventObject> ret = new LinkedList<EventObject>();

        while (events.size() > 0) {
            final EventObject ev;
            try {
                if ((ev = events.remove()) != null) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Send Mail action.execute(): working on incoming event: "
                                + ev.getSource());
                    }

                    File mail = (File) ev.getSource();

                    FileInputStream fis = new FileInputStream(mail);
                    String kmlURL = IOUtils.toString(fis);

                    // /////////////////////////////////////////////
                    // Send the mail with the given KML URL
                    // /////////////////////////////////////////////

                    // Recipient's email ID needs to be mentioned.
                    String mailTo = conf.getMailToAddress();

                    // Sender's email ID needs to be mentioned
                    String mailFrom = conf.getMailFromAddress();

                    // Get system properties
                    Properties properties = new Properties();

                    // Setup mail server
                    String mailSmtpAuth = conf.getMailSmtpAuth();
                    properties.put("mail.smtp.auth", mailSmtpAuth);
                    properties.put("mail.smtp.host", conf.getMailSmtpHost());
                    properties.put("mail.smtp.starttls.enable", conf.getMailSmtpStarttlsEnable());
                    properties.put("mail.smtp.port", conf.getMailSmtpPort());

                    // Get the default Session object.
                    final String mailAuthUsername = conf.getMailAuthUsername();
                    final String mailAuthPassword = conf.getMailAuthPassword();

                    Session session = Session.getDefaultInstance(properties, (mailSmtpAuth
                            .equalsIgnoreCase("true") ? new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(mailAuthUsername, mailAuthPassword);
                        }
                    } : null));

                    try {
                        // Create a default MimeMessage object.
                        MimeMessage message = new MimeMessage(session);

                        // Set From: header field of the header.
                        message.setFrom(new InternetAddress(mailFrom));

                        // Set To: header field of the header.
                        message.addRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));

                        // Set Subject: header field
                        message.setSubject(conf.getMailSubject());

                        String mailHeaderName = conf.getMailHeaderName();
                        String mailHeaderValule = conf.getMailHeaderValue();
                        if (mailHeaderName != null && mailHeaderValule != null) {
                            message.addHeader(mailHeaderName, mailHeaderValule);
                        }

                        String mailMessageText = conf.getMailContentHeader();

                        message.setText(mailMessageText + "\n\n" + kmlURL);

                        // Send message
                        Transport.send(message);

                        if (LOGGER.isInfoEnabled())
                            LOGGER.info("Sent message successfully....");

                    } catch (MessagingException exc) {
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn("An error occurrd when sent message ....", exc);
                    }
                } else {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Send Mail action.execute(): Encountered a NULL event: SKIPPING...");
                    }

                    continue;
                }
            } catch (Exception ioe) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Send Mail action.execute(): Unable to produce the output: ",
                            ioe.getLocalizedMessage(), ioe);
                }
                throw new ActionException(this, ioe.getLocalizedMessage(), ioe);
            }
        }

        return ret;
    }
}
