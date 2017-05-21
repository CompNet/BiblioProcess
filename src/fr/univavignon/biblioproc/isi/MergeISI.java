package fr.univavignon.biblioproc.isi;

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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import fr.univavignon.biblioproc.tools.FileTools;

/**
 * This class takes a set of ISI files, and
 * merge them (taking care of the EOF markers).
 *  
 * @author Vincent Labatut
 */
public class MergeISI
{	
	/**
	 * Opens the specified folder, retrieves the list of ISI files,
	 * combines them to form a single file containing all
	 * references.
	 * 
	 * @param args
	 * 		Not used.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while opening the input ISI files or the generated output file.
	 * @throws UnsupportedEncodingException
	 * 		Problem while encoding the text, writing in the file.
	 */
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{	// retrieve all .isi files in the folder
		System.out.println("Scanning the folder " + FileTools.FO_ISI);
		File folder = new File(FileTools.FO_ISI);
		FilenameFilter ff = FileTools.createExtensionFilter(FileTools.EX_ISI);
		File[] files = folder.listFiles(ff);
		System.out.println(">> "+ files.length + " files found");
		
		// open the output file
		System.out.println("Opening output file " + FileTools.FI_ISI_ALL);
		PrintWriter writer = FileTools.openTextFileWrite(FileTools.FI_ISI_ALL, "UTF8");
		
		// parse the ISI files
		int count = 1;
		for(File file: files)
		{	// open the input file
			System.out.println("Processing input file " + count + "/" + files.length + " " + file);
			Scanner scanner = FileTools.openTextFileRead(file, null);
			
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
}
