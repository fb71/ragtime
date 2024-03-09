/*
 * Copyright (C) 2024, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package ragtime.cc;

import java.util.Properties;

import areca.common.base.Consumer.RConsumer;
import areca.common.log.LogFactory;
import areca.common.log.LogFactory.Log;
import jakarta.mail.Authenticator;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/**
 *
 * @author Falko Bräutigam
 */
public class Email {

    private static final Log LOG = LogFactory.getLog( Email.class );

    /**
     *
     */
    public static void send(  String TO, String subject, String body ) throws MessagingException {
        send( TO, subject, body, msg -> {} );
    }

    /**
     *
     */
    public static void send( String TO, String subject, String body, RConsumer<MimeMessage> add ) throws MessagingException {
        Properties props = new Properties();
        props.put( "mail.smtp.ssl.enable", "true" );
        props.put( "mail.smtp.ssl.checkserveridentity", "false" );
        props.put( "mail.smtp.host", CCApp.config.smtpHost );
        props.put( "mail.smtp.port", CCApp.config.smtpPort );
        props.put( "mail.smtp.auth", "true" );

        var auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication( CCApp.config.smtpUser, CCApp.config.smtpPassword );
            }
        };

        var session = Session.getInstance( props, auth );
        var msg = new MimeMessage( session );
        msg.setSubject( subject );
        msg.setFrom( "fb71.org <falko@fb71.org>" );
        msg.setRecipients( MimeMessage.RecipientType.TO, InternetAddress.parse( TO ) );
        msg.setText( body, "UTF-8" );
        add.accept( msg );

        Transport.send( msg );
    }
}
