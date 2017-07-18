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

import java.util.Map;

import fr.univavignon.biblioproc.tools.string.StringTools;

/**
 * This class is used to represent an author.
 */
public class Author implements Comparable<Author>
{	/**
	 * Builds an author using a string representing
	 * both its firstname(s) and lastname, of the form:
	 * "Lastname, Firstname1 Firstname2..."
	 * 
	 * @param fullName
	 * 		String representing the author's name.
	 */
	public Author(String fullName)
	{	// setup last name
		fullName = StringTools.clean(fullName);
		String[] temp = fullName.split(", "); 
		lastname = temp[0];
		
		// setup firstnames
		if(temp.length==1)
			throw new IllegalArgumentException("Could not find the firstname in fullname \""+fullName+"\"");
		else if(temp.length>1)
			firstnameInitials = retrieveInitials(temp[1]);
		
		// setup normalized fullname
		initNormName();
	}
	
	/**
	 * Builds an author using strings separately representing
	 * his lastname and firstname(s). The firstname is supposed to
	 * be under the form of uppercase initials separated by spaces
	 * or hyphens, and ended with dots. For instance: "X.", "J.-P."
	 * or "X. Y. Z.".
	 * 
	 * @param lastname
	 * 		Lastname of the author.
	 * @param firstnameInitials
	 * 		Initial(s) of the author's firstname(s).
	 */
	public Author(String lastname, String firstnameInitials)
	{	// setup last name
		this.lastname = lastname;
		// setup firstnames
		this.firstnameInitials = firstnameInitials;
		// setup normalized fullname
		initNormName();
	}
	
	/**
	 * Looks up the specified name and returns the corresponding
	 * author if it already exists. Otherwise, the method creates
	 * the author, adds it to the list and returns it.
	 * 
	 * @param author
	 * 		Targeted author (containing the appropriate name).
	 * @param authors
	 * 		Map of previously loaded authors.
	 * @return
	 * 		The targeted author.
	 */
	public static Author retrieveAuthor(Author author, Map<String,Author> authors)
	{	Author result = author;
		Author temp = authors.get(author.normname);
		if(temp!=null)
			result = temp;
		else
			authors.put(author.normname, result);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// FIRSTNAME INITIALS	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** First name initial(s)  */
	public String firstnameInitials = null;

	/////////////////////////////////////////////////////////////////
	// LASTNAME				/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Lastname initial  */
	public String lastname = null;
	
	/////////////////////////////////////////////////////////////////
	// NORMALIZED NAME		/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Normalized fullname */
	public String normname = null;
	
	/**
	 * Initializes the normalized form of the name, based
	 * on the existing lastname and firstnames.
	 */
	private void initNormName()
	{	normname = firstnameInitials.replace(" ", "");	// remove spaces between initials
		normname = normname.replace(".", "");			// remove dots after initials
if(normname.contains("-"))		
		normname = normname.replace("-", "");			// remove hyphens between initials
		normname = lastname.replace("-"," ") + " " + normname;	// replace hyphens by spaces in the lastname
		normname = StringTools.normalize(normname);		// normalize the resulting string
	}
	
	/////////////////////////////////////////////////////////////////
	// COMPARISON		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public int compareTo(Author author)
	{	int result = normname.compareTo(author.normname);
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{	boolean result = false;
		if(obj != null && obj instanceof Author)
		{	Author author = (Author) obj;
			result = compareTo(author) == 0;
		}
		return result;
	}

	@Override
	public int hashCode()
	{	String fullname = getFullname();
		int result = fullname.hashCode();
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// STRINGS			/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/**
	 * Returns the full name of the author,
	 * under the form "Xxxxx, Y. Z.", where "Xxxxx" is the
	 * last name and "Y." and "Z." the initials of the firstnames.
	 * 
	 * @return
	 * 		A string of the form "Lastname, F. M.".
	 */
	public String getFullname()
	{	String result = lastname;
		if(firstnameInitials!=null)
			result = result + ", " + firstnameInitials;
		return result;
	}
	
	@Override
	public String toString()
	{	String result = lastname + ", " + firstnameInitials;
		return result;
	}

	/**
	 * Gets a text of space- or hyphen-separated words,
	 * and returns a new string in which each one of these
	 * words is represented by its uppercase initial followed
	 * by a dot. This method is designed to process sequences
	 * of firstnames and to return the corresponding sequence of
	 * initials.
	 *  
	 * @param text
	 * 		Sequence of firstnames.
	 * @return
	 * 		The corresponding sequence of initials.
	 */
	private String retrieveInitials(String text)
	{	String result = "";
		String temp[] = text.split(" ");
		for(int i=0;i<temp.length;i++)
		{	if(i>0)
				result = result + " ";
			String tmp = temp[i];
			String temp2[] = tmp.split("-");
			for(int j=0;j<temp2.length;j++)
			{	if(j>0)
					result = result + "-";
				String tmp2 = temp2[j];
				result = result + tmp2.substring(0,1).toUpperCase() + ".";
			}
		}
		return result;
	}
}
