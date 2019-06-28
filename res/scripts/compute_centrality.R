#
# Biblio Process
# Copyright 2011-19 Vincent Labatut 
# 
# This file is part of Biblio Process.
# 
# Biblio Process is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 2 of the License, or
# (at your option) any later version.
# 
# Biblio Process is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with Biblio Process.  If not, see <http://www.gnu.org/licenses/>.
#
######################################################################################
# Post processing of the extracted networks.
######################################################################################
library("igraph")


# load network
folder <- "out/v3/"
#prefix <- "citations"
#prefix <- "cociting"
prefix <- "cocited"
net.file <- paste(folder,prefix,".xml",sep="")
g <- read.graph(file=net.file,format="graphml")


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
modularity(g,membership)

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
