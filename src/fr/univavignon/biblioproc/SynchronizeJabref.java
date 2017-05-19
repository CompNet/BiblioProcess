package fr.univavignon.biblioproc;

/*
 * Biblio Post Process
 * Copyright 2011-2017 Vincent Labatut 
 * 
 * This file is part of Biblio Post Process.
 * 
 * Biblio Post Process is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Biblio Post Process is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Biblio Post Process.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map.Entry;

import fr.univavignon.biblioproc.data.Article;

/**
 * Used to synchronize bibtex keys in the two
 * JabRef files: the main one and the review.
 * 
 * @author Vincent
 *
 */
public class SynchronizeJabref
{	
	public static void main(String[] args) throws FileNotFoundException
	{	// load both files
		HashMap<String,Article> reviewMap = ParseSci2Network.loadJabRefFile(REVIEW_FILE, false);
		HashMap<String,Article> completeMap = ParseSci2Network.loadJabRefFile(COMPLETE_FILE, false);
		
		// create inverted map
		HashMap<String,Article> invertedMap = new HashMap<String, Article>();
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
	
	/////////////////////////////////////////////////////////////////
	// FILES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** JabRef review file */
	private static final String REVIEW_FILE = "C:/Users/Vincent/Documents/Travail/Ecrits/Networks Analysis/Papiers/2012.1.Bouquin/review.bib";
//	private static final String REVIEW_FILE = "C:/Documents and Settings/Vincent/Mes documents/Travail/Ecrits/Networks Analysis/Papiers/2012.1.Bouquin/review.bib";
	/** JabRef complete file */
	private static final String COMPLETE_FILE = "C:/Users/Vincent/Documents/Travail/Ecrits/Networks Analysis/Biblio/network analysis.bib";
//	private static final String COMPLETE_FILE = "C:/Documents and Settings/Vincent/Mes documents/Travail/Ecrits/Networks Analysis/Biblio/network analysis.bib";
}