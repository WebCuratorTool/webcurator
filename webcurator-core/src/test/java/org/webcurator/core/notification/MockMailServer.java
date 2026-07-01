/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.core.notification;

import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;


/**
 * The implementation of the MailServer interface.
 * @see MailServer
 * @author bprice
 */
public class MockMailServer extends MailServer {


    /**
     * Constructor.
     * @param aMailConfig mail config
     */
    public MockMailServer(Properties aMailConfig) {
        super(aMailConfig);
    }
    
    private Mailable email;
    
    public Mailable getEmailResult()
    {
    	return email;
    }
    
    
    public void send(Mailable email, String filename, String mimeType, InputStream file) throws MessagingException {
        this.email = email;
    }


    public void send(Mailable email) throws MessagingException {
    	this.email = email;
    }
    
    /**
     * Return a mail session for this helpers config.
     * @return the session
     */
    public Session getSession() {
        return null;
    }

    public void send(Mailable email, String filename, String mimeType, String file) throws MessagingException {
    	this.email = email;
    }
    
    public void sendHTML(Mailable email) throws MessagingException {
    	this.email = email;
    }

}
