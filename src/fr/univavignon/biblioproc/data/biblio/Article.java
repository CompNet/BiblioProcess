package fr.univavignon.biblioproc.data.biblio;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import fr.univavignon.biblioproc.data.graph.Graph;
import fr.univavignon.biblioproc.data.graph.Node;
import fr.univavignon.biblioproc.inout.JabrefFileHandler;
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
	// CORE				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Indicates whether this article was on the targeted list of article */
	public boolean core = false;
	
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
		sourceName = sourceName.trim();
		this.sourceName = sourceName;
		switch(sourceType)
		{	case BOOK:
			case COLLECTION:
				publisher = sourceName;
				break;
			case CHAPTER:
			case IN_COLLECTION:
			case IN_PROCEEDINGS:
				booktitle = sourceName;
				break;
			case ELECTRONIC:
				organization = sourceName;
				break;
			case ARTICLE:
				journal = sourceName;
				break;
			case TECH_REPORT:
				institution = sourceName;
				break;
			case THESIS_MSC:
			case THESIS_PHD:
				school = sourceName;
				break;
		}
		
		normSourceName = StringTools.normalize(sourceName);
//		if(sourceType==SourceType.IN_PROCEEDINGS)
		{	// remove a possible ending string between parenthesis (typically for conferences)
			int pos = normSourceName.indexOf('(');
			if(pos!=-1)
				normSourceName = normSourceName.substring(0,pos);
			// remove a possible year at the end 
			while(Character.isDigit(sourceName.charAt(sourceName.length()-1)))
				sourceName = sourceName.substring(0,sourceName.length()-1);
			// remove a possible number at the begining
			if(normSourceName.substring(0,1).matches("[0-9]"))
			{	pos = normSourceName.indexOf(" ");
				if(pos<0)
					throw new IllegalArgumentException("Problem with conference name "+sourceName+" ("+normSourceName+")");
				else
					normSourceName = normSourceName.substring(pos+1);
			}
			// remove possible association acronym at the beginning
			for(String acro: Arrays.asList("ieee","wic","acm","siam"))
			{	if(normSourceName.startsWith(acro))
					normSourceName = normSourceName.substring(acro.length()+1);
			}
		}
		normSourceName = normSourceName.replaceAll("[^a-zA-Z0-9]", "");
		normSourceName = normSourceName.trim();
if(normSourceName.equals("ieeeinternationalconferenceonmultimediaandexpo"))
	System.out.print("");
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
	/** List of articles cited by this article */
	public Set<Article> citedArticles = new TreeSet<Article>();
	
	/////////////////////////////////////////////////////////////////
	// CITING ARTICLES	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** List of articles citing this article */
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
	// SCHOOL			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Name of the school publishing this thesis */
	public String school;
	
	/////////////////////////////////////////////////////////////////
	// MONTH			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Month of publication of the Web page */
	public String month;

	/////////////////////////////////////////////////////////////////
	// MONTH			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** How the Web page was published */
	public String howpublished;
	
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
	// COMPARISON		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public int compareTo(Article article)
	{	
//		String citeAs1 = getCiteAs();
//		String citeAs2 = article.getCiteAs();
//		int result = citeAs1.compareTo(citeAs2);
		String bibtexKey2 = article.bibtexKey;
		int result = bibtexKey.compareTo(bibtexKey2);
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
	 * forms. The idea is to compare only fields existing in
	 * both articles.
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
		if(this.abstrct==null)
		{	String abstrct = article.abstrct;
			if(abstrct!=null)
				this.abstrct = abstrct;
		}
		
		// address
		if(this.address==null)
		{	String address = article.address;
			if(address!=null)
				this.address = address;
		}
		
		// authors
		for(Author author: article.getAuthors())
			addAuthor(author);
		
		// booktitle
		if(this.booktitle==null)
		{	String booktitle = article.booktitle;
			if(booktitle!=null)
				this.booktitle = booktitle;
		}
		
		// chapter
		if(this.chapter==null)
		{	String chapter = article.chapter;
			if(chapter!=null)
				this.chapter = chapter;
		}
		
		// cited/citing articles
		Set<Article> citingArticles = article.citingArticles;
		this.citingArticles.addAll(citingArticles);
		Set<Article> citedArticles = article.citedArticles;
		this.citedArticles.addAll(citedArticles);
		
		// doi
		if(this.doi==null)
		{	String doi = article.doi;
			if(doi!=null)
				this.doi = doi;
		}
		
		// edition
		if(this.edition==null)
		{	String edition = article.edition;
			if(edition!=null)
				this.edition = edition;
		}
		
		// editor
		if(this.editor==null)
		{	String editor = article.editor;
			if(editor!=null)
				this.editor = editor;
		}
		
		// file
		if(this.file==null)
		{	String file = article.file;
			if(file!=null)
				this.file = file;
		}
		
		// institution
		if(this.institution==null)
		{	String institution = article.institution;
			if(institution!=null)
				this.institution = institution;
		}
		
		// issue
		if(this.issue==null)
		{	String issue = article.issue;
			if(issue!=null)
				this.issue = issue;
		}
		
		// journal
		if(this.journal==null)
		{	String journal = article.journal;
			if(journal!=null)
				this.journal = journal;
		}
		
		// month
		if(this.month==null)
		{	String month = article.month;
			if(month!=null)
				this.month = month;
		}
		
		// organization
		if(this.organization==null)
		{	String organization = article.organization;
			if(organization!=null)
				this.organization = organization;
		}
		
		// howpublished
		if(this.howpublished==null)
		{	String howpublished = article.howpublished;
			if(howpublished!=null)
				this.howpublished = howpublished;
		}
		
		// owner
		if(this.owner==null)
		{	String owner = article.owner;
			if(owner!=null)
				this.owner = owner;
		}
		
		// page
		if(this.page==null)
		{	String page = article.page;
			if(page!=null)
				this.page = page;
		}
		
		// publisher
		if(this.publisher==null)
		{	String publisher = article.publisher;
			if(publisher!=null)
				this.publisher = publisher;
		}
		
		// review
		if(this.review==null)
		{	String review = article.review;
			if(review!=null)
				this.review = review;
		}
		
		// school
		if(this.school==null)
		{	String school = article.school;
			if(school!=null)
				this.school = school;
		}
		
		// series
		if(this.series==null)
		{	String series = article.series;
			if(series!=null)
				this.series = series;
		}
		
		// sortkey
		if(this.sortkey==null)
		{	String sortkey = article.sortkey;
			if(sortkey!=null)
				this.sortkey = sortkey;
		}
		
		// source
		if(this.sourceName==null)
		{	String sourceName = article.sourceName;
			if(sourceName!=null)
			{	this.sourceName = sourceName;
				this.normSourceName = article.normSourceName;
				this.sourceType = article.sourceType;
			}
		}
		
		// timestamp
		if(timestamp==null)
		{	String timestamp = article.timestamp;
			if(timestamp!=null)
				this.timestamp = timestamp;
		}
		
		// title
		if(this.title==null)
		{	String title = article.title;
			if(title!=null)
			{	this.title = title;
				this.normTitle = article.normTitle;
			}
		}
		
		// type
		if(this.type==null)
		{	String type = article.type;
			if(type!=null)
				this.type = type;
		}
		
		// url
		if(this.url==null)
		{	String url = article.url;
			if(url!=null)
				this.url = url;
		}
		
		// volume
		if(this.volume==null)
		{	String volume = article.volume;
			if(volume!=null)
				this.volume = volume;
		}
		
		// year
		if(this.year==null)
		{	String year = article.year;
			if(year!=null)
				this.year = year;
		}
		
		// groups
		if(this.groups==null)
		{	String groups = article.groups;
			if(groups!=null)
				this.groups = groups;
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
	/** Name of the core property */
	public final static String PROP_CORE = "core";
	
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
		result.setProperty(PROP_CORE, Boolean.toString(core));
		
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
		
		//TODO add stat-related fields?
		
		return result;
	}
}
