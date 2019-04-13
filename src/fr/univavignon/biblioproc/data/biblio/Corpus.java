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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import fr.univavignon.biblioproc.data.graph.Graph;
import fr.univavignon.biblioproc.data.graph.Link;
import fr.univavignon.biblioproc.data.graph.Node;
import fr.univavignon.biblioproc.inout.JabrefFileHandler;

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
	public Article getArticleByBibkey(String bibkey)
	{	Article result = articlesMap.get(bibkey);
		return result;
	}
	
	/**
	 * Returns the article corresponding to the specified DOI,
	 * or {@code null} if no such article currently exists.
	 * 
	 * @param doi
	 * 		DOI of the article.
	 * @return
	 * 		The article possessing the specified DOI.
	 */
	public Article getArticleByDoi(String doi)
	{	Article result = null;
		Iterator<Article> it = getArticles().iterator();
		while(it.hasNext() && result==null)
		{	Article article = it.next();
			if(article.doi!=null && article.doi.equalsIgnoreCase(doi))
				result = article;
		}
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
	/** Counter used to number the authors */
	private int authorCounter = 0;
	
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
		{	authorsMap.put(author.normname, result);
			author.authorId = authorCounter;
			authorCounter++;
		}
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
	
	/**
	 * Return a collection containing all the authors from this
	 * corpus.
	 * 
	 * @return
	 * 		The authors of this corpus.
	 */
	public Collection<Author> getAuthors()
	{	Collection<Author> result = authorsMap.values();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// GRAPH			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Property name for the labels of the core nodes */
	public static final String PROP_CORE_LABEL = "core_label";
	/** Name of the link weight property */
	private final static String PROP_WEIGHT = "weight";
	/** Name of the alt link weight property */
	private final static String PROP_COUNT = "count";
	/** Name of the node type (article vs. author) property */
	public final static String PROP_TYPE = "type";
	
	/**
	 * Builds a bipartite citation network of articles and authors. 
	 * Each node is an article or an author, and each link is undirected 
	 * and connects one author to an article (s)he wrote, and is unweighted.
	 * 
	 * @return
	 * 		A bipartite citation graph. 
	 */
	public Graph buildAuthorshipGraph()
	{	Graph result;
		
		// create the graph
		String title = "Authorship network";
		result = new Graph(title, false);
		result.addNodeProperty(PROP_TYPE, "string");
		result.addNodeProperty(Author.PROP_FULLNAME, "string");
		result.addNodeProperty(Article.PROP_CORE, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_AUTHOR, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_CHAPTER, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_DOI, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_JOURNAL1, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_NUMBER, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_AUTHOR, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_PAGES, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_TITLE_ARTICLE, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_TITLE_BOOK, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_URL, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_VOLUME, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_YEAR, "string");
		result.addNodeProperty(PROP_CORE_LABEL, "string");

		// add the nodes
		for(Article article: getArticles())
		{	Node node = article.buildNode(result);
			node.setProperty(PROP_TYPE, "Article");
			if(article.core)
				node.setProperty(Corpus.PROP_CORE_LABEL, article.bibtexKey);
			else
				node.setProperty(Corpus.PROP_CORE_LABEL, "");
		}
		Map<Author,Node> nodeMap = new HashMap<Author,Node>();
		for(Author author: getAuthors())
		{	Node node = author.buildNode(result);
			node.setProperty(PROP_TYPE, "Author");
			node.setProperty(Corpus.PROP_CORE_LABEL, "");
			nodeMap.put(author, node);
		}
		
		// add the links
		for(Article article: getArticles())
		{	String sourceName = article.bibtexKey;
			for(Author author: article.getAuthors())
			{	Node targetNode = nodeMap.get(author);
				String targetName = targetNode.getName();
				result.retrieveLink(sourceName, targetName);
			}
		}
		
		return result;
	}

	/**
	 * Builds a citation network of articles. Each node is an article, 
	 * each link is directed from the cited to the citing article, and 
	 * is unweighted.
	 * 
	 * @return
	 * 		An article citation graph. 
	 */
	public Graph buildArticleCitationGraph()
	{	Graph result;
		
		// create the graph
		String title = "Article citation network";
		result = new Graph(title, true);
		result.addNodeProperty(Article.PROP_CORE, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_AUTHOR, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_CHAPTER, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_DOI, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_JOURNAL1, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_NUMBER, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_AUTHOR, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_PAGES, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_TITLE_ARTICLE, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_TITLE_BOOK, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_URL, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_VOLUME, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_YEAR, "string");
		result.addNodeProperty(PROP_CORE_LABEL, "string");
		result.addLinkProperty(PROP_WEIGHT, "int");

		// add the nodes
		for(Article article: getArticles())
		{	Node node = article.buildNode(result);
			if(article.core)
				node.setProperty(Corpus.PROP_CORE_LABEL, article.bibtexKey);
			else
				node.setProperty(Corpus.PROP_CORE_LABEL, "");
		}
		
		// add the links
		for(Article artSrc: getArticles())
		{	String sourceName = artSrc.bibtexKey;
			for(Article artTarg: artSrc.citingArticles)
			{	String targetName = artTarg.bibtexKey;
				Link link = result.retrieveLink(sourceName, targetName);
				link.incrementIntProperty(PROP_WEIGHT);
			}
		}
		
		return result;
	}

	/**
	 * Builds a citation network of authors. Each node is an author, 
	 * each link is directed from the cited to the citing author,
	 * and its weight represents the number of citations (i.e. distinct articles).
	 * 
	 * @return
	 * 		An author citation graph. 
	 */
	public Graph buildAuthorCitationGraph()
	{	Graph result;
		
		// create the graph
		String title = "Author citation network";
		result = new Graph(title, true);
		result.addNodeProperty(Author.PROP_FULLNAME, "string");
		result.addLinkProperty(PROP_WEIGHT, "int");

		// add the nodes
		Map<Author,Node> nodeMap = new HashMap<Author,Node>();
		for(Author author: getAuthors())
		{	Node node = author.buildNode(result);
			nodeMap.put(author, node);
		}
		
		// add the links
		for(Article artSrc: getArticles())
		{	List<Author> authorsSrc = artSrc.getAuthors();
			for(Article artTarg: artSrc.citingArticles)
			{	List<Author> authorsTarg = artTarg.getAuthors();
				for(Author authorSrc: authorsSrc)
				{	Node nodeSrc = nodeMap.get(authorSrc);
					for(Author authorTarg: authorsTarg)
					{	Node nodeTarg = nodeMap.get(authorTarg);
						Link link = result.retrieveLink(nodeSrc, nodeTarg);
						link.incrementIntProperty(PROP_WEIGHT);
					}
				}
			}
		}
		
		return result;
	}

	/**
	 * Builds a coauthoring network of articles. Each node is an article, 
	 * each link is undirected and connects two articles having at least
	 * one author in common, and its weight represents Jaccard's coefficient 
	 * processed over the two concerned groups of authors. An additional
	 * integer link attribute corresponds to the number of common authors.
	 * 
	 * @return
	 * 		An article coauthorship graph. 
	 */
	public Graph buildArticleCoauthorshipGraph()
	{	Graph result;
		
		// create the graph
		String title = "Article coauthorship network";
		result = new Graph(title, false);
		result.addNodeProperty(Article.PROP_CORE, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_AUTHOR, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_CHAPTER, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_DOI, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_JOURNAL1, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_NUMBER, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_AUTHOR, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_PAGES, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_TITLE_ARTICLE, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_TITLE_BOOK, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_URL, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_VOLUME, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_YEAR, "string");
		result.addNodeProperty(PROP_CORE_LABEL, "string");
		result.addLinkProperty(PROP_WEIGHT, "float");
		result.addLinkProperty(PROP_COUNT, "int");

		// add the nodes
		for(Article article: getArticles())
		{	Node node = article.buildNode(result);
			if(article.core)
				node.setProperty(Corpus.PROP_CORE_LABEL, article.bibtexKey);
			else
				node.setProperty(Corpus.PROP_CORE_LABEL, "");
		}
		
		// add the links
		List<Article> articles = new ArrayList<Article>(getArticles());
		for(int i=0;i<articles.size()-1;i++)
		{	Article article1 = articles.get(i);
			String name1 = article1.bibtexKey;
			List<Author> authors1 = article1.getAuthors();
			for(int j=i+1;j<articles.size();j++)
			{	Article article2 = articles.get(j);
				String name2 = article2.bibtexKey;
				List<Author> authors2 = article2.getAuthors();
				Set<Author> intersection = new TreeSet<Author>(authors1);
				intersection.retainAll(authors2);
				if(!intersection.isEmpty())
				{	Set<Author> union = new TreeSet<Author>(authors1);
					union.addAll(authors2);
					float weight = intersection.size() / (float)union.size();
					Link link = result.retrieveLink(name1, name2);
					link.incrementFloatProperty(PROP_WEIGHT,weight);
					link.incrementIntProperty(PROP_COUNT,intersection.size());
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Builds a coauthoring network of authors. Each node is an author, 
	 * each link is undirected and connects two authors having published
	 * an article together, and its weight represents the number of 
	 * co-authored articles.
	 * 
	 * @return
	 * 		An author coauthorship graph. 
	 */
	public Graph buildAuthorCoauthorshipGraph()
	{	Graph result;
	
		// create the graph
		String title = "Author coauthorship network";
		result = new Graph(title, false);
		result.addNodeProperty(Author.PROP_FULLNAME, "string");
		result.addLinkProperty(PROP_WEIGHT, "int");
	
		// add the nodes
		Map<Author,Node> nodeMap = new HashMap<Author,Node>();
		for(Author author: getAuthors())
		{	Node node = author.buildNode(result);
			nodeMap.put(author, node);
		}
		
		// add the links
		for(Article article: getArticles())
		{	List<Author> authors = article.getAuthors();
			for(int i=0;i<authors.size()-1;i++)
			{	Author author1 = authors.get(i);
				Node node1 = nodeMap.get(author1);
				for(int j=i+1;j<authors.size();j++)
				{	Author author2 = authors.get(j);
					Node node2 = nodeMap.get(author2);
					Link link = result.retrieveLink(node1, node2);
					link.incrementIntProperty(PROP_WEIGHT);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Builds a cociting network of articles. Each node is an article, 
	 * each link is undirected and connects two articles citing the same
	 * reference, and its weight represents Jaccard's coefficient 
	 * processed over the two concerned groups of references. An additional
	 * integer link attribute corresponds to the number of common references.
	 * 
	 * @return
	 * 		An article coauthorship graph. 
	 */
	public Graph buildArticleCocitingGraph()
	{	Graph result;
		
		// create the graph
		String title = "Article cociting network";
		result = new Graph(title, false);
		result.addNodeProperty(Article.PROP_CORE, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_AUTHOR, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_CHAPTER, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_DOI, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_JOURNAL1, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_NUMBER, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_AUTHOR, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_PAGES, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_TITLE_ARTICLE, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_TITLE_BOOK, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_URL, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_VOLUME, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_YEAR, "string");
		result.addNodeProperty(PROP_CORE_LABEL, "string");
		result.addLinkProperty(PROP_WEIGHT, "float");
		result.addLinkProperty(PROP_COUNT, "int");

		// add the nodes
		for(Article article: getArticles())
		{	Node node = article.buildNode(result);
			if(article.core)
				node.setProperty(Corpus.PROP_CORE_LABEL, article.bibtexKey);
			else
				node.setProperty(Corpus.PROP_CORE_LABEL, "");
		}
		
		// add the links
		List<Article> articles = new ArrayList<Article>(getArticles());
		for(int i=0;i<articles.size()-1;i++)
		{	Article article1 = articles.get(i);
			String name1 = article1.bibtexKey;
			Set<Article> cited1 = article1.citedArticles;
			for(int j=i+1;j<articles.size();j++)
			{	Article article2 = articles.get(j);
				String name2 = article2.bibtexKey;
				Set<Article> cited2 = article2.citedArticles;
				Set<Article> intersection = new TreeSet<Article>(cited1);
				intersection.retainAll(cited2);
				if(!intersection.isEmpty())
				{	Set<Article> union = new TreeSet<Article>(cited1);
					union.addAll(cited2);
					float weight = intersection.size() / (float)union.size();
					Link link = result.retrieveLink(name1, name2);
					link.incrementFloatProperty(PROP_WEIGHT,weight);
					link.incrementIntProperty(PROP_COUNT,intersection.size());
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Builds a cocited network of articles. Each node is an article, 
	 * each link is undirected and connects two articles cited by the same
	 * reference, and its weight represents Jaccard's coefficient 
	 * processed over the two concerned groups of references. An additional
	 * integer link attribute corresponds to the number of common references.
	 * 
	 * @return
	 * 		An article coauthorship graph. 
	 */
	public Graph buildArticleCocitedGraph()
	{	Graph result;
		
		// create the graph
		String title = "Article cocited network";
		result = new Graph(title, false);
		result.addNodeProperty(Article.PROP_CORE, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_AUTHOR, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_CHAPTER, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_DOI, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_JOURNAL1, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_NUMBER, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_AUTHOR, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_PAGES, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_TITLE_ARTICLE, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_TITLE_BOOK, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_URL, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_VOLUME, "string");
		result.addNodeProperty(JabrefFileHandler.FLD_YEAR, "string");
		result.addNodeProperty(PROP_CORE_LABEL, "string");
		result.addLinkProperty(PROP_WEIGHT, "float");
		result.addLinkProperty(PROP_COUNT, "int");

		// add the nodes
		for(Article article: getArticles())
		{	Node node = article.buildNode(result);
			if(article.core)
				node.setProperty(Corpus.PROP_CORE_LABEL, article.bibtexKey);
			else
				node.setProperty(Corpus.PROP_CORE_LABEL, "");
		}
		
		// add the links
		List<Article> articles = new ArrayList<Article>(getArticles());
		for(int i=0;i<articles.size()-1;i++)
		{	Article article1 = articles.get(i);
			String name1 = article1.bibtexKey;
			Set<Article> citing1 = article1.citingArticles;
			for(int j=i+1;j<articles.size();j++)
			{	Article article2 = articles.get(j);
				String name2 = article2.bibtexKey;
				Set<Article> citing2 = article2.citingArticles;
				Set<Article> intersection = new TreeSet<Article>(citing1);
				intersection.retainAll(citing2);
				if(!intersection.isEmpty())
				{	Set<Article> union = new TreeSet<Article>(citing1);
					union.addAll(citing2);
					float weight = intersection.size() / (float)union.size();
					Link link = result.retrieveLink(name1, name2);
					link.incrementFloatProperty(PROP_WEIGHT,weight);
					link.incrementIntProperty(PROP_COUNT,intersection.size());
				}
			}
		}
		
		return result;
	}
}
