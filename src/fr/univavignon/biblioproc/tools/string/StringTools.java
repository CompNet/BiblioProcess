package fr.univavignon.biblioproc.tools.string;

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

import java.util.Locale;

/**
 * Common methods used to process strings. 
 */
public class StringTools extends fr.univavignon.tools.string.StringTools
{
	/**
	 * Normalizes the specified text by removing
	 * diacritics, switching to lowercase, and replacing
	 * all dashes variants by a simple hyphen (others?).
	 * 
	 * @param text
	 * 		Original text.
	 * @return
	 * 		Normalized text.
	 */
	public static String normalize(String text)
	{	String result = null;
		if(text!=null)
		{	result = removeDiacritics(text).trim();			// remove accents and other diacritics
			result = result.toLowerCase(Locale.ENGLISH);	// switch to lower case
			result = result.replaceAll("\\p{Pd}", "-");		// replace all dashes and variants by regular hyphens
		}
		return result;
	}
	
	/**
	 * Cleans the text without changing it as much
	 * as {@link #normalize(String)}: replace
	 * dashes by hyphens.
	 * 
	 * @param text
	 * 		Text to clean.
	 * @return
	 * 		Cleaned text.
	 */
	public static String clean(String text)
	{	String result = null;
		if(text!=null)
		{	result = text.replaceAll("\\p{Pd}", "-");			// replace all dashes and variants by regular hyphens
		}
		return result;
	}
}
