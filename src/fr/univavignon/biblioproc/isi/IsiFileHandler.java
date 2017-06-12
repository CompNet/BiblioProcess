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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import fr.univavignon.biblioproc.data.Article;
import fr.univavignon.biblioproc.data.Author;
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
	// CIW PREFIXES	/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** CIW prefix for author names with long firstnames */
	private final static String PFX_AUTHOR_LONG =  "AF ";
	/** CIW prefix for author names with initials for firstnames */
	private final static String PFX_AUTHOR_SHORT =  "AU ";
	/** CIW prefix for DOI */
	private final static String PFX_DOI =  "DI ";
	/** CIW prefix for journal issue */
	private final static String PFX_ISSUE =  "IS ";
	/** CIW prefix for journal full name */
	private final static String PFX_JOURNAL_LONG =  "SO ";
	/** CIW prefix for journal abreviated name */
	private final static String PFX_JOURNAL_SHORT =  "J9 ";
	/** CIW prefix for article number */
	private final static String PFX_PAGE =  "AR ";
	/** CIW prefix for starting page */
	private final static String PFX_PAGE_START =  "BP ";
	/** CIW prefix for ending page */
	private final static String PFX_PAGE_END =  "EP ";
	/** CIW prefix for reference list */
	private final static String PFX_REFERENCES =  "CR ";
	/** CIW prefix for article title */
	private final static String PFX_TITLE =  "TI ";
	/** CIW prefix for journal volume */
	private final static String PFX_VOLUME =  "VL ";
	/** CIW prefix for publication year */
	private final static String PFX_YEAR =  "PY ";
	/** CIW prefix to separate articles */
	private final static String PFX_SEPARATOR =  "ER ";
	
	/////////////////////////////////////////////////////////////////
	// LOADING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Loads the specified ISI file, and builds the corresponding 
	 * map of articles.
	 * 
	 * @param path
	 * 		Jabref file.
	 * @return
	 * 		A list containing all the read articles.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the Jabref file.
	 * @throws UnsupportedEncodingException 
	 * 		Problem while accessing the Jabref file.
	 */
	public List<Article> loadIsiFile(String path) throws FileNotFoundException, UnsupportedEncodingException
	{	logger.log("Start loading ISI file " + path);
		logger.increaseOffset();
		
		// open the ISI file
		logger.log("Open the file " + FileNames.FI_ISI_ALL);
		Scanner scanner = FileTools.openTextFileRead(FileNames.FI_ISI_ALL,null);
		
		// parse the ISI file
		logger.log("Processing each entry in the file");
		logger.increaseOffset();
			Article article = null;
			List<Article> articles = new ArrayList<Article>();
			int count = 0;
			do
			{	count++;
				article = processIsiArticle(scanner, articles);
				logger.log("Processing " + count + " :"+article);
			}
			while(article!=null);
			scanner.close();
		logger.log("Done reading the file");
		logger.decreaseOffset();
		
		// display the unknown articles
		logger.log("List of unknown articles:");
		logger.increaseOffset();
			count = 0;
			Collections.sort(articles, new Comparator<Article>()
			{	public int compare(Article o1, Article o2)
				{	int result = o1.timesCited - o2.timesCited;
					return result;
				};
			});
			for(Article a: articles)
			{	if(!a.present)
				{	count++;
					logger.log(count + ". [" + a.timesCited + "]" + a);
				}
			}
		logger.decreaseOffset();
		
		logger.decreaseOffset();
		return articles;
	}
	
	/**
	 * Parses one article from the ISI file.
	 * 
	 * @param scanner
	 * 		Scanner giving access to the text.
	 * @param articles
	 * 		List of read articles.
	 * @return
	 * 		The corresponding article instance.
	 */
	private Article processIsiArticle(Scanner scanner, List<Article> articles)
	{	Article result = null;
		if(scanner.hasNextLine())
		{	Map<String, String> data = new HashMap<String, String>();
			String line = scanner.nextLine();
			
			// get authors
			while(!line.startsWith(PFX_AUTHOR_SHORT))
			{	line = scanner.nextLine();
				if(line.startsWith(PFX_SEPARATOR))
					throw new NullPointerException();
			}
			String authors = "";
			do
			{	String author = line.substring(3);
				authors = authors + author + " and ";
				line = scanner.nextLine();
			}
			while(line.startsWith(" "));
			authors = authors.substring(0,authors.length()-5);
			data.put("author", authors);
			
			// get title
			while(!line.startsWith(PFX_TITLE))
			{	line = scanner.nextLine();
				if(line.startsWith(PFX_SEPARATOR))
					throw new NullPointerException();
			}
			String title = "";
			do
			{	String temp = line.substring(3);
				title = title + temp + " ";
				line = scanner.nextLine();
			}
			while(line.startsWith(" "));
			title = title.substring(0,title.length()-1);
			data.put("title", title);
			
			// get conference
			while(!line.startsWith("SO ") && !line.startsWith("LA "))
			{	line = scanner.nextLine();
				if(line.startsWith(PFX_SEPARATOR))
					throw new NullPointerException();
			}
			if(line.startsWith("SO "))
			{	String booktitle = "";
				do
				{	String temp = line.substring(3);
					booktitle = booktitle + temp + " ";
					line = scanner.nextLine();
				}
				while(line.startsWith(" "));
				booktitle = booktitle.substring(0,booktitle.length()-1);
				data.put("booktitle", booktitle);
			}
			
			// get references
			Set<Article> citedArticles = new TreeSet<Article>(); 
			while(!line.startsWith("CR ") && !line.startsWith("NR "))
			{	line = scanner.nextLine();
				if(line.startsWith(PFX_SEPARATOR))
					throw new NullPointerException();
			}
			if(line.startsWith("CR "))
			{	do
				{	String articleStr = line.substring(3);
					Article article = buildArticle(articleStr, authorsMap);
					if(article!=null)
					{	article = retrieveArticle(article, articles);
						citedArticles.add(article);
					}
					line = scanner.nextLine();
				}
				while(line.startsWith(" "));
			}
			
			// get journal
			while(!line.startsWith("J9 ") && !line.startsWith("PY "))
			{	line = scanner.nextLine();
				if(line.startsWith(PFX_SEPARATOR))
					throw new NullPointerException();
			}
			if(line.startsWith("J9 "))
			{	String journal = line.substring(3);
				data.put("journal", journal);
				line = scanner.nextLine();
			}
						
			// get year
			while(!line.startsWith("PY "))
			{	line = scanner.nextLine();
				if(line.startsWith(PFX_SEPARATOR))
					throw new NullPointerException();
			}
			String year = line.substring(3);
			data.put("year", year);
			line = scanner.nextLine();
						
			// get volume
			while(!line.startsWith("VL ") && !line.startsWith("IS ")
				&& !line.startsWith("BP ") && !line.startsWith("EP ")
				&& !line.startsWith(PFX_SEPARATOR))
				line = scanner.nextLine();
			if(line.startsWith("VL "))
			{	String volume = line.substring(3);
				data.put("volume", volume);
				line = scanner.nextLine();
			}
			
			// get issue
			while(!line.startsWith("IS ")
				&& !line.startsWith("BP ") && !line.startsWith("EP ")
				&& !line.startsWith(PFX_SEPARATOR))
				line = scanner.nextLine();
			if(line.startsWith("IS "))
			{	String issue = line.substring(3);
				data.put("number", issue);
				line = scanner.nextLine();
			}
			
			// get page(s)
			while(!line.startsWith("BP ") && !line.startsWith("AR") && !line.startsWith(PFX_SEPARATOR))
				line = scanner.nextLine();
			if(line.startsWith("BP "))
			{	String pages = line.substring(3);
				line = scanner.nextLine();
				data.put("pages", pages);
			}
			while(!line.startsWith("AR ") && !line.startsWith(PFX_SEPARATOR))
				line = scanner.nextLine();
			if(line.startsWith("AR "))
			{	String pages = line.substring(3);
				line = scanner.nextLine();
				data.put("pages", pages);
			}
			
			// finish reference
			while(!line.startsWith(PFX_SEPARATOR))
				line = scanner.nextLine();
			for(int i=0;i<2;i++)
				line = scanner.nextLine();
			
			result = buildArticle(data, citedArticles);
			result = retrieveArticle(result,articles);
		}
		
		return result;
	}
	
	/**
	 * Retrieves an article from the article map.
	 * If it already exists, it might be completed.
	 * Otherwise, it is added to the map.
	 *  
	 * @param article
	 * 		The article to retrieve from the map.
	 * @return
	 * 		The article retrieved from the map (possibly the same object).
	 */
	private Article retrieveArticle(Article article)
	{	// lookup the article
		Article result = null;
		Iterator<Article> it = articles.iterator();
		while(it.hasNext() && result==null)
		{	Article a = it.next();
			if(article.isCompatible(a))
			{	result = a;
				result.timesCited++;
			}
		}
		
		// update the article or list
		if(result==null)
		{	articles.add(article);
			result = article;
		}
		else
			result.completeWith(article);
		
		return result;
	}
	
	/**
	 * Builds an {@code Article} by parsing the specified string,
	 * which is a compact representation of a bibliographic reference.
	 * 
	 * @param string
	 * 		The bibtex string representing the article.
	 * @param authorsMap
	 * 		Map containing the known authors.
	 * @return
	 * 		The corresponding {@code Article} object.
	 */
	public Article buildArticle(String string)
	{	Article result = new Article();
		String temp[] = string.split(", ");
		int index = 0;
		
		// first author
		String temp2[] = temp[index].split(" ");
		if(temp2.length==1 && temp2[0].contains("."))
			temp2 = temp2[0].split("\\.");
		String lastname = temp2[0];
		if(lastname.startsWith("*") || Character.isDigit(lastname.charAt(0)))
			return null;
		String firstnameInitial = null;
		int idx = 1;
		while(temp2.length>idx && firstnameInitial==null)
		{	if(temp2[idx].length()>3 && idx<temp2.length-1)
				lastname = lastname + temp2[idx];
			else
				firstnameInitial = temp2[idx];
			idx++;
		}
		if(firstnameInitial!=null && firstnameInitial.length()>1)
			firstnameInitial = temp2[1].substring(0,1);
		Author author = new Author(lastname, firstnameInitial);
		author = Author.retrieveAuthor(author, authorsMap);
		result.addAuthor(author);
		index++;
		
		// year
		if(temp.length>index)
		{	String tempYear = StringTools.normalize(temp[index]);
			try
			{	Integer.parseInt(tempYear);
			}
			catch(NumberFormatException e)
			{	tempYear = null;
				result. year = "N/A";
			}
			if(tempYear!=null)
			{	result.year = tempYear;
				index++;
			}
		}
		
		// source
		if(temp.length>index)
		{	result.source = StringTools.normalize(temp[index]);
			index++;
		}
		
		// volume
		if(temp.length>index && (temp[index].startsWith("V") || temp[index].startsWith("v")))
		{	result.volume = StringTools.normalize(temp[index].substring(1));
			index++;
		}
		
		// page
		if(temp.length>index && (temp[index].startsWith("P") || temp[index].startsWith("p")))
		{	result.page = StringTools.normalize(temp[index].substring(1).trim());
			index++;
		}
		
		// doi
		if(temp.length>index && (temp[index].startsWith("DOI") || temp[index].startsWith("Doi")))
		{	result.doi = temp[index].substring(4).trim();
			index++;
		}
		
		return result;
	}

	/**
	 * Builds an {@code Article} using the specified map and adding
	 * the specified citations. The {@code Map} must contain at least 
	 * the required fields: {@code bibtexkey}, {@code authors}, 
	 * {@code title}, {@code year}.
	 * 
	 * @param map
	 * 		A map used to initialize the {@code Article} object.
	 * @param citedArticles
	 * 		Articles cited by the newly created article.
	 * @return
	 * 		The created article.
	 */
	public Article buildArticle(Map<String,String> map, Set<Article> citedArticles)
	{	Article result = buildArticle(map);
		result.present = false;
		
		result.citedArticles.addAll(citedArticles);
		for(Article article: citedArticles)
			article.citingArticles.add(article);
		
		return result;
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
	{
		// TODO
	}
}
