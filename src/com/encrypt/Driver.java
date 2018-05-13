package com.encrypt;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.encrypt.impl.FileListener;
import com.encrypt.service.DirectoryWatchService;
import com.encrypt.service.MicrotechJobListeningService;
import com.encrypt.util.Util;

public class Driver {

    
    private static final Logger debugLogger = Logger.getLogger("debuglog");
	private static final Logger errorLogger = Logger.getLogger("errorlog");
	private static ExecutorService mainexecutorpool = null;
	private static ExecutorService encryptorexecutorpool = null;
	
	static {

		// TODO Auto-generated constructor stub
		mainexecutorpool = Executors.newCachedThreadPool();
		encryptorexecutorpool = Executors.newCachedThreadPool();
	}


	public static ExecutorService getEncryptorexecutorpool() {
		return encryptorexecutorpool;
	}

	public static void setEncryptorexecutorpool(
			ExecutorService encryptorpool) {
		encryptorexecutorpool = encryptorpool;
	}

	public static void setExecutorthreadpool(ExecutorService executorthreadpool) {
		mainexecutorpool = executorthreadpool;
	}


    public static void main(String[] args) {
        try {
        	
        	
        	
//        	PropertyConfigurator.configure("log4j.properties");

    		// Monitor WATCH_DIR every 30 seconds
    		if (args == null || args.length == 0 || args[0] == null
    				|| args[1] == null || args[2] == null || args[3] == null
    				|| args[4] == null || args[5] == null || args[6] == null
    				|| args[7] == null || args[8] == null) {
    			debugLogger
    					.debug("Please set the environment before starting the application System exiting...");
    			errorLogger
    					.error("Please set the environment before starting the application");
    			System.exit(0);
    		}
    		Util.setupEnv(args[0], args[1], new Integer(args[2]).intValue(),
    				args[3], args[4], args[5], args[6], args[7], new Integer(
    						args[8]).intValue(), args[9]);


    		Properties properties = new Properties();
			properties.load(new FileInputStream(Util.getLog4j_loc()));
			LogManager.resetConfiguration();
			PropertyConfigurator.configure(properties);

    		MicrotechJobListeningService watchService = new MicrotechJobListeningService(); // May throw
            watchService.register( // May throw
                    new DirectoryWatchService.OnFileChangeListener() {
                        @Override
                        public void onFileCreate(Path file) {
                            // File created
                        	debugLogger.debug("file created" + file);
                        	new FileListener().onAdd(file);
                        }
                
                        @Override
                        public void onFileModify(Path file) {
                            // File modified
                        	debugLogger.debug("file modified" + file);
                        }
                        
                        @Override
                        public void onFileDelete(Path file) {
                            // File deleted
                        	debugLogger.debug("file deleted" + file);
                        }
                    },
                    Util.getWatchDir(), "*.*" // Directory to watch and file pattern
              
            );
           
    		

    		Future<?> future = mainexecutorpool.submit(watchService);
    		// executor.shutdown();

    		if (future.isDone()) {
    			debugLogger.debug("\t\t main method is done !");
    		} else {
    			System.out.println("\t\t main method is not done !");
    		}

            
        } catch (Exception e) {
            errorLogger.error("Unable to register file change listener for " + e.getMessage());
        }

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // watchService.stop();
                errorLogger.error("Driver thread interrupted.");
                break;
            }
        }
    }
}