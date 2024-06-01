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

/**
 * This type represents the various types of publications.  
 */
public enum SourceType
{	/** Journal paper */
	ARTICLE,
	
	/** Full book */
	BOOK,
	
	/** Book chapter */
	IN_BOOK,
	
	/** Full collection */
	COLLECTION,
	
	/** Collection chapter */
	IN_COLLECTION,
	
	/** Conference paper */
	IN_PROCEEDINGS,
	
	/** Electronic resource */
	ELECTRONIC,
	
	/** Technical report */
	TECH_REPORT,

	/** MSc thesis */
	THESIS_MSC,
	
	/** PhD thesis */
	THESIS_PHD;
}
