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
import java.util.Map.Entry;

import fr.univavignon.biblioproc.data.Article;
import fr.univavignon.biblioproc.tools.FileTools;

/**
 * Used to synchronize BibTex keys in the two
 * JabRef files: the main one and the one from 
 * the review article.
 * 
 * @author Vincent Labatut
 */
public class SynchronizeJabref
{	
	/**
	 * Synchronizes both BibTex files, and print any
	 * detected problem.
	 * 
	 * @param args
	 * 		Not used.
	 * @throws FileNotFoundException
	 * 		Problem while accessing the files.
	 * @throws UnsupportedEncodingException 
	 * 		Problem while accessing the files.
	 */
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{	// load both files
		Map<String,Article> reviewMap = JabrefFileHandler.loadJabRefFile(FileTools.FI_BIBTEX_REVIEW, false);
		Map<String,Article> completeMap = JabrefFileHandler.loadJabRefFile(FileTools.FI_BIBTEX_COMPLETE, false);
		
		// create inverted map
		Map<String,Article> invertedMap = new HashMap<String, Article>();
		for(Article article: completeMap.values())
		{	invertedMap.put(article.getCiteAs(), article);
			System.out.println("inserting " + article.getCiteAs());
		}
		
		// check bibtex keys
		System.out.print("\n\nCheck BibTex keys");
		int count = 0;
		for(Entry<String,Article> entry: reviewMap.entrySet())
		{	count++;
			System.out.println("Processing article #" + count);
			String key = entry.getKey();
			Article article = entry.getValue();
			Article article2 = invertedMap.get(article.getCiteAs());
			if(article2==null)
				System.out.println(">> cannot find " + article);
			else
			{	String key2 = article2.getBibtexKey();
				if(!key.equals(key2))
				{	System.out.println(article);
					System.out.println(">> key should be " + key2 + "\n");
				}
			}
		}
	}
}
