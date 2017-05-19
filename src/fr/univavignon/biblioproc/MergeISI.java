package fr.univavignon.biblioproc;

/*
 * Biblio Process
 * Copyright 2011-2017 Vincent Labatut 
 * 
 * This file is part of Biblio Process.
 * 
 * Biblio Process is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Biblio Process is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Biblio Process.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 * This class takes a set of ISI files, and
 * merge them (taking care of the EOF markers).
 *  
 * @since 1
 * @version 1
 * @author Vincent Labatut
 */
public class MergeISI
{	
	/**
	 * Open the specified folder, retrieve the list of ISI files,
	 * combine them to form a single file containing all
	 * references.
	 * 
	 * @param args
	 * 		Not used.
	 * @throws FileNotFoundException
	 * 		Problem while opening the input ISI files or the generated output file.
	 * @throws UnsupportedEncodingException
	 * 		Problem while encoding the text, writing in the file.
	 */
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{	// retrieve all .isi files in the folder
		System.out.println("Scanning the folder " + FOLDER);
		File folder = new File(FOLDER);
		File[] files = folder.listFiles(new FilenameFilter()
		{	@Override
			public boolean accept(File arg0, String arg1)
			{	boolean result = arg1.endsWith(FILE_EXT);
				result = result && !arg1.startsWith(OUT_FILE);
				//System.out.println(arg1+" >> "+result);
				return result;
			}
		});
		System.out.println(">> "+ files.length + " files found");
		
		// open the output file
		String outFile = FOLDER + File.separator + OUT_FILE + FILE_EXT;
		System.out.println("Opening output file " + outFile);
		FileOutputStream fos = new FileOutputStream(outFile);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
		PrintWriter writer = new PrintWriter(osw);

		// parse the ISI files
		int count = 1;
		for(File file: files)
		{	// open the input file
			System.out.println("Processing input file " + count + "/" + files.length + " " + file);
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(fis);
			Scanner scanner = new Scanner(reader);
			
			// read it
			while(scanner.hasNextLine())
			{	String line = scanner.nextLine();
				if(line.equals("EF"))
					writer.println();
				else
					writer.println(line);
			}
			
			// close input file
			scanner.close();
			count++;
		}
		
		// close output file
		writer.close();
	}
	
	/////////////////////////////////////////////////////////////////
	// FILES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Folder containing all files */
	private static final String FOLDER = "C:/Users/Vincent/Documents/Travail/Ecrits/Network Extraction/ComDet Biblio/data/2012.07.13 - individual";
//	private static final String FOLDER = "C:/Documents and Settings/Vincent/Mes documents/Travail/Ecrits/Network Extraction/ComDet Biblio/data/2012.07.13 - individual";
	/** Extension of the ISI files */
	private static final String FILE_EXT = ".isi";
	/** Output file */
	private static final String OUT_FILE = "_all";
}