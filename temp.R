library("igraph")


# load network
folder <- "C:/Users/Vincent/Documents/Travail/Ecrits/Network Extraction/ComDet Biblio/data/2012.07.13 - individual/_"
#prefix <- "citations"
#prefix <- "cociting"
prefix <- "cocited"
net.file <- paste(folder,prefix,".xml",sep="")
g <- read.graph(file=net.file,format="graphml")


# clusters
no.clusters(graph=g, mode="weak") # components: 4 303 10353


# remove hubs
print(rev(sort(degree(g,mode="in")))[1:15])
print(vcount(g))
for(i in 1:10)
{	d <- degree(g,mode="in")
	index <- which.max(d) - 1
	g <- delete.vertices(g,v=index)
}
print(vcount(g))
print(max(degree(g,mode="in")))


# remove isolates
print(min(degree(g)))
indices <- which(degree(g)==0) - 1
g <- delete.vertices(g,v=indices)
print(vcount(g))
print(min(degree(g)))


# communities
communities <- walktrap.community(graph=g)
membership <- communities$membership
com.nbr <- length(unique(membership))
colors <- rainbow(com.nbr,v=0.8)
clrs <- colors[membership]
modularity(g,membership) #modularity: 0.5481472 0.4594513 0.02327592

# layout
lay.file <- paste(folder,prefix,".layout",sep="")
#lay <- layout.kamada.kawai(graph=g)
lay <- layout.drl(graph=g)
#lay <- layout.circle(graph=g)
write(file=lay.file,lay)


# plot network
plot.file <- paste(folder,prefix,".pdf",sep="")
pdf(file=plot.file,bg="white")
plot.igraph(x=g,
	vertex.size=3, vertex.label.cex=0.2, vertex.color=clrs,
	edge.arrow.size=0.5, edge.arrow.width=0.5,
	vertex.label=NA,
	layout=lay)
dev.off()
