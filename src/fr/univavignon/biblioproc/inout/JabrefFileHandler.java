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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.univavignon.biblioproc.data.biblio.Article;
import fr.univavignon.biblioproc.data.biblio.Author;
import fr.univavignon.biblioproc.data.biblio.Corpus;
import fr.univavignon.biblioproc.data.biblio.SourceType;
import fr.univavignon.biblioproc.tools.file.FileNames;

import fr.univavignon.tools.file.FileTools;
import fr.univavignon.tools.log.HierarchicalLogger;
import fr.univavignon.tools.log.HierarchicalLoggerManager;

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
	/** String marking the beginning of a BibTex field */
	private static final String FIELD_BEGINNING = " = {";
	/** String marking the end of a BibTex entry */
	private static final String ENTRY_END = "}";
	/** String marking the beginning of a BibTex entry */
	private static final String ENTRY_BEGINNING = "{";
	
	/////////////////////////////////////////////////////////////////
	// JABREF MARKERS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** String marking the end of the actual BibTex file (and the begining of the JabRef part) */
	private static final String COMMENT_PREFIX = "@Comment";
	/** String marking the begining of the list of ignored articles */
	private static final String IGNORED_PREFIX = "3 ExplicitGroup:Ignored\\;2\\;";
	/** String marking the begining of the list of purely applicative articles */
	private static final String APPLICATION_PREFIX = "3 ExplicitGroup:Applications Only\\;2\\;";
	/** String marking the end of a JabRef group */
	private static final String GROUP_END = ";;";
	/** String separating the BibTex kes in a JabRef group */
	private static final String KEY_SEPARATOR = "\\;";
	
	/////////////////////////////////////////////////////////////////
	// BIBTEX FIELDS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Source of the Bibtex entry (journal, book title, etc.) */
	private static final String FLD_SOURCE = "source";
	/** Bibtex key for the article key */
	private static final String FLD_KEY = "bibtexkey";
	/** Bibtex key for the authors */
	public static final String FLD_AUTHOR = "author";
	/** Bibtex key for the abstract */
	private static final String FLD_ABSTRACT = "abstract";
	/** Bibtex key for the chapter */
	public static final String FLD_CHAPTER = "chapter";
	/** Bibtex key for the DOI */
	public static final String FLD_DOI = "doi";
	/** Bibtex key for the file */
	private static final String FLD_FILE = "file";
	/** Bibtex key for the institution */
	private static final String FLD_INSTITUTION = "institution";
	/** Bibtex key for the school */
	private static final String FLD_SCHOOL = "school";
	/** Bibtex key for the issue */
	private static final String FLD_ISSUE = "issue";
	/** Traditional Bibtex key for the journal */
	public static final String FLD_JOURNAL1 = "journal";
	/** Alterantive Bibtex key for the journal */
	private static final String FLD_JOURNAL2 = "journaltitle";
	/** Bibtex key for the number */
	public static final String FLD_NUMBER = "number";
	/** Bibtex key for the month */
	public static final String FLD_MONTH = "month";
	/** Bibtex key for the owner */
	private static final String FLD_OWNER = "owner";
	/** Bibtex key for the pages */
	public static final String FLD_PAGES = "pages";
	/** Bibtex key for the time stamp */
	private static final String FLD_TIMESTAMP = "timestamp";
	/** Bibtex key for the article title */
	public static final String FLD_TITLE_ARTICLE = "title";
	/** Bibtex key for the book title */
	public static final String FLD_TITLE_BOOK = "booktitle";
	/** Bibtex key for the URL */
	public static final String FLD_URL = "url";
	/** Bibtex key for the volume */
	public static final String FLD_VOLUME = "volume";
	/** Bibtex key for the publication year */
	public static final String FLD_YEAR = "year";
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
	/** Bibtex key for article groups */
	private static final String FLD_GROUPS = "groups";
	/** Organization associated to an electronic reference */
	private static final String FLD_ORGANIZATION = "organization";	
	/** How the Web page was published */
	private static final String FLD_HOWPUB = "howpublished";	
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
			FLD_EDITION, FLD_ORGANIZATION, FLD_GROUPS, FLD_MONTH, FLD_HOWPUB,
			// ignored:
			FLD_MARKED
	);
	
	/////////////////////////////////////////////////////////////////
	// BIBTEX FIELDS	/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Bibtex type of entry for a journal article */
	private static final String TYPE_ARTICLE = "Article";
	/** Bibtex type of entry for a whole book */
	private static final String TYPE_BOOK = "Book";
	/** Bibtex type of entry for a book chapter */
	private static final String TYPE_INBOOK = "InBook";
	/** Bibtex type of entry for a whole collection */
	private static final String TYPE_COLLECTION = "Collection";
	/** Bibtex type of entry for a collection chapter */
	private static final String TYPE_INCOLLECTION = "InCollection";
	/** Bibtex type of entry for a conference article */
	private static final String TYPE_INPROCEEDINGS = "InProceedings";
	/** Bibtex type of entry for an electronic resource */
	private static final String TYPE_ELECTRONIC = "Electronic";
	/** Bibtex type of entry for a report */
	private static final String TYPE_TECH_REPORT = "TechReport";
	/** Bibtex type of entry for a MSc thesis */
	private static final String TYPE_THESIS_MSC = "MastersThesis";
	/** Bibtex type of entry for a PhD thesis */
	private static final String TYPE_THESIS_PHD = "PhdThesis";
	
	/////////////////////////////////////////////////////////////////
	// DATA				/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Collection of articles */
	public Corpus corpus = new Corpus();
	/** Jabref Commands located at the end of the file */
	public String jabrefCommands = null;
	
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
if(count==1054)
	System.out.print("");
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
		jabrefCommands = "\n" + line;
		
		// update with the list of purely applicative articles
		if(updateGroups)
		{	logger.log("Mark the articles belonging to the \"applications\" Jabref group");
			logger.increaseOffset();
				do
				{	line = jrScanner.nextLine();
					jabrefCommands = jabrefCommands + "\n" + line;
				}
				while(!line.startsWith(APPLICATION_PREFIX));
				String listStr = line.substring(APPLICATION_PREFIX.length());
				do
				{	line = jrScanner.nextLine();
					listStr = listStr + line;
					jabrefCommands = jabrefCommands + "\n" + line;
				}
				while(!line.endsWith(GROUP_END));
				listStr = listStr.substring(0,listStr.length()-2);
				String keys[] = listStr.split(KEY_SEPARATOR);
				logger.log("Purely applicative articles:");
				logger.increaseOffset();
					count = 0;
					for(String key: keys)
					{	count++;
						String nkey = key.substring(0,key.length()-1);
						Article article = corpus.getArticleByBibkey(nkey);
						article.ignored = true;
						logger.log(count + ". " + article);
					}
				logger.decreaseOffset();
			logger.decreaseOffset();
			
			// add the ignored articles
			logger.log("Mark the articles belonging to the \"ignored\" Jabref group");
			logger.increaseOffset();
				do
				{	line = jrScanner.nextLine();
					jabrefCommands = jabrefCommands + "\n" + line;
				}
				while(!line.startsWith(IGNORED_PREFIX));
				listStr = line.substring(IGNORED_PREFIX.length());
				do
				{	line = jrScanner.nextLine();
					listStr = listStr + line;
					jabrefCommands = jabrefCommands + "\n" + line;
				}
				while(!line.endsWith(GROUP_END));
				listStr = listStr.substring(0,listStr.length()-3);
				keys = listStr.split(KEY_SEPARATOR);
				logger.log("Ignored articles:");
				logger.decreaseOffset();
					count = 0;
					for(String key: keys)
					{	count++;
						String nkey = key.substring(0,key.length()-1);
						Article article = corpus.getArticleByBibkey(nkey);
						article.ignored = true;
						System.out.println(count + ". " + article);
					}
				logger.decreaseOffset();
			logger.decreaseOffset();
		}
		
		// possibly finish reading the file
		logger.log("Finish reading the file");
		while(jrScanner.hasNextLine())
		{	line = jrScanner.nextLine();
			jabrefCommands = jabrefCommands + "\n" + line;
		}
		
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
				if(fieldName.startsWith("_") && !fieldName.startsWith("__"))
					fieldName = fieldName.substring(1);
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
		if(corpus.containsKey(result.bibtexKey))
			throw new IllegalArgumentException("The corpus already contains the Bibtex key "+result.bibtexKey+" ("+data+")");
		
		// init source type
		String typeSrc = data.get(FLD_SOURCE);
		if(typeSrc.equals(TYPE_BOOK))
		{	String source = data.get(FLD_PUBLISHER);
			if(source==null)
				throw new IllegalArgumentException("Publisher name missing in ("+result.bibtexKey+") "+data);
			else
				result.setSource(SourceType.BOOK, source);
		}
		else if(typeSrc.equals(TYPE_INBOOK))
		{	String source = data.get(FLD_TITLE_BOOK);
			if(source==null)
				throw new IllegalArgumentException("Book title missing in ("+result.bibtexKey+") "+data);
			else
				result.setSource(SourceType.IN_BOOK, source);
		}
		else if(typeSrc.equals(TYPE_COLLECTION))
		{	String source = data.get(FLD_PUBLISHER);
			if(source==null)
				throw new IllegalArgumentException("Publisher name missing in ("+result.bibtexKey+") "+data);
			else
				result.setSource(SourceType.BOOK, source);
		}
		else if(typeSrc.equals(TYPE_INCOLLECTION))
		{	String source = data.get(FLD_TITLE_BOOK);
			if(source==null)
				throw new IllegalArgumentException("Book title missing in ("+result.bibtexKey+") "+data);
			else
				result.setSource(SourceType.IN_BOOK, source);
		}
		else if(typeSrc.equals(TYPE_INPROCEEDINGS))
		{	String source = data.get(FLD_TITLE_BOOK);
			if(source==null)
				throw new IllegalArgumentException("Conference name missing in "+data);
			else
				result.setSource(SourceType.IN_PROCEEDINGS, source);
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
				result.setSource(SourceType.ARTICLE, source);
			
		}
		else if(typeSrc.equals(TYPE_TECH_REPORT))
		{	String source = data.get(FLD_INSTITUTION);
			if(source==null)
				throw new IllegalArgumentException("Institutiong name missing ("+result.bibtexKey+") in "+data);
			else
				result.setSource(SourceType.TECH_REPORT, source);
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
		{	Pattern pattern = Pattern.compile("[\\p{L}]\\.[\\p{L}]");
			Matcher matcher = pattern.matcher(authorStr);
			if(matcher.find())
				throw new IllegalArgumentException("Probably a dot/space problem in "+authorStr);
			Author author = new Author(authorStr);
			author = corpus.retrieveAuthor(author);
			result.addAuthor(author);
		}
		
		// init title
		String title = data.get(FLD_TITLE_ARTICLE);
		result.setTitle(title);
		
		// init journal
		result.journal = data.get(FLD_JOURNAL1);
		if(result.journal==null)
			result.journal = data.get(FLD_JOURNAL2);
		
		// init publisher
		result.publisher = data.get(FLD_PUBLISHER);
		
		// init book title
		result.booktitle = data.get(FLD_TITLE_BOOK);
		
		// init month
		result.month = data.get(FLD_MONTH);
		
		// init howpublished
		result.howpublished = data.get(FLD_HOWPUB);
		
		// init organization
		result.organization = data.get(FLD_ORGANIZATION);
		
		// init institution
		result.institution = data.get(FLD_INSTITUTION);
		
		// init school
		result.school = data.get(FLD_SCHOOL);
		
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
		
		// groups
		String groups = data.get(FLD_GROUPS);
		if(groups!=null)
			result.groups = groups.trim();
		
		// present
		result.present = true;
		
		corpus.addArticle(result);
		return result;
	}

	/////////////////////////////////////////////////////////////////
	// WRITING			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Record the current article collection to a Jabref file.
	 * 
	 * @param fileName
	 * 		Name of the Jabref file.
	 * @param pdfFolder
	 * 		Path of the folder containing the PDF files associated to
	 * 		the articles, or {@code null} if no such folder.
	 * 
	 * @throws UnsupportedEncodingException
	 * 		Problem while opening the output file.
	 * @throws FileNotFoundException
	 * 		Problem while opening the output file.
	 */
	public void writeJabRefFile(String fileName, String pdfFolder) throws UnsupportedEncodingException, FileNotFoundException
	{	logger.log("Writing Jabref file "+fileName);
		logger.increaseOffset();
		
		// init output file
		logger.log("Open file");
		String path = FileNames.FO_OUTPUT + File.separator + fileName;
		PrintWriter pw = FileTools.openTextFileWrite(path, "UTF-8");
		pw.println("% Encoding: UTF-8\n");
		
		// write each article
		logger.log("Write each article");
		for(Article article: corpus.getArticles())
			writeArticle(article, pw);
		
		// add Jabref stuff
		logger.log("Add Jabref commands");
		if(jabrefCommands==null || jabrefCommands.isEmpty())
		{	pw.println("\n@Comment{jabref-meta: databaseType:bibtex;}");
			if(pdfFolder!=null)
				pw.println("\n@Comment{jabref-meta: fileDirectory:"+pdfFolder.replace("\\","\\\\")+";}");
			pw.println("\n@Comment{jabref-meta: groupsversion:3;}");
			pw.println("\n@Comment{jabref-meta: saveOrderConfig:original;abstract;false;abstract;false;abstract;false;}");
		}
		else
			pw.println(jabrefCommands);
		
		pw.close();
		logger.increaseOffset();
		logger.log("File written");
	}
	
	/**
	 * Writes the specificed article in the previously opened file
	 * represented by the specific print writer.
	 *  
	 * @param article
	 * 		Article to write.
	 * @param pw
	 * 		Stream in which to write.
	 */
	private void writeArticle(Article article, PrintWriter pw)
	{	logger.log("Writing article "+article.toString());
		logger.increaseOffset();
		
		// print entry type and bibtex key 
		SourceType sourceType = article.getSourceType();
		switch(sourceType)
		{	case BOOK:
				pw.println("@"+TYPE_BOOK+ENTRY_BEGINNING+article.bibtexKey+",");
				break;
			case IN_BOOK:
				pw.println("@"+TYPE_INBOOK+ENTRY_BEGINNING+article.bibtexKey+",");
				break;
			case COLLECTION:
				pw.println("@"+TYPE_COLLECTION+ENTRY_BEGINNING+article.bibtexKey+",");
				break;
			case IN_COLLECTION:
				pw.println("@"+TYPE_INCOLLECTION+ENTRY_BEGINNING+article.bibtexKey+",");
				break;
			case IN_PROCEEDINGS:
				pw.println("@"+TYPE_INPROCEEDINGS+ENTRY_BEGINNING+article.bibtexKey+",");
				break;
			case ELECTRONIC:
				pw.println("@"+TYPE_ELECTRONIC+ENTRY_BEGINNING+article.bibtexKey+",");
				break;
			case ARTICLE:
				pw.println("@"+TYPE_ARTICLE+ENTRY_BEGINNING+article.bibtexKey+",");
				break;
			case TECH_REPORT:
				pw.println("@"+TYPE_TECH_REPORT+ENTRY_BEGINNING+article.bibtexKey+",");
				break;
			case THESIS_MSC:
				pw.println("@"+TYPE_THESIS_MSC+ENTRY_BEGINNING+article.bibtexKey+",");
				break;
			case THESIS_PHD:
				pw.println("@"+TYPE_THESIS_PHD+ENTRY_BEGINNING+article.bibtexKey+",");
				break;
		}
		
		// authors
		List<Author> authors = article.getAuthors();
		String authStr = "";
		for(Author author: authors)
		{	if(!authStr.isEmpty())
				authStr = authStr + " and ";
			authStr = authStr + author.getFullname();
		}
		if(!authStr.isEmpty())
			pw.println("  "+FLD_AUTHOR+FIELD_BEGINNING+authStr+FIELD_END);
		
		// title
		String title = article.getTitle();
		if(title!=null)
			pw.println("  "+FLD_TITLE_ARTICLE+FIELD_BEGINNING+title+FIELD_END);
		
		// year
		if(article.year!=null)
			pw.println("  "+FLD_YEAR+FIELD_BEGINNING+article.year+FIELD_END);
		
		// journal
		if(article.journal!=null)
			pw.println("  "+FLD_JOURNAL1+FIELD_BEGINNING+article.journal+FIELD_END);
		
		// volume
		if(article.volume!=null)
			pw.println("  "+FLD_VOLUME+FIELD_BEGINNING+article.volume+FIELD_END);
		
		// issue
		if(article.issue!=null)
			pw.println("  "+FLD_NUMBER+FIELD_BEGINNING+article.issue+FIELD_END);
		
		// pages
		if(article.page!=null)
			pw.println("  "+FLD_PAGES+FIELD_BEGINNING+article.page+FIELD_END);
		
		// editor
		if(article.editor!=null)
			pw.println("  "+FLD_EDITOR+FIELD_BEGINNING+article.editor+FIELD_END);
		
		// edition
		if(article.edition!=null)
			pw.println("  "+FLD_EDITION+FIELD_BEGINNING+article.edition+FIELD_END);
		
		// series
		if(article.series!=null)
			pw.println("  "+FLD_SERIES+FIELD_BEGINNING+article.series+FIELD_END);
		
		// chapter
		if(article.chapter!=null)
			pw.println("  "+FLD_CHAPTER+FIELD_BEGINNING+article.chapter+FIELD_END);
		
		// institution
		if(article.institution!=null)
			pw.println("  "+FLD_INSTITUTION+FIELD_BEGINNING+article.institution+FIELD_END);
		
		// school
		if(article.school!=null)
			pw.println("  "+FLD_SCHOOL+FIELD_BEGINNING+article.school+FIELD_END);
		
		// booktitle
		if(article.booktitle!=null)
			pw.println("  "+FLD_TITLE_BOOK+FIELD_BEGINNING+article.booktitle+FIELD_END);
		
		// publisher
		if(article.publisher!=null)
			pw.println("  "+FLD_PUBLISHER+FIELD_BEGINNING+article.publisher+FIELD_END);
		
		// address
		if(article.address!=null)
			pw.println("  "+FLD_ADDRESS+FIELD_BEGINNING+article.address+FIELD_END);
		
		// type
		if(article.type!=null)
			pw.println("  "+FLD_TYPE+FIELD_BEGINNING+article.type+FIELD_END);
		
		// month
		if(article.month!=null)
			pw.println("  "+FLD_MONTH+FIELD_BEGINNING+article.month+FIELD_END);
		
		// organization
		if(article.organization!=null)
			pw.println("  "+FLD_ORGANIZATION+FIELD_BEGINNING+article.organization+FIELD_END);
		
		// howpublished
		if(article.howpublished!=null)
			pw.println("  "+FLD_HOWPUB+FIELD_BEGINNING+article.howpublished+FIELD_END);
		
		// doi
		if(article.doi!=null)
			pw.println("  "+FLD_DOI+FIELD_BEGINNING+article.doi+FIELD_END);
		
		// file
		if(article.file!=null)
			pw.println("  "+FLD_FILE+FIELD_BEGINNING+article.file+FIELD_END);
		
		// abstract
		if(article.abstrct!=null)
			pw.println("  "+FLD_ABSTRACT+FIELD_BEGINNING+article.abstrct+FIELD_END);
		
		// owner
		if(article.owner!=null)
			pw.println("  "+FLD_OWNER+FIELD_BEGINNING+article.owner+FIELD_END);
		
		// timestamp
		if(article.timestamp!=null)
			pw.println("  "+FLD_TIMESTAMP+FIELD_BEGINNING+article.timestamp+FIELD_END);
		
		// url
		if(article.url!=null)
			pw.println("  "+FLD_URL+FIELD_BEGINNING+article.url+FIELD_END);
		
		// review
		if(article.review!=null)
			pw.println("  "+FLD_REVIEW+FIELD_BEGINNING+article.review+FIELD_END);
		
		// groups
		if(article.groups!=null)
			pw.println("  "+FLD_GROUPS+FIELD_BEGINNING+article.groups+FIELD_END);
		
		// sortkey
		if(article.sortkey!=null)
			pw.println("  "+FLD_SORTKEY+FIELD_BEGINNING+article.sortkey+FIELD_END);
		
		pw.println(ENTRY_END);
		pw.println();
		logger.decreaseOffset();
	}
	
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
//		String path = FileNames.FI_BIBTEX_COMPLETE;
//		String path = FileNames.FI_BIBTEX_REVIEW;
		String path = FileNames.FI_BIBTEX_STRUCT_BAL;
		boolean updateGroups = false;
		jfh.loadJabRefFile(path, updateGroups);
		
		jfh.writeJabRefFile("test.bib", FileNames.FO_OUTPUT);
	}
}
