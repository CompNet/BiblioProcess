# Extracts networks using the HAL API.
# https://api.archives-ouvertes.fr/docs/search
# https://api.archives-ouvertes.fr/docs/search/schema/fields/#fields
# 
# Author: Vincent Labatut # 04/2024
# 
# 
# setwd("D:/Users/Vincent/eclipse/workspaces/Extraction/BiblioProcess")
# source("res/scripts/hal_extraction.R")
###############################################################################
library("odyssey")		# https://rdrr.io/github/nfrerebeau/odyssey/f/README.md
library("text2vec")		# https://www.rdocumentation.org/packages/text2vec/versions/0.6.4
library("SnowballC")	# stemmer
library("plot.matrix")
library("igraph")


# fields used to select the articles by date
date.fields <- c(
	"conferenceEndDateY_i",
	"conferenceStartDateY_i",
	"producedDateY_i",
	"ePublicationDateY_i",
	"publicationDateY_i",
#	"releasedDateY_i",
#	"submittedDateY_i",
	"writingDateY_i"
)

col.names <- c(
	"title_s", 
	"producedDate_tdate", 
	"halId_s", 
	"fr_abstract_s", 
	"en_abstract_s"
)

# targeted authors
authors <- c(
	# Yo
	"Vincent Labatut"=482,
	
	# BEAGLE
	"Guillaume Beslon"=4190,
	"Anton Crombach"=174020,
	"Jonathan Rouzaud-Cornabas"=7579,
	
	# BD
	"Armand Baboli"=13449,
	"Angela Bonifati"=4999,
	"Sylvie Cazalens"=7493,
	"Francoise Conil"=15199,
	"Emmanuel Coquery"=4088,
	"Stephane Coulondre"=7024,
	"Fabien De Marchi"=6947,
	"Fabien Duchateau"=4098,
	"Javier Alfonso Espinosa Oviedo"=957720,
	"Franck Favetta"=4204,
	"Yann Gripay"=4133,
	"Mohand-Said Hacid"=7283,
	"Philippe Lamarre"=4087,
	"Nicolas Lumineau"=4091,
	"Andrea Mauri"=1208304,
	"Jean-Marc Petit"=4224,
	"John Samuel"=951,
	"Vasile-Marian Scuturici"=3040,
	"Sylvie Servigne"=4123,
	"Anne Tchounikine"=6932,
	"Romuald Thion"=4093,
	"Riccardo Tommasini"=1273255,
	"Genoveva Vargas-Solar"=7250,
	
	# DM2L
	"Alexandre Aussem"=7691,
	"Khalid Benabdeslem"=4692,
	"Remy Cazabet"=15745,
	"Haytham Elghazel"=7703,
	"Ludovic Moncla"=172,
	"Florence Perraud"=7835,
	"Christophe Rigotti"=4426,
	"Celine Robardet"=3355,
	
	# DRIM
	"Nadia Bennani"=5053,
	"Sara Bouchenak"=6304,
	"Lionel Brunie"=4389,
	"Sylvie Calabretto"=7155,
	"Elod Egyed-Zsigmond"=4181,
	"Omar Hasan"=5705,
	"Mathieu Maranzana"=5428,
	"Vlad Nitu"=749417,
	"Diana Nurbakova"=8911,
	"Pierre-Edouard Portier"=2642,
	"Didier Puzenat"=14193,
	"Xavier Urbain"=7387,
	
	# GOAL
	"Nicolas Bousquet"=11616,
	"Eric Duchene"=4993,
	"Brice Effantin"=4714,
	"Laurent Feuilloley"=177191,
	"Mohammed Haddad"=7664,
	"Hamamache Kheddouci"=7630,
	"Samba Ndojh Ndiaye"=4125,
	"Aline Parreau"=3752,
	"Theo Pierron"=737291,
	"Hamida Seba"=4131,
	
	# IMAGINE
	"Mohsen Ardabilian"=7814,
	"Atilla Baskurt"=4271,
	"Stephane Bres"=7718,
	"Liming Chen"=7562,
	"Carlos Crispim-Junior"=20421,
	"Franck Davoine"=3173,
	"Emmanuel Dellandrea"=7701,
	"Stephane Derrode"=3569,
	"Stefan Duffner"=3908,
	"Veronique Eglin"=4096,
	"Christophe Garcia"=3989,
	"Khalid Idrissi"=6793,
	"Bertrand Kerautret"=176679,
	"Frank Lebourgeois"=7699,
	"Eric Lombardi"=12850,
	"Serge Miguet"=7700,
	"Shaifali Parashar"=18122,
	"Catherine Pothier"=13543,
	"Alexandre Sadegh Saidi"=7220,
	"Mihaela Scuturici"=7685,
	"Iuliia Tkachenko"=177740,
	"Bruno Tellez"=4095,
	"Laure Tougne Rodet"=13655,
	
	# ORIGAMI 
	"Martial Tola"=9662,
	"Lorenzo Marnat"=1187006,
	"Eric Lombardi"=12850,
	"Raphaëlle Chaine"=7223,
	"Eric Galin"=7221,
	"Guillaume Lavoue"=3939,
	"Florent Dupont"=2839,
	"Victor Ostromoukhov"=7709,
	"Gilles Gesquiere"=3634,
	"Jean-Claude Iehl"=6791,
	"Fabrice Jaillet"=4005,
	"Eliane Perna"=7877,
	"Eric Guerin"=2945,
	"Johanna Delanoy"=1186993,
	"Florence Denis"=3935,
	"Vincent Vidal"=4136,
	"Adrien Peytavie"=3663,
	"Pierre Raimbaud"=177685,
	"Sylvain Brandel"=4028,
	"Tristan Roussillon"=4020,
	"Vincent Nivoliers"=3469,
	"Jean-Philippe Farrugia"=6833,
	"Florence Zara"=3945,
	"Thierry Excoffier"=9828,
	"Guillaume Damiand"=3353,
	"David Coeurjolly"=2745,
	"Nicolas Bonneel"=4159,
	"Julie Digne"=3109,
	
	# SAARA
	"Saida Bouakaz"=5389,
	"Elodie Desseree"=7696,
	"Erwan Guillou"=3447,
	"Hamid Ladjal"=5271,
	"Alexandre Meyer"=3446,
	"Nicolas Pronost"=2922,
	"Behzad Shariat"=7859,
	
	# SICAL
	"Rene Chalon"=2927,
	"Antoine Coutrot"=178893,
	"Bertrand David"=3299,
	"Benoît Encelle"=7106,
	"Elise Lavoue"=3046,
	"Mathieu Loiseau"=11378,
	"Francoise Sandoz-Guermond"=7756,
	"Karim Sehaba"=5239,
	"Audrey Serna"=4578,
	"Romain Vuillemot"=2912,
	
	# SOC
	"Mahmoud Barhamgi"=7834,
	"Nabila Benharkat"=5364,
	"Karim Benouaret"=7413,
	"Djamal Benslimane"=2923,
	"Frederique Biennier"=3072,
	"Mohamed Essaid Khanouche"=1336330,
	"Noura Faci"=5279,
	"Jean-Patrick Gelas"=1004721,
	"Chirine Ghedira Guegan"=5354,
	"Parisa Ghodous"=6844,
	
	# SyCoSMA
	"Samir Aknine"=4210,
	"Frederic Armetta"=5426,
	"Veronique Deslandres"=3289,
	"Mathieu Lefort"=3549,
	"Laetitia Matignon"=3290,
	"Bruno Yun"=1280752,
	
	# TWEAK
	"Beatrice Fuchs"=3080,
	"Nathalie Guin"=4707,
	"Stephanie Jean-Daubias"=3077,
	"Frederique Laforest"=2645,
	"Marie Lefevre"=4719,
	"Lionel Medini"=6745,
	"Alain Mille"=3994,
	"Nadia Yacoubi"=1130992
)
teams <- list(
	"none"=c("Vincent Labatut"),
	"BEAGLE"=c("Guillaume Beslon", "Anton Crombach", "Jonathan Rouzaud-Cornabas"),
	"BD"=c("Armand Baboli", "Angela Bonifati", "Sylvie Cazalens", "Francoise Conil", "Emmanuel Coquery", "Stephane Coulondre", "Fabien De Marchi", "Fabien Duchateau", "Javier Alfonso Espinosa Oviedo", "Franck Favetta", "Yann Gripay", "Mohand-Said Hacid", "Philippe Lamarre", "Nicolas Lumineau", "Andrea Mauri", "Jean-Marc Petit", "John Samuel", "Vasile-Marian Scuturici", "Sylvie Servigne", "Anne Tchounikine", "Romuald Thion", "Riccardo Tommasini", "Genoveva Vargas-Solar"),
	"DM2L"=c("Alexandre Aussem", "Khalid Benabdeslem", "Remy Cazabet", "Haytham Elghazel", "Ludovic Moncla", "Florence Perraud", "Christophe Rigotti", "Celine Robardet"),
	"DRIM"=c("Nadia Bennani", "Sara Bouchenak", "Lionel Brunie", "Sylvie Calabretto", "Elod Egyed-Zsigmond", "Omar Hasan", "Mathieu Maranzana", "Vlad Nitu", "Diana Nurbakova", "Pierre-Edouard Portier", "Didier Puzenat", "Xavier Urbain"),
	"GOAL"=c("Nicolas Bousquet", "Eric Duchene", "Brice Effantin", "Laurent Feuilloley", "Mohammed Haddad", "Hamamache Kheddouci", "Samba Ndojh Ndiaye", "Aline Parreau", "Theo Pierron", "Hamida Seba"),
	"IMAGINE"=c("Mohsen Ardabilian", "Atilla Baskurt", "Stephane Bres", "Liming Chen", "Carlos Crispim-Junior", "Franck Davoine", "Emmanuel Dellandrea", "Stephane Derrode", "Stefan Duffner", "Veronique Eglin", "Christophe Garcia", "Khalid Idrissi", "Bertrand Kerautret", "Frank Lebourgeois", "Eric Lombardi", "Serge Miguet", "Shaifali Parashar", "Catherine Pothier", "Alexandre Sadegh Saidi", "Mihaela Scuturici", "Iuliia Tkachenko", "Bruno Tellez", "Laure Tougne Rodet"),
	"ORIGAMI"=c("Martial Tola", "Lorenzo Marnat", "Eric Lombardi", "Raphaëlle Chaine", "Eric Galin", "Guillaume Lavoue", "Florent Dupont", "Victor Ostromoukhov", "Gilles Gesquiere", "Jean-Claude Iehl", "Fabrice Jaillet", "Eliane Perna", "Eric Guerin", "Johanna Delanoy", "Florence Denis", "Vincent Vidal", "Adrien Peytavie", "Pierre Raimbaud", "Sylvain Brandel", "Tristan Roussillon", "Vincent Nivoliers", "Jean-Philippe Farrugia", "Florence Zara", "Thierry Excoffier", "Guillaume Damiand", "David Coeurjolly", "Nicolas Bonneel", "Julie Digne"),
	"SAARA"=c("Saida Bouakaz", "Elodie Desseree", "Erwan Guillou", "Hamid Ladjal", "Alexandre Meyer", "Nicolas Pronost", "Behzad Shariat"),
	"SICAL"=c("Rene Chalon", "Antoine Coutrot", "Bertrand David", "Benoît Encelle", "Elise Lavoue", "Mathieu Loiseau", "Francoise Sandoz-Guermond", "Karim Sehaba", "Audrey Serna", "Romain Vuillemot"),
	"SOC"=c("Mahmoud Barhamgi", "Nabila Benharkat", "Karim Benouaret", "Djamal Benslimane", "Frederique Biennier", "Mohamed Essaid Khanouche", "Noura Faci", "Jean-Patrick Gelas", "Chirine Ghedira Guegan", "Parisa Ghodous"),
	"SyCoSMA"=c("Samir Aknine", "Frederic Armetta", "Veronique Deslandres", "Mathieu Lefort", "Laetitia Matignon", "Bruno Yun"),
	"TWEAK"=c("Beatrice Fuchs", "Nathalie Guin", "Stephanie Jean-Daubias", "Frederique Laforest", "Marie Lefevre", "Lionel Medini", "Alain Mille", "Nadia Yacoubi")
)
lab <- "liris"

# LIA
#authors <- c(
#	# Pluridis
#	"Abderrahim Benslimane"=830662,
#	
#	# Cornet
#	"Francesco De Pellegrini"=1044331,
#	"Rosa Figueiredo"=176258,
#	"Rachid El-Azouzi"=175784,
#	"Serigne Gueye"=176897,
#	"Majed Haddad"=21595,
#	"Yezekael Hayel"=753572,
#	"Pierre Jourlin"=175244,
#	"Vincent Labatut"=482,
#	"Eric Sanjuan"=912763,
#	"Juan-Manuel Torres"=12610,
#	"Mathilde Vernet"=174768,
#	"Fen Zhou"=183709,
#	
#	# SLG
#	"Jean-Francois Bonastre"=172421,
#	"Pierre Michel Bousquet"=1078752,
#	"Richard Dufour"=178348,
#	"Yannick Esteve"=11645,
#	"Corinne Fredouille"=173870,
#	"Stephane Huet"=10005,
#	"Bassam Jabaian"=172824,
#	"Fabrice Lefevre"=175133,
#	"Georges Linares"=4977,
#	"Driss Matrouf"=176307,
#	"Salima Mdhaffar"=17127,
#	"Mohamed Morchid"=21451,
#	"Mickael Rouvier"=982551
#)
#teams <- list(
#	"Pluridis"=c("Abderrahim Benslimane"),
#	"Cornet"=c("Francesco De Pellegrini", "Rosa Figueiredo", "Rachid El-Azouzi", "Serigne Gueye", "Majed Haddad", "Yezekael Hayel", "Pierre Jourlin", "Vincent Labatut", "Eric Sanjuan", "Juan-Manuel Torres", "Mathilde Vernet", "Fen Zhou"),
#	"Slg"=c("Jean-Francois Bonastre", "Pierre Michel Bousquet", "Richard Dufour", "Yannick Esteve", "Corinne Fredouille", "Stephane Huet", "Bassam Jabaian", "Fabrice Lefevre", "Georges Linares", "Driss Matrouf", "Salima Mdhaffar", "Mohamed Morchid", "Mickael Rouvier")
#)
#lab <- "lia"

# loop over authors
texts <- c()
papers <- list()
for(a in 1:length(authors))
{	name <- names(authors)[a]
	author <- authors[a]
	cat(">> Processing author \"",name,"\" (",author,")\n",sep="")
	
	# retrieve the abstracts
	cum <- NA
	for(field in date.fields)
	{	cat("Dealing with date \"",field,"\"\n",sep="")
		q <- hal_api()
		q <- hal_query(x=q, value=author, field="authIdPerson_i")
		q <- hal_select(x=q, col.names)
		q <- hal_filter(x=q, value="[2014 TO 2025]", field=field)
		res <- as.data.frame(hal_search(x=q, limit=10000))
		if(ncol(res)!=length(col.names))
		{	missing <- setdiff(col.names, colnames(res))
			mm <- matrix(NA,nrow=nrow(res),ncol=length(missing))
			colnames(mm) <- missing
			res <- cbind(res, mm)
			res <- res[,col.names]
		}
		# add to cumulative table
		if(all(is.na(cum)))
			cum <- res
		else if(nrow(res)>0)
		{	ids.old <- cum$halId_s
			ids.new <- res$halId_s
			ids.keep <- setdiff(ids.new,ids.old)
			if(length(ids.keep)>0)
			{	idx <- match(ids.keep, res$halId_s)
				cum <- rbind(cum, res[idx,])
			}
		}
	}
	
	# clean the text
	if(nrow(cum)>0)
	{	titles <- paste0(cum[,"title_s"][!is.na(cum[,"title_s"])], collapse=", ")
		if("fr_abstract_s" %in% colnames(cum))
			abstracts.fr <- paste0(cum[,"fr_abstract_s"][!is.na(cum[,"fr_abstract_s"])], collapse=", ")
		else
			abstracts.fr <- ""	
		abstracts.en <- paste0(cum[,"en_abstract_s"][!is.na(cum[,"en_abstract_s"])], collapse=", ")
#		texts[name] <- paste0(titles, abstracts.fr, abstracts.en)
		texts[name] <- paste0(titles, abstracts.en)
	}
	else
		texts[name] <- ""
	
	# update paper list
	papers[[name]] <- unique(cum[,"halId_s"])
}

# remove authors without any text
idx <- which(texts=="")
removed <- c()
if(length(idx)>0)
{	removed <- names(texts)[idx]
	cat("The following authors have no associated content: ")
	print(idx)
	texts <- texts[-idx]
}

# computing tfidf scores
stem_tokenizer1 =function(x) {
	tokens = word_tokenizer(x)
	lapply(tokens, SnowballC::wordStem, language="en")
}
tokens <- stem_tokenizer1(tolower(texts))
it <- itoken(tokens)
vocab <- create_vocabulary(it)
pruned.vocab <- prune_vocabulary(vocabulary=vocab, doc_count_max=100)
vectorizer = vocab_vectorizer(pruned.vocab)
dtm <- create_dtm(it, vectorizer)
model.tfidf <- TfIdf$new()
dtm.tfidf <- model.tfidf$fit_transform(dtm)
sim.mat <- as.matrix(sim2(x=dtm.tfidf, y=dtm.tfidf, method="cosine"))
plot(sim.mat)
hist(sim.mat[upper.tri(sim.mat,diag=FALSE)])

# build similarity graph
g.sim <- graph_from_adjacency_matrix(
		adjmatrix=sim.mat,
		mode="undirected",
		weighted=TRUE,
		diag=FALSE,
		add.colnames=NULL
	)
for(t in 1:length(teams))
{	team <- names(teams)[t]
	for(a in teams[[t]])
	{	#print(a)
		if(a %in% V(g.sim)$name)
			V(g.sim)[a]$team <- team
	}
}
write_graph(graph=g.sim, file=paste0("out/hal_sim_",lab,".graphml"), format="graphml")

# build co-author graph
g.collab <- make_empty_graph(n=length(papers), directed=FALSE)
V(g.collab)$name <- names(papers)
for(t in 1:length(teams))
{	team <- names(teams)[t]
	for(a in teams[[t]])
	{	if(a %in% V(g.collab)$name)
			V(g.collab)[a]$team <- team
	}
}
for(paper in unique(unlist(papers)))
{	authors <- names(papers)[sapply(papers,function(auths) paper %in% auths)]
	if(length(authors)>1)
	{	for(i in 1:(length(authors)-1))
		{	auth1 <- authors[i]
			for(j in (i+1):length(authors))
			{	auth2 <- authors[j]
				# edge already exists
				if(are_adjacent(graph=g.collab, auth1, auth2))
					E(g.collab)[auth1 %--% auth2]$weight <- E(g.collab)[auth1 %--% auth2]$weight + 1
				# edge does not exist yet
				else
					g.collab <- add_edges(graph=g.collab, edges=c(auth1,auth2), attr=list(weight=1))
			}
		}
	}
}
write_graph(graph=g.collab, file=paste0("out/hal_collab_",lab,".graphml"), format="graphml")
