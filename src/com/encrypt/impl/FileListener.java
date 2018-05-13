package com.encrypt.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.InetAddress;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import com.encrypt.Driver;
import com.encrypt.arch.BaseListener;
import com.encrypt.arch.IFileListener;
import com.encrypt.exception.FileMoveException;
import com.encrypt.exception.ResourceAddException;
import com.encrypt.service.EncryptorService;
import com.encrypt.util.Emailer;
import com.encrypt.util.Util;

public class FileListener extends BaseListener implements IFileListener {

	private static final Logger errorLogger = Logger.getLogger("errorlog");
	private static final Logger debugLogger = Logger.getLogger("debuglog");
	private static final Logger txnLogger = Logger.getLogger("txnlog");

	@Override
	public void onStart(Object monitoredResource) {
		// On startup
		if (monitoredResource instanceof File) {
			File resource = (File) monitoredResource;
			if (resource.isDirectory()) {

				debugLogger
						.debug("Start to monitor the resource........................ "
								+ resource.getAbsolutePath());

			}
		}
	}

	/**
	 * Connstructor
	 */
	public FileListener() {
		super();
	}

	@Override
	public void onStop(Object notMonitoredResource) {

	}

	@Override
	public void onAdd(Object newResource) {
		debugLogger.debug("adding the resource and triggering File Listener");
		if (newResource instanceof Path) {
			Path filepath = (Path) newResource;
			if (filepath.toFile().isFile()) {
				boolean fileIsReady = false;
				while (!fileIsReady) {
					try {

						filepath.toFile().canWrite();
						fileIsReady = true;
					} catch (Exception e) {

						errorLogger.error("Failed to add file "
								+ filepath.toFile().getName());
						throw new ResourceAddException(Util.ADD_RESOURCE_ERROR,
								e);

					}
				}

				debugLogger.debug("About to initiate processing the  file");
				if (filepath.toFile().canRead())
					processFile(filepath);

			}
		}
	}

	private void processFile(final Path fr) {

		debugLogger.debug("process file in progress " + " path "
				+ fr.toFile().getName());

		String line = null;
		List<String> records = new ArrayList<String>();
		String value = "";

		try (BufferedReader fs = new BufferedReader(new FileReader(fr.toFile()));) {

			// wrap a BufferedReader around FileReader

			// use the readLine method of the BufferedReader to read one line at
			// a time.
			// the readLine method returns null when there is nothing else to
			// read.
			while ((line = fs.readLine()) != null) {

				String[] data = line.split("=");

				if (data[0].trim().equals("DVDFile")) {
					value = data[1];

				}
				records.add(line);
			}
		} catch (Exception e) {
			errorLogger.error("LOC007:- File Read Error " + " Error Message - "
					+ e.getMessage());
		}
		// close the BufferedReader when we're done
		try {

			String encryptedContent = null;
			boolean exists = checkIfTheContentExists(new File(value.trim()));
			if (exists) {
				encryptedContent = encryptContent(value.trim(), fr.toFile());

			}

			List<String> newrecords = new ArrayList<String>();
			int j = 0;

			for (int i = 0; i < records.size(); i++) {
				String newline = records.get(i);
				String[] newdata = newline.split("=");
				if (newdata[0].trim().equals("DVDFile")) {
					System.out.println("Encryted location" + encryptedContent);

					newrecords.add(j, "DVDFile =  " + encryptedContent);
					j = j + 1;
				} else {
					if ((!newdata[0].trim().equals("SonicMethod"))
							&& (!newdata[0].trim().equals("SonicFile"))) {

						newrecords.add(j, newline);
						j = j + 1;
					}
				}

			}
			createNewJob(newrecords, fr.toFile().getName());

		} catch (Exception e) {
			errorLogger.error("LOC008:- File processing error "
					+ " Error Message - " + e.getMessage());
		} finally {

			moveFiletoNewDestination(fr.toFile(), Util.getProcessDest());
		}

		txnLogger.info("Processing Ends@.....................................");

		txnLogger.info("\n");

	}

	private void createNewJob(List<String> records, String jobFilename) {

		// java.nio.charset.Charset charset =
		// java.nio.charset.StandardCharsets.US_ASCII;

		try (java.io.BufferedWriter writer = new BufferedWriter(new FileWriter(
				Util.getMicrotech_job_dir() + "/" + jobFilename))) {

			for (int i = 0; i < records.size(); i++) {

				writer.write(records.get(i));
				writer.newLine();
			}
			writer.flush();

		} catch (Exception e) {
			e.printStackTrace();
			errorLogger.error("LOC009:- File write error "
					+ " Error Message - " + e.getMessage() + " job id "
					+ jobFilename);
		}

	}

	private String encryptContent(String sourceFile, File jobFileName)
			throws Exception {
		EncryptorService es = new EncryptorService();
		es.setSourceFile(sourceFile);
		es.setJobId(jobFileName.getName());
		Future<?> future = Driver.getEncryptorexecutorpool().submit(es);

		return (String) future.get();

	}

	private static boolean checkIfTheContentExists(File file) throws Exception {

		boolean contentVerification = false;
		int attempts = 0;

		if (file.exists() && !file.isDirectory()) {
			return true;
		}
		do {
			attempts += 1;
			try {
				if (file.exists() && !file.isDirectory()) {
					return true;
				}
			} catch (Exception e) {
				errorLogger.error("LOC010:- File accessing error "
						+ " Error Message - " + e.getMessage());
			}
		} while (attempts < 3);

		if (!contentVerification) {
			String message = "\"Microtech Listener checkIfTheContentExists()  :- Either the file \"\r\n"
					+ file.getName()
					+ " "
					+ "\r\n"
					+ "							+ \" does not exist or Network path not found - 3 attempts made and failed - make sure network maps are active ";
			errorLogger.error(message);
			Emailer.sendEMail("CSS Listener Failure  - @ Machine   - "
					+ InetAddress.getLocalHost() + " ", message);

		}
		return contentVerification;
	}

	private void moveFiletoNewDestination(File afile, String dest) {

		Path target = null;

		try {

			debugLogger.debug("Moving file...................... "
					+ afile.toString() + " to destination... " + dest);

			String filename = afile.getName().toString();
			target = FileSystems.getDefault().getPath(dest, filename);
			Files.move(afile.toPath(), target,
					StandardCopyOption.REPLACE_EXISTING);

		}

		catch (Exception e) {
			errorLogger
					.error("FileListener moveFileToNewDestination() 1 - error moving file  "
							+ afile.toString()
							+ " Error Message  "
							+ e.getMessage());
			throw new FileMoveException(
					"FileListener moveFileToNewDestination() 2 - error moving file  "
							+ afile.toString() + " Error Message = "
							+ e.getMessage(), e);

		}

	}

}
