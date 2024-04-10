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
	"Françoise Conil"=15199,
	"Emmanuel Coquery"=4088,
	"Stéphane Coulondre"=7024,
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
	"Rémy Cazabet"=15745,
	"Haytham Elghazel"=7703,
	"Ludovic Moncla"=172,
	"Florence Perraud"=7835,
	"Christophe Rigotti"=4426,
	"Céline Robardet"=3355,
	
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
	"Stéphane Bres"=7718,
	"Liming Chen"=7562,
	"Carlos Crispim-Junior"=20421,
	"Franck Davoine"=3173,
	"Emmanuel Dellandréa"=7701,
	"Stéphane Derrode"=3569,
	"Stefan Duffner"=3908,
	"Véronique Eglin"=4096,
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
	"Guillaume Lavoué"=3939,
	"Florent Dupont"=2839,
	"Victor Ostromoukhov"=7709,
	"Gilles Gesquiere"=3634,
	"Jean-Claude Iehl"=6791,
	"Fabrice Jaillet"=4005,
	"Eliane Perna"=7877,
	"Eric Guérin"=2945,
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
	"Elodie Desserée"=7696,
	"Erwan Guillou"=3447,
	"Hamid Ladjal"=5271,
	"Alexandre Meyer"=3446,
	"Nicolas Pronost"=2922,
	"Behzad Shariat"=7859,
	
	# SICAL
	"René Chalon"=2927,
	"Antoine Coutrot"=178893,
	"Bertrand David"=3299,
	"Benoît Encelle"=7106,
	"Elise Lavoué"=3046,
	"Mathieu Loiseau"=11378,
	"Françoise Sandoz-Guermond"=7756,
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
	"Frédéric Armetta"=5426,
	"Véronique Deslandres"=3289,
	"Mathieu Lefort"=3549,
	"Laetitia Matignon"=3290,
	"Bruno Yun"=1280752,
	
	# TWEAK
	"Béatrice Fuchs"=3080,
	"Nathalie Guin"=4707,
	"Stéphanie Jean-Daubias"=3077,
	"Frédérique Laforest"=2645,
	"Marie Lefevre"=4719,
	"Lionel Médini"=6745,
	"Alain Mille"=3994,
	"Nadia Yacoubi"=1130992
)

# loop over authors
texts <- c()
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
		q <- hal_filter(x=q, value="[2019 TO 2024]", field=field) 
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
		texts[name] <- paste0(titles, abstracts.fr, abstracts.en)
	}
	else
		texts[name] <- ""
}

# remove authors without any text
idx <- which(texts=="")
cat("The following authors have no associated content: ")
print(idx)
texts <- texts[-idx]

# computing tfidf scores
tokens = word_tokenizer(tolower(texts))
dtm = create_dtm(itoken(tokens), hash_vectorizer())
model.tfidf = TfIdf$new()
dtm.tfidf = model.tfidf$fit_transform(dtm)
sim.mat <- as.matrix(sim2(x=dtm.tfidf, y=dtm.tfidf, method="cosine"))
plot(sim.mat)
hist(sim.mat[upper.tri(sim.mat,diag=FALSE)])

# build graph
g <- graph_from_adjacency_matrix(
		adjmatrix=sim.mat,
		mode="undirected",
		weighted=TRUE,
		diag=FALSE,
		add.colnames=NULL
	)
write_graph(graph=g, file="out/hal_network.graphml", format="graphml")
