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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import fr.univavignon.biblioproc.tools.FileTools;

/**
 * Main class, allowing to launch the whole process.
 *  
 * @author Vincent Labatut
 */
public class Launcher
{	
	/**
	 * xxx
	 * 
	 * @param args
	 * 		Not used.
	 * 
	 * @throws Exception
	 * 		Whatever exception occured.
	 */
	public static void main(String[] args) throws Exception
	{	//TODO control the use of "normalize", since we now want to export bibtex files too.
		
		/**TODO
		 * - finish using the new bibtex constants
		 * - check by loading actual bibtex file that no bibtex attribute is ignored
		 * - check the ciw stuff works by loading the file
		 * - synchronize all ciw entries with exactly one bibtex entry
		 * - generate the short ref for full ciw entries
		 * - be able to synch ciw short ref with bibtex entries. maybe use manual annotations stored in the "notes" bibtex file?
		 * 
		 * - generate a folder containing all the PDF file of the articles listed in the bibtex file
		 * - generate the list of articles present as PDF files but missing from the bibtex file
		 */
		
		
		/* SIGNED NETS
		 * 1) load the jabref file, store the authors and articles
		 * 2) load the ISI file, match the authors and articles (including references)
		 * 
		 * matching the full ISI entries with the jabref file is straightforward.
		 * not so much for the ISI short citations used to list the article references.
		 * 	- generate the same short form based on the bibtex data?
		 *  - take advantage of the ISI API?
		 *  - use the DOI whenever available.
		 * 
		 * some articles have distinct references (as a proper paper and as a preprint, for
		 * instance). 
		 * 
		 */
		
		/* COM DET
		 * goal: complete review file using main file
		 *  - ignore comments
		 *  - automate? or just list, so that we can complete manually?
		 */
	}
}
