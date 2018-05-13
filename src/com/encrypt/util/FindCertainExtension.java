package com.encrypt.util;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;

public class FindCertainExtension {

	private static final String FILE_DIR = "c:\\JimTest";
	private static final String FILE_TEXT_EXT = ".pdf";

	public static void main(String args[]) {
		new FindCertainExtension().listFile(FILE_DIR, FILE_TEXT_EXT);
	}

	public void listFile(String folder, String ext) {

		GenericExtFilter filter = new GenericExtFilter(ext);

		File dir = new File(folder);

		if (dir.isDirectory() == false) {
			System.out.println("Directory does not exists : " + FILE_DIR);
			return;
		}

		// list out all the file name and filter by the extension
		String[] list = dir.list(filter);

		if (list.length == 0) {
			System.out.println("no files end with : " + ext);
			return;
		}

		for (String file : list) {
			String temp = new StringBuffer(FILE_DIR).append(File.separator).append(file).toString();
			File files = new File(temp);
			System.out.println(files.getParentFile());
			System.out.println(files.getParent());
			System.out.println("file : " + temp);
			Path path = files.toPath();
			System.out.println(path.getFileName());
		}
	}

	// inner class, generic extension filter
	public class GenericExtFilter implements FilenameFilter {

		private String ext;

		public GenericExtFilter(String ext) {
			this.ext = ext;
		}

		@Override
		public boolean accept(File dir, String name) {

			return (name.endsWith("pdf") || name.endsWith("tif"));
		}
	}

}
