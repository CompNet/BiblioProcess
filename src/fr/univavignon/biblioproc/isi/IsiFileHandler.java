package fr.univavignon.biblioproc.isi;

import java.io.File;

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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import fr.univavignon.biblioproc.bibtex.JabrefFileHandler;
import fr.univavignon.biblioproc.data.biblio.Article;
import fr.univavignon.biblioproc.data.biblio.Author;
import fr.univavignon.biblioproc.data.biblio.Corpus;
import fr.univavignon.biblioproc.data.biblio.SourceType;
import fr.univavignon.biblioproc.tools.file.FileNames;
import fr.univavignon.biblioproc.tools.file.FileTools;
import fr.univavignon.biblioproc.tools.log.HierarchicalLogger;
import fr.univavignon.biblioproc.tools.log.HierarchicalLoggerManager;
import fr.univavignon.biblioproc.tools.string.StringTools;

/**
 * Class dedicated to reading ISI files.
 *  
 * @author Vincent Labatut
 */
public class IsiFileHandler
{	
	/**
	 * Creates a new ISI handler based on the data previously loaded
	 * from a Bibtex file. When loading an ISI file, we will try to
	 * match the loaded references and authors with the ones already 
	 * present in the maps.
	 * 
	 * @param corpus
	 * 		Collection of articles and authors.
	 */
	public IsiFileHandler(Corpus corpus)
	{	this.corpus = corpus;
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/////////////////////////////////////////////////////////////////
	// DATA			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Collection of articles */
	public Corpus corpus = new Corpus();
	/** String used to create new BibTex keys */
	private final static String NEW_KEY = "NewKey";
	/** List of series of proceedings ignored during certain processing steps */
	private final static List<String> IGNORED_SERIES = Arrays.asList(
		"lecture notes in computer science",
		"lecture notes in artificial intelligence"
	);
	
	/////////////////////////////////////////////////////////////////
	// SHORT NAMES	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Stores the long name of a journal/conference based on its short name */
	public final static Map<String,List<String>> SHORT_NAMES = new HashMap<String,List<String>>();
	
	/**
	 * Loads the map of journal/conference names.
	 */
	static
	{	try
		{	Scanner sc = FileTools.openTextFileRead(FileNames.FI_ISI_NAMES, "UTF-8");
			while(sc.hasNextLine())
			{	String line = sc.nextLine();
				String tmp[] = line.split("\t");
				String shortName = StringTools.normalize(tmp[0]).replace(".", "");
				List<String> list = new ArrayList<String>();
				for(int i=1;i<tmp.length;i++)
				{	String longName = tmp[i].trim();
					list.add(longName);
				}
				SHORT_NAMES.put(shortName, list);
			}
			sc.close();
		}
		catch (FileNotFoundException e) 
		{	e.printStackTrace();
		}
		catch (UnsupportedEncodingException e) 
		{	e.printStackTrace();
		}
	}
	
	/**
	 * Update the file containing the conversion from short 
	 * conference/journal names to long ones.
	 */
	private void recordShortNames()
	{	logger.log("Updating ISI shortnales file");
		try 
		{	PrintWriter pw = FileTools.openTextFileWrite(FileNames.FI_ISI_NAMES, "UTF-8");
			Set<String> shortNames = new TreeSet<String>(SHORT_NAMES.keySet());
			for(String shortName: shortNames)
			{	String line = shortName;
				List<String> longNames = SHORT_NAMES.get(shortName);
				for(String longName: longNames)
					line = line + "\t" + longName;
				pw.println(line);
			}
			pw.close();
		} 
		catch (UnsupportedEncodingException e) 
		{	e.printStackTrace();
		} 
		catch (FileNotFoundException e) 
		{	e.printStackTrace();
		}
	}

	/////////////////////////////////////////////////////////////////
	// ERROR FIXES	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Used to fix some mistakes present in the ISI file */
	public final static Map<String,List<String>> ERROR_FIXES = new HashMap<String,List<String>>();
	
	/**
	 * Loads the map of error fixes.
	 */
	static
	{	try
		{	Scanner sc = FileTools.openTextFileRead(FileNames.FI_ISI_FIXES, "UTF-8");
			while(sc.hasNextLine())
			{	String line = sc.nextLine();
				String tmp[] = line.split("\t");
				String title = StringTools.normalize(tmp[0]).replace(".", "");
				List<String> list = new ArrayList<String>();
				for(int i=1;i<tmp.length;i++)
				{	String field = tmp[i].trim();
					list.add(field);
				}
				ERROR_FIXES.put(title, list);
			}
			sc.close();
		}
		catch (FileNotFoundException e) 
		{	e.printStackTrace();
		}
		catch (UnsupportedEncodingException e) 
		{	e.printStackTrace();
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// IGNORED REFERENCES	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** ISI references that must be ignored */
	public final static List<String> IGNORED_REFS = new ArrayList<String>();
	
	/**
	 * Loads the list of ignored references.
	 */
	static
	{	try
		{	Scanner sc = FileTools.openTextFileRead(FileNames.FI_ISI_IGNORED, "UTF-8");
			while(sc.hasNextLine())
			{	String line = sc.nextLine().trim();
				line = StringTools.normalize(line).replace(".", "");
				IGNORED_REFS.add(line);
			}
			sc.close();
		}
		catch (FileNotFoundException e) 
		{	e.printStackTrace();
		}
		catch (UnsupportedEncodingException e) 
		{	e.printStackTrace();
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// CIW PREFIXES	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** CIW prefix for abstracts */
	private final static String PFX_ABSTRACT = "AB";
//	/** CIW prefix for author names with long firstnames */
//	private final static String PFX_AUTHOR_LONG = "AF ";
	/** CIW prefix for author names with initials for firstnames */
	private final static String PFX_AUTHOR_SHORT = "AU";
	/** CIW prefix for conference name */
	private final static String PFX_CONFERENCE = "CT";
	/** CIW prefix for DOI */
	private final static String PFX_DOI = "DI";
	/** CIW prefix for journal issue */
	private final static String PFX_ISSUE = "IS";
	/** CIW prefix for source full name */
	private final static String PFX_JOURNAL_LONG = "SO";
	/** CIW prefix for journal abreviated name */
	private final static String PFX_JOURNAL_SHORT = "J9";
	/** CIW prefix for article number */
	private final static String PFX_PAGE = "AR";
	/** CIW prefix for starting page */
	private final static String PFX_PAGE_START = "BP";
	/** CIW prefix for reference list */
	private final static String PFX_REFERENCES = "CR";
	/** CIW prefix for the article series */
	private final static String PFX_SERIES = "SE";
	/** CIW prefix for article title */
	private final static String PFX_TITLE = "TI";
	/** CIW prefix for article type */
	private final static String PFX_TYPE = "DT";
	/** CIW prefix for journal volume */
	private final static String PFX_VOLUME = "VL";
	/** CIW prefix for publication year */
	private final static String PFX_YEAR = "PY";
	/** CIW prefix to separate articles */
	private final static String PFX_SEPARATOR = "ER";
	/** Internal prefix for organization */
	private final static String INT_BIBKEY = "BK";
	
	/////////////////////////////////////////////////////////////////
	// LOADING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Loads the specified ISI file, and complete the current maps of 
	 * articles and authors.
	 * 
	 * @param path
	 * 		Jabref file.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the Jabref file.
	 * @throws UnsupportedEncodingException 
	 * 		Problem while accessing the Jabref file.
	 */
	public void loadIsiFile(String path) throws FileNotFoundException, UnsupportedEncodingException
	{	logger.log("Start loading ISI file " + path);
		logger.increaseOffset();
		
		// open the ISI file
		logger.log("Open the file " + FileNames.FI_ISI_ALL);
		Scanner scanner = FileTools.openTextFileRead(FileNames.FI_ISI_ALL,null);
		
		// parse the ISI file
		Map<Article,List<String>> tempRef = new HashMap<Article, List<String>>();
		logger.log("Processing each entry in the file");
		logger.increaseOffset();
		{	Article article = null;
			int count = 0;
			do
			{	count++;
				article = processIsiArticle(scanner, tempRef);
				logger.log("Processing " + count + " :"+article);
			}
			while(scanner.hasNext());
			scanner.close();
		}
		logger.log("Done reading the file");
		logger.decreaseOffset();
		
		// resolve the (short) references
		PrintWriter pw = FileTools.openTextFileWrite(FileNames.FO_OUTPUT+File.separator+"missing.txt", "UTF-8");
		logger.log("Resolve the short references");
		logger.increaseOffset();
		{	int i = 1;
			for(Entry<Article,List<String>> entry: tempRef.entrySet())
			{	Article article = entry.getKey();
				logger.log("Processing article ("+i+"/"+tempRef.size()+") "+article);
				logger.increaseOffset();
				{	pw.println("\n"+i+". "+article.bibtexKey);
					int j = 1;
					List<String> refs = entry.getValue();
					for(String ref: refs)
					{	logger.log("Processing reference ("+j+"/"+refs.size()+") "+ref);
						logger.increaseOffset();
						{	Article r = retrieveArticle(ref);
							if(r!=null)
							{	if(article.citedArticles.contains(r))
									throw new IllegalArgumentException("Trying to insert twice the same reference for this article");
								article.citedArticles.add(r);
								r.citingArticles.add(article);
								if(r.bibtexKey.startsWith(NEW_KEY))
									pw.println(ref);
							}
						}
						logger.decreaseOffset();
						j++;
					}
				}
				logger.decreaseOffset();
				i++;
			}
		}
		logger.decreaseOffset();
		pw.close();
		
		// display the un-matched articles
		logger.log("List of unknown articles:");
		logger.increaseOffset();
		{	int count = 0;
			for(Article article: corpus.getArticles())
			{	String key = article.bibtexKey;
				if(key.startsWith(NEW_KEY))
				{	count++;
					logger.log(count + ". " + article);
				}
			}
			if(count>0)
				throw new IllegalArgumentException("Some short references could not be matched to existing full references");
		}
		logger.decreaseOffset();
		
		// display the DOIs of missing articles
		logger.log("List of missing DOIs:");
		logger.increaseOffset();
		{	int count = 0;
			for(Article article: corpus.getArticles())
			{	String key = article.bibtexKey;
				if(key.startsWith(NEW_KEY))
				{	if(article.doi!=null)
					{	count++;
						logger.log(count + ". " + article.doi);
					}
				}
			}
			if(count>0)
				throw new IllegalArgumentException("Some articles whose DOI is known are missing from the corpus");
		}
		logger.decreaseOffset();
		
		// complete with the manually annotated references
		completeReferences();
		
		logger.decreaseOffset();
	}
	
	/**
	 * Parses one article from the ISI file, matches it with
	 * one of the previously loaded Jabref articles, and merge
	 * them.
	 * 
	 * @param scanner
	 * 		Scanner giving access to the text.
	 * @param references
	 * 		Temporary map containing the references associated to each retrieved article.
	 * @return
	 * 		The corresponding article instance.
	 */
	private Article processIsiArticle(Scanner scanner, Map<Article,List<String>> references)
	{	Article result = new Article();
		
		if(!scanner.hasNextLine())
			throw new IllegalArgumentException("Empty scanner when reading next ISI article");
		String line = scanner.nextLine();
		
		// get authors
		logger.log("Getting the authors");
		logger.increaseOffset();
		while(!line.startsWith(PFX_AUTHOR_SHORT+" "))
		{	line = scanner.nextLine();
			if(line.startsWith(PFX_SEPARATOR))
				throw new IllegalArgumentException("Could not find the article authors (current state: "+result+")");
		}
		do
		{	String authorStr = line.substring(3).trim();
			logger.log("Raw author name: "+authorStr);
			String tmp[] = authorStr.split(",");
			String lastname = tmp[0].trim();
			String firstnameInitials = tmp[1].trim();
			firstnameInitials = firstnameInitials.replaceAll("(?<=\\p{L})(?=\\p{L})", ". ").trim(); // adding spaces and dots between letters
			firstnameInitials = firstnameInitials.replaceAll("(?<=\\p{L})(?=-)", ".-").trim(); 		// adding dots before hyphens
			if(firstnameInitials.charAt(firstnameInitials.length()-1)!='.')
				firstnameInitials = firstnameInitials + ".";											// adding the final dot if not already there
			Author author = new Author(lastname, firstnameInitials);
			logger.log("Processed author name: "+author);
			result.addAuthor(author);
			line = scanner.nextLine();
		}
		while(line.startsWith(" "));
		logger.decreaseOffset();
		
		// get title
		while(!line.startsWith(PFX_TITLE+" "))
		{	line = scanner.nextLine();
			if(line.startsWith(PFX_SEPARATOR))
				throw new IllegalArgumentException("Could not find the article title (current state: "+result+")");
		}
		String title = "";
		do
		{	String temp = line.substring(3).trim();
			title = title + temp + " ";
			line = scanner.nextLine();
		}
		while(line.startsWith(" "));
		title = title.trim();
		if(title.toUpperCase(Locale.ENGLISH).equals(title))
		{	String initial = title.substring(0,1).toUpperCase();
			String rest = title.substring(1).toLowerCase(Locale.ENGLISH);
			title = initial + rest;
		}
		logger.log("Title: "+title);
		result.setTitle(title);
		
		// get source
		logger.log("Getting the source name");
		logger.increaseOffset();
		while(!line.startsWith(PFX_JOURNAL_LONG+" "))
		{	line = scanner.nextLine();
			if(line.startsWith(PFX_SEPARATOR))
				throw new IllegalArgumentException("Could not find the article source (current state: "+result+")");
		}
		String sourceName = "";
		do
		{	String temp = line.substring(3).trim();
			sourceName = sourceName + temp + " ";
			line = scanner.nextLine();
		}
		while(line.startsWith(" "));
		sourceName = sourceName.trim();
		sourceName = sourceName.replace("&","\\&");
		logger.log("Raw source name: "+sourceName);
		if(sourceName.toUpperCase(Locale.ENGLISH).equals(sourceName))
		{	// switch the initial to uppercase
			String initial = sourceName.substring(0,1).toUpperCase();
			String rest = sourceName.substring(1).toLowerCase(Locale.ENGLISH);
			sourceName = initial + rest;
		}
		logger.log("Clean source name: "+sourceName);
		logger.decreaseOffset();
		
		// get series
		while(!line.startsWith(PFX_SERIES+" ")
				&& !line.startsWith(PFX_TYPE+" "))
			line = scanner.nextLine();
		String series = null;
		if(line.startsWith(PFX_SERIES+" "))
		{	series = "";
			do
			{	String temp = line.substring(3).trim();
				series = series + temp + " ";
				line = scanner.nextLine();
			}
			while(line.startsWith(" "));
			series = StringTools.normalize(series);
		}
		
		// get type
if(title.equals("Efficient Solution of the Correlation Clustering Problem: An Application to Structural Balance"))
	System.out.print("");
		logger.log("Determining source type");
		logger.increaseOffset();
		while(!line.startsWith(PFX_TYPE+" "))
		{	line = scanner.nextLine();
			if(line.startsWith(PFX_SEPARATOR))
				throw new IllegalArgumentException("Could not find the article type (current state: "+result+")");
		}
		String typeStr = line.substring(3).trim();
		logger.log("Source type code: "+typeStr);
		SourceType sourceType = null;
		switch(typeStr)
		{	case "Article":
			case "Editorial Material":
				sourceType = SourceType.JOURNAL;
				break;
				
			case "Proceedings Paper":
			case "Article; Proceedings Paper":
				logger.increaseOffset();
				sourceType = SourceType.CONFERENCE;
				while(!line.startsWith(PFX_CONFERENCE+" "))
				{	line = scanner.nextLine();
					if(line.startsWith(PFX_SEPARATOR))
						throw new IllegalArgumentException("Could not find the conference name (current state: "+result+")");
				}
				sourceName = "";
				do
				{	String temp = line.substring(3).trim();
					sourceName = sourceName + temp + " ";
					line = scanner.nextLine();
				}
				while(line.startsWith(" "));
				sourceName = sourceName.trim();
				logger.log("Raw conference name: "+sourceName);
				sourceName = sourceName.replace("&","\\&");
				// remove a possible ending string between parenthesis (typically for conferences)
				{	int pos = sourceName.indexOf('(');
					if(pos!=-1)
						sourceName = sourceName.substring(0,pos);
				}
				// remove a possible year at the end 
				{	while(Character.isDigit(sourceName.charAt(sourceName.length()-1)))
						sourceName = sourceName.substring(0,sourceName.length()-1);
					sourceName = sourceName.trim();
				}
				// if only caps
				if(sourceName.toUpperCase(Locale.ENGLISH).equals(sourceName))
				{	// switch the initial to uppercase
					String initial = sourceName.substring(0,1).toUpperCase();
					String rest = sourceName.substring(1).toLowerCase(Locale.ENGLISH);
					sourceName = initial + rest;
				}
				logger.log("Clean conference name: "+sourceName);
				String normName = StringTools.normalize(sourceName).replace(".", "");
				List<String> longNames = SHORT_NAMES.get(normName);
				if(longNames!=null)
					sourceName = longNames.get(0);
				logger.decreaseOffset();
				break;
				
			default:
				throw new IllegalArgumentException("Unknown ISI article type: "+typeStr);
		}
		logger.log("Source type: "+sourceType);
		result.setSource(sourceType, sourceName);
		logger.decreaseOffset();
		
		// get abstract
		while(!line.startsWith(PFX_ABSTRACT+" ")
				&& !line.startsWith(PFX_REFERENCES+" "))
		{	line = scanner.nextLine();
			if(line.startsWith(PFX_SEPARATOR))
				throw new IllegalArgumentException("Could not find the article abstract (current state: "+result+")");
		}
		if(line.startsWith(PFX_ABSTRACT+" "))
		{	String abstrct = "";
			do
			{	String temp = line.substring(3).trim();
				abstrct = abstrct + temp + " ";
				line = scanner.nextLine();
			}
			while(line.startsWith(" "));
			result.abstrct = abstrct.trim();
		}
		
		// get references
		while(!line.startsWith(PFX_REFERENCES+" "))
		{	line = scanner.nextLine();
			if(line.startsWith(PFX_SEPARATOR))
				throw new IllegalArgumentException("Could not find the article references (current state: "+result+")");
		}
		List<String> citedArticles = new ArrayList<String>(); 
		do
		{	String articleCitation = line.substring(3).trim();
			citedArticles.add(articleCitation);
			line = scanner.nextLine();
		}
		while(line.startsWith(" "));
		
		// possibly get the short source name
		while(!line.startsWith(PFX_JOURNAL_SHORT+" ") 
				&& !line.startsWith(PFX_YEAR))
			line = scanner.nextLine();
		if(line.startsWith(PFX_JOURNAL_SHORT))
		{	String shortSrc = StringTools.normalize(line.substring(3));
			logger.log("Short source name: "+shortSrc);
			if(!IGNORED_SERIES.contains(series))
			{	String normSourceName = StringTools.normalize(sourceName).replace(".","");
				// remove a possible initial year or conference number
				if(sourceType==SourceType.CONFERENCE && Character.isDigit(normSourceName.charAt(0)))
				{	int pos = normSourceName.indexOf(" ");
					normSourceName = normSourceName.substring(pos).trim();
				}
				List<String> existingLongSrcs = SHORT_NAMES.get(shortSrc);
				if(existingLongSrcs!=null)
				{	boolean found = false;
					Iterator<String> it = existingLongSrcs.iterator();
					while(it.hasNext() && !found)
					{	String srcName = it.next();
						srcName = StringTools.normalize(srcName).replace(".","");
						found = srcName.equalsIgnoreCase(normSourceName);
					}
					if(!found)
						throw new IllegalArgumentException("Found an unknown long name for source \""+shortSrc+"\" (\""+existingLongSrcs.get(0)+"\"): \""+normSourceName+"\"");
				}
				else
				{	List<String> list = new ArrayList<String>();
					list.add(normSourceName);
					SHORT_NAMES.put(shortSrc, list);
					recordShortNames();
				}
			}
		}
		
		// get year
		while(!line.startsWith(PFX_YEAR+" "))
		{	line = scanner.nextLine();
			if(line.startsWith(PFX_SEPARATOR))
				throw new IllegalArgumentException("Could not find the article year (current state: "+result+")");
		}
		result.year = line.substring(3).trim();
		logger.log("Year: "+result.year);
		
		// get volume
		while(!line.startsWith(PFX_VOLUME+" ")
				&& !line.startsWith(PFX_ISSUE+" ") 
				&& !line.startsWith(PFX_PAGE_START+" ")  && !line.startsWith(PFX_PAGE+" ") 
				&& !line.startsWith(PFX_DOI+" ") && !line.startsWith(PFX_SEPARATOR))
			line = scanner.nextLine();
		if(line.startsWith(PFX_VOLUME+" "))
		{	result.volume = line.substring(3).trim();
			logger.log("Volume: "+result.volume);
		}
		
		// get issue
		while(!line.startsWith(PFX_ISSUE+" ") 
				&& !line.startsWith(PFX_PAGE_START+" ")  && !line.startsWith(PFX_PAGE+" ") 
				&& !line.startsWith(PFX_DOI+" ") && !line.startsWith(PFX_SEPARATOR))
			line = scanner.nextLine();
		if(line.startsWith(PFX_ISSUE+" "))
		{	result.issue = line.substring(3).trim();
			logger.log("Volume: "+result.volume);
		}
		
		// get pages
		while(!line.startsWith(PFX_PAGE_START+" ")  && !line.startsWith(PFX_PAGE+" ") 
				&& !line.startsWith(PFX_DOI+" ") && !line.startsWith(PFX_SEPARATOR))
			line = scanner.nextLine();
		if(line.startsWith(PFX_PAGE_START+" "))
		{	String startPage = line.substring(3).trim();
			line = scanner.nextLine();
			String endPage = line.substring(3).trim();
			result.page = startPage+"-"+endPage;
			logger.log("Page: "+result.page);
		}
		else if(line.startsWith(PFX_PAGE+" "))
		{	result.page = line.substring(3).trim();
			logger.log("Page: "+result.page);
		}
		
		// get doi
		while(!line.startsWith(PFX_DOI+" ") && !line.startsWith(PFX_SEPARATOR))
			line = scanner.nextLine();
		if(line.startsWith(PFX_DOI+" "))
		{	String doi = line.substring(3).trim();
			doi = doi.replaceAll("//+", "/");
			result.doi = doi;
if(result.doi.equalsIgnoreCase("10.1016/j.cpc.2010.06.016"))
	System.out.print("");
			logger.log("DOI: "+result.doi);
		}
		
		// finish reference
if(title.equalsIgnoreCase("A partitioning approach to structural balance"))
	System.out.print("");
		logger.log("Article read: "+result);
		while(!line.startsWith(PFX_SEPARATOR))
			line = scanner.nextLine();
		for(int i=0;i<2;i++)
			line = scanner.nextLine();
		
		String normStr = StringTools.normalize(title).replace(".", "");
		List<String> fixes = ERROR_FIXES.get(normStr);
		if(fixes!=null)
		{	logger.log("Retrieving fields from the external fix file");
			logger.increaseOffset();
			for(String str: fixes)
			{	if(str.startsWith(PFX_TITLE+"="))
				{	title = str.substring(PFX_TITLE.length()+1);
					logger.log("Title: "+title);
					result.setTitle(title);
				}
//				else if(str.startsWith(PFX_AUTHOR_SHORT+"="))
//				{	String authorStr = str.substring(PFX_AUTHOR_SHORT.length()+1);
//					logger.log("Author: "+author);
//				}
				else if(str.startsWith(PFX_DOI+"="))
				{	String doi = str.substring(PFX_DOI.length()+1);
					doi = doi.replaceAll("//+", "/");
					result.doi = doi;
					logger.log("DOI: "+result.doi);
				}
				else if(str.startsWith(PFX_ISSUE+"="))
				{	result.issue = str.substring(PFX_ISSUE.length()+1);
					logger.log("Issue: "+result.issue);
				}
				else if(str.startsWith(PFX_VOLUME+"="))
				{	result.volume = str.substring(PFX_VOLUME.length()+1);
					logger.log("Volume: "+result.volume);
				}
				else if(str.startsWith(PFX_YEAR+"="))
				{	result.year = str.substring(PFX_YEAR.length()+1);
					logger.log("Year: "+result.year);
				}
				else if(str.startsWith(PFX_PAGE+"="))
				{	result.page = str.substring(PFX_PAGE.length()+1);
					logger.log("Pages: "+result.page);
				}
				else if(str.startsWith(PFX_JOURNAL_LONG+"="))
				{	sourceName = str.substring(PFX_JOURNAL_LONG.length()+1);
					result.setSource(sourceType, sourceName);
					logger.log("Source name: "+sourceName);
				}
				else if(str.startsWith(PFX_TYPE+"="))
				{	String sourceTypeStr = str.substring(PFX_TYPE.length()+1).toUpperCase(Locale.ENGLISH);
					sourceType = SourceType.valueOf(sourceTypeStr);
					result.setSource(sourceType, sourceName);
					logger.log("Source name: "+sourceType);
				}
				else if(str.startsWith(INT_BIBKEY+"="))
				{	result.bibtexKey = str.substring(INT_BIBKEY.length()+1);
					logger.log("Bibtex key: "+result.bibtexKey);
				}
			}
			logger.decreaseOffset();
			logger.log("Fixed article: "+result);
		}
		
		// match with the existing articles
		logger.log("Looking for compatible articles in the list of previously retrieved articles");
		logger.increaseOffset();
		List<Article> articles = new ArrayList<Article>(); 
		for(Article article: corpus.getArticles())
		{	
if(article.bibtexKey.equals("Drummond2013") && title.equals("Efficient Solution of the Correlation Clustering Problem: An Application to Structural Balance")) //for debug
	System.out.print("");
			if((result.bibtexKey!=null && result.bibtexKey.equals(article.bibtexKey))
				|| (result.doi!=null && result.doi.equalsIgnoreCase(article.doi))
				|| result.isCompatible(article))
			{	articles.add(article);
				logger.log("Found "+article);
			}
		}
		logger.log("Found "+articles.size()+" compatible article(s) in total");
		if(articles.isEmpty())
		{	List<Author> authors = result.getAuthors();
			for(Author author: authors)
				logger.log(author.toString());
			throw new IllegalArgumentException("Could not find article: "+result);
		
		}
		else if(articles.size()>1)
			throw new IllegalArgumentException("Found more than one compatible article for: "+result);
		Article selectedArticle = articles.get(0);
//		selectedArticle.completeWith(result);
		result = selectedArticle;
		result.core = true;
		logger.decreaseOffset();
		
		references.put(result,citedArticles);
		return result;
	}
	
	/**
	 * Parses the specified string and tries to identify a
	 * matching article in the current article map. If none
	 * can be found, a new incomplete article is created
	 * and returned.
	 *  
	 * @param string
	 * 		The string representing the targeted article.
	 * @return
	 * 		The retrieved (possibly newly created) article.
	 */
	private Article retrieveArticle(String string)
	{	Article result = null;
		Article tmpArticle = new Article();
		String tmp[] = string.split(",");
		
if(string.equalsIgnoreCase("DeParle Jason, 2005, NY TIMES        0627, pA1"))
	System.out.print("");
		// check if the reference should be ignored
		String normStr = StringTools.normalize(string).replace(".", "");
		if(IGNORED_REFS.contains(normStr))
		{	logger.log("The reference is in the black list >> it is ignored");
		}
		
		// if the reference must be processed
		else
		{	// check the presence of a DOI: if there is, we don't need the rest
			String last = tmp[tmp.length-1].trim();
			if(last.startsWith("DOI "))
			{	String doi = tmp[tmp.length-1].substring(4).trim();
				doi = doi.replaceAll("//+", "/");
				tmpArticle.doi = doi;
				if(tmpArticle.doi.startsWith("DOI ")) // sometimes DOI appears twice...
					tmpArticle.doi = tmpArticle.doi.substring(4).trim();
				logger.log("Found a DOI (not processing the rest): "+tmpArticle.doi);
				// see if the ref is an exception, already fixed manually
				List<String> fix = ERROR_FIXES.get(normStr);
				if(fix!=null)
				{	logger.log("Retrieving a correct DOI from the external short names file");
					logger.increaseOffset();
					String str = fix.get(0);
					if(str.startsWith(PFX_DOI+"="))
					{	tmpArticle.doi = str.substring(PFX_DOI.length()+1);
						logger.log("DOI: "+tmpArticle.doi);
					}
					logger.decreaseOffset();
				}
			}
			
			// no DOI: using the other fields
			else
			{	logger.log("Did not find a DOI: need to process the rest");
				// see if the ref is an exception, already fixed manually
				List<String> fix = ERROR_FIXES.get(normStr);
				if(fix!=null)
				{	logger.log("Retrieving fields from the external short names file");
					logger.increaseOffset();
					String sourceName = null;
					SourceType sourceType = null;
					for(String str: fix)
					{	if(str.startsWith(PFX_TITLE+"="))
						{	String title = str.substring(PFX_TITLE.length()+1);
							logger.log("Title: "+title);
							tmpArticle.setTitle(title);
						}
						else if(str.startsWith(PFX_AUTHOR_SHORT+"="))
						{	String fullname = str.substring(PFX_AUTHOR_SHORT.length()+1);
							logger.log("Author: "+fullname);
							Author author = new Author(fullname);
							tmpArticle.addAuthor(author);
						}
						else if(str.startsWith(PFX_DOI+"="))
						{	String doi = str.substring(PFX_DOI.length()+1);
							doi = doi.replaceAll("//+", "/");
							tmpArticle.doi = doi;
							logger.log("DOI: "+tmpArticle.doi);
						}
						else if(str.startsWith(PFX_ISSUE+"="))
						{	tmpArticle.issue = str.substring(PFX_ISSUE.length()+1);
							logger.log("Issue: "+tmpArticle.issue);
						}
						else if(str.startsWith(PFX_VOLUME+"="))
						{	tmpArticle.volume = str.substring(PFX_VOLUME.length()+1);
							logger.log("Volume: "+tmpArticle.volume);
						}
						else if(str.startsWith(PFX_YEAR+"="))
						{	tmpArticle.year = str.substring(PFX_YEAR.length()+1);
							logger.log("Year: "+tmpArticle.year);
						}
						else if(str.startsWith(PFX_PAGE+"="))
						{	tmpArticle.page = str.substring(PFX_PAGE.length()+1);
							logger.log("Pages: "+tmpArticle.page);
						}
						else if(str.startsWith(PFX_JOURNAL_LONG+"="))
						{	sourceName = str.substring(PFX_JOURNAL_LONG.length()+1);
							logger.log("Source name: "+sourceName);
							if(sourceType!=null)
								tmpArticle.setSource(sourceType, sourceName);
						}
						else if(str.startsWith(PFX_TYPE+"="))
						{	String sourceTypeStr = str.substring(PFX_TYPE.length()+1).toUpperCase(Locale.ENGLISH);
							sourceType = SourceType.valueOf(sourceTypeStr);
							logger.log("Source name: "+sourceType);
							if(sourceType!=null)
								tmpArticle.setSource(sourceType, sourceName);
						}
						else if(str.startsWith(INT_BIBKEY+"="))
						{	tmpArticle.bibtexKey = str.substring(INT_BIBKEY.length()+1);
							logger.log("Bibtex key: "+tmpArticle.bibtexKey);
						}
					}
					logger.decreaseOffset();
				}
				
				// no exception: use the string
				else
				{	// get the name of the first author
					String fullname = tmp[0].trim();
					logger.log("Author: "+fullname);
			
					// get the year
					tmpArticle.year = tmp[1].trim();
					logger.log("Year: "+tmpArticle.year);
					
					// determine the type, and possibly volume/pages/doi
					SourceType sourceType = null;
					if(tmp.length>3)
					{	for(int i=3;i<tmp.length;i++)
						{	String tmp3 = tmp[i].trim();
							if(tmp3.startsWith("V"))
							{	tmpArticle.volume = tmp3.substring(1);
								logger.log("Volume: "+tmpArticle.volume);
								if(sourceType==null)
								{	sourceType = SourceType.JOURNAL;
									logger.log("Source type: "+sourceType);
								}
							}
							else if(tmp3.toUpperCase(Locale.ENGLISH).startsWith("P"))
							{	tmpArticle.page = tmp3.substring(1);
								logger.log("Pages: "+tmpArticle.page);
								if(sourceType==null)
								{	sourceType = SourceType.CONFERENCE;
									logger.log("Source type: "+sourceType);
								}
							}
							//else if(tmp3.startsWith("DOI "))
							//{	tmpArticle.doi = tmp3.substring(4);
							//	logger.log("DOI: "+tmpArticle.doi);
							//}
						}
					}
					else
						sourceType = SourceType.BOOK;
					
					// get the source
					String sourceName = tmp[2].trim();
					sourceName = StringTools.normalize(sourceName).replace(".","");
if(sourceName.equals("p 22 ieee int c tool"))
	System.out.print("");
					//remove a possible "P xx" start, where xx is a number
					if(sourceName.startsWith("p ") && Character.isDigit(sourceName.charAt(2)))
					{	int pos = sourceName.indexOf(' ', 2);
						sourceName = sourceName.substring(pos+1);
					}
					else if(Character.isDigit(sourceName.charAt(0)))
					{	int pos = sourceName.indexOf(' ');
						sourceName = sourceName.substring(pos+1);
					}
					// additional cleaning for conference names
					if(sourceType==SourceType.CONFERENCE)
					{	// remove a possible ending string between parenthesis (typically for conferences)
						int pos = sourceName.indexOf('(');
						if(pos!=-1)
							sourceName = sourceName.substring(0,pos);
						// remove a possible year at the end 
						while(Character.isDigit(sourceName.charAt(sourceName.length()-1)))
							sourceName = sourceName.substring(0,sourceName.length()-1);
						sourceName = sourceName.trim();
					}
					if(sourceType!=SourceType.BOOK)
					{	List<String> longSrcs = SHORT_NAMES.get(sourceName);
						if(longSrcs==null)
							throw new IllegalArgumentException("Could not find \""+sourceName+"\" among the short names");
						String longSrc = longSrcs.get(0);
						tmpArticle.setSource(sourceType, longSrc);
					}
				
					// setup the first author
					logger.log("Trying to match the (first) author");
					logger.increaseOffset();
					String firstname, lastname;
					fullname = fullname.replace(".", "");							// remove possible dots
					String tmp2[] = fullname.split(" ");
					// if the last word is a firstname, we shorten it
					firstname = tmp2[tmp2.length-1];
					if(!firstname.toUpperCase().equals(firstname))
					{	firstname = firstname.substring(1,2);
						lastname = "";
						for(int i=0;i<tmp2.length-1;i++)
							lastname = lastname+ tmp2[i] + " ";
						lastname = lastname.substring(0,lastname.length()-1);
					}
					// otherwise, if the last word contains several uppercase letters, we separate them
					else if(firstname.length()>1)
					{	firstname = firstname.replace("-", "");			// remove possible hyphens
						firstname = firstname.replace("", " ").trim();	// insert space between letters
						firstname = firstname.replace(" ",".");			// add dot after each letter
						firstname = firstname + ".";
						lastname = "";
						for(int i=0;i<tmp2.length-1;i++)
							lastname = lastname+ tmp2[i] + " ";
						lastname = lastname.substring(0,lastname.length()-1);
					}
					// otherwise, (last word is a single letter) we look for separate single letters
					else
					{	lastname = tmp2[0];
						int i = 1;
						while(tmp2[i].length()>1)
						{	lastname = lastname + " " + tmp2[i];
							i++;
						}
						firstname = tmp2[i];
						i++;
						while(i<tmp2.length)
						{	firstname = firstname + " " + tmp2[i];
							i++;
						}
					}
					logger.log("Normalized lastname: "+lastname);
					logger.log("Normalized firstname: "+firstname);
					Author author = new Author(lastname, firstname);
					author = corpus.retrieveAuthor(author);
					tmpArticle.addAuthor(author);
					logger.decreaseOffset();
				}
			}
			
			// look for the paper in the current map
if(tmpArticle.getTitle()==null)
	System.out.print("");
if(tmpArticle.doi!=null && tmpArticle.doi.equals("10.1145/167088.167261"))
	System.out.print("");
if(tmpArticle.bibtexKey!=null && tmpArticle.bibtexKey.equals("Yang2007a"))
	System.out.print("");
			List<Article> articles = new ArrayList<Article>();
			for(Article article: corpus.getArticles())
			{	
if(article.bibtexKey.equals("Yang2007a"))
	System.out.print("");
if(article.doi!=null && article.doi.equals("10.1016/j.cpc.2010.06.016"))
	System.out.print("");
				if(tmpArticle.doi!=null && article.doi!=null)
				{	if(tmpArticle.doi.equalsIgnoreCase(article.doi))
						articles.add(article);
				}
				else if(tmpArticle.bibtexKey!=null && article.bibtexKey!=null)
				{	if(tmpArticle.bibtexKey.equals(article.bibtexKey))
						articles.add(article);
				}
				else if(tmpArticle.isCompatible(article))
					articles.add(article);
			}
			logger.log("Compatible articles found: "+articles.size());
			logger.increaseOffset();
			List<String> msg = new ArrayList<String>();
			for(Article article: articles)
				msg.add(article.toString());
			logger.decreaseOffset();
			
			// setup the result
			if(articles.isEmpty())
			{	logger.log("Could not find any existing paper for "+tmpArticle);
				int i = 0;
				while(corpus.containsKey(NEW_KEY+i))
					i++;
				String bibtexKey = NEW_KEY+i;
if(bibtexKey.equals("NewKey214"))				
	System.out.print("");
				tmpArticle.bibtexKey = bibtexKey;
				logger.log("Creating a new one and adding to the map, using the new bibtexkey "+bibtexKey);
				corpus.addArticle(tmpArticle);	// adding to the existing map for later use
				result = tmpArticle;
				throw new IllegalArgumentException("Could not find the article for "+tmpArticle);
			}
			else if(articles.size()==1)
			{	Article article = articles.get(0);
				logger.log("Found an equivalent article: "+article);
				result = article;
			}
			else // more than one article found
			{	msg = new ArrayList<String>();
				msg.add("ERROR: Found several articles:");
				for(Article article: articles)
					msg.add(article.toString());
				logger.log(msg);
				throw new IllegalArgumentException("Found several articles for "+tmpArticle);
			}
		}
		
		return result;
	}
	
	/**
	 * Uses the manual annotations contained in a text file to complete
	 * the information previously extracted from the ISI file.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the text file.
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing the text file.
	 */
	private void completeReferences() throws FileNotFoundException, UnsupportedEncodingException
	{	File file = new File(FileNames.FI_ISI_COMPLETED);
		if(file.exists())
		{	logger.log("Found a file containing manual annotations: adding them to the corpus");
			logger.increaseOffset();
			Scanner scanner = FileTools.openTextFileRead(file, "UTF-8");
			while(scanner.hasNextLine())
			{	Article article = null;
				String line = scanner.nextLine().trim();
				while(scanner.hasNextLine() && !line.isEmpty())
				{	if(line.startsWith(PFX_DOI))
					{	String prefix = PFX_DOI + "=";
						String doi = line.substring(prefix.length(), line.length()).trim();
						if(article==null)
						{	article = corpus.getArticleByDoi(doi);
							logger.log("Completing article "+article);
							logger.increaseOffset();
						}
						else
						{	Article ref = corpus.getArticleByDoi(doi);
							logger.log("Adding article "+ref);
							article.citedArticles.add(ref);
							ref.citingArticles.add(article);
						}
					}
					else if(line.startsWith(INT_BIBKEY))
					{	String prefix = INT_BIBKEY + "=";
						String bibkey = line.substring(prefix.length(), line.length()).trim();
						if(article==null)
						{	article = corpus.getArticleByBibkey(bibkey);
							logger.log("Completing article "+article);
							logger.increaseOffset();
						}
						else
						{	Article ref = corpus.getArticleByBibkey(bibkey);
							logger.log("Adding article "+ref);
							article.citedArticles.add(ref);
							ref.citingArticles.add(article);
						}
					}
					else
						throw new IllegalArgumentException("Unknown key \""+line+"\" in file "+file);
					line = scanner.nextLine().trim();
				}
				logger.decreaseOffset();
			}
			logger.decreaseOffset();
		}
		
		// no available file 
		else
			logger.log("No manual annotation available");
	}
	
	/////////////////////////////////////////////////////////////////
	// TESTS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Method used to test this class.
	 * 
	 * @param args
	 * 		None needed.
	 * @throws Exception
	 * 		Whatever exception.
	 */
	public static void main(String[] args) throws Exception
	{	// first load the jabref file
		JabrefFileHandler jfh = new JabrefFileHandler();
		String path = FileNames.FI_BIBTEX_STRUCTBAL;
		boolean updateGroups = false;
		jfh.loadJabRefFile(path, updateGroups);
		
		// then the ISI file
		IsiFileHandler ifh = new IsiFileHandler(jfh.corpus);
		path = FileNames.FI_ISI_ALL;
		ifh.loadIsiFile(path);
	}
}
