package fr.univavignon.biblioproc.inout;

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

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import fr.univavignon.biblioproc.data.biblio.Article;
import fr.univavignon.biblioproc.data.biblio.Corpus;
import fr.univavignon.biblioproc.tools.file.FileNames;
import fr.univavignon.biblioproc.tools.log.HierarchicalLogger;
import fr.univavignon.biblioproc.tools.log.HierarchicalLoggerManager;

/**
 * Used to synchronize BibTex keys in the two
 * JabRef files: the main one and the one from 
 * the review article.
 * 
 * @author Vincent Labatut
 */
public class SynchronizeJabref
{	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
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
		Corpus corpusOrig = jfhOrig.corpus;
		
		// load the smaller one
		JabrefFileHandler jfhSelect = new JabrefFileHandler();
		updateGroups = false;
		jfhSelect.loadJabRefFile(selectionFile, updateGroups);
		Corpus corpusSelect = jfhSelect.corpus;
		
		// get the original refs for each bibtex key in the smaller collection
		logger.log("Compare both collections");
		logger.increaseOffset();
		Collection<String> keys = corpusSelect.getKeys();
		int i = 0;
		for(String key: keys)
		{	i++;
			logger.log("Processing key "+i+"/"+keys.size()+": "+key);
			Article article = corpusOrig.getArticleByBibkey(key);
			if(article==null)
				throw new IllegalArgumentException("Article \""+key+"\" not found in the main file");
			Article article2 = corpusSelect.getArticleByBibkey(key);
			if(!article.getNormTitle().equals(article2.getNormTitle()))
				throw new IllegalArgumentException("Incompatible articles: \n"+article+"\n"+article2);
			corpusSelect.addArticle(article);
		}
		logger.decreaseOffset();
		
		// record the updated collection
		jfhSelect.writeJabRefFile("updated.bib", null);
		
	}
	
	/**
	 * Tests one of the methods in this class.
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
