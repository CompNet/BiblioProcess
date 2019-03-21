package fr.univavignon.biblioproc;

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

import java.io.File;

import fr.univavignon.biblioproc.data.biblio.Article;
import fr.univavignon.biblioproc.data.biblio.Author;
import fr.univavignon.biblioproc.data.biblio.Corpus;
import fr.univavignon.biblioproc.data.biblio.SourceType;
import fr.univavignon.biblioproc.data.graph.Graph;
import fr.univavignon.biblioproc.inout.IsiFileHandler;
import fr.univavignon.biblioproc.inout.JabrefFileHandler;
import fr.univavignon.biblioproc.tools.file.FileNames;
import fr.univavignon.biblioproc.tools.log.HierarchicalLogger;
import fr.univavignon.biblioproc.tools.log.HierarchicalLoggerManager;

/**
 * Main class, allowing to launch the whole process.
 *  
 * @author Vincent Labatut
 */
public class Launcher
{	
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private static HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/**
	 * Builds a fake corpus, for testing purposes.
	 * 
	 * @return
	 * 		A fake corpus.
	 */
	public static Corpus buildFakeCorpus()
	{	Corpus result = new Corpus();
		
		// build a few fake authors
		Author author1 = new Author("Lastname1","A. B.");
		author1 = result.retrieveAuthor(author1);
		Author author2 = new Author("Lastname2","A. B.");
		author2 = result.retrieveAuthor(author2);
		Author author3 = new Author("Lastname3","A. B.");
		author3 = result.retrieveAuthor(author3);
		Author author4 = new Author("Lastname4","A. B.");
		author4 = result.retrieveAuthor(author4);
		Author author5 = new Author("Lastname5","A. B.");
		author5 = result.retrieveAuthor(author5);
		
		// build a few fake articles
		Article article1 = new Article();
		article1.bibtexKey = "Art1";
		article1.setSource(SourceType.ARTICLE, "Journal1");
		article1.setTitle("Article 1");
		article1.addAuthor(author1);
		article1.addAuthor(author2);
		result.addArticle(article1);
		Article article2 = new Article();
		article2.bibtexKey = "Art2";
		article2.setSource(SourceType.ARTICLE, "Journal1");
		article2.setTitle("Article 2");
		article2.addAuthor(author1);
		result.addArticle(article2);
		Article article3 = new Article();
		article3.bibtexKey = "Art3";
		article3.setSource(SourceType.ARTICLE, "Journal2");
		article3.setTitle("Article 3");
		article3.addAuthor(author2);
		article3.addAuthor(author3);
		article3.addAuthor(author4);
		result.addArticle(article3);
		Article article4 = new Article();
		article4.bibtexKey = "Art4";
		article4.setSource(SourceType.ARTICLE, "Journal3");
		article4.setTitle("Article 4");
		article4.addAuthor(author1);
		article4.addAuthor(author2);
		article4.addAuthor(author4);
		article4.addAuthor(author5);
		result.addArticle(article4);
		
		// connect the articles
		{	article2.citedArticles.add(article1);
			article1.citingArticles.add(article2);
		}
		{	article3.citedArticles.add(article1);
			article1.citingArticles.add(article3);
		}
		{	article4.citedArticles.add(article1);
			article1.citingArticles.add(article4);
			article4.citedArticles.add(article2);
			article2.citingArticles.add(article4);
		}
		
		return result;
	}
	
	/**
	 * xxx
	 * 
	 * @param args
	 * 		Not used.
	 * 
	 * @throws Exception
	 * 		Whatever exception occurred.
	 */
	public static void main(String[] args) throws Exception
	{	logger.log("Starting the process");
		logger.increaseOffset();
		
		// first load the jabref file
		JabrefFileHandler jfh = new JabrefFileHandler();
//		String path = FileNames.FI_BIBTEX_STRUCT_BAL;
		String path = FileNames.FI_BIBTEX_CHARNETS;
		boolean updateGroups = false;
		jfh.loadJabRefFile(path, updateGroups);
		
		// then the ISI file
		IsiFileHandler ifh = new IsiFileHandler(jfh.corpus);
		path = FileNames.FI_ISI_ALL_SIGNETS;
		ifh.loadIsiFile(path);
		Corpus corpus = ifh.corpus;
		
//		Corpus corpus = buildFakeCorpus();
		
//		// extract and record the networks
//		{	// authorship graph
//			logger.log("Extracting authorship graph");
//			Graph authorshipGraph = corpus.buildAuthorshipGraph();
//			File authorshipFile = new File(FileNames.FO_OUTPUT+File.separator+"authorship.graphml");
//			authorshipGraph.writeToXml(authorshipFile);
//		}
//		{	// article citation graph
//			logger.log("Extracting article citation graph");
//			Graph articleCitationGraph = corpus.buildArticleCitationGraph();
//			File articleCitationFile = new File(FileNames.FO_OUTPUT+File.separator+"article_citation.graphml");
//			articleCitationGraph.writeToXml(articleCitationFile);
//		}
//		{	// author citation graph
//			logger.log("Extracting author citation graph");
//			Graph authorCitationGraph = corpus.buildAuthorCitationGraph();
//			File authorCitationFile = new File(FileNames.FO_OUTPUT+File.separator+"author_citation.graphml");
//			authorCitationGraph.writeToXml(authorCitationFile);
//		}
//		{	// article coauthorship graph
//			logger.log("Extracting article coauthorship graph");
//			Graph articleCoauthorshipGraph = corpus.buildArticleCoauthorshipGraph();
//			File articleCoauthorshipFile = new File(FileNames.FO_OUTPUT+File.separator+"article_coauthorship.graphml");
//			articleCoauthorshipGraph.writeToXml(articleCoauthorshipFile);
//		}
//		{	// author coauthorship graph
//			logger.log("Extracting author coauthorship graph");
//			Graph authorCoauthorshipGraph = corpus.buildAuthorCoauthorshipGraph();
//			File authorCoauthorshipFile = new File(FileNames.FO_OUTPUT+File.separator+"author_coauthorship.graphml");
//			authorCoauthorshipGraph.writeToXml(authorCoauthorshipFile);
//		}
//		{	// article cociting graph
//			logger.log("Extracting article cociting graph");
//			Graph articleCocitingGraph = corpus.buildArticleCocitingGraph();
//			File articleCocitingFile = new File(FileNames.FO_OUTPUT+File.separator+"article_cociting.graphml");
//			articleCocitingGraph.writeToXml(articleCocitingFile);
//		}
//		{	// article cocited graph
//			logger.log("Extracting article cocited graph");
//			Graph articleCocitedGraph = corpus.buildArticleCocitedGraph();
//			File articleCocitedFile = new File(FileNames.FO_OUTPUT+File.separator+"article_cocited.graphml");
//			articleCocitedGraph.writeToXml(articleCocitedFile);
//		}
		
		logger.decreaseOffset();
		logger.log("All done");
	}
}

/**TODO
 * - check graph extraction methods on a simple (programmatic) example
 * - extract the full authorship bipartite network
 * 
 * 
 * - generate a folder containing all the PDF files of the articles listed in the bibtex file
 * - generate the list of articles present as PDF files but missing from the bibtex file
 */

/**
 * Update procedure when adding a field:
 * 
 * - JabrefFileHandler
 *   - Field
 *   - ALL_FIELDS
 *   - buildArticle
 *   - writeArticle
 * - Article
 *   - Field
 *   - completeWith
 */
