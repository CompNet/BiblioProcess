package fr.univavignon.biblioproc.data;

import java.util.Map;

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

import fr.univavignon.biblioproc.tools.StringTools;

/**
 * This class is used to represent an author.
 */
public class Author implements Comparable<Author>
{	/**
	 * Builds an author using a string representing
	 * both its firstname and lastname, of the form
	 * Lastname, Firstname1 Firstname2...
	 * 
	 * @param fullName
	 * 		String representing the author's name.
	 */
	public Author(String fullName)
	{	String[] temp = fullName.split(", "); 
		lastname = StringTools.normalize(temp[0]);
		if(temp.length>1)
			firstnameInitial = StringTools.normalize(temp[1].substring(0,1));
	}
	
	/**
	 * Builds an author using strings separately representing
	 * his lastname and firstname.
	 * 
	 * @param lastname
	 * 		Lastname of the author.
	 * @param firstnameInitial
	 * 		Initial of the author's firstname.
	 */
	public Author(String lastname, String firstnameInitial)
	{	this.lastname = StringTools.normalize(lastname);
		this.firstnameInitial = StringTools.normalize(firstnameInitial);
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
		String fullname = result.getFullname();
		Author temp = authors.get(fullname);
		if(temp!=null)
			result = temp;
		else
			authors.put(fullname, result);
		return result;
	}
	
	/////////////////////////////////////////////////////////////////
	// FIRSTNAME INITIAL	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** First name initial  */
	private String firstnameInitial = null;
	
	/**
	 * Returns the first name initial.
	 * 
	 * @return
	 * 		The first name initial of the author.
	 */
	public String getFirstnameInitial()
	{	return firstnameInitial;
	}

	/////////////////////////////////////////////////////////////////
	// FIRSTNAME INITIAL	/////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	/** Last name initial  */
	private String lastname = null;
	
	/**
	 * Returns the last name.
	 * 
	 * @return
	 * 		The last name of the author.
	 */
	public String getLastname()
	{	return lastname;
	}
	
	/////////////////////////////////////////////////////////////////
	// COMPARISON		/////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	@Override
	public int compareTo(Author author)
	{	String fullname1 = getFullname();
		String fullname2 = author.getFullname();
		int result = fullname1.compareTo(fullname2);
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
	 * under the form "xxxxx, y", where "xxxxx" is the
	 * last name and "y" the initial of the first name.
	 * 
	 * @return
	 * 		A string of the form "lastname, f".
	 */
	public String getFullname()
	{	String result = lastname;
		if(firstnameInitial!=null)
			result = result + ", " + firstnameInitial;
		return result;
	}
	
	@Override
	public String toString()
	{	String result = lastname + ", " + firstnameInitial;
		return result;
	}
}
