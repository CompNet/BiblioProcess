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
import java.io.UnsupportedEncodingException;

import fr.univavignon.biblioproc.tools.FileTools;

/**
 * Old class used to clean file names.
 * 
 * @author Vincent Labatut
 */
public class DetectHyphens
{	
	/**
	 * Prints the names of files containing a long hyphen.
	 * 
	 * @param args
	 *		Not used. 
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while parsing the files.
	 * @throws UnsupportedEncodingException
	 * 		Problem while parsing the files.
	 */
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{	// retrieve all .isi files in the folder
		System.out.println("Scanning the folder " + FOLDER);
		File folder = new File(FOLDER);
		FilenameFilter ff = FileTools.createExtensionFilter(FileTools.EX_PDF);
		File[] files = folder.listFiles(ff);
		System.out.println(">> "+ files.length + " files found");
		
		// parse the PDF files
		int count = 0;
		for(File file: files)
		{	count++;
			System.out.print(count);
			if(file.getName().contains("\u2012")
					|| file.getName().contains("\u2013")
					|| file.getName().contains("\u2014")
					|| file.getName().contains("\u2015")
					|| file.getName().contains("\u2053"))
				System.out.println(file.getName());
			else
				System.out.println();
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// FILES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Folder containing all files */
	private static final String FOLDER = "C:/Users/Vincent/Documents/Travail/Ecrits/Networks Analysis/Biblio";
}
