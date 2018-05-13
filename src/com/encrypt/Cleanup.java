package com.encrypt;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.encrypt.util.Util;

public class Cleanup {

	private static final Logger debugLogger = Logger
			.getLogger("cleanupDebuglog");
	private static final Logger errorLogger = Logger
			.getLogger("cleanupErrorlog");
	private static final Logger reportLogger = Logger
			.getLogger("cleanupReportlog");
	private static String cleanup_dir = null;
	private static String job_status_xtn = null;
	private static String content_type_wild = null;
	private static String microtech_job_dir = null;
	private static String jobs_archive_dir = null;
	private static String log4j_loc = null;

	public static void main(String[] args) {

		InetAddress ip = null;
		try {
			ip = InetAddress.getLocalHost();

						// Monitor WATCH_DIR every 30 seconds
			if (args == null || args.length == 0 || args[2] == null
					|| args[3]  == null || args[4] == null) {
				debugLogger
						.debug("Please set the environment before starting the application System exiting...");
				errorLogger
						.error("Please set the environment before starting the application");
				System.exit(0);
			}
			// Util.setupCleanupEnv(args[0], args[1], args[2], args[3]);

			setupCleanupEnv(args[0], args[1], args[2], args[3], args[4], args[5]);
			
			Properties properties = new Properties();
			properties.load(new FileInputStream(Cleanup.log4j_loc));
			LogManager.resetConfiguration();
			PropertyConfigurator.configure(properties);


			
			processJobsDrecitory(Cleanup.cleanup_dir);

			
			debugLogger.debug(("Debug Process Completed"));

		} catch (Exception e) {
			errorLogger.error("Content deletion exception " + e.getMessage());
			e.printStackTrace();
			sendEMail(
					"Encryption content folder cleanup process failure  - @ Machine   - "
							+ ip, e.getMessage());

		}

	}

	/**
	 * Store the file names and the last modified timestamps of all the files
	 * and directories that exist in the directory at this moment.
	 */
	private static void processJobsDrecitory(String directory) throws Exception {

		debugLogger.debug("takesnapshot() of the directory.....");

		File dir = new File(directory);
		FileFilter fileFilter = new WildcardFileFilter(
				Cleanup.content_type_wild, IOCase.INSENSITIVE);
		File[] currentFiles = dir.listFiles(fileFilter);
		if (currentFiles != null && currentFiles.length > 0) {
			File job_status_file = null;
			debugLogger.debug("clean up directory size " + currentFiles.length);
			for (File file : currentFiles) {

				
				 job_status_file = new File(Cleanup.microtech_job_dir
						+ "\\" + FilenameUtils.getBaseName(file.getName())
						+ Cleanup.job_status_xtn);
				if (job_status_file.exists()) {
					reportLogger.info("burn Job existed for  the content "
							+ file);
					file.delete();
				
				}
				else {
					 job_status_file = new File(Cleanup.jobs_archive_dir
							+ "\\" + FilenameUtils.getBaseName(file.getName())
							+ Cleanup.job_status_xtn);
					 if (job_status_file.exists()) {
							reportLogger.info("burn Job existed for  the content "
									+ file);
							file.delete();
						}
			
				}
					File mapFile = new File(file.getParentFile() + "\\"
							+ file.getName() + ".map");
					if (mapFile.exists()) {
						reportLogger.info("deleting the corresponding map "
								+ mapFile);
						mapFile.delete();

					}
				}

			}
		}

	public static void setupCleanupEnv(String microtech_enc_cont_dir,
			String job_dir, String archive_dir, String xtn, String wildcard, String log4j) {

		cleanup_dir = microtech_enc_cont_dir;
		microtech_job_dir = job_dir;
		job_status_xtn = xtn;
		content_type_wild = wildcard;
		jobs_archive_dir = archive_dir;
		log4j_loc = log4j;

	}

	public static void sendEMail(String subject, String body) {

		try {
			Email email = new SimpleEmail();
			email.setHostName(Util.getProperty("HOST_NAME"));
			email.setSmtpPort(new Integer(Util.getProperty("HOST_PORT"))
					.intValue());
			email.setAuthenticator(new DefaultAuthenticator(Util
					.getProperty("USER"), Util.getProperty("PASSWORD")));
			email.setSSLOnConnect(true);
			email.setFrom(Util.getProperty("FROM_EMAIL_ADDRESS"));
			email.setSubject(subject);
			email.setMsg(body);
			StringTokenizer st = new StringTokenizer(
					Util.getProperty("TO_EMAIL_ADDRESSES"), ",");
			while (st.hasMoreElements()) {
				String next = (String) st.nextElement();
				System.out.println(next);
				email.addTo(next);
			}
			email.send();

		} catch (final Exception ex) {
			errorLogger.error(" Emailer : sendEmail() : Email sending error "
					+ ex.getMessage());
		}

	}

}