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

import fr.univavignon.biblioproc.tools.file.FileTools;

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
	{	/**TODO
		 * - USE THE DOI for matching short refs
		 * 
		 * 
		 * - generate a folder containing all the PDF file of the articles listed in the bibtex file
		 * - generate the list of articles present as PDF files but missing from the bibtex file
		 */
		
		
		/* SIGNED NETS
		 * x) load the jabref file, store the authors and articles
		 * 2) load the ISI file, match the authors and articles (including references)
		 */
	}
}

//TODO pour les bouquins, la source citée en court est le nom de la série...
//faut donc un traitement spécifique