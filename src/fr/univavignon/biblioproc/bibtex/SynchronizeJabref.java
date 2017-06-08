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
import java.util.Map;
import java.util.Set;

import fr.univavignon.biblioproc.data.Article;
import fr.univavignon.biblioproc.tools.file.FileNames;

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
	 * Looks up the articles from the second file in the first file, and use
	 * them to update it. This is meant to update the second file using more
	 * up-to-date versions of the articles. The second file is supposed to
	 * contain a subset of the articles from the first one. The updated file
	 * is recorded in the {@link FileNames#FO_OUTPUT} folder.
	 * 
	 * @param originalFile
	 * 		Larger file, also supposedly more recent.
	 * @param selectionFile
	 * 		Smaller file, also supposedly obsolete.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing one of the files.
	 * @throws UnsupportedEncodingException
	 * 		Problem while accessing one of the files.
	 */
	private static void synchronize(String originalFile, String selectionFile) throws FileNotFoundException, UnsupportedEncodingException
	{	// load the larger file
		JabrefFileHandler jfhOrig = new JabrefFileHandler();
		boolean updateGroups = false;
		jfhOrig.loadJabRefFile(originalFile, updateGroups);
		Map<String, Article> mapOrig = jfhOrig.articlesMap;
		
		// load the smaller one
		JabrefFileHandler jfhSelect = new JabrefFileHandler();
		updateGroups = false;
		jfhSelect.loadJabRefFile(selectionFile, updateGroups);
		Map<String, Article> mapSelect = jfhSelect.articlesMap;
		
		// get the original refs for each bibtex key in the smaller collection
		Set<String> keys = mapSelect.keySet();
		for(String key: keys)
		{	Article article = mapOrig.get(key);
			if(article==null)
				throw new IllegalArgumentException("Article \""+key+"\" not found in the main file");
			Article article2 = mapSelect.get(key);
			if(!article.getNormTitle().equals(article2.getNormTitle()))
				throw new IllegalArgumentException("Incompatible articles: \n"+article+"\n"+article2);
			mapSelect.put(key,article);
		}
		
		// record the updated collection
		jfhSelect.writeJabRefFile("updated.bib", null);
		
	}
	
	/**
	 * Testes one of the methods in this class.
	 * 
	 * @param args
	 * 		Not used.
	 * @throws FileNotFoundException
	 * 		Problem while accessing the files.
	 * @throws UnsupportedEncodingException 
	 * 		Problem while accessing the files.
	 */
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{	synchronize(FileNames.FI_BIBTEX_COMPLETE, FileNames.FI_BIBTEX_REVIEW);
	}
}
