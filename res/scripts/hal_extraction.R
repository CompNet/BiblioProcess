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


# fields used to select the articles by date
fields <- c(
	"conferenceEndDateY_i",
	"conferenceStartDateY_i",
	"producedDateY_i",
	"ePublicationDateY_i",
	"publicationDateY_i",
#	"releasedDateY_i",
#	"submittedDateY_i",
	"writingDateY_i"
)

# targeted authors
authors <- c(
	"Vincent Labatut"=482,
	
	"Alexandre Aussem"=7691,
	"Khalid Benabdeslem"=4692,
	"Rémy Cazabet"=15745,
	"Haytham Elghazel"=7703,
	"Ludovic Moncla"=172,
	"Florence Perraud"=7835,
	"Christophe Rigotti"=4426,
	"Céline Robardet"=3355
)

# loop over authors
texts <- c()
for(a in 1:length(authors))
{	name <- names(authors)[a]
	author <- authors[a]
	cat(">> Processing author \"",name,"\" (",author,")\n",sep="")
	
	# retrieve the abstracts
	cum <- NA
	for(field in fields)
	{	cat("Dealing with date \"",field,"\"\n",sep="")
		q <- hal_api()
		q <- hal_query(x=q, value=author, field="authIdPerson_i")
		q <- hal_select(x=q, "title_s", "producedDate_tdate", "halId_s", "fr_abstract_s", "en_abstract_s")
		q <- hal_filter(x=q, value="[2019 TO 2024]", field=field) 
		res <- as.data.frame(hal_search(x=q, limit=10000))
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

# computing tfidf scores
tokens = word_tokenizer(tolower(texts))
dtm = create_dtm(itoken(tokens), hash_vectorizer())
model.tfidf = TfIdf$new()
dtm.tfidf = model.tfidf$fit_transform(dtm)
sim.mat <- sim2(x=dtm.tfidf, y=dtm.tfidf, method="cosine")
plot(as.matrix(sim.mat))
