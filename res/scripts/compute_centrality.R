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
# setwd("D:/Users/Vincent/eclipse/workspaces/Extraction/BiblioProcess")
# source("res/scripts/compute_centrality.R")
######################################################################################
# Post processing of the extracted networks.
######################################################################################
library("igraph")


# file names
folder <- "out"
net.files <- c(
	"article_citation.graphml",
	"article_coauthorship.graphml",
	"article_cocited.graphml",
	"article_cociting.graphml",
	"author_citation.graphml",
	"author_coauthorship.graphml",
	"authorship.graphml"
)


# process each file
for(net.file in net.files)
{	cat("Loading graph \"",net.file,"\"\n",sep="")
	g <- read.graph(file=file.path(folder,net.file),format="graphml")
	
	# process node measures
	cat("..Processing node measures\n")
	V(g)$all_auth <- authority_score(graph=g, scale=TRUE)$vector
	V(g)$all_hub <- hub_score(graph=g, scale=TRUE)$vector
	V(g)$all_betw <- betweenness(graph=g, directed=is.directed(g), normalized=TRUE)
	if(is.directed(g))
	{	V(g)$all_clos <- closeness(graph=g, mode="all", normalized=TRUE)
	}else
	{	V(g)$all_clos_all <- closeness(graph=g, mode="all", normalized=TRUE)
		V(g)$all_clos_in <- closeness(graph=g, mode="in", normalized=TRUE)
		V(g)$all_clos_out <- closeness(graph=g, mode="out", normalized=TRUE)
	}
	V(g)$all_eigen <- eigen_centrality(graph=g, directed=is.directed(g), scale=TRUE)$vector
	V(g)$all_trans <- transitivity(graph=g, type="local", isolates="zero")
	
	# focus on the core nodes
	cat("..Filtering graph\n")
	g2 <- delete_vertices(g, V(g)$core=="NA")
	g2 <- delete_vertices(g2, !as.logical(V(g2)$core))
	#g2 <- delete_vertices(g2, degree(g2)<=1)	# not necessary
		
	# process node measures
	cat("..Processing node measures (again)\n")
	V(g2)$core_auth <- authority_score(graph=g2, scale=TRUE)$vector
	V(g2)$core_hub <- hub_score(graph=g2, scale=TRUE)$vector
	V(g2)$core_betw <- betweenness(graph=g2, directed=is.directed(g2), normalized=TRUE)
	if(is.directed(g2))
	{	V(g2)$core_clos <- closeness(graph=g2, mode="all", normalized=TRUE)
	}else
	{	V(g2)$core_clos_all <- closeness(graph=g2, mode="all", normalized=TRUE)
		V(g2)$core_clos_in <- closeness(graph=g2, mode="in", normalized=TRUE)
		V(g2)$core_clos_out <- closeness(graph=g2, mode="out", normalized=TRUE)
	}
	V(g2)$core_eigen <- eigen_centrality(graph=g2, directed=is.directed(g2), scale=TRUE)$vector
	V(g2)$core_trans <- transitivity(graph=g2, type="local", isolates="zero")
	
	# communities
	if(ecount(g2)>0)
	{	cat("..Detecting communities\n")
		{	# walktrap
			temp <- cluster_walktrap(graph=g2, modularity=FALSE)
			cat("....Walktrap:", max(temp$modularity), "\n")
			V(g2)$core_com_wt <- temp$membership
			# edge betweenness
			temp <- cluster_edge_betweenness(graph=g2, directed=is.directed(g2), weights=max(E(g2)$weight)+1-E(g2)$weight)
			cat("....EdgeBetweenness:", max(temp$modularity), "\n")
			V(g2)$core_com_eb <- temp$membership
			# fast greedy
			temp <- cluster_fast_greedy(graph=as.undirected(g2))
			cat("....FastGreedy:", max(temp$modularity), "\n")
			V(g2)$core_com_fg <- temp$membership
			# infomap
			temp <- cluster_infomap(graph=g2)
			cat("....InfoMap:", temp$modularity, "\n")
			V(g2)$core_com_im <- temp$membership
			# label propagation
			temp <- cluster_label_prop(graph=g2)
			cat("....LabelPropagation:", temp$modularity, "\n")
			V(g2)$core_com_lp <- temp$membership
			# leading eigenvector
			temp <- cluster_leading_eigen(graph=as.undirected(g2))
			cat("....LeadingEigenvector:", temp$modularity, "\n")
			V(g2)$core_com_ev <- temp$membership
			# louvain
			temp <- cluster_louvain(graph=as.undirected(g2))
			cat("....Louvain:", max(temp$modularity), "\n")
			V(g2)$core_com_lv <- temp$membership
			# spinglass
			if(clusters(g2)$no==1)
			{	temp <- cluster_spinglass(graph=g2)
				cat("....SpinGlass:", temp$modularity, "\n")
				V(g2)$core_com_sg <- temp$membership
			}
		}
	}
	
	# copy results to original graph
	cat("..Copying attributes\n")
	for(attr in vertex_attr_names(graph=g2))
	{	vals <- vertex_attr(graph=g2, name=attr) 
		idx <- match(V(g2)$id, V(g)$id)
		g <- set_vertex_attr(graph=g, name=attr, index=idx, value=vals)
	}
	
	# record graphml file
	cat("..Updating Graphml file\n")
	write.graph(graph=g, file=file.path(folder,net.file), format="graphml")
}
