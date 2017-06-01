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

/**
 * Class dedicated to Jabref I/Os.
 * 
 * @author Vincent Labatut
 */
public class JabrefFileHandler
{
	/** String marking the end of a BibTex field */
	private static final String FIELD_END = "},";
	/** String marking the end of a BibTex entry */
	private static final String ENTRY_END = "}";
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
	
	/** Bibtex key for the authors */
	public static final String PFX_AUTHOR = "author";
	/** Bibtex key for the abstract */
	public static final String PFX_ABSTRACT = "abstract";
	/** Bibtex key for the chapter */
	public static final String PFX_CHAPTER = "chapter";
	/** Bibtex key for the DOI */
	public static final String PFX_DOI = "doi";
	/** Bibtex key for the file */
	public static final String PFX_FILE = "file";
	/** Bibtex key for the institution */
	public static final String PFX_INSTITUTION = "institution";
	/** Bibtex key for the issue */
	public static final String PFX_ISSUE = "issue";
	/** Bibtex key for the journal */
	public static final String PFX_JOURNAL = "journal";
	/** Bibtex key for the number */
	public static final String PFX_NUMBER = "number";
	/** Bibtex key for the owner */
	public static final String PFX_OWNER = "owner";
	/** Bibtex key for the pages */
	public static final String PFX_PAGES = "pages";
	/** Bibtex key for the time stamp */
	public static final String PFX_TIMESTAMP = "timestamp";
	/** Bibtex key for the article title */
	public static final String PFX_TITLE_ARTICLE = "title";
	/** Bibtex key for the book title */
	public static final String PFX_TITLE_BOOK = "booktitle";
	/** Bibtex key for the URL */
	public static final String PFX_URL = "url";
	/** Bibtex key for the volume */
	public static final String PFX_VOLUME = "volume";
	/** Bibtex key for the publication year */
	public static final String PFX_YEAR = "year";
	
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
				Article article = Article.buildArticle(data,authorsMap);
				// insert in the map of articles
				result.put(article.getBibtexKey(), article);
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
				article.setIgnored(true);
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
				article.setIgnored(true);
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
		result.put("bibtexkey", bibtexkey);
		
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
}
