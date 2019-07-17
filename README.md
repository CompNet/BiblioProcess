BiblioProcess
=======
*Processing of bibliographic files*

* Copyright 2011-19 Vincent Labatut

BiblioProcess is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation. For source availability and license information see `licence.txt`

* Lab website: http://lia.univ-avignon.fr/
* GitHub repo: https://github.com/CompNet/BiblioProcess
* Contact: vincent.labatut@univ-avignon.fr

**This software is not user-friendly, and mainly intended for internal use!**

-----------------------------------------------------------------------

## Description
This software aims at extracting networks from bibliographic corpora. It can open Bibtex files, to get the detail of a collection of articles,
as well as Thomson ISI files, in order to get the bibliographic references contained in these articles. It matches both files and exports several networks:
* *Authorship* network : node are articles/authors (bipartite network), unweighted undirected links connect authors with their articles.
* *Citation* networks
  * Article citation networks: nodes are articles, unweighted links are directed from the cited to the citing articles.
  * Author citation networks: nodes are authors, links are directed from the cited to the citing authors, weights are the numbers of citations.
* *Coauthorship* networks
  * Article coauthorship networks: nodes are articles, undirected links connect articles with common authors, weights are Jaccard's coefficient of compared authors.
  * Author coauthorship networks: nodes are authors, undirected links connect co-authors, weights are the numbers of co-authored papers.
* *Cocitation* networks
  * Article co-citing networks: nodes are articles, undirected links connect articles citing the same reference, weights are the numbers of co-citations.
  * Article co-cited networks: nodes are articles, undirected links connect articles cited by the same reference, weights are the numbers of co-citations.

## Organization
The source code is organized as follows:
* Package `data` contains classes related to article and network representation.
* Package `inout` contains classes to read BibTeX and ISI files.
* Package `tex` contains classes related to the processing of LaTeX files.
* Package `tools` contains helper classes.

The other resources are organized as follows:
* Folder `in`: input files` 
  * `bibtex`: the BibTeX file (we need just a single one).
  * `isi`: several files
    * `savedrecs.ciw`: The ISI file, which lists a series of bibliographic entries with their detail, in particular the articles they cite
    * `additional_refs.txt`: list of bibliographic entries, specifying for each one the articles they cite. This list is constitued manually and aims at completing the ISI file with enties missing from WoS.
    * `error_fixes.txt`: allows overriding some citations from the ISI file, which are sometimes incorrect. In this file, one can associated the correct entry from the BibTeX file (instead of the wrong one). This file is consituted manually.
    * `ignored_refs.txt`: another fix of the ISI file: this is the list of citations that we just want to ignore. It is constitued manually, by trial and error.
    * `short_names.txt`: the journal and conference names mentioned in the ISI file are abbreviated. This file is a map allowing to specify the corresponding full name, which is required to automatically find the entry in the BibTeX file. 
  * `latex`: LaTeX files, for a processing unrelated to network extraction.
* Folder `out`: output files, resulting from the processing
  * `*.graphml``: the graph extracted from the BibTeX and ISI files.
  * `missing.refs`: articles cited in the ISI file which could not be retrieved in the BibTeX file. 

## Installation
* Nothing to install, besides the Java Runtime Environment (JRE).

## Use
The `main` method in class `Launcher` shows how to:
1. Load a BibTeX file to retrieve a corpus of bibtex entries;
2. Parse an ISI file describing the links between these entries;
3. Extract all sorts of networks.

The software first needs to load a corpus of bibliographic entries, represented by a BibTeX file. Then, it loads an ISI file containing a list of biographic entries extracted from Web of Science. For each entry, there are several information, and also the article cited in this entry. Thus, the ISI file must be properly extracted from WoS, with the appropriate options (by default, citated references are not included).

The software processes each bibliographic entry listed in the ISI file, and tries to match it to one in from the BibTeX file. Then, it does the same with the article its cites (according to the ISI file). Therefore, the BibTeX file must contain all the entries mentioned in the ISI file (both citing and cited articles). 

In practice, we execute the software, which raises an exception when it stumbles upon a missing entries. We add the entry to the BibTeX file, the run the software again. We iterate similarly until no more exception is raised. Once this part of the process is over, the software can extract all the graphs listed above. 


## Extension
N/A

## Dependencies
Here are the dependencies for BiblioProcess:
* [JDOM](http://www.jdom.org) v2.06 is used to access XML files.

## Todo
* Other types of networks could be extracted, see [Sci²](http://wiki.cns.iu.edu/pages/viewpage.action?pageId=1245863#id-4.9NetworkAnalysis(WithWhom?)-4.9.1NetworkExtraction).

## References
 * **[XX'19]** X. Yyyyyy & A. Bbbbbb, *My title*, My journal, X(X)/XX-XX, 201X. [doi: XXXXXXXXXX](https://doi.org/XXXXXXXXXX) - [⟨hal-XXXXXXXX⟩](https://hal.archives-ouvertes.fr/hal-XXXXXXXX)
