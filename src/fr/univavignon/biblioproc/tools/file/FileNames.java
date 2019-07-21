package fr.univavignon.biblioproc.tools.file;

/*
 * Biblio Process
 * Copyright 2011-19 Vincent Labatut 
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
public class FileNames extends fr.univavignon.tools.file.FileNames
{	
	/////////////////////////////////////////////////////////////////
	// FOLDERS		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
//	/** Input folder */
//	public final static String FO_INPUT = "in";
		/** Bibtex input folder */
		public final static String FO_BIBTEX = FO_INPUT + File.separator + "bibtex";
		/** Thomson ISI input folder */
		public final static String FO_ISI = FO_INPUT + File.separator + "isi";
		/** LaTeX input folder */
		public final static String FO_LATEX = FO_INPUT + File.separator + "latex";
//	/** Output folder */
//	public final static String FO_OUTPUT = "out";
//	/** Log folder */
//	public final static String FO_LOG = "log";
//	/** Resources folder */
//	public final static String FO_RESOURCES = "res";
//		/** Folder containing the XML schemas */
//		public final static String FO_SCHEMA = FO_RESOURCES + File.separator + "schemas";

	/////////////////////////////////////////////////////////////////
	// FILES		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** JabRef character networks file */
	public static final String FI_BIBTEX_CHARNETS = FO_BIBTEX + File.separator + "charnets" + EX_BIBTEX;
	/** JabRef review file */
	public static final String FI_BIBTEX_REVIEW = FO_BIBTEX + File.separator + "review" + EX_BIBTEX;
	/** JabRef complete file */
	public static final String FI_BIBTEX_COMPLETE = FO_BIBTEX + File.separator + "network analysis" + EX_BIBTEX;
	/** JabRef structural balance file */
	public static final String FI_BIBTEX_STRUCT_BAL = FO_BIBTEX + File.separator + "biblio" + EX_BIBTEX;
	/** ISI merged file for character networks */
	public static final String FI_ISI_ALL_CHARNETS = FO_ISI + File.separator + "charnets_savedrecs" + EX_ISI;
	/** ISI merged file for signed networks */
	public static final String FI_ISI_ALL_SIGNETS = FO_ISI + File.separator + "signets_savedrecs" + EX_ISI;
	/** Journal/conference short names */
	public static final String FI_ISI_NAMES = FO_ISI + File.separator + "short_names" + EX_TEXT;
	/** Error fixes */
	public static final String FI_ISI_FIXES = FO_ISI + File.separator + "error_fixes" + EX_TEXT;
	/** Ignored references */
	public static final String FI_ISI_IGNORED = FO_ISI + File.separator + "ignored_refs" + EX_TEXT;
	/** Manually completed references */
	public static final String FI_ISI_COMPLETED = FO_ISI + File.separator + "additional_refs" + EX_TEXT;
}
