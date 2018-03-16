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
add.prefix(store, "pubmed", "http://identifiers.org/pubmed/")
add.prefix(store, "owl", "http://www.w3.org/2002/07/owl#")
add.prefix(store, "wd", "http://www.wikidata.org/entity/")

pubmedIDs = as.vector(unlist(omimdata[,"pubmed ID"]))

pubmedidsAsValues = ""
for(pmid in pubmedIDs) {
  pubmedidsAsValues = paste(
    pubmedidsAsValues, "    \"", pmid, "\"\n",
    sep=""
  )
}

template = "
PREFIX wdt: <http://www.wikidata.org/prop/direct/>
SELECT * WHERE {
  VALUES ?pmid {
${pubmedidsAsValues}
  }
  ?article wdt:P698 ?pmid
}
"
query = sub("${pubmedidsAsValues}", pubmedidsAsValues, template, fixed=TRUE)
#print(query)

results = sparql.remote(
  "https://query.wikidata.org/bigdata/namespace/wdq/sparql",
  query,
  jena=FALSE
)
results

adply(results, 1, function(row) {
  wkid = as.character(row["article"])
  pmid = as.character(row["pmid"])
  pubmedIRI = paste("http://identifiers.org/pubmed/", pmid, sep="")
  add.triple(store,
    pubmedIRI, "http://www.w3.org/2002/07/owl#sameAs", wkid
  )
})


save.rdf(store, "omim_wikidata_pubmed.ttl", format="N3")
