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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import fr.univavignon.biblioproc.bibtex.JabrefFileHandler;
import fr.univavignon.biblioproc.data.graph.Graph;
import fr.univavignon.biblioproc.data.graph.Node;
import fr.univavignon.biblioproc.tools.string.StringTools;

/**
 * This class is used to represent a publication,
 * with a minimum set of fields. It can be extracted
 * from a Bibtex file, or a ISI (Endnote) file. 
 * <br/>
 * This class is also able to represent additional information, 
 * such as various corpus-wise stats and cited/citing papers.  
 */
public class Article implements Comparable<Article>
{	
	/////////////////////////////////////////////////////////////////
	// IGNORED			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Indicates if this article was ignored in the bibliographic review */
	public boolean ignored = false;
	
	/////////////////////////////////////////////////////////////////
	// TIMES CITED		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Number of times the article was cited in the corpus */
	public int timesCited = 0;
	
	/////////////////////////////////////////////////////////////////
	// PRESENT			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Indicates if this article is present in the JabRef file */
	public boolean present = false;
	
	/////////////////////////////////////////////////////////////////
	// BIBTEX KEY		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** BibTex key of this article */
	public String bibtexKey = null;
	
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
	
//	/**
//	 * Returns the full name of the first author,
//	 * under the form "xxxxx, y", where "xxxxx" is the
//	 * lastname and "y" the initial of the firstname.
//	 * 
//	 * @return
//	 * 		A string of the form "lastname, f".
//	 */
//	public String getFirstAuthorFullname()
//	{	String result = authors.get(0).getFullname();
//		return result;
//	}
	
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
	/** Normalized version of the title */
	private String normTitle = null;
	
	/**
	 * Returns the title of this article.
	 *  
	 * @return
	 * 		Title of this article.
	 */
	public String getTitle()
	{	return title;
	}
	
	/**
	 * Returns the normalized version of the title of this article.
	 *  
	 * @return
	 * 		Normalized title of this article.
	 */
	public String getNormTitle()
	{	return normTitle;
	}
	
	/**
	 * Update the title and its normalized version.
	 * 
	 * @param title
	 * 		New title of this article.
	 */
	public void setTitle(String title)
	{	this.title = StringTools.clean(title);
		normTitle = StringTools.normalize(this.title);
	}
	
	/////////////////////////////////////////////////////////////////
	// SOURCE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Type of source of this article (conference, journal, etc.) */
	private SourceType sourceType = null;
	/** Name of the source of this article (conference, journal, etc.) */
	private String sourceName = null;
	/** Normalized name of the source  */
	private String normSourceName = null;
	
	/**
	 * Returns the type of source of this article 
	 * (journal, conference, thesis, etc.).
	 * 
	 * @return
	 * 		Type of source.
	 */
	public SourceType getSourceType()
	{	return sourceType;
	}
	
	/**
	 * Returns the source of the article 
	 * (journal, conference, thesis, etc.).
	 * 
	 * @return
	 * 		Source name.
	 */
	public String getSourceName()
	{	return sourceName;
	}
	
	/**
	 * Returns the normalized version of the article 
	 * source (journal, conference, thesis, etc.).
	 * 
	 * @return
	 * 		Normalized source name.
	 */
	public String getNormSourceName()
	{	return normSourceName;
	}
	
	/**
	 * Sets up the source of this article
	 * (journal, conference, thesis, etc.).
	 * 
	 * @param sourceType
	 * 		Type of source for this article.
	 * @param sourceName
	 * 		Name of the article source.
	 */
	public void setSource(SourceType sourceType, String sourceName)
	{	this.sourceType = sourceType;
		this.sourceName = sourceName;
		switch(sourceType)
		{	case BOOK:
				publisher = sourceName;
				break;
			case CHAPTER:
			case CONFERENCE:
				booktitle = sourceName;
				break;
			case ELECTRONIC:
				organization = sourceName;
				break;
			case JOURNAL:
				journal = sourceName;
				break;
			case REPORT:
				institution = sourceName;
				break;
			case THESIS_MSC:
			case THESIS_PHD:
				school = sourceName;
				break;
		}
		
		normSourceName = StringTools.normalize(sourceName);
		normSourceName = normSourceName.replaceAll("[^a-zA-Z0-9]", "");
		if(sourceType==SourceType.CONFERENCE)
		{	// remove a possible ending string between parenthesis (typically for conferences)
			int pos = normSourceName.indexOf('(');
			if(pos!=-1)
				normSourceName = normSourceName.substring(0,pos);
			// remove a possible year at the end 
			while(Character.isDigit(sourceName.charAt(sourceName.length()-1)))
				sourceName = sourceName.substring(0,sourceName.length()-1);
			sourceName = sourceName.trim();
		}
	}
	
	/////////////////////////////////////////////////////////////////
	// JOURNAL			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Journal publishing this article */
	public String journal = null;
	
	/////////////////////////////////////////////////////////////////
	// VOLUME			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Volume number of this article */
	public String volume = null;
	
	/////////////////////////////////////////////////////////////////
	// ISSUE			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Issue number of this article */
	public String issue = null;
	
	/////////////////////////////////////////////////////////////////
	// STARTING PAGE	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Starting page of this article */
	public String page = null;
	
	/////////////////////////////////////////////////////////////////
	// YEAR				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Year of publication of this article */
	public String year = null;
	
	/////////////////////////////////////////////////////////////////
	// DOI				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Digital object identifier of this article */
	public String doi = null;
	
	/////////////////////////////////////////////////////////////////
	// CITED ARTICLES	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of cited articles */
	public Set<Article> citedArticles = new TreeSet<Article>();
	
	/////////////////////////////////////////////////////////////////
	// CITING ARTICLES	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of citing articles */
	public Set<Article> citingArticles = new TreeSet<Article>();
	
	/////////////////////////////////////////////////////////////////
	// CHAPTER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Chapter number */
	public String chapter;
	
	/////////////////////////////////////////////////////////////////
	// FILE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the PDF file */
	public String file;
	
	/////////////////////////////////////////////////////////////////
	// URL				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** URL of the article */
	public String url;
	
	/////////////////////////////////////////////////////////////////
	// OWNER			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Owner in Jabref */
	public String owner;
	
	/////////////////////////////////////////////////////////////////
	// TIMESTAMP		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Time at which the entry was created in Jabref */
	public String timestamp;
	
	/////////////////////////////////////////////////////////////////
	// ABSTRACT			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Summary of the article */
	public String abstrct;
	
	/////////////////////////////////////////////////////////////////
	// SERIES			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of a book series */
	public String series;
	
	/////////////////////////////////////////////////////////////////
	// EDITOR			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the editor of a book */
	public String editor;
	
	/////////////////////////////////////////////////////////////////
	// REVIEW			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Custom comments associated to the article */
	public String review;
	
	/////////////////////////////////////////////////////////////////
	// PUBLISHER		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the article publisher */
	public String publisher;
	
	/////////////////////////////////////////////////////////////////
	// BOOK TITLE		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the book containing this article */
	public String booktitle;
	
	/////////////////////////////////////////////////////////////////
	// ADDRESS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Place of a conference or city of a publisher */
	public String address;
	
	/////////////////////////////////////////////////////////////////
	// INSTITUTION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the institution publishing this report */
	public String institution;
	
	/////////////////////////////////////////////////////////////////
	// INSTITUTION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the school publishing this thesis */
	public String school;
	
	/////////////////////////////////////////////////////////////////
	// ORGANIZATION		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the organization publishing this Web page */
	public String organization;
	
	/////////////////////////////////////////////////////////////////
	// TYPE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Type of technical report or thesis */
	public String type;
	
	/////////////////////////////////////////////////////////////////
	// SORTKEY			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Key used to sort with Biblatex */
	public String sortkey;
	
	/////////////////////////////////////////////////////////////////
	// EDITION			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Edition of a book */
	public String edition;
	
	/////////////////////////////////////////////////////////////////
	// GROUPS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Groups to which the paper belongs in Jabref */
	public String groups;
	
	/////////////////////////////////////////////////////////////////
	// ERRORS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
//	/**
//	 * Apply some manual corections to certain articles.
//	 * 
//	 * @param authorsMap
//	 * 		Map containing the known authors.
//	 */
//	public void checkErrors(Map<String,Author> authorsMap)
//	{	if(authors.get(0).getFullname().equals("flake, g")
//			//&& article.source!=null && article.source.equals("ieee comput")
//			&& volume!=null && volume.equals("36")
//			&& page!=null && page.equals("66")
//			&& year!=null && year.equals("2002"))
//		{	volume = "35";
//		}
//	
//		else if(getAuthors().get(0).getFullname().equals("vandongen, s")
//			//&& article.source!=null && article.source.equals("thesis u ultrecht ne")
//			//&& article.page!=null && article.page.equals("371")
//			&& year!=null && year.equals("2000"))
//		{	Author author = getAuthors().get(0);
//			authorsMap.remove(author.getFullname());
//			author = authorsMap.get("van dongen, s");
//			authors.set(0,author);
//		}
//	
//		else if(getAuthors().get(0).getFullname().equals("granovet, m")
//			&& source!=null && source.equals("am j sociol")
//			&& volume!=null && volume.equals("78")
//			&& page!=null && page.equals("1360")
//			&& year!=null && year.equals("1973"))
//		{	Author author = getAuthors().get(0);
//			authorsMap.remove(author.getFullname());
//			author = authorsMap.get("granovetter, m");
//			authors.set(0,author);
//		}
//	
//		else if(getAuthors().get(0).getFullname().equals("leydesdorff, l")
//			&& source!=null && source.equals("j math sociol")
//			&& year!=null && year.equals("1971"))
//		{	Author author = authorsMap.get("lorrain, f");
//			authors.set(0,author);
//			author = authorsMap.get("white, h");
//			authors.add(author);
//			volume = "1";
//			page = "49";
//			year = "1971";
//		}
//	
//		else if(getAuthors().get(0).getFullname().equals("vonmering, c")
//			&& source!=null && source.equals("nature")
//			&& volume!=null && volume.equals("417")
//			&& page!=null && page.equals("399")
//			&& year!=null && year.equals("2002"))
//		{	Author author = getAuthors().get(0);
//		authorsMap.remove(author.getFullname());
//			author = authorsMap.get("von mering, c");
//			authors.set(0,author);
//		}
//	}
	
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
	
//boolean same = false;
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
		if(authors.isEmpty() || authors2.isEmpty())
			result = false;
		else if(authors.size()>1 && authors2.size()>1)
		{	Iterator<Author> it1 = authors.iterator();
			Iterator<Author> it2 = authors2.iterator();
			while(result && it1.hasNext() && it2.hasNext())
			{	Author a1 = it1.next();
				Author a2 = it2.next();
				result = a1.equals(a2);
			}
//			result = result && (it1.hasNext() && it2.hasNext() || !it1.hasNext() && !it2.hasNext());
		}
		else
		{	Author author1 = authors.get(0);
			Author author2 = authors2.get(0);
			result = author1.equals(author2);
		}
//if(same && !result)
//{	for(Author a: authors)
//		System.out.println(a);
//	System.out.println(">> VS <<");
//	for(Author a: article.getAuthors())
//		System.out.println(a);
//	return(result);
//}
		
		// title
		if(result)
		{	String normTitle2 = article.normTitle;
			if(normTitle!=null && normTitle2!=null)
				result = normTitle.equals(normTitle2);
		}
//if(same && !result)
//{	System.out.println(normTitle);
//	System.out.println(">> VS <<");
//	System.out.println(article.normTitle);
//	return(result);
//}
		
		// source
		// not considered because the spelling varies a lot
		if(result)
		{	String normSourceName2 = article.normSourceName;
			if(normSourceName!=null && normSourceName2!=null)
				result = normSourceName.equals(normSourceName2);
		}
		
		// volume
		if(result)
		{	String volume2 = article.volume;
			if(volume!=null && volume2!=null)
				result = volume.equals(volume2);
		}
//if(same && !result)
//{	System.out.println(volume);
//	System.out.println(">> VS <<");
//	System.out.println(article.volume);
//	return(result);
//}
		
		// issue
		if(result)
		{	String issue2 = article.issue;
			if(issue!=null && issue2!=null)
				result = issue.equals(issue2);
		}
//if(same && !result)
//{	System.out.println(issue);
//	System.out.println(">> VS <<");
//	System.out.println(article.issue);
//	return(result);
//}
		
		// page (only the first page)
		if(result)
		{	String page1 = page;
			String page2 = article.page;
			if(page1!=null && page2!=null)
			{	if(page1.contains("-"))
					page1 = page1.split("-")[0];
				if(page2.contains("-"))
					page2 = page2.split("-")[0];
				result = page1.equals(page2);
			}
		}
//if(same && !result)
//{	System.out.println(page);
//	System.out.println(">> VS <<");
//	System.out.println(article.page);
//	return(result);
//}
		
		// year
		if(result)
		{	String year2 = article.year;
			if(year!=null && year2!=null)
				result = year.equals(year2);
		}
//if(same && !result)
//{	System.out.println(year);
//	System.out.println(">> VS <<");
//	System.out.println(article.year);
//	return(result);
//}
		
		return result;
	}

	/**
	 * Completes an existing {@code Article} object
	 * with additional data.
	 * 
	 * @param article
	 * 		The additional data.
	 */
	public void completeWith(Article article)
	{	// abstract
		if(abstrct==null)
		{	String abstrct2 = article.abstrct;
			if(abstrct2!=null)
				abstrct = abstrct2;
		}
		
		// address
		if(address==null)
		{	String address2 = article.address;
			if(address2!=null)
				address = address2;
		}
		
		// authors
		for(Author author: article.getAuthors())
			addAuthor(author);
		
		// booktitle
		if(booktitle==null)
		{	String booktitle2 = article.booktitle;
			if(booktitle2!=null)
				booktitle = booktitle2;
		}
		
		// chapter
		if(chapter==null)
		{	String chapter2 = article.chapter;
			if(chapter2!=null)
				chapter = chapter2;
		}
		
		// cited/citing articles
		Set<Article> citingArticles2 = article.citingArticles;
		citingArticles.addAll(citingArticles2);
		Set<Article> citedArticles2 = article.citedArticles;
		citedArticles.addAll(citedArticles2);
		
		// doi
		if(doi==null)
		{	String doi2 = article.doi;
			if(doi2!=null)
				doi = doi2;
		}
		
		// edition
		if(edition==null)
		{	String edition2 = article.edition;
			if(edition2!=null)
				edition = edition2;
		}
		
		// editor
		if(editor==null)
		{	String editor2 = article.editor;
			if(editor2!=null)
				editor = editor2;
		}
		
		// file
		if(file==null)
		{	String file2 = article.file;
			if(file2!=null)
				file = file2;
		}
		
		// institution
		if(institution==null)
		{	String institution2 = article.institution;
			if(institution2!=null)
				institution = institution2;
		}
		
		// issue
		if(issue==null)
		{	String issue2 = article.issue;
			if(issue2!=null)
				issue = issue2;
		}
		
		// journal
		if(journal==null)
		{	String journal2 = article.journal;
			if(journal2!=null)
				journal = journal2;
		}
		
		// organization
		if(organization==null)
		{	String organization2 = article.organization;
			if(organization2!=null)
				organization = organization2;
		}
		
		// owner
		if(owner==null)
		{	String owner2 = article.owner;
			if(owner2!=null)
				owner = owner2;
		}
		
		// page
		if(page==null)
		{	String page2 = article.page;
			if(page2!=null)
				page = page2;
		}
		
		// publisher
		if(publisher==null)
		{	String publisher2 = article.publisher;
			if(publisher2!=null)
				publisher = publisher2;
		}
		
		// review
		if(review==null)
		{	String review2 = article.review;
			if(review2!=null)
				review = review2;
		}
		
		// school
		if(school==null)
		{	String school2 = article.school;
			if(school2!=null)
				school = school2;
		}
		
		// series
		if(series==null)
		{	String series2 = article.series;
			if(series2!=null)
				series = series2;
		}
		
		// sortkey
		if(sortkey==null)
		{	String sortkey2 = article.sortkey;
			if(sortkey2!=null)
				sortkey = sortkey2;
		}
		
		// source
		if(sourceName==null)
		{	String sourceName2 = article.sourceName;
			if(sourceName2!=null)
			{	sourceName = sourceName2;
				normSourceName = article.normSourceName;
				sourceType = article.sourceType;
			}
		}
		
		// timestamp
		if(timestamp==null)
		{	String timestamp2 = article.timestamp;
			if(timestamp2!=null)
				timestamp = timestamp2;
		}
		
		// title
		if(title==null)
		{	String title2 = article.title;
			if(title2!=null)
			{	title = title2;
				normTitle = article.normTitle;
			}
		}
		
		// type
		if(type==null)
		{	String type2 = article.type;
			if(type2!=null)
				type = type2;
		}
		
		// url
		if(url==null)
		{	String url2 = article.url;
			if(url2!=null)
				url = url2;
		}
		
		// volume
		if(volume==null)
		{	String volume2 = article.volume;
			if(volume2!=null)
				volume = volume2;
		}
		
		// year
		if(year==null)
		{	String year2 = article.year;
			if(year2!=null)
				year = year2;
		}
		
		// groups
		if(groups==null)
		{	String groups2 = article.groups;
			if(groups2!=null)
				groups = groups2;
		}
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
		
		// first author
		if(!authors.isEmpty())
		result = result + authors.get(0).normname;
		// year
		result = result + ", " + year;
		// volume if present
		if(volume!=null)
			result = result + ", V" + volume;
		// page if present
		if(page!=null)
			result = result + ", P" + page;
		// doi if present
		if(doi!=null)
			result = result + ", DOI " + doi;
		return result;
	}
	
	@Override
	public String toString()
	{	String result = "";

		// bibkey
		if(bibtexKey!=null)
			result = result + "[" + bibtexKey + "] ";
		
		// title
		result = result + title;
		result = result + ". ";
		
		// first author
		if(!authors.isEmpty())
			result = result + authors.get(0).getFullname();
		result = result + ". ";
		
		// source
		result = result + sourceType+":"+sourceName;
		result = result + " ";
		
		// volume
		if(volume!=null)
		{	result = result + volume;
			if(issue==null && page!=null)
				result = result + ":";
		}
		
		// issue
		if(issue!=null)
		{	result = result + "(" + issue + ")";
			if(page!=null)
				result = result + ":";
		}
		
		// starting page
		if(page!=null)
			result = result + page;
		
		// year
		if(year!=null)
			result = result + ", "  + year;
		result = result + ". ";
		
		// doi
		if(doi!=null)
			result = result + "DOI: " + doi;
		
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// GRAPH			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Builds a node representing this article, using the specified graph.
	 * 
	 * @param graph
	 * 		The graph which will contain the node.
	 * @return
	 * 		The created node.
	 */
	public Node buildNode(Graph graph)
	{	Node result = graph.retrieveNode(bibtexKey);
		
		if(!authors.isEmpty())
		{	String authorsStr = "";
			for(int i=0;i<authors.size();i++)
			{	Author author = authors.get(i);
				authorsStr = authorsStr + author.lastname + " " + author.firstnameInitials;
				if(i<authors.size()-2)
					authorsStr = authorsStr + ", ";
				else if(i==authors.size()-2)
					authorsStr = authorsStr + " & ";
			}
			result.setProperty(JabrefFileHandler.FLD_AUTHOR, authorsStr);
		}
		
		if(chapter!=null)
			result.setProperty(JabrefFileHandler.FLD_CHAPTER, chapter);
		
		if(doi!=null)
			result.setProperty(JabrefFileHandler.FLD_DOI, doi);
		
		if(journal!=null)
			result.setProperty(JabrefFileHandler.FLD_JOURNAL1, journal);
		
		if(issue!=null)
			result.setProperty(JabrefFileHandler.FLD_NUMBER, issue);
		
		if(page!=null)
			result.setProperty(JabrefFileHandler.FLD_PAGES, page);
		
		if(title!=null)
			result.setProperty(JabrefFileHandler.FLD_TITLE_ARTICLE, title);
		
		if(booktitle!=null)
			result.setProperty(JabrefFileHandler.FLD_TITLE_BOOK, booktitle);
		
		if(url!=null)
			result.setProperty(JabrefFileHandler.FLD_URL, url);
		
		if(volume!=null)
			result.setProperty(JabrefFileHandler.FLD_VOLUME, volume);
		
		if(year!=null)
			result.setProperty(JabrefFileHandler.FLD_YEAR, year);

		return result;
	}
}
