package fr.univavignon.biblioproc;

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

import java.text.Normalizer;
import java.text.Normalizer.Form;

/**
 * This class is used to represent a publication,
 * with a minimum set of fields.
 */
public class StringTools
{
	/**
	 * Retrieved from http://www.drillio.com/en/software-development/java/removing-accents-diacritics-in-any-language/
	 * @author Istv�n So�s
	 * @param text
	 * @return
	 */
	public static String removeAccents(String text)
	{	return text == null ? null
	        : Normalizer.normalize(text, Form.NFD)
	            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}
	
	public static String normalize(String text)
	{	String result = null;
		if(text!=null)
			result = removeAccents(text).trim().toLowerCase();
		return result;
	}
}
