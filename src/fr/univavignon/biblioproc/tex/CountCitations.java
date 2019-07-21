package fr.univavignon.biblioproc.tex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.univavignon.biblioproc.tools.file.FileNames;

import fr.univavignon.tools.file.FileTools;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;

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

/**
 * Counts how many times BibTeX references appear in some LaTeX file.
 * The result is recorded in a text file placed in the out folder.
 * 
 * @author Vincent Labatut
 */
public class CountCitations
{	
	/**
	 * Method used to launch this processing. Just change
	 * the latex file when calling {@link #countCitations(String)}.
	 * 
	 * @param args
	 * 		None used.
	 * 
	 * @throws Exception
	 * 		Whatever exception.
	 */
	public static void main(String[] args) throws Exception
	{	
//		String texFile = "test.tex";
		String texFile = "article.tex";
		
		countCitations(texFile);
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/////////////////////////////////////////////////////////////////
	// PROCESSING	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** 
	 * Regex pattern used to look for BibTeX keys.
	 * Originally taken from https://stackoverflow.com/a/29210433/1254730
	 * Then modified to consider command starting with "cite" (and not just
	 * exaclty "cite") 
	 */
	private final static Pattern BIBTEX_PATTERN = Pattern.compile("(?:\\\\cite\\w*\\{|(?<!^)\\G),?\\s*([^,}]+)");
	
	/**
	 * Counts how many times BibTeX references appear in some LaTeX file.
	 * The result is recorded in a text file of the same name, placed in the 
	 * output folder.
	 * 
	 * @param texFile
	 * 		Input LaTeX file.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the input our output file.
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing the input our output file.
	 */
	private final static void countCitations(String texFile) throws FileNotFoundException, UnsupportedEncodingException
	{	logger.setName("CountCitations");
		logger.log("Start counting BibTeX keys in file \""+texFile+"\"");
		logger.increaseOffset();
		
		// read the latex file
		logger.log("Reading the file content");
		String filePath = FileNames.FO_LATEX + File.separator + texFile;
		String content = FileTools.readTextFile(filePath, "UTF-8");
		
		// look for \cite commands and count the BibTeX ketys
		logger.log("Parsing the LaTeX file");
		logger.increaseOffset();
		Map<String,Integer> counts = new TreeMap<String, Integer>();
        Matcher matcher = BIBTEX_PATTERN.matcher(content);
        while(matcher.find())
        {	// get key
        	String key = matcher.group(1);
	    	int startPos = matcher.start();
	    	int endPos = matcher.end();
        	logger.log(startPos+"-"+endPos+" >> "+key);
        	// add to map
        	Integer count = counts.get(key);
        	if(count==null)
        		count = 1;
        	else
        		count++;
        	counts.put(key, count);
        }
		logger.decreaseOffset();
		
		// record the produced map
		String outFile = FileNames.FO_OUTPUT + File.separator + texFile + FileNames.EX_TEXT;
		logger.log("Recording the bitex key counts in file \""+outFile+"\"");
		PrintWriter pw = FileTools.openTextFileWrite(outFile, "UTF-8");
		for(Entry<String,Integer> entry: counts.entrySet())
		{	String key = entry.getKey();
			Integer count = entry.getValue();
			pw.println(key+"\t"+count);
		}
		pw.close();
		
		// displaying single-occurrence keys
    	logger.log("Keys appearing only once:");
		logger.increaseOffset();
		for(Entry<String,Integer> entry: counts.entrySet())
		{	String key = entry.getKey();
			Integer count = entry.getValue();
			if(count==1)
				logger.log(key);
		}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
	}
}
