package fr.univavignon.biblioproc;

/*
 * Biblio Post Process
 * Copyright 2011-2017 Vincent Labatut 
 * 
 * This file is part of Biblio Post Process.
 * 
 * Biblio Post Process is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Biblio Post Process is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Biblio Post Process.  If not, see <http://www.gnu.org/licenses/>.
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

public class DetectHyphens
{	
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{	// retrieve all .isi files in the folder
		System.out.println("Scanning the folder " + FOLDER);
		File folder = new File(FOLDER);
		File[] files = folder.listFiles(new FilenameFilter()
		{	@Override
			public boolean accept(File arg0, String arg1)
			{	boolean result = arg1.endsWith(FILE_EXT);
				//System.out.println(arg1+" >> "+result);
				return result;
			}
		});
		System.out.println(">> "+ files.length + " files found");
		
		// parse the PDF files
		int count = 0;
		for(File file: files)
		{	count++;
			System.out.print(count);
			if(file.getName().contains("�"))
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
	/** Extension of the documents */
	private static final String FILE_EXT = ".pdf";
}