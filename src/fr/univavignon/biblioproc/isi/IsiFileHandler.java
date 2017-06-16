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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import fr.univavignon.biblioproc.data.Article;
import fr.univavignon.biblioproc.data.Author;
import fr.univavignon.biblioproc.data.SourceType;
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
	 * @param articlesMap
	 * 		Map of articles, indexed by Bibtex entry.
	 * @param authorsMap
	 * 		Map of authors, indexed by normalized full name.
	 */
	public IsiFileHandler(Map<String, Article> articlesMap, Map<String, Author> authorsMap)
	{	this.articlesMap = articlesMap;
		this.authorsMap = authorsMap;
	}
	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/////////////////////////////////////////////////////////////////
	// DATA			/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map containing all the loaded articles, indexed by their Bibtex id */
	public Map<String, Article> articlesMap = new HashMap<String, Article>();
	/** Map containing all the loaded authors, index by their normalized name */
	public Map<String, Author> authorsMap = new HashMap<String, Author>();
	
	/////////////////////////////////////////////////////////////////
	// SHORT NAMES	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Retrieve the long name of a journal/conference based on its short name */
	public static Map<String,String> SHORT_NAMES = new HashMap<String,String>();
	
	/**
	 * Loads the map of journal/conference names
	 */
	static
	{	try
		{	Scanner sc = FileTools.openTextFileRead(FileNames.FI_ISI_NAMES, "UTF-8");
			while(sc.hasNextLine())
			{	String line = sc.nextLine();
				String tmp[] = line.split("\t");
				String shortName = StringTools.normalize(tmp[0]).replace("\\.", "");
				String longName = StringTools.normalize(tmp[1]).replace("\\.", "");
				SHORT_NAMES.put(shortName, longName);
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
	private static void recordShortNames()
	{	try 
		{	PrintWriter pw = FileTools.openTextFileWrite(FileNames.FI_ISI_NAMES, "UTF-8");
			for(Entry<String,String> entry: SHORT_NAMES.entrySet())
			{	String shortName = entry.getKey();
				String longName = entry.getValue();
				pw.println(shortName+"\t"+longName);
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
	// CIW PREFIXES	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** CIW prefix for abstracts */
	private final static String PFX_ABSTRACT =  "AB";
//	/** CIW prefix for author names with long firstnames */
//	private final static String PFX_AUTHOR_LONG =  "AF ";
	/** CIW prefix for author names with initials for firstnames */
	private final static String PFX_AUTHOR_SHORT =  "AU";
	/** CIW prefix for DOI */
	private final static String PFX_DOI =  "DI";
	/** CIW prefix for journal issue */
	private final static String PFX_ISSUE =  "IS";
	/** CIW prefix for journal full name */
	private final static String PFX_JOURNAL_LONG =  "SO";
	/** CIW prefix for journal abreviated name */
	private final static String PFX_JOURNAL_SHORT =  "J9";
	/** CIW prefix for article number */
	private final static String PFX_PAGE =  "AR";
	/** CIW prefix for starting page */
	private final static String PFX_PAGE_START =  "BP";
	/** CIW prefix for reference list */
	private final static String PFX_REFERENCES =  "CR";
	/** CIW prefix for article title */
	private final static String PFX_TITLE =  "TI";
	/** CIW prefix for article type */
	private final static String PFX_TYPE =  "JT";
	/** CIW prefix for journal volume */
	private final static String PFX_VOLUME =  "VL";
	/** CIW prefix for publication year */
	private final static String PFX_YEAR =  "PY";
	/** CIW prefix to separate articles */
	private final static String PFX_SEPARATOR =  "ER ";
	
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
			while(article!=null);
			scanner.close();
		}
		logger.log("Done reading the file");
		logger.decreaseOffset();
		
		// resolving the (short) references
		for(Entry<Article,List<String>> entry: tempRef.entrySet())
		{	Article article = entry.getKey();
			List<String> refs = entry.getValue();
			for(String ref: refs)
			{	Article r = retrieveArticle(ref);
				if(r!=null)
				{	article.citedArticles.add(r);
					r.citingArticles.add(article);
				}
			}
		}
		//TODO list the unresolved refs? 
		
//		// display the unknown articles
//		// TODO check the necessity of the stuff below
//		logger.log("List of unknown articles:");
//		logger.increaseOffset();
//			count = 0;
//			Collections.sort(articles, new Comparator<Article>()
//			{	public int compare(Article o1, Article o2)
//				{	int result = o1.timesCited - o2.timesCited;
//					return result;
//				};
//			});
//			for(Article a: articles)
//			{	if(!a.present)
//				{	count++;
//					logger.log(count + ". [" + a.timesCited + "]" + a);
//				}
//			}
//		logger.decreaseOffset();
		
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
		
		// get type
		while(!line.startsWith(PFX_TYPE+" "))
		{	line = scanner.nextLine();
			if(line.startsWith(PFX_SEPARATOR))
				throw new IllegalArgumentException("Could not find the article type (current state: "+result+")");
		}
		String typeStr = line.substring(3).trim();
		SourceType sourceType = null;
		switch(typeStr)
		{	case "J":
				sourceType = SourceType.JOURNAL;
				break;
			case "S":
				sourceType = SourceType.CONFERENCE;
				break;
			default:
				throw new IllegalArgumentException("Unknown ISI article type: "+typeStr);
		}
				
		// get authors
		while(!line.startsWith(PFX_AUTHOR_SHORT+" "))
		{	line = scanner.nextLine();
			if(line.startsWith(PFX_SEPARATOR))
				throw new IllegalArgumentException("Could not find the article authors (current state: "+result+")");
		}
		do
		{	String authorStr = line.substring(3).trim();
			Author author = parseAuthorString(authorStr);
			result.addAuthor(author);
			line = scanner.nextLine();
		}
		while(line.startsWith(" "));
		
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
		result.setTitle(title);
		
		// get source
		String prefix = null;
		switch(sourceType)
		{	case BOOK:
				//TODO to complete
				break;
			case CHAPTER:
				//TODO to complete
				break;
			case CONFERENCE:
				prefix = PFX_JOURNAL_LONG+" ";
				break;
			case ELECTRONIC:
				//TODO to complete
				break;
			case JOURNAL:
				prefix = PFX_JOURNAL_LONG+" ";
				break;
			case REPORT:
				//TODO to complete
				break;
			case THESIS_MSC:
				//TODO to complete
				break;
			case THESIS_PHD:
				//TODO to complete
				break;
		}
		if(prefix==null)
			throw new IllegalArgumentException("Unknown article type for "+sourceType+" (source code must be completed accordingly)");
		while(!line.startsWith(prefix))
		{	line = scanner.nextLine();
			if(line.startsWith(PFX_SEPARATOR))
				throw new IllegalArgumentException("Could not find the article prefix \""+prefix+"\" (current state: "+result+")");
		}
		String sourceName = "";
		do
		{	String temp = line.substring(3).trim();
			sourceName = sourceName + temp + " "; //TODO if all uppercase >> switch to lowercase with initials
			line = scanner.nextLine();
		}
		while(line.startsWith(" "));
		sourceName = sourceName.trim();
		result.setSource(sourceType, sourceName);
		
		// get abstract
		while(!line.startsWith(PFX_ABSTRACT+" "))
		{	line = scanner.nextLine();
			if(line.startsWith(PFX_SEPARATOR))
				throw new IllegalArgumentException("Could not find the article abstract (current state: "+result+")");
		}
		String abstrct = "";
		do
		{	String temp = line.substring(3).trim();
			abstrct = abstrct + temp + " ";
			line = scanner.nextLine();
		}
		while(line.startsWith(" "));
		result.abstrct = abstrct.trim();
		
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
		
		// possibly get the short journal name
		while(!line.startsWith(PFX_JOURNAL_SHORT+" ") 
				&& !line.startsWith(PFX_YEAR))
			line = scanner.nextLine();
		String shortSrc = StringTools.normalize(line.substring(3)).replace("\\.","");
		String normSourceName = StringTools.normalize(sourceName).replace("\\.","");
		String existingLongSrc = SHORT_NAMES.get(shortSrc);
		if(existingLongSrc!=null && !existingLongSrc.equals(normSourceName))
			throw new IllegalArgumentException("Found two different long names (\""+normSourceName+"\" vs. \""+existingLongSrc+"\") for the same short name ("+shortSrc+")");
		SHORT_NAMES.put(shortSrc, normSourceName);
		recordShortNames();
		
		// get year
		while(!line.startsWith(PFX_YEAR+" "))
		{	line = scanner.nextLine();
			if(line.startsWith(PFX_SEPARATOR))
				throw new IllegalArgumentException("Could not find the article year (current state: "+result+")");
		}
		result.year = line.substring(3).trim();
		
		// get volume
		while(!line.startsWith(PFX_VOLUME+" ")
				&& !line.startsWith(PFX_ISSUE+" ") 
				&& !line.startsWith(PFX_PAGE_START+" ")  && !line.startsWith(PFX_PAGE+" ") 
				&& !line.startsWith(PFX_DOI+" ") && !line.startsWith(PFX_SEPARATOR))
			line = scanner.nextLine();
		if(line.startsWith(PFX_VOLUME+" "))
			result.volume = line.substring(3).trim();
		
		// get issue
		while(!line.startsWith(PFX_ISSUE+" ") 
				&& !line.startsWith(PFX_PAGE_START+" ")  && !line.startsWith(PFX_PAGE+" ") 
				&& !line.startsWith(PFX_DOI+" ") && !line.startsWith(PFX_SEPARATOR))
			line = scanner.nextLine();
		if(line.startsWith(PFX_ISSUE+" "))
			result.issue = line.substring(3).trim();
		
		// get pages
		while(!line.startsWith(PFX_PAGE_START+" ")  && !line.startsWith(PFX_PAGE+" ") 
				&& !line.startsWith(PFX_DOI+" ") && !line.startsWith(PFX_SEPARATOR))
			line = scanner.nextLine();
		if(line.startsWith(PFX_PAGE_START+" "))
		{	String startPage = line.substring(3).trim();
			line = scanner.nextLine();
			String endPage = line.substring(3).trim();
			result.page = startPage+"-"+endPage;
		}
		else if(line.startsWith(PFX_PAGE+" "))
			result.page = line.substring(3).trim();
		
		// get doi
		while(!line.startsWith(PFX_DOI+" ") && !line.startsWith(PFX_SEPARATOR))
			line = scanner.nextLine();
		if(line.startsWith(PFX_DOI+" "))
			result.doi = line.substring(3).trim();
		
		// finish reference
		while(!line.startsWith(PFX_SEPARATOR))
			line = scanner.nextLine();
		for(int i=0;i<2;i++)
			line = scanner.nextLine();
		
		// match with the existing articles
		Iterator<Article> it = articlesMap.values().iterator();
		boolean found = false;
		while(it.hasNext() && !found)
		{	Article article = it.next();
			if(result.isCompatible(article))
			{	article.completeWith(result);
				result = article;
				found = true;
			}
		}
		if(!found)
			throw new IllegalArgumentException("Could not find article: "+result);
		
		references.put(result,citedArticles);
		return result;
	}
	
	/**
	 * Parse a string representing an author's full name,
	 * and returns an {@link Author} object representing the
	 * same person.
	 *  
	 * @param authorStr
	 * 		Text representation of the author.
	 * @return
	 * 		The author as an... {@code Author}.
	 */
	private Author parseAuthorString(String authorStr)
	{	String tmp[] = authorStr.split(",");
		String lastname = tmp[0].trim();
		String firstnameInitials = tmp[1].replace("(?<=\\p{L})(?=\\p{L})", ". ").trim(); // adding spaces and dots between letters
		firstnameInitials = firstnameInitials.replace("(?<=\\p{L})(?=-)", ".-").trim(); // adding dots before hyphens
		Author result = new Author(lastname, firstnameInitials);
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
	{	Article tmpArticle = new Article();
		SourceType sourceType;
		String tmp[] = string.split(",");
		
		// normalize the name of the first author
		String fullname = tmp[0].trim();
//		fullname = fullname.replace("-", "");							// remove hyphens
//		fullname = fullname.replace("\\.", "");							// remove dots
//		fullname = fullname.replace("(?<=\\p{Lu}) (?=\\p{Lu})", "");	// remove spaces remaining between initials
//		String tmp2[] = fullname.split(" ");							// possible shorten if full firstname
//		String last = tmp2[tmp2.length-1];
//		if(!last.toUpperCase().equals(last))
//		{	last = last.substring(1,2);
//			fullname = "";
//			for(int i=0;i<tmp2.length-1;i++)
//				fullname = fullname+ tmp2[i] + " ";
//			fullname = fullname + last;
//		}
//		fullname = StringTools.removeDiacritics(fullname);				// remove diacritics
//		fullname = fullname.toLowerCase(Locale.ENGLISH);				// switch to lower case
		
		// get the year
		tmpArticle.year = tmp[1].trim();
		
		// determine the type, and possibly volume/pages/doi
		if(tmp.length>3)
		{	for(int i=3;i<tmp.length;i++)
			{	String tmp3 = tmp[i].trim();
				if(tmp3.startsWith("V"))
				{	tmpArticle.volume = tmp3.substring(1);
					if(sourceType==null)
						sourceType = SourceType.JOURNAL;
				}
				else if(tmp3.startsWith("P"))
				{	tmpArticle.page = tmp3.substring(1);
					if(sourceType==null)
						sourceType = SourceType.CONFERENCE;
				}
				else if(tmp3.startsWith("DOI "))
					tmpArticle.doi = tmp3.substring(4);
			}
		}
		else
			sourceType = SourceType.BOOK;
		
		// get the source (and possibly other external information)
		String sourceName = tmp[2].trim();
		sourceName = StringTools.normalize(sourceName).replace("\\.","");
		String longSrc = SHORT_NAMES.get(sourceName);
		if(longSrc==null)
			throw new IllegalArgumentException("Could not find \""+sourceName+"\" among the short names");
		if(longSrc.contains(", "))	// the indication may contain several fields
		{	String tmp3[] = longSrc.split(", ");
			for(String str: tmp3)
			{	if(str.startsWith(PFX_TITLE+"="))
				{	String title = str.substring(PFX_TITLE.length()+1);
					tmpArticle.setTitle(title);
				}
				else if(str.startsWith(PFX_AUTHOR_SHORT+"="))
					fullname = str.substring(PFX_AUTHOR_SHORT.length()+1);
				else if(str.startsWith(PFX_DOI+"="))
					tmpArticle.doi = str.substring(PFX_DOI.length()+1);
				else if(str.startsWith(PFX_ISSUE+"="))
					tmpArticle.issue = str.substring(PFX_ISSUE.length()+1);
				else if(str.startsWith(PFX_VOLUME+"="))
					tmpArticle.volume = str.substring(PFX_VOLUME.length()+1);
				else if(str.startsWith(PFX_YEAR+"="))
					tmpArticle.year = str.substring(PFX_YEAR.length()+1);
				else if(str.startsWith(PFX_PAGE+"="))
					tmpArticle.page = str.substring(PFX_PAGE.length()+1);
				else if(str.startsWith(PFX_JOURNAL_LONG+"="))
					sourceName = str.substring(PFX_JOURNAL_LONG.length()+1);
				else if(str.startsWith(PFX_TYPE+"="))
				{	String sourceTypeStr = str.substring(PFX_TYPE.length()+1).toUpperCase(Locale.ENGLISH);
					sourceType = SourceType.valueOf(sourceTypeStr);
				}
			}
		}
		tmpArticle.setSource(sourceType, sourceName);
		Author author = parseAuthorString(fullname); //TODO actually we cannot use the same method, because it expects a ','. use the commented code abov	e?
		tmpArticle.addAuthor(author);
		
		// look for the paper in the current map
		//TODO
		
		
		
		
		
		
		
		// get the papers whose first author is similar
		List<Article> articles = new ArrayList<Article>();
		for(Article article: articlesMap.values())
		{	List<Author> authors = article.getAuthors();
			Author first = authors.get(0);
			if(fullname.equals(first.normname))
					articles.add(article);
		}
		
		Iterator<Article> it = articles.iterator();
		while(it.hasNext())
		{	Article article = it.next();
			if(!year.equals(article.year))
				it.remove();
		}
		it = articles.iterator();
		while(it.hasNext())
		{	Article article = it.next();
			String normSourceName = article.getNormSourceName().replace("\\.","");
			if(!longSrc.equals(normSourceName))
				it.remove();
		}
		//TODO need to keep only first page for comparison
		
		
		
		
		
		
		
		
		
		// update the article or list
		Article result = null;
		if(result==null)
		{	articles.add(article);
			result = article;
		}
		else
			result.completeWith(article);
		
		return result;
	}
	
//	/**
//	 * Builds an {@code Article} by parsing the specified string,
//	 * which is a compact representation of a bibliographic reference.
//	 * 
//	 * @param string
//	 * 		The bibtex string representing the article.
//	 * @param authorsMap
//	 * 		Map containing the known authors.
//	 * @return
//	 * 		The corresponding {@code Article} object.
//	 */
//	public Article buildArticle(String string)
//	{	Article result = new Article();
//		String temp[] = string.split(", ");
//		int index = 0;
//		
//		// first author
//		String temp2[] = temp[index].split(" ");
//		if(temp2.length==1 && temp2[0].contains("."))
//			temp2 = temp2[0].split("\\.");
//		String lastname = temp2[0];
//		if(lastname.startsWith("*") || Character.isDigit(lastname.charAt(0)))
//			return null;
//		String firstnameInitial = null;
//		int idx = 1;
//		while(temp2.length>idx && firstnameInitial==null)
//		{	if(temp2[idx].length()>3 && idx<temp2.length-1)
//				lastname = lastname + temp2[idx];
//			else
//				firstnameInitial = temp2[idx];
//			idx++;
//		}
//		if(firstnameInitial!=null && firstnameInitial.length()>1)
//			firstnameInitial = temp2[1].substring(0,1);
//		Author author = new Author(lastname, firstnameInitial);
//		author = Author.retrieveAuthor(author, authorsMap);
//		result.addAuthor(author);
//		index++;
//		
//		// year
//		if(temp.length>index)
//		{	String tempYear = StringTools.normalize(temp[index]);
//			try
//			{	Integer.parseInt(tempYear);
//			}
//			catch(NumberFormatException e)
//			{	tempYear = null;
//				result. year = "N/A";
//			}
//			if(tempYear!=null)
//			{	result.year = tempYear;
//				index++;
//			}
//		}
//		
//		// source
//		if(temp.length>index)
//		{	result.source = StringTools.normalize(temp[index]);
//			index++;
//		}
//		
//		// volume
//		if(temp.length>index && (temp[index].startsWith("V") || temp[index].startsWith("v")))
//		{	result.volume = StringTools.normalize(temp[index].substring(1));
//			index++;
//		}
//		
//		// page
//		if(temp.length>index && (temp[index].startsWith("P") || temp[index].startsWith("p")))
//		{	result.page = StringTools.normalize(temp[index].substring(1).trim());
//			index++;
//		}
//		
//		// doi
//		if(temp.length>index && (temp[index].startsWith("DOI") || temp[index].startsWith("Doi")))
//		{	result.doi = temp[index].substring(4).trim();
//			index++;
//		}
//		
//		return result;
//	}

//	/**
//	 * Builds an {@code Article} using the specified map and adding
//	 * the specified citations. The {@code Map} must contain at least 
//	 * the required fields: {@code bibtexkey}, {@code authors}, 
//	 * {@code title}, {@code year}.
//	 * 
//	 * @param map
//	 * 		A map used to initialize the {@code Article} object.
//	 * @param citedArticles
//	 * 		Articles cited by the newly created article.
//	 * @return
//	 * 		The created article.
//	 */
//	public Article buildArticle(Map<String,String> map, Set<Article> citedArticles)
//	{	Article result = buildArticle(map);
//		result.present = false;
//		
//		result.citedArticles.addAll(citedArticles);
//		for(Article article: citedArticles)
//			article.citingArticles.add(article);
//		
//		return result;
//	}

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
	{
		// TODO
	}
}
