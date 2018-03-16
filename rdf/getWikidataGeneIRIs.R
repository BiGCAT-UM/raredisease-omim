library(googlesheets)
library(foreach)
library(plyr)
library(rrdf)

#gs_ls(regex="Dataset Monogenic Rare Diseases")

omimspread <- gs_title("Dataset Monogenic Rare Diseases - Version 2018-03-14")
omimdata = gs_read(omimspread, ws="gene-disease-provenance")

store = new.rdf(ontology=FALSE)
add.prefix(store, "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
add.prefix(store, "rdfs", "http://www.w3.org/2000/01/rdf-schema#")
add.prefix(store, "ens", "http://identifiers.org/ensembl/")
add.prefix(store, "owl", "http://www.w3.org/2002/07/owl#")
add.prefix(store, "wd", "http://www.wikidata.org/entity/")

geneIDs = as.vector(unlist(omimdata[,"Gene stable ID"]))

geneidsAsValues = ""
for(id in geneIDs) {
  geneidsAsValues = paste(
    geneidsAsValues, "    \"", id, "\"\n",
    sep=""
  )
}

template = "
PREFIX wdt: <http://www.wikidata.org/prop/direct/>
SELECT * WHERE {
  VALUES ?geneid {
${geneidsAsValues}
  }
  ?gene wdt:P594 ?geneid}
"
query = sub("${geneidsAsValues}", geneidsAsValues, template, fixed=TRUE)
#print(query)

results = sparql.remote(
  "https://query.wikidata.org/bigdata/namespace/wdq/sparql",
  query,
  jena=FALSE
)
results

adply(results, 1, function(row) {
  wkid = as.character(row["gene"])
  geneid = as.character(row["geneid"])
  geneIRI = paste("http://identifiers.org/ensembl/", geneid, sep="")
  add.triple(store,
    geneIRI, "http://www.w3.org/2002/07/owl#sameAs", wkid
  )
})

save.rdf(store, "omim_wikidata_gene.ttl", format="N3")
