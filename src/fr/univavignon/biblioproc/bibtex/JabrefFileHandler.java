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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import fr.univavignon.biblioproc.data.Article;
import fr.univavignon.biblioproc.data.Author;
import fr.univavignon.biblioproc.data.SourceType;
import fr.univavignon.biblioproc.tools.file.FileNames;
import fr.univavignon.biblioproc.tools.file.FileTools;
import fr.univavignon.biblioproc.tools.log.HierarchicalLogger;
import fr.univavignon.biblioproc.tools.log.HierarchicalLoggerManager;

/**
 * Class dedicated to Jabref I/Os.
 * 
 * @author Vincent Labatut
 */
public class JabrefFileHandler
{
	/////////////////////////////////////////////////////////////////
	// LOGGER		/////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Common object used for logging */
	private HierarchicalLogger logger = HierarchicalLoggerManager.getHierarchicalLogger();
	
	/////////////////////////////////////////////////////////////////
	// BIBTEX MARKERS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String marking the end of a BibTex field */
	private static final String FIELD_END = "},";
	/** String marking the end of a BibTex entry */
	private static final String ENTRY_END = "}";
	
	/////////////////////////////////////////////////////////////////
	// JABREF MARKERS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String marking the end of the actual BibTex file (and the begining of the JabRef part) */
	private static final String COMMENT_PREFIX = "@Comment";
	/** String marking the begining of the list of ignored articles */
	private static final String IGNORED_PREFIX = "3 ExplicitGroup:Ignored\\;2\\;";
	/** String marking the begining of the list of pureley applicative articles */
	private static final String APPLICATION_PREFIX = "3 ExplicitGroup:Applications Only\\;2\\;";
	/** String marking the end of a JabRef group */
	private static final String GROUP_END = ";;";
	/** String separating the BibTex kes in a JabRef group */
	private static final String KEY_SEPARATOR = "\\;";
	
	/////////////////////////////////////////////////////////////////
	// BIBTEX FIELDS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Type of Bibtex entry */
	private static final String FLD_SOURCE = "source";
	/** Bibtex key for the article key */
	private static final String FLD_KEY = "bibtexkey";
	/** Bibtex key for the authors */
	private static final String FLD_AUTHOR = "author";
	/** Bibtex key for the abstract */
	private static final String FLD_ABSTRACT = "abstract";
	/** Bibtex key for the chapter */
	private static final String FLD_CHAPTER = "chapter";
	/** Bibtex key for the DOI */
	private static final String FLD_DOI = "doi";
	/** Bibtex key for the file */
	private static final String FLD_FILE = "file";
	/** Bibtex key for the institution */
	private static final String FLD_INSTITUTION = "institution";
	/** Bibtex key for the school */
	private static final String FLD_SCHOOL = "school";
	/** Bibtex key for the issue */
	private static final String FLD_ISSUE = "issue";
	/** Traditional Bibtex key for the journal */
	private static final String FLD_JOURNAL1 = "journal";
	/** Alterantive Bibtex key for the journal */
	private static final String FLD_JOURNAL2 = "journaltitle";
	/** Bibtex key for the number */
	private static final String FLD_NUMBER = "number";
	/** Bibtex key for the owner */
	private static final String FLD_OWNER = "owner";
	/** Bibtex key for the pages */
	private static final String FLD_PAGES = "pages";
	/** Bibtex key for the time stamp */
	private static final String FLD_TIMESTAMP = "timestamp";
	/** Bibtex key for the article title */
	private static final String FLD_TITLE_ARTICLE = "title";
	/** Bibtex key for the book title */
	private static final String FLD_TITLE_BOOK = "booktitle";
	/** Bibtex key for the URL */
	private static final String FLD_URL = "url";
	/** Bibtex key for the volume */
	private static final String FLD_VOLUME = "volume";
	/** Bibtex key for the publication year */
	private static final String FLD_YEAR = "year";
	/** Bibtex key for the publisher of a book */
	private static final String FLD_PUBLISHER = "publisher";
	/** Bibtex key for the series title of a book */
	private static final String FLD_SERIES = "series";
	/** Bibtex key for the editors of a book */
	private static final String FLD_EDITOR = "editor";
	/** Bibtex key for the comments associated to an article */
	private static final String FLD_REVIEW = "review";
	/** Bibtex key for the place of a conference/publisher */
	private static final String FLD_ADDRESS = "address";
	/** Bibtex key for the type of report/thesis */
	private static final String FLD_TYPE = "type";
	/** Bibtex key used to sort entries */
	private static final String FLD_SORTKEY = "sortkey";
	/** Bibtex key for book edition */
	private static final String FLD_EDITION = "edition";
	/** Organization associated to an electronic reference */
	private static final String FLD_ORGANIZATION = "organization";	
	/** Internal Jabref field */
	private static final String FLD_MARKED = "__markedentry";	
	/** List of all known Bibtex fields */
	private static final List<String> ALL_FIELDS = Arrays.asList(
			FLD_SOURCE, FLD_KEY, FLD_AUTHOR, FLD_ABSTRACT, FLD_CHAPTER,
			FLD_DOI, FLD_FILE, FLD_INSTITUTION, FLD_ISSUE, FLD_JOURNAL1,
			FLD_JOURNAL2, FLD_NUMBER, FLD_OWNER, FLD_PAGES, FLD_TIMESTAMP,
			FLD_TITLE_ARTICLE, FLD_TITLE_BOOK, FLD_TITLE_BOOK, FLD_URL,
			FLD_VOLUME, FLD_YEAR, FLD_PUBLISHER, FLD_SERIES, FLD_EDITOR, 
			FLD_REVIEW, FLD_ADDRESS, FLD_SCHOOL, FLD_TYPE, FLD_SORTKEY,
			FLD_EDITION, FLD_ORGANIZATION,
			// ignored:
			FLD_MARKED
	);
	
	/////////////////////////////////////////////////////////////////
	// BIBTEX FIELDS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Bibtex type of entry for a whole book */
	private static final String TYPE_BOOK = "Book";
	/** Bibtex type of entry for a book chapter */
	private static final String TYPE_CHAPTER = "InCollection";
	/** Bibtex type of entry for a conference article */
	private static final String TYPE_CONFERENCE = "InProceedings";
	/** Bibtex type of entry for an electronic resource */
	private static final String TYPE_ELECTRONIC = "Electronic";
	/** Bibtex type of entry for a journal article */
	private static final String TYPE_ARTICLE = "Article";
	/** Bibtex type of entry for a report */
	private static final String TYPE_REPORT = "TechReport";
	/** Bibtex type of entry for a MSc thesis */
	private static final String TYPE_THESIS_MSC = "MastersThesis";
	/** Bibtex type of entry for a PhD thesis */
	private static final String TYPE_THESIS_PHD = "PhdThesis";
	
	/////////////////////////////////////////////////////////////////
	// DATA				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Map containing all the loaded articles, indexed by their Bibtex id */
	public Map<String, Article> articlesMap = new HashMap<String, Article>();
	/** Map containing all the loaded authors, index by their normalized name */
	public Map<String, Author> authorsMap = new HashMap<String, Author>();
	
	/////////////////////////////////////////////////////////////////
	// LOADING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Loads the specified Jabref file, and builds the corresponding 
	 * maps of articles and authors.
	 * 
	 * @param path
	 * 		Jabref file.
	 * @param updateGroups
	 * 		Whether or not to take into account Jabref groups.
	 * 
	 * @throws FileNotFoundException
	 * 		Problem while accessing the Jabref file.
	 * @throws UnsupportedEncodingException 
	 * 		Problem while accessing the Jabref file.
	 */
	public void loadJabRefFile(String path, boolean updateGroups) throws FileNotFoundException, UnsupportedEncodingException
	{	logger.log("Start loading JabRef file " + path);
		logger.increaseOffset();
		
		// open the JabRef file
		logger.log("Open the JabRef file");
		Scanner jrScanner = FileTools.openTextFileRead(path,null);
		
		// retrieve the data
		logger.log("Skip the beginning of the file");
		String line = null;
		// skip jabref comments
		do
			line = jrScanner.nextLine();
		while(!line.isEmpty());

		// retrieve the articles
		logger.log("Retrieve the articles");
		logger.increaseOffset();
		int count = 0;
		do
		{	line = jrScanner.nextLine();
			if(!line.isEmpty() && !line.startsWith(COMMENT_PREFIX))
			{	count++;
				logger.log("Process article #"+count);
				// parse the BibTex entry
				Map<String,String> data = retrieveArticleMap(line, jrScanner);
				// build the article object (automatic insertion in the maps)
				Article article = buildArticle(data);
				// display for verification
				logger.log("Resulting article: " + article);
			}
		}
		while(!line.startsWith(COMMENT_PREFIX));
		logger.log("Number of article retrieved from the file: "+count);
		logger.decreaseOffset();
	
		// update with the list of purely applicative articles
		if(updateGroups)
		{	logger.log("Mark the articles belonging to the \"applications\" Jabref group");
			logger.increaseOffset();
				do
					line = jrScanner.nextLine();
				while(!line.startsWith(APPLICATION_PREFIX));
				String listStr = line.substring(APPLICATION_PREFIX.length());
				do
				{	line = jrScanner.nextLine();
					listStr = listStr + line;
				}
				while(!line.endsWith(GROUP_END));
				listStr = listStr.substring(0,listStr.length()-2);
				String keys[] = listStr.split(KEY_SEPARATOR);
				logger.log("Purely applicative articles:");
				logger.increaseOffset();
					count = 0;
					for(String key: keys)
					{	count++;
						Article article = articlesMap.get(key.substring(0,key.length()-1));
						article.ignored = true;
						logger.log(count + ". " + article);
					}
				logger.decreaseOffset();
			logger.decreaseOffset();
			
			// add the ignored articles
			logger.log("Mark the articles belonging to the \"ignored\" Jabref group");
			logger.increaseOffset();
				do
					line = jrScanner.nextLine();
				while(!line.startsWith(IGNORED_PREFIX));
				listStr = line.substring(IGNORED_PREFIX.length());
				do
				{	line = jrScanner.nextLine();
					listStr = listStr + line;
				}
				while(!line.endsWith(GROUP_END));
				listStr = listStr.substring(0,listStr.length()-3);
				keys = listStr.split(KEY_SEPARATOR);
				logger.log("Ignored articles:");
				logger.decreaseOffset();
					count = 0;
					for(String key: keys)
					{	count++;
						Article article = articlesMap.get(key.substring(0,key.length()-1));
						article.ignored = true;
						System.out.println(count + ". " + article);
					}
				logger.decreaseOffset();
			logger.decreaseOffset();
		}
		
		// close JabRef file
		jrScanner.close();
		logger.decreaseOffset();
		logger.log("Finished loading the JabRef file");
	}
	
	/**
	 * Receives the first line of a BibTex entry,
	 * and a scanner pointing on the second line,
	 * and builds the corresponding map, which can
	 * subsequently be used to build an {@link Article}
	 * object.
	 * 
	 * @param line
	 * 		First line of the BibTex entry.
	 * @param scanner
	 * 		Scanner pointing on the rest of the entry.
	 * @return
	 * 		Map containing the entry data.
	 */
	private Map<String, String> retrieveArticleMap(String line, Scanner scanner)
	{	// init map
		Map<String, String> result = new HashMap<String, String>();
		
		// entry type
		int start = line.indexOf('@') + 1;
		int end = line.indexOf('{');
		String source = line.substring(start,end);
		result.put(FLD_SOURCE, source);
		
		// bibtex key
		start = line.indexOf('{') + 1;
		end = line.length() - 1;
		String bibtexkey = line.substring(start, end);
		result.put(FLD_KEY, bibtexkey);
		
		// rest of the fields
		do
		{	// get the first line for this field
			line = scanner.nextLine();
			
			if(!line.equals(ENTRY_END))
			{	// retrieve the name of the field
				int pos = line.indexOf('=');
				String fieldName = line.substring(0,pos-1).trim();
				if(!ALL_FIELDS.contains(fieldName))
					throw new IllegalArgumentException("Unknown Bibtex field \""+fieldName+"\" in line \""+line+"\"");
				// retrieve the associated value
				String fieldValue = null;
				if(line.endsWith(FIELD_END))
					fieldValue = line.substring(pos+3,line.length()-FIELD_END.length()).trim();
				else if(line.endsWith("}"))
					fieldValue = line.substring(pos+3,line.length()-1).trim();
				else
				{	fieldValue = line.substring(pos+3).trim();
					do
					{	line = scanner.nextLine();
						if(line.endsWith(FIELD_END))
							fieldValue = fieldValue + " " + line.substring(0,line.length()-FIELD_END.length()).trim();
						else
							fieldValue = fieldValue + " " + line.trim();
					}
					while(!line.endsWith(FIELD_END));
				}
				// insert in the map
				result.put(fieldName, fieldValue);
			}
		}
		while(!line.equals(ENTRY_END));
		
		return result;
	}

	/**
	 * Builds the Article object from a 
	 * {@code Map} containing at least the required Bibtex
	 * fields: {@code bibtexkey}, {@code authors}, {@code title}, {@code year}.
	 * Both authors and articles maps are updated by this method.
	 * 
	 * @param data
	 * 		Map containing the needed data.
	 * @return 
	 * 		The new article instance.
	 */
	private Article buildArticle(Map<String,String> data)
	{	Article result = new Article();
		
		// init BibTex key
		result.bibtexKey = data.get(FLD_KEY);
		
		// init source type
		String typeSrc = data.get(FLD_SOURCE);
		if(typeSrc.equals(TYPE_BOOK))
		{	String source = data.get(FLD_PUBLISHER);
			if(source==null)
				throw new IllegalArgumentException("Publisher name missing in ("+result.bibtexKey+") "+data);
			else
				result.setSource(SourceType.BOOK, source);
		}
		else if(typeSrc.equals(TYPE_CHAPTER))
		{	String source = data.get(FLD_TITLE_BOOK);
			if(source==null)
				throw new IllegalArgumentException("Book title missing in ("+result.bibtexKey+") "+data);
			else
				result.setSource(SourceType.CHAPTER, source);
		}
		else if(typeSrc.equals(TYPE_CONFERENCE))
		{	String source = data.get(FLD_TITLE_BOOK);
			if(source==null)
				throw new IllegalArgumentException("Conference name missing in "+data);
			else
				result.setSource(SourceType.CONFERENCE, source);
		}
		else if(typeSrc.equals(TYPE_ELECTRONIC))
		{	String source = data.get(FLD_ORGANIZATION);
			if(source==null)
				throw new IllegalArgumentException("Organization name missing ("+result.bibtexKey+") in "+data);
			else
				result.setSource(SourceType.ELECTRONIC, source);
		}
		else if(typeSrc.equals(TYPE_ARTICLE))
		{	String source = data.get(FLD_JOURNAL1);
			if(source==null)
				source = data.get(FLD_JOURNAL2);
			if(source==null)
				throw new IllegalArgumentException("Journal name missing ("+result.bibtexKey+") in "+data);
			else
				result.setSource(SourceType.JOURNAL, source);
			
		}
		else if(typeSrc.equals(TYPE_REPORT))
		{	String source = data.get(FLD_INSTITUTION);
			if(source==null)
				throw new IllegalArgumentException("Institutiong name missing ("+result.bibtexKey+") in "+data);
			else
				result.setSource(SourceType.REPORT, source);
		}
		else if(typeSrc.equals(TYPE_THESIS_MSC))
		{	String source = data.get(FLD_INSTITUTION);
			if(source==null)
				source = data.get(FLD_SCHOOL);
			if(source==null)
				throw new IllegalArgumentException("Institutiong name missing ("+result.bibtexKey+") in "+data);
			else
				result.setSource(SourceType.THESIS_MSC, source);
		}
		else if(typeSrc.equals(TYPE_THESIS_PHD))
		{	String source = data.get(FLD_INSTITUTION);
			if(source==null)
				source = data.get(FLD_SCHOOL);
			if(source==null)
				throw new IllegalArgumentException("Institutiong name missing ("+result.bibtexKey+") in "+data);
			else
				result.setSource(SourceType.THESIS_PHD, source);
		}
		else
			throw new IllegalArgumentException("Bibtex entry type unknown ("+result.bibtexKey+"): "+typeSrc);
		
		// init authors
		String temp[] = data.get(FLD_AUTHOR).split(" and ");
		for(String authorStr: temp)
		{	Author author = new Author(authorStr);
			author = Author.retrieveAuthor(author, authorsMap);
			result.addAuthor(author);
		}
		
		// init title
		String title = data.get(FLD_TITLE_ARTICLE);
		result.setTitle(title);
		
		// init volume
		result.volume = data.get(FLD_VOLUME);
		
		// init issue
		String issue = data.get(FLD_NUMBER);
		if(issue==null)
			issue = data.get(FLD_ISSUE);
		if(issue!=null)
			result.issue = issue.trim();
		
		// init page
		String page = data.get(FLD_PAGES);
		if(page!=null)
			result.page = page.trim();
		
		// init year
		String year = data.get(FLD_YEAR);
		result.year = year.trim();
		
		// init doi
		String doi = data.get(FLD_DOI);
		if(doi!=null)
			result.doi = doi.trim();
		
		// abstract
		String abstrct = data.get(FLD_ABSTRACT);
		if(abstrct!=null)
			result.abstrct = abstrct.trim();
		
		// chapter
		String chapter = data.get(FLD_CHAPTER);
		if(chapter!=null)
			result.chapter = chapter.trim();
		
		// file
		String file = data.get(FLD_FILE);
		if(file!=null)
			result.file = file.trim();
		
		// owner
		String owner = data.get(FLD_OWNER);
		if(owner!=null)
			result.owner = owner.trim();
		
		// timestamp
		String timestamp = data.get(FLD_TIMESTAMP);
		if(timestamp!=null)
			result.timestamp = timestamp.trim();
		
		// url
		String url = data.get(FLD_URL);
		if(url!=null)
			result.url = url.trim();
		
		// series
		String series = data.get(FLD_SERIES);
		if(series!=null)
			result.series = series.trim();
		
		// series
		String editor = data.get(FLD_EDITOR);
		if(editor!=null)
			result.editor = editor.trim();
		
		// review
		String review = data.get(FLD_REVIEW);
		if(review!=null)
			result.review = review.trim();
		
		// address
		String address = data.get(FLD_ADDRESS);
		if(address!=null)
			result.address = address.trim();
		
		// type
		String type = data.get(FLD_TYPE);
		if(type!=null)
			result.type = type.trim();
		
		// sortkey
		String sortkey = data.get(FLD_SORTKEY);
		if(sortkey!=null)
			result.sortkey = sortkey.trim();
		
		// edition
		String edition = data.get(FLD_EDITION);
		if(edition!=null)
			result.edition = edition.trim();
		
		// present
		result.present = true;
		
		articlesMap.put(result.bibtexKey, result);
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// WRITING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	// TODO

	/////////////////////////////////////////////////////////////////
	// TESTS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Method used to test this class.
	 * 
	 * @param args
	 * 		None needed.
	 * @throws Exception
	 * 		Whatever exception.
	 */
	public static void main(String[] args) throws Exception
	{	JabrefFileHandler jfh = new JabrefFileHandler();
		String path = FileNames.FI_BIBTEX_COMPLETE;
//		String path = FileNames.FI_BIBTEX_REVIEW;
//		String path = FileNames.FI_BIBTEX_STRUCTBAL;
		boolean updateGroups = false;
		jfh.loadJabRefFile(path, updateGroups);
	}
}
