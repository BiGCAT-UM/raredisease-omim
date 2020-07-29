# R Scripts

## Requirements

To run this code you need an R installation, e.g. RStudio. Furthermore, you need the following
R packages (see [rdf/preprequisites.R](rdf/preprequisites.R)):

* googlesheets
* plyr
* foreach
* rrdf

## Creating the RDF

Three R scripts can then be executed to create RDF statements for the rare disease data:

* [rdf/loadData.R](rdf/loadData.R) creates the RDF for the data in the spreadsheet
* [rdf/getWikidataPubMedIRIs.R](rdf/getWikidataPubMedIRIs.R) creates a link set to Wikidata for PubMed identifiers
* [rdf/getWikidataGeneIRIs.R](rdf/getWikidataGeneIRIs.R) creates a link set to Wikidata for Ensembl gene identifiers

