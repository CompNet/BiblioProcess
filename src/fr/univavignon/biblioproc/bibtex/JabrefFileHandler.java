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
import fr.univavignon.biblioproc.isi.ParseSci2Network;
import fr.univavignon.biblioproc.tools.FileTools;

/**
 * Class dedicated to Jabref I/Os.
 * 
 * @author Vincent Labatut
 */
public class JabrefFileHandler
{

	/**
	 * Loads the specified Jabref files, and builds the corresponding 
	 * map of articles.
	 * 
	 * @param path
	 * 		Jabref file.
	 * @param updateGroups
	 * 		Whether or not consider Jabref groups.
	 * @return
	 * 		A Map containing the article.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the Jabref file.
	 * @throws UnsupportedEncodingException 
	 * 		Problem while accessing the Jabref file.
	 */
	public static Map<String,Article> loadJabRefFile(String path, boolean updateGroups) throws FileNotFoundException, UnsupportedEncodingException
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
			if(!line.isEmpty() && !line.startsWith(ParseSci2Network.COMMENT_PREFIX))
			{	count++;
				// parse the BibTex entry
				Map<String,String> data = ParseSci2Network.retrieveMap(line,jrScanner);
				// build the article object
				Article article = Article.buildArticle(data);
				// insert in the map of articles
				result.put(article.getBibtexKey(), article);
				// display for verification
				System.out.println(count + ". " + article);
			}
		}
		while(!line.startsWith(ParseSci2Network.COMMENT_PREFIX));
	
		// update with the list of purely applicative articles
		if(updateGroups)
		{	do
				line = jrScanner.nextLine();
			while(!line.startsWith(ParseSci2Network.APPLICATION_PREFIX));
			String listStr = line.substring(ParseSci2Network.APPLICATION_PREFIX.length());
			do
			{	line = jrScanner.nextLine();
				listStr = listStr + line;
			}
			while(!line.endsWith(ParseSci2Network.GROUP_END));
			listStr = listStr.substring(0,listStr.length()-2);
			String keys[] = listStr.split(ParseSci2Network.KEY_SEPARATOR);
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
			while(!line.startsWith(ParseSci2Network.IGNORED_PREFIX));
			listStr = line.substring(ParseSci2Network.IGNORED_PREFIX.length());
			do
			{	line = jrScanner.nextLine();
				listStr = listStr + line;
			}
			while(!line.endsWith(ParseSci2Network.GROUP_END));
			listStr = listStr.substring(0,listStr.length()-3);
			keys = listStr.split(ParseSci2Network.KEY_SEPARATOR);
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
}
