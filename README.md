BiblioProcess
=======
*Processing of bibliographic files*

* Copyright 2011-19 Vincent Labatut

BiblioProcess is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation. For source availability and license information see `licence.txt`

* Lab website: http://lia.univ-avignon.fr/
* GitHub repo: https://github.com/CompNet/BiblioProcess
* Contact: vincent.labatut@univ-avignon.fr

-----------------------------------------------------------------------

## Description
This software aims at extracting networks from bibliographic corpora. It can open Bibtex files, to get the detail of a collection of articles,
as well as Thomson ISI files, in order to get the bibliographic references contained in these articles. It matches both files and exports several networks:
* Citation networks
  * Article citation networks: nodes are articles, links are directed from the cited to the citing articles.
  * Author citation networks: nodes are authors, links are directed from the cited to the citing authors, weights are the numbers of citations.
* Coauthorship networks
	* Article coauthorship networks: nodes are articles, undirected links connect articles with common authors, weights are Jaccard's coefficient of compared authors.
	* Author coauthorship networks: nodes are authors, undirected links connect co-authors, weights are the numbers of co-authored papers.
* Cocitation networks
	* Article co-citing networks: nodes are articles, undirected links connect articles citing the same reference, weights are the numbers of co-citations.
	* Article co-cited networks: nodes are articles, undirected links connect articles cited by the same reference, weights are the numbers of co-citations.

**This software is currently in development. Don't use it (yet)!**

## Organization
* Package `data` contains classes related to article and network representation.
* Package `inout` contains classes to read BibTeX and ISI files.
* Package `tex` contains classes related to the processing of LaTeX files.
* Package `tools` contains helper classes.

## Installation
* Nothing to install, besides the Java Runtime Environment (JRE).

## Use


## Extension

## Dependencies
Here are the dependencies for BiblioProcess:
* [JDOM](http://www.jdom.org) v2.06 is used to access XML files.

## Todo
* Other types of networks could be extracted, see [Sci²](http://wiki.cns.iu.edu/pages/viewpage.action?pageId=1245863#id-4.9NetworkAnalysis(WithWhom?)-4.9.1NetworkExtraction).

## References
[xxx'YY] xxxxx.
