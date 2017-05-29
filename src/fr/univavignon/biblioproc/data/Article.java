package fr.univavignon.biblioproc.data;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import fr.univavignon.biblioproc.tools.StringTools;

/**
 * This class is used to represent a publication,
 * with a minimum set of fields.
 */
public class Article implements Comparable<Article>
{	
	/**
	 * Builds the Article object from a 
	 * {@code Map} containing at least the required
	 * fields: {@code bibtexkey}, {@code authors}, {@code title}, {@code year}.
	 * 
	 * @param articleMap
	 * 		Map containing the needed data.
	 * @param authorsMap
	 * 		Map containing the known authors.
	 * @return 
	 * 		The new article instance.
	 */
	public static Article buildArticle(Map<String,String> articleMap, Map<String,Author> authorsMap)
	{	Article result = new Article();
	
		// init BibTex key
		result.bibtexKey = articleMap.get("bibtexkey");
		
		// init authors
		String temp[] = articleMap.get("author").split(" and ");
		for(String authorStr: temp)
		{	Author author = new Author(authorStr);
			author = Author.retrieveAuthor(author, authorsMap);
			result.authors.add(author);
		}
		
		// init title
		String title = articleMap.get("title");
		result.title = StringTools.normalize(title);
		
		// init source
		String source = articleMap.get("source");
		if(source==null)
			source = articleMap.get("journal");
		if(source==null)
			source = articleMap.get("booktitle");
		if(source==null)
			source = articleMap.get("institution");
		if(source==null)
			source = articleMap.get("school");
		if(source==null)
			source = articleMap.get("publisher");
		result.source = StringTools.normalize(source);
		
		// init volume
		result.volume = articleMap.get("volume");
		result.volume = StringTools.normalize(result.volume);
		
		// init issue
		String issue = articleMap.get("number");
		result.issue = StringTools.normalize(issue);
		
		// init page
		String page = articleMap.get("pages");
		if(page!=null)
		{	if(page.contains("-"))
				page = page.split("-")[0];
			result.page = StringTools.normalize(page);
		}
		
		// init year
		String year = articleMap.get("year");
		result.year = StringTools.normalize(year);
		
		// init doi
		String doi = articleMap.get("doi");
		if(doi!=null)
			result.doi = doi.trim();
		
		// present
		result.present = true;
		
		return result;
	}
	
	/**
	 * Builds an {@code Article} by parsing the specified string.
	 * 
	 * @param string
	 * 		The bibtex string representing the article.
	 * @param authorsMap
	 * 		Map containing the known authors.
	 * @return
	 * 		The corresponding {@code Article} object.
	 */
	public static Article buildArticle(String string, Map<String,Author> authorsMap)
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
		result.authors.add(author);
		index++;
		
		// year
		if(temp.length>index)
		{	String tempYear = StringTools.normalize(temp[index]);
			try
			{	Integer.parseInt(tempYear);
			}
			catch(NumberFormatException e)
			{	tempYear = null;
				result.year = "N/A";
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
		
		// correct some of the errors in ISI
		checkErrors(result, authorsMap);
		
//if(
//	result.year!=null && result.year.equals("2003")
//	&& result.volume!=null && result.volume.equals("45")
//	&& result.page!=null && result.page.equals("167")
//	)
//	System.out.print("");

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
	 * @param authorsMap
	 * 		Map containing the known authors.
	 * @return
	 * 		The created article.
	 */
	public static Article buildArticle(Map<String,String> map, Set<Article> citedArticles, Map<String,Author> authorsMap)
	{	Article result = buildArticle(map, authorsMap);
		result.present = false;
		
		result.citedArticles.addAll(citedArticles);
		for(Article article: citedArticles)
			article.addCitingArticle(article);
		
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// IGNORED			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Indicates if this article was ignored in the bibliographic review */
	private boolean ignored = false;
	
	/**
	 * Returns {@code true} iff this article was
	 * ignored in the bibliographic review.
	 * 
	 * @return
	 * 		{@code true} in case of ignored article.
	 */
	public boolean isIgnored()
	{	return ignored;
	}
	
	/**
	 * Set the {@code ignored} flag.
	 * 
	 * @param ignored
	 * 		New value for the {@code ignored} flag.
	 */
	public void setIgnored(boolean ignored)
	{	this.ignored = ignored;
	}

	/////////////////////////////////////////////////////////////////
	// TIMES CITED		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Number of times the article was cited in the corpus */
	private int timesCited = 0;
	
	/**
	 * Returns the number of times the article was cited.
	 * 
	 * @return
	 * 		Number of times the article was cited.
	 */
	public int getTimesCited()
	{	return timesCited;
	}
	
	/**
	 * Increments the number of times the article
	 * was cited.
	 */
	public void incrementTimesCited()
	{	timesCited++;
	}
	
	/////////////////////////////////////////////////////////////////
	// PRESENT			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Indicates if this article is present in the JabRef file */
	private boolean present = false;
	
	/**
	 * Returns {@code true} iff this article is
	 * present in the JabRef file.
	 * 
	 * @return
	 * 		{@code true} in case of present article.
	 */
	public boolean isPresent()
	{	return present;
	}

	/////////////////////////////////////////////////////////////////
	// BIBTEX KEY		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** BibTex key of this article */
	private String bibtexKey = null;
	
	/**
	 * Returns the BibTex key of this article
	 * 
	 * @return
	 * 		BibTex key of this article.
	 */
	public String getBibtexKey()
	{	return bibtexKey;
	}

	/////////////////////////////////////////////////////////////////
	// AUTHOR LIST		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of authors */
	private List<Author> authors = new ArrayList<Author>();
	
	/**
	 * Returns the list of authors.
	 * 
	 * @return
	 * 		The list of authors of this article.
	 */
	public List<Author> getAuthors()
	{	return authors;
	}
	
	/**
	 * Returns the full name of the first author,
	 * under the form "xxxxx, y", where "xxxxx" is the
	 * lastname and "y" the initial of the firstname.
	 * 
	 * @return
	 * 		A string of the form "lastname, f".
	 */
	public String getFirstAuthorFullname()
	{	String result = authors.get(0).getFullname();
		return result;
	}
	
	/**
	 * Adds a new author to this article.
	 * 
	 * @param author
	 * 		Author to add to this article.
	 */
	public void addAuthor(Author author)
	{	if(!authors.contains(author))
			authors.add(author);
	}

	/////////////////////////////////////////////////////////////////
	// TITLE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Title of this article */
	private String title = null;
	
	/**
	 * Returns the title of this article
	 * 
	 * @return
	 * 		Title of this article.
	 */
	public String getTitle()
	{	return title;
	}

	/////////////////////////////////////////////////////////////////
	// SOURCE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Source of this article (conference, journal, etc.) */
	private String source = null;

	/**
	 * Returns the source of this article
	 * (conference, journal, etc.)
	 * 
	 * @return
	 * 		Source of this article.
	 */
	public String getSource()
	{	return source;
	}
	
	/////////////////////////////////////////////////////////////////
	// VOLUME			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Volume number of this article */
	private String volume = null;

	/**
	 * Returns the volume number of this article.
	 * 
	 * @return
	 * 		Volume of this article.
	 */
	public String getVolume()
	{	return volume;
	}
	
	/////////////////////////////////////////////////////////////////
	// ISSUE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Issue number of this article */
	private String issue = null;

	/**
	 * Returns the issue number of this article.
	 * 
	 * @return
	 * 		Issue of this article.
	 */
	public String getIssue()
	{	return issue;
	}
	
	/////////////////////////////////////////////////////////////////
	// STARTING PAGE	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Starting page of this article */
	private String page = null;

	/**
	 * Returns the page of this article.
	 * 
	 * @return
	 * 		Starting page of this article.
	 */
	public String getPage()
	{	return page;
	}
	
	/////////////////////////////////////////////////////////////////
	// YEAR				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Year of publication of this article */
	private String year = null;

	/**
	 * Returns the year of publication of this article
	 * 
	 * @return
	 * 		Year of publication of this article.
	 */
	public String getYear()
	{	return year;
	}
	
	/////////////////////////////////////////////////////////////////
	// DOI				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Digital object identifier of this article */
	private String doi = null;

	/**
	 * Returns the digital object identifier of this article.
	 * 
	 * @return
	 * 		Digital object identifier of this article.
	 */
	public String getDoi()
	{	return doi;
	}
	
	/////////////////////////////////////////////////////////////////
	// CITED ARTICLES	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of cited articles */
	private Set<Article> citedArticles = new TreeSet<Article>();

	/**
	 * Returns the list of cited articles.
	 * 
	 * @return
	 * 		List of articles cited by this article.
	 */
	public Set<Article> getCitedArticles()
	{	return citedArticles;
	}

//	/**
//	 * Adds a new article in the list of articles
//	 * cited by this article.
//	 * 
//	 * @param article
//	 * 		The new cited article to be added.
//	 */
//	public void addCitedArticle(Article article)
//	{	citedArticles.add(article);
//	}
	
	/////////////////////////////////////////////////////////////////
	// CITING ARTICLES	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of citing articles */
	private Set<Article> citingArticles = new TreeSet<Article>();

	/**
	 * Returns the list of citing articles.
	 * 
	 * @return
	 * 		List of articles citing this article.
	 */
	public Set<Article> getCitingArticles()
	{	return citingArticles;
	}
	
	/**
	 * Adds a new article in the list of articles
	 * citng this article.
	 * 
	 * @param article
	 * 		The new citing article to be added.
	 */
	public void addCitingArticle(Article article)
	{	citingArticles.add(article);
	}

	/////////////////////////////////////////////////////////////////
	// ERRORS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Apply some manual corections to certain articles.
	 * 
	 * @param article
	 * 		Article to correct.
	 * @param authorsMap
	 * 		Map containing the known authors.
	 */
	public static void checkErrors(Article article, Map<String,Author> authorsMap)
	{	if(article.getAuthors().get(0).getFullname().equals("flake, g")
			//&& article.getSource()!=null && article.getSource().equals("ieee comput")
			&& article.getVolume()!=null && article.getVolume().equals("36")
			&& article.getPage()!=null && article.getPage().equals("66")
			&& article.getYear()!=null && article.getYear().equals("2002"))
		{	article.volume = "35";
		}
	
		else if(article.getAuthors().get(0).getFullname().equals("vandongen, s")
			//&& article.getSource()!=null && article.getSource().equals("thesis u ultrecht ne")
			//&& article.getPage()!=null && article.getPage().equals("371")
			&& article.getYear()!=null && article.getYear().equals("2000"))
		{	Author author = article.getAuthors().get(0);
			authorsMap.remove(author.getFullname());
			author = authorsMap.get("van dongen, s");
			article.authors.set(0,author);
		}
	
		else if(article.getAuthors().get(0).getFullname().equals("granovet, m")
			&& article.getSource()!=null && article.getSource().equals("am j sociol")
			&& article.getVolume()!=null && article.getVolume().equals("78")
			&& article.getPage()!=null && article.getPage().equals("1360")
			&& article.getYear()!=null && article.getYear().equals("1973"))
		{	Author author = article.getAuthors().get(0);
			authorsMap.remove(author.getFullname());
			author = authorsMap.get("granovetter, m");
			article.authors.set(0,author);
		}
	
		else if(article.getAuthors().get(0).getFullname().equals("leydesdorff, l")
			&& article.getSource()!=null && article.getSource().equals("j math sociol")
			&& article.getYear()!=null && article.getYear().equals("1971"))
		{	Author author = authorsMap.get("lorrain, f");
			article.authors.set(0,author);
			author = authorsMap.get("white, h");
			article.authors.add(author);
			article.volume = "1";
			article.page = "49";
			article.year = "1971";
		}
	
		else if(article.getAuthors().get(0).getFullname().equals("vonmering, c")
			&& article.getSource()!=null && article.getSource().equals("nature")
			&& article.getVolume()!=null && article.getVolume().equals("417")
			&& article.getPage()!=null && article.getPage().equals("399")
			&& article.getYear()!=null && article.getYear().equals("2002"))
		{	Author author = article.getAuthors().get(0);
		authorsMap.remove(author.getFullname());
			author = authorsMap.get("von mering, c");
			article.authors.set(0,author);
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// COMPARISON		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public int compareTo(Article article)
	{	String citeAs1 = getCiteAs();
		String citeAs2 = article.getCiteAs();
		int result = citeAs1.compareTo(citeAs2);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{	boolean result = false;
		if(obj != null && obj instanceof Article)
		{	Article article = (Article) obj;
			result = compareTo(article) == 0;
		}
		return result;
	}

	@Override
	public int hashCode()
	{	String citeAs = getCiteAs();
		int result = citeAs.hashCode();
		return result;
	}
	
	/**
	 * Checks if two articles (this one and the specified
	 * one) are likely to be the same one under two different 
	 * forms.
	 * 
	 * @param article
	 * 		Article to which to compare this article.
	 * @return
	 * 		{@code true} iff the two articles are compatible.
	 */
	public boolean isCompatible(Article article)
	{	boolean result = true;
	
boolean same = false;
//if(article.getTitle()!=null && article.getTitle().equals(title))
//{	System.out.println(title);
//	same = true;
//}

//if(article.getPage()!=null && article.getPage().equals("026113"))
//	System.out.print("");
//if(article.getTitle()!=null && article.getTitle().equals("the structure and function of complex networks"))
//	System.out.print("");

		// authors
		List<Author> authors2 = article.getAuthors();
		if(authors.size()>1 && authors2.size()>1)
		{	Iterator<Author> it1 = authors.iterator();
			Iterator<Author> it2 = authors2.iterator();
			while(result && it1.hasNext() && it2.hasNext())
			{	Author a1 = it1.next();
				Author a2 = it2.next();
				result = a1.equals(a2);
			}
			result = result && (it1.hasNext() && it2.hasNext() || !it1.hasNext() && !it2.hasNext());
		}
		else
		{	Author author1 = authors.get(0);
			Author author2 = authors2.get(0);
			result = author1.equals(author2);
		}
if(same && !result)
{	for(Author a: authors)
		System.out.println(a);
	System.out.println(">> VS <<");
	for(Author a: article.getAuthors())
		System.out.println(a);
	return(result);
}
		
		// title
		if(result)
		{	String title2 = article.getTitle();
			if(title!=null && title2!=null)
				result = title.equals(title2);
		}
if(same && !result)
{	System.out.println(title);
	System.out.println(">> VS <<");
	System.out.println(article.getTitle());
	return(result);
}
		
		// source
		// not considered because the spelling varies a lot
		
		// volume
		if(result)
		{	String volume2 = article.getVolume();
			if(volume!=null && volume2!=null)
				result = volume.equals(volume2);
		}
if(same && !result)
{	System.out.println(volume);
	System.out.println(">> VS <<");
	System.out.println(article.getVolume());
	return(result);
}
		
		// issue
		if(result)
		{	String issue2 = article.getIssue();
			if(issue!=null && issue2!=null)
				result = issue.equals(issue2);
		}
if(same && !result)
{	System.out.println(issue);
	System.out.println(">> VS <<");
	System.out.println(article.getIssue());
	return(result);
}
		
		// page
		if(result)
		{	String page2 = article.getPage();
			if(page!=null && page2!=null)
				result = page.equals(page2);
		}
if(same && !result)
{	System.out.println(page);
	System.out.println(">> VS <<");
	System.out.println(article.getPage());
	return(result);
}
		
		// year
		if(result)
		{	String year2 = article.getYear();
			if(year!=null && year2!=null)
				result = year.equals(year2);
		}
if(same && !result)
{	System.out.println(year);
	System.out.println(">> VS <<");
	System.out.println(article.getYear());
	return(result);
}
		
		return result;
	}

	/**
	 * Complete an existing Article object
	 * with additional data.
	 * 
	 * @param article
	 * 		The additional data.
	 */
	public void completeWith(Article article)
	{	// authors
		for(Author author: article.getAuthors())
			addAuthor(author);
		
		// title
		if(title==null)
		{	String title2 = article.getTitle();
			if(title2!=null)
				title = title2;
		}
		
		// volume
		if(volume==null)
		{	String volume2 = article.getVolume();
			if(volume2!=null)
				volume = volume2;
		}
		
		// issue
		if(issue==null)
		{	String issue2 = article.getIssue();
			if(issue2!=null)
				issue = issue2;
		}
		
		// page
		if(page==null)
		{	String page2 = article.getPage();
			if(page2!=null)
				page = page2;
		}
		
		// references
		Set<Article> citingArticles2 = article.getCitingArticles();
		citingArticles.addAll(citingArticles2);
		Set<Article> citedArticles2 = article.getCitedArticles();
		citedArticles.addAll(citedArticles2);
	}
	
	/////////////////////////////////////////////////////////////////
	// STRINGS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns a citation of this article as a String.
	 * 
	 * @return
	 * 		String representing a citation of this article.
	 */
	public String getCiteAs()
	{	String result = "";
//		for(Author author: authors)
//			result = result + author.getFullname() + ", ";
		result = result + getFirstAuthorFullname() + ", ";
		
		result = result + year;
		if(volume!=null)
			result = result + ", v" + volume;
		if(page!=null)
			result = result + ", p" + page;
		return result;
	}
	
	@Override
	public String toString()
	{	String result = "";

		// bibkey
		if(bibtexKey!=null)
			result = result + bibtexKey;
		result = result + "\t";
		
		// title
		result = result + title;
		result = result + ". ";
		
		// first author
		result = result + getFirstAuthorFullname();
		result = result + ". ";
		
		// source
		result = result + source;
		result = result + " ";
		
		// volume
		if(volume!=null)
			result = result + volume;
		result = result + "(";
		
		// issue
		if(issue!=null)
			result = result + issue;
		result = result + "):";
		
		// starting page
		if(page!=null)
			result = result + page;
		result = result + ", ";
		
		// year
		result = result + year + ".";
		
		// doi
		if(doi!=null)
			result = result + " DOI: " + doi;
		
		return result;
	}
}
