package fr.univavignon.biblioproc.bibtex;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import fr.univavignon.biblioproc.data.Article;
import fr.univavignon.biblioproc.data.Author;
import fr.univavignon.biblioproc.tools.FileTools;
import fr.univavignon.biblioproc.tools.StringTools;

/**
 * Class dedicated to Jabref I/Os.
 * 
 * @author Vincent Labatut
 */
public class JabrefFileHandler
{
	/////////////////////////////////////////////////////////////////
	// BIBTEX MARKERS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String marking the end of a BibTex field */
	private static final String FIELD_END = "},";
	/** String marking the end of a BibTex entry */
	private static final String ENTRY_END = "}";
	
	/////////////////////////////////////////////////////////////////
	// JABREF MARKERS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String marking the end of the actual BibTex file (and the begining of the JabRef part) */
	private static final String COMMENT_PREFIX = "@comment";
	/** String marking the begining of the list of ignored articles */
	private static final String IGNORED_PREFIX = "3 ExplicitGroup:Ignored\\;2\\;";
	/** String marking the begining of the list of pureley applicative articles */
	private static final String APPLICATION_PREFIX = "3 ExplicitGroup:Applications Only\\;2\\;";
	/** String marking the end of a JabRef group */
	private static final String GROUP_END = ";;";
	/** String separating the BibTex kes in a JabRef group */
	private static final String KEY_SEPARATOR = "\\;";
	
	/////////////////////////////////////////////////////////////////
	// BIBTEX FIELDS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Bibtex key for the article key */
	private static final String PFX_KEY = "bibtexkey";
	/** Bibtex key for the authors */
	private static final String PFX_AUTHOR = "author";
	/** Bibtex key for the abstract */
	private static final String PFX_ABSTRACT = "abstract";
	/** Bibtex key for the chapter */
	private static final String PFX_CHAPTER = "chapter";
	/** Bibtex key for the DOI */
	private static final String PFX_DOI = "doi";
	/** Bibtex key for the file */
	private static final String PFX_FILE = "file";
	/** Bibtex key for the institution */
	private static final String PFX_INSTITUTION = "institution";
	/** Bibtex key for the issue */
	private static final String PFX_ISSUE = "issue";
	/** Traditional Bibtex key for the journal */
	private static final String PFX_JOURNAL1 = "journal";
	/** Alterantive Bibtex key for the journal */
	private static final String PFX_JOURNAL2 = "journaltitle";
	/** Bibtex key for the number */
	private static final String PFX_NUMBER = "number";
	/** Bibtex key for the owner */
	private static final String PFX_OWNER = "owner";
	/** Bibtex key for the pages */
	private static final String PFX_PAGES = "pages";
	/** Bibtex key for the time stamp */
	private static final String PFX_TIMESTAMP = "timestamp";
	/** Bibtex key for the article title */
	private static final String PFX_TITLE_ARTICLE = "title";
	/** Bibtex key for the book title */
	private static final String PFX_TITLE_BOOK = "booktitle";
	/** Bibtex key for the URL */
	private static final String PFX_URL = "url";
	/** Bibtex key for the volume */
	private static final String PFX_VOLUME = "volume";
	/** Bibtex key for the publication year */
	private static final String PFX_YEAR = "year";
	
	/////////////////////////////////////////////////////////////////
	// LOADING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Loads the specified Jabref file, and builds the corresponding 
	 * map of articles.
	 * 
	 * @param path
	 * 		Jabref file.
	 * @param updateGroups
	 * 		Whether or not consider Jabref groups.
	 * @param authorsMap
	 * 		Map containing all the loaded authors.
	 * @return
	 * 		A Map containing the articles.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the Jabref file.
	 * @throws UnsupportedEncodingException 
	 * 		Problem while accessing the Jabref file.
	 */
	public static Map<String,Article> loadJabRefFile(String path, boolean updateGroups, Map<String,Author> authorsMap) throws FileNotFoundException, UnsupportedEncodingException
	{	// open the JabRef file
		System.out.println("Open the JabRef file " + path);
		Scanner jrScanner = FileTools.openTextFileRead(path,null);
		
		// retrieve the titles of the articles
		System.out.println("Get the articles data");
		Map<String,Article> result = new HashMap<String, Article>();
		int count = 0;
		String line = null;
		// pass comments
		do
			line = jrScanner.nextLine();
		while(!line.isEmpty());
		// get the articles
		do
		{	line = jrScanner.nextLine();
			if(!line.isEmpty() && !line.startsWith(COMMENT_PREFIX))
			{	count++;
				// parse the BibTex entry
				Map<String,String> data = retrieveArticleMap(line,jrScanner);
				// build the article object
				Article article = buildArticle(data, authorsMap);
				// insert in the map of articles
				result.put(article.bibtexKey, article);
				// display for verification
				System.out.println(count + ". " + article);
			}
		}
		while(!line.startsWith(COMMENT_PREFIX));
	
		// update with the list of purely applicative articles
		if(updateGroups)
		{	do
				line = jrScanner.nextLine();
			while(!line.startsWith(APPLICATION_PREFIX));
			String listStr = line.substring(APPLICATION_PREFIX.length());
			do
			{	line = jrScanner.nextLine();
				listStr = listStr + line;
			}
			while(!line.endsWith(GROUP_END));
			listStr = listStr.substring(0,listStr.length()-2);
			String keys[] = listStr.split(KEY_SEPARATOR);
			System.out.println("\nPurely applicative articles:");
			count = 0;
			for(String key: keys)
			{	count++;
				Article article = result.get(key.substring(0,key.length()-1));
				article.ignored = true;
				System.out.println(count + ". " + article);
			}
	
			// add the ignored articles
			do
				line = jrScanner.nextLine();
			while(!line.startsWith(IGNORED_PREFIX));
			listStr = line.substring(IGNORED_PREFIX.length());
			do
			{	line = jrScanner.nextLine();
				listStr = listStr + line;
			}
			while(!line.endsWith(GROUP_END));
			listStr = listStr.substring(0,listStr.length()-3);
			keys = listStr.split(KEY_SEPARATOR);
			System.out.println("\nIgnored articles:");
			count = 0;
			for(String key: keys)
			{	count++;
				Article article = result.get(key.substring(0,key.length()-1));
				article.ignored = true;
				System.out.println(count + ". " + article);
			}
		}
		
		// close JabRef file
		jrScanner.close();
		
		return result;
	}

	/**
	 * Receives the first line of a BibTex entry,
	 * and a scanner pointing on the second line,
	 * and builds the corresponding map, which can
	 * subsequently be used to build an {@link Article}
	 * object.
	 * 
	 * @param line
	 * 		First line of the BibTex entry.
	 * @param scanner
	 * 		Scanner pointing on the rest of the entry.
	 * @return
	 * 		Map containing the entry data.
	 */
	private static Map<String, String> retrieveArticleMap(String line, Scanner scanner)
	{	// init map
		Map<String, String> result = new HashMap<String, String>();
		
		// bibtex key
		int start = line.indexOf('{') + 1;
		int end = line.length() - 1;
		String bibtexkey = line.substring(start, end);
		result.put(PFX_KEY, bibtexkey);
		
		// rest of the fields
		do
		{	// get the first line for this field
			line = scanner.nextLine();
			
			if(!line.equals(ENTRY_END))
			{	// retrieve the name of the field
				int pos = line.indexOf('=');
				String fieldName = line.substring(2,pos-1);
				// retrieve the associated value
				String fieldValue = null;
				if(line.endsWith(FIELD_END))
					fieldValue = line.substring(pos+3,line.length()-2);
				else if(line.endsWith("}"))
					fieldValue = line.substring(pos+3,line.length()-1);
				else
				{	fieldValue = line.substring(pos+3);
					do
					{	line = scanner.nextLine();
						if(line.endsWith(FIELD_END))
							fieldValue = fieldValue + " " + line.substring(1,line.length()-2);
						else
							fieldValue = fieldValue + " " + line.substring(1);
					}
					while(!line.endsWith(FIELD_END));
				}
				// insert in the map
				result.put(fieldName, fieldValue);
			}
		}
		while(!line.equals(ENTRY_END));
		
		return result;
	}

	/**
	 * Builds the Article object from a 
	 * {@code Map} containing at least the required Bibtex
	 * fields: {@code bibtexkey}, {@code authors}, {@code title}, {@code year}.
	 * 
	 * @param articleMap
	 * 		Map containing the needed data.
	 * @param authorsMap
	 * 		Map containing the known authors.
	 * @return 
	 * 		The new article instance.
	 */
	private static Article buildArticle(Map<String,String> articleMap, Map<String,Author> authorsMap)
	{	Article result = new Article();
	
		// init BibTex key
		result.bibtexKey = articleMap.get(PFX_KEY);
		
		// init authors
		String temp[] = articleMap.get(PFX_AUTHOR).split(" and ");
		for(String authorStr: temp)
		{	Author author = new Author(authorStr);
			author = Author.retrieveAuthor(author, authorsMap);
			result.addAuthor(author);
		}
		
		// init title
		String title = articleMap.get(PFX_TITLE_ARTICLE);
		result.title = StringTools.normalize(title);
		
		// init source
		String source = null;
		if(source==null)
			source = articleMap.get(PFX_JOURNAL1);
		if(source==null)
			source = articleMap.get(PFX_JOURNAL2);
		if(source==null)
			source = articleMap.get(PFX_TITLE_BOOK);
		if(source==null)
			source = articleMap.get(PFX_INSTITUTION);
		result.source = StringTools.normalize(source);//TODO should we normalize here ? (if we want to record later, shouldn't)
		
		// init volume
		result.volume = articleMap.get(PFX_VOLUME);
		result.volume = StringTools.normalize(result.volume);
		
		// init issue
		String issue = articleMap.get(PFX_NUMBER);
		if(issue==null)
			issue = articleMap.get(PFX_ISSUE);
		result.issue = StringTools.normalize(issue);
		
		// init page
		String page = articleMap.get(PFX_PAGES);
		if(page!=null)
			result.page = StringTools.normalize(page);
		
		// init year
		String year = articleMap.get(PFX_YEAR);
		result.year = StringTools.normalize(year);
		
		// init doi
		String doi = articleMap.get(PFX_DOI);
		if(doi!=null)
			result.doi = doi.trim();
		
		// abstract
		String abstrct = articleMap.get(PFX_ABSTRACT);
		if(abstrct!=null)
			result.abstrct = abstrct.trim();
		
		// chapter
		String chapter = articleMap.get(PFX_CHAPTER);
		if(chapter!=null)
			result.chapter = chapter.trim();
		
		// file
		String file = articleMap.get(PFX_FILE);
		if(file!=null)
			result.file = file.trim();
		
		// owner
		String owner = articleMap.get(PFX_OWNER);
		if(owner!=null)
			result.owner = owner.trim();
		
		// timestamp
		String timestamp = articleMap.get(PFX_TIMESTAMP);
		if(timestamp!=null)
			result.timestamp = timestamp.trim();
		
		// url
		String url = articleMap.get(PFX_URL);
		if(url!=null)
			result.url = url.trim();
		
		// present
		result.present = true;
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// WRITING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	// TODO
}
