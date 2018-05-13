package com.encrypt.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.concurrent.Callable;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.encrypt.util.Util;

public class EncryptorService implements Callable<String> {
	private String sourceFile = null;
	private String jobId = null;

	public String getSourceFile() {
		return sourceFile;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	/** The Constant logger. */

	private static final Logger debugLogger = Logger.getLogger("debuglog");
	private static final Logger errorLogger = Logger.getLogger("errorlog");

	@Override
	public String call() throws Exception {
		String encryptedOutput = null;
		try {

			String copyTarget = Util.getEncrypted_output() + "\\" + new File(this.getSourceFile()).getName();
			encryptedOutput = Util.getEncrypted_output() + "\\"
					+ FilenameUtils.getBaseName(getJobId()) + "."
					+ FilenameUtils.getExtension(sourceFile);
			String[] list = { Util.getEncryption_process(),
					this.getSourceFile(), copyTarget, encryptedOutput };
			ProcessBuilder builder = new ProcessBuilder(list);
			File log = new File(Util.getEncryption_log());
			builder.redirectErrorStream(true);
			builder.redirectOutput(Redirect.appendTo(log));
			final Process p = builder.start();

			InputStream stderr = p.getErrorStream(); // or one may use
														// getInputStream to get
														// the actual output of
														// process
			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				debugLogger.debug("status of converting file "
						+ getSourceFile() + " " + line);
			}
			int returncode = p.waitFor();
			debugLogger.debug(" Return code after converting file "
					+ getSourceFile() + "=" + returncode);
		

		} catch (Exception err) {
			errorLogger.error("Preprocessor Error  art file " + getSourceFile()
					+ " " + err.getMessage());
		}

		return encryptedOutput;
	}
}