package fr.univavignon.biblioproc.tools.file;

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

/**
 * This class contains file names and paths.
 *  
 * @author Vincent Labatut
 */
public class FileNames
{	
	/////////////////////////////////////////////////////////////////
	// EXTENSIONS	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Bash file extension */
	public final static String EX_BIBTEX = ".bib";
	/** Thomson ISI file extension */
	public final static String EX_ISI = ".ciw";
	/** PDF file extension */
	public static final String EX_PDF = ".pdf";
	/** Text file extension */
	public static final String EX_TXT = ".txt";

	/////////////////////////////////////////////////////////////////
	// FOLDERS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Input folder */
	public final static String FO_INPUT = "in";
		/** Bibtex input folder */
		public final static String FO_BIBTEX = FO_INPUT + File.separator + "bibtex";
		/** Thomson ISI input folder */
		public final static String FO_ISI = FO_INPUT + File.separator + "isi";
	/** Output folder */
	public final static String FO_OUTPUT = "out";
	/** Log folder */
	public final static String FO_LOG = "log";

	/////////////////////////////////////////////////////////////////
	// FILES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** JabRef review file */
	public static final String FI_BIBTEX_REVIEW = FO_BIBTEX + File.separator + "review" + EX_BIBTEX;
	/** JabRef complete file */
	public static final String FI_BIBTEX_COMPLETE = FO_BIBTEX + File.separator + "network analysis" + EX_BIBTEX;
	/** JabRef structural balance file */
	public static final String FI_BIBTEX_STRUCTBAL = FO_BIBTEX + File.separator + "biblio" + EX_BIBTEX;
	/** ISI merged file */
	public static final String FI_ISI_ALL = FO_ISI + File.separator + "savedrecs" + EX_ISI;
	/** Journal/conference short names */
	public static final String FI_ISI_NAMES = FO_ISI + File.separator + "short_names" + EX_TXT;
	/** Error fixes */
	public static final String FI_ISI_FIXES = FO_ISI + File.separator + "error_fixes" + EX_TXT;
	/** Ignored references */
	public static final String FI_ISI_IGNORED = FO_ISI + File.separator + "ignored_refs" + EX_TXT;
}
