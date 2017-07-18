package fr.univavignon.biblioproc.data.biblio;

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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to represent a collection of publications.  
 */
public class Corpus
{	
	/////////////////////////////////////////////////////////////////
	// ARTICLES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map containing all the loaded articles, indexed by their Bibtex id */
	private Map<String, Article> articlesMap = new HashMap<String, Article>();
	
	/**
	 * Returns the article corresponding to the specified Bibtex
	 * key, or {@code null} if no such article currently exists.
	 * 
	 * @param bibkey
	 * 		Bibtex key of the article.
	 * @return
	 * 		The article possessing the specified Bibtex key.
	 */
	public Article getArticle(String bibkey)
	{	Article result = articlesMap.get(bibkey);
		return result;
	}
	
	/**
	 * Return a collection containing all the articles from this
	 * corpus.
	 * 
	 * @return
	 * 		The articles of this corpus.
	 */
	public Collection<Article> getArticles()
	{	Collection<Article> result = articlesMap.values();
		return result;
	}
	
	/**
	 * Adds a new article to this collection. Checks if
	 * its Bibtex key is not already used.
	 * 
	 * @param article
	 * 		Article to add to this corpus.
	 */
	public void addArticle(Article article)
	{	String key = article.bibtexKey;
		if(containsKey(key))
			throw new IllegalArgumentException("Trying to insert an article whose Bibtex key ("+key+") already exists ("+article+")");
		articlesMap.put(key, article);
	}
	
	/**
	 * Checks whether one article alredy has the specified
	 * Bibtex key.
	 *  
	 * @param bibkey
	 * 		Bibtex key to check.
	 * @return
	 * 		{@code true} iff one article uses the key.
	 */
	public boolean containsKey(String bibkey)
	{	boolean result = articlesMap.containsKey(bibkey);
		return result;
	}
	
	/**
	 * Returns a collection of all the Bibtex keys used
	 * in this collection.
	 *  
	 * @return
	 * 		Collection of Bibtex keys.
	 */
	public Collection<String> getKeys()
	{	Collection<String> result = articlesMap.keySet();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// AUTHORS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map containing all the loaded authors, indexed by their normalized name */
	private Map<String, Author> authorsMap = new HashMap<String, Author>();
	
	/**
	 * Looks up the specified name and returns the corresponding
	 * author if it already exists. Otherwise, the method creates
	 * the author, adds it to the corpus and returns it.
	 * 
	 * @param author
	 * 		Targeted author (containing the appropriate name).
	 * @return
	 * 		The targeted author.
	 */
	public Author retrieveAuthor(Author author)
	{	Author result = author;
		Author temp = authorsMap.get(author.normname);
		if(temp!=null)
			result = temp;
		else
			authorsMap.put(author.normname, result);
		return result;
	}
	
//	/**
//	 * Returns the author corresponding to the specified
//	 * name, or {@code null} if no such author exists.
//	 * 
//	 * @param authorName
//	 * 		Name of the targeted author.
//	 * @return
//	 * 		Author corresponding to the name.
//	 */
//	public Author getAuthor(String authorName)
//	{	Author result = authorsMap.get(authorName);
//		return result;
//	}
	
	/////////////////////////////////////////////////////////////////
	// GRAPH			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
}
