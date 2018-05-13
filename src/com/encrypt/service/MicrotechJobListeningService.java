package com.encrypt.service;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.encrypt.util.Util;

public class MicrotechJobListeningService implements DirectoryWatchService,
		Runnable {

	/**
	 * Logger instance
	 * 
	 * 
	 */

	/** The Constant logger. */

	private static final Logger debugLogger = Logger.getLogger("debuglog");
	private static final Logger errorLogger = Logger.getLogger("errorlog");
	private final WatchService mWatchService;
	private final AtomicBoolean mIsRunning;
	private final ConcurrentMap<WatchKey, Path> mWatchKeyToDirPathMap;
	private final ConcurrentMap<Path, Set<OnFileChangeListener>> mDirPathToListenersMap;
	private final ConcurrentMap<OnFileChangeListener, Set<PathMatcher>> mListenerToFilePatternsMap;

	

	private static <K, V> ConcurrentMap<K, V> newConcurrentMap() {
		return new ConcurrentHashMap<>();
	}

	private static <T> Set<T> newConcurrentSet() {
		return Collections.newSetFromMap(newConcurrentMap());
	}

	public static PathMatcher matcherForGlobExpression(String globPattern) {
		return FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
	}

	public static boolean matches(Path input, PathMatcher pattern) {
		return pattern.matches(input);
	}

	public static boolean matchesAny(Path input, Set<PathMatcher> patterns) {
		for (PathMatcher pattern : patterns) {
			if (matches(input, pattern)) {
				return true;
			}
		}

		return false;
	}

	private Path getDirPath(WatchKey key) {
		return mWatchKeyToDirPathMap.get(key);
	}

	private Set<OnFileChangeListener> getListeners(Path dir) {
		return mDirPathToListenersMap.get(dir);
	}

	private Set<PathMatcher> getPatterns(OnFileChangeListener listener) {
		return mListenerToFilePatternsMap.get(listener);
	}

	private Set<OnFileChangeListener> matchedListeners(Path dir, Path file) {

		return getListeners(dir).stream()
				.filter(listener -> matchesAny(file, getPatterns(listener)))
				.collect(Collectors.toSet());
	}

	public MicrotechJobListeningService() throws Exception {

		mWatchService = FileSystems.getDefault().newWatchService();
		mIsRunning = new AtomicBoolean(false);
		mWatchKeyToDirPathMap = newConcurrentMap();
		mDirPathToListenersMap = newConcurrentMap();
		mListenerToFilePatternsMap = newConcurrentMap();

	}

	@Override
	public void run() {
		System.out.println("run meth" + Thread.currentThread().getName());

		try {

			for (;;) {
				// wait for key to be signalled
				WatchKey key = mWatchService.take();
				System.out.println("WatchKey recognized!");

				if (null == getDirPath(key)) {
					debugLogger.debug("Watch key not recognized.");
					continue;
				}
				for (WatchEvent<?> event : key.pollEvents()) {

					WatchEvent.Kind<?> kind = event.kind();

					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					// Path fileName = ev.context();

					
					if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
						System.out.println("Event create or modify triggered!");
						 Path fileName = Paths.get(Util.getWatchDir() + "\\"
								+ ev.context().toFile().getName());

						// TODO: handle event. E.g. call listeners
						if (!(FilenameUtils.isExtension(event.context()
								.toString(), Util.getJobFileXtension()))) {
							errorLogger.error("Processing very Large file");
							try {
								Thread.sleep(1000);
							} catch (Exception e) {
								e.printStackTrace();
								errorLogger.error("Too long a wait");
							}
							System.out.println("Inside the PartFile "
									+ ev.context().toString());
						} else {
							 matchedListeners(getDirPath(key), ev.context())
		                        .forEach(listener -> listener.onFileCreate(fileName));
								System.out.println("File Processing Complete "
									+ ev.context().toString());
							// Do what ever you want to do with this files.
						}
					}
				}

				// reset key
				if (!key.reset()) {
					break;
				}
			}
		} catch (Exception x) {
			return;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(OnFileChangeListener listener, String dirPath,
			String... globPatterns) throws IOException {
		Path dir = Paths.get(dirPath);

		if (!Files.isDirectory(dir)) {
			throw new IllegalArgumentException(dirPath + " is not a directory.");
		}

		if (!mDirPathToListenersMap.containsKey(dir)) {
			// May throw
			WatchKey key = dir.register(mWatchService, ENTRY_CREATE,
					ENTRY_MODIFY);

			mWatchKeyToDirPathMap.put(key, dir);
			mDirPathToListenersMap.put(dir, newConcurrentSet());
		}

		getListeners(dir).add(listener);

		Set<PathMatcher> patterns = newConcurrentSet();

		for (String globPattern : globPatterns) {
			patterns.add(matcherForGlobExpression(globPattern));
		}

		if (patterns.isEmpty()) {
			patterns.add(matcherForGlobExpression("*")); // Match everything if
															// no filter is
															// found
		}

		mListenerToFilePatternsMap.put(listener, patterns);

		debugLogger.debug("Watching files matching "
				+ Arrays.toString(globPatterns) + " under " + dirPath
				+ " for changes.");
	}

	/**
	 * Start this <code>SimpleDirectoryWatchService</code> instance by spawning
	 * a new thread.
	 *
	 * @see #stop()
	 */
	@Override
	public void start() {
		if (mIsRunning.compareAndSet(false, true)) {
			Thread runnerThread = new Thread(this,
					DirectoryWatchService.class.getSimpleName());
			runnerThread.start();
		}

	}

	@Override
	public void stop() {
		// Kill thread lazily
		mIsRunning.set(false);
	}

}
