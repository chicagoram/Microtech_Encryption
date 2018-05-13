// File:         [Emailer.java]
// Created:      [Dec 16, 2009 creation date]
// Last Changed: [Dec 16, 2009 date last changed]
// Author:       [Ram]
//
// This code is copyright (c) 2009 Allied Vaughn
// 
// History:
// 
//

package com.encrypt.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;

/**
 * The Class Emailer.
 */
public class Emailer {

	/** The Constant logger. */

	/** The mail server config. */
	private static Properties fMailServerConfig = new Properties();

	/**
	 * Simple demonstration of using the javax.mail API.
	 * 
	 * Run from the command line. Please edit the implementation to use correct
	 * email addresses and host name.
	 */

	private static final Logger errorLogger = Logger.getLogger("errorLog");

	/*
	 * static { Emailer.fetchConfig(); }
	 */

	/**
	 * The main method.
	 * 
	 * @param aArguments
	 *            the arguments
	 */
	public static void main(String... aArguments) {

		// the domains of these email addresses should be valid,
		// or the example will fail:
		// Emailer.sendEmail("fromblah@blah.com", "toblah@blah.com",
		// "Testing 1-2-3", "blah blah blah");
		Emailer.testEMail("testing", "teter");
	}

	public static void getList(String fileDir) {

		File dir = new File(fileDir);
		FileFilter fileFilter = new WildcardFileFilter("*.txt");
		File[] files = dir.listFiles(fileFilter);
		if (files != null && files.length > 0)
			System.out.println("size" + files.length);

	}

	// PRIVATE //

	/**
	 * Send email.
	 * 
	 * @param aFromEmailAddr
	 *            the a from email addr
	 * @param aToEmailAddr
	 *            the a to email addr
	 * @param aSubject
	 *            the a subject
	 * @param aBody
	 *            the a body
	 */
	public static void sendEmail(String subject, String aBody) {

		// Here, no Authenticator argument is used (it is null).
		// Authenticators are used to prompt the user for user
		// name and password.
		final Session session = Session.getDefaultInstance(Emailer.fMailServerConfig, null);
		final Message message = new MimeMessage(session);
		try {
			// the "from" address may be set in code, or set in the
			// config file under "mail.from" ; here, the latter style is used
			// message.setFrom( new InternetAddress(aFromEmailAddr) );
			message.setFrom(new InternetAddress(Util.getProperty("FROM_EMAIL_ADDRESS")));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(Util.getProperty("TO_EMAIL_ADDRESSES"), true));

			message.setSubject(subject);
			message.setText(aBody);
			Transport.send(message);

		} catch (final Exception ex) {
			errorLogger.error(" Emailer : sendEmail() : 1 Email sending error " + ex);
		}
	}

	public static void sendEMail(String subject, String body) {

		try {
			Email email = new SimpleEmail();
			email.setHostName(Util.getProperty("HOST_NAME"));
			email.setSmtpPort(new Integer(Util.getProperty("HOST_PORT")).intValue());
			email.setAuthenticator(new DefaultAuthenticator(Util.getProperty("USER"), Util.getProperty("PASSWORD")));
			email.setSSLOnConnect(true);
			email.setFrom(Util.getProperty("FROM_EMAIL_ADDRESS"));
			email.setSubject(subject);
			email.setMsg(body);
			StringTokenizer st = new StringTokenizer(Util.getProperty("TO_EMAIL_ADDRESSES"), ",");
			while (st.hasMoreElements()) {
				String next = (String) st.nextElement();
				System.out.println(next);
				email.addTo(next);
			}
			email.send();

		} catch (final Exception ex) {
			errorLogger.error(" Emailer : sendEmail() : Email sending error " + ex.getMessage());
		}

	}

	public static void testEMail(String subject, String body) {

		/*
		 * try { Email email = new SimpleEmail(); email.setHostName("10.8.13.51");
		 * email.setSmtpPort(new Integer(25) .intValue());
		 * 
		 * //email.setAuthenticator(new DefaultAuthenticator(Util //
		 * .getProperty("USER"), Util.getProperty("PASSWORD")));
		 * email.setSSLOnConnect(true);
		 * email.setFrom("sysadmin-printserver@alliedvaughn.com");
		 * email.setSubject(subject); email.setMsg(body); /* StringTokenizer st = new
		 * StringTokenizer( Util.getProperty("TO_EMAIL_ADDRESSES"), ","); while
		 * (st.hasMoreElements()) { String next = (String) st.nextElement();
		 * System.out.println(next); email.addTo(next); }
		 */
		// email.addTo("chicagoram@gmail.com");
		// email.send();

		// } catch (final Exception ex) {
		// ex.printStackTrace();
		// }

		System.out.println("SimpleEmail Start");

		String smtpHostServer = "10.8.13.51";
		String emailID = "sysadmin-printserver@alliedvauhn.com";

		Properties props = System.getProperties();

		props.put("mail.smtp.host", smtpHostServer);

		Session session = Session.getInstance(props, null);

		// EmailUtil.sendEmail(session, emailID,"SimpleEmail Testing Subject",
		// "SimpleEmail Testing Body");
	}

	/**
	 * Fetch config.
	 */
	private static void fetchConfig() {

		errorLogger.debug("Emailer:fetchConfig() ");

		InputStream input = null;
		try {
			// If possible, one should try to avoid hard-coding a path in this
			// manner; in a web application, one should place such a file in
			// WEB-INF, and access it using ServletContext.getResourceAsStream.
			// Another alternative is Class.getResourceAsStream.
			// This file contains the javax.mail config properties mentioned
			// above.
			input = new FileInputStream(Util.getProperty("EMAIL_CONFIG_DIR"));
			Emailer.fMailServerConfig.load(input);
		} catch (final Exception ex) {
			errorLogger.error("Emailer : FetchConfig() : 1 - Cannot open and load mail server properties file."
					+ ex.getMessage());

		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (final IOException e) {
				errorLogger.error(
						"Emailer : FetchConfig() : 2 - Cannot close mail server properties file." + e.getMessage());
			}
		}

	}

}
