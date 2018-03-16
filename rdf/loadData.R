library(googlesheets)
library(foreach)
library(plyr)
library(rrdf)

#gs_ls(regex="Dataset Monogenic Rare Diseases")

omimspread <- gs_title("Dataset Monogenic Rare Diseases - Version 2018-03-14")
omimdata = gs_read(omimspread, ws="gene-disease-provenance")

datasetIRI = "http://www.bigcat.unimaas.nl/freddie/diseasegene/#version"

store = new.rdf(ontology=FALSE)
add.prefix(store, "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
add.prefix(store, "rdfs", "http://www.w3.org/2000/01/rdf-schema#")
add.prefix(store, "ens", "http://identifiers.org/ensembl/")
add.prefix(store, "ncit", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#")
add.prefix(store, "pubmed", "http://identifiers.org/pubmed/")
add.prefix(store, "sio", "http://semanticscience.org/resource/")
add.prefix(store, "assoc", "http://example.org/freddie/association/")
add.prefix(store, "pmid", "http://identifiers.org/pubmed/")
add.prefix(store, "omim", "http://identifiers.org/omim/")
add.prefix(store, "void", "http://rdfs.org/ns/void#")
add.prefix(store, "xsd", "http://www.w3.org/2001/XMLSchema#")
add.prefix(store, "dcterms", "http://purl.org/dc/terms/")

processAssociation = function(assocRow) {
  #print(assocRow)
  geneID = as.character(assocRow["Gene stable ID"])
  omimID = as.character(assocRow["OMIM ID"])
  hgncSymbols = as.character(assocRow["HGNC symbol"])
  pubmedIDs = as.character(assocRow["pubmed ID"])
  if (!is.na(geneID) && startsWith(geneID, "ENSG")) {
    # we have a gene
    geneIRI = paste("http://identifiers.org/ensembl/",geneID, sep="")
    add.triple(store,
      geneIRI,
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
      "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C16612"
    )
    add.data.triple(store,
      geneIRI, "http://purl.org/dc/terms/identifier", geneID
    )
    if (!is.na(hgncSymbols)) {
      adply(strsplit(hgncSymbols, ",")[[1]][1], 1, function(hgnc) {
        hgnc = trimws(hgnc)
        add.data.triple(store,
          geneIRI, "http://www.w3.org/2000/01/rdf-schema#label", hgnc
        )
      })
    }
    # and a disease
    omimIRI = paste("http://identifiers.org/omim/", omimID, sep="")

    # we have an associtation
    assocIRI = paste("http://example.org/freddie/association/assoc-",geneID,"-",omimID, sep="")
    add.triple(store,
      assocIRI,
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
      "http://semanticscience.org/resource/SIO_000983"
    )
    add.triple(store, assocIRI, "http://rdfs.org/ns/void#inDataset", datasetIRI)
    add.triple(store,
      assocIRI, "http://semanticscience.org/resource/SIO_000628", geneIRI
    )
    add.triple(store,
      assocIRI, "http://semanticscience.org/resource/SIO_000628", omimIRI
    )
    # add the provenence (one or more articles)
    if (!is.na(pubmedIDs)) {
      adply(strsplit(pubmedIDs, ",")[[1]], 1, function(pmid) {
        pmid = trimws(pmid)
        pubmedIRI = paste("http://identifiers.org/pubmed/", pmid, sep="")
        add.triple(store,
          assocIRI, "http://semanticscience.org/resource/SIO_000772", pubmedIRI
        )
        add.triple(store,
          pubmedIRI,
          "http://www.w3.org/1999/02/22-rdf-syntax-ns#type",
          "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C47902"
        )
        add.data.triple(store,
          pubmedIRI, "http://purl.org/dc/terms/identifier", pmid
        )
        add.data.triple(store,
          pubmedIRI, "http://www.w3.org/2000/01/rdf-schema#label",
          paste("pubmed:", pmid, sep="")
        )
        pubmedIDs = c(pubmedIDs, pmid)
      })
    }
  }
  #print(summarize.rdf(store))
}

pubmedIDs = c()
adply(omimdata, 1, processAssociation)
summarize.rdf(store)

save.rdf(store, "omim.ttl", format="N3")

#omimTurtle = asString.rdf(store, format="N3")
#cat(omimTurtle)
