@Grapes([
  @Grab(group='org.nanopub', module='nanopub', version='1.18'),
  @Grab('com.xlson.groovycsv:groovycsv:1.1')
])

import static com.xlson.groovycsv.CsvParser.parseCsv

import org.nanopub.Nanopub;
import org.nanopub.NanopubCreator;
import org.nanopub.NanopubUtils;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.SKOS;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.rio.RDFFormat;

factory = ValueFactoryImpl.getInstance()

input = args[0]

counter = 0

hasSource = factory.createURI("http://semanticscience.org/resource/SIO_000253")
database = factory.createURI("http://semanticscience.org/resource/SIO_000750")
SIO000983 = factory.createURI("http://semanticscience.org/resource/SIO_000983")
SIO000628 = factory.createURI("http://semanticscience.org/resource/SIO_000628")

ncitC4873 = factory.createURI("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#C4873")
DOID0050177 = factory.createURI("http://purl.obolibrary.org/obo/DOID_0050177")
wasDerivedFrom = factory.createURI("http://www.w3.org/ns/prov#wasDerivedFrom")
wasGeneratedBy = factory.createURI("http://www.w3.org/ns/prov#wasGeneratedBy")
Q241953 = factory.createURI("http://www.wikidata.org/entity/Q241953")
ECO000218 = factory.createURI("http://purl.obolibrary.org/obo/eco.owl#ECO_000218")
rights = factory.createURI("http://purl.org/dc/terms/rights")
cczero = factory.createURI("http://creativecommons.org/publicdomain/zero/1.0/")
p3331 = factory.createURI("http://www.wikidata.org/prop/direct/P3331")

figshare = factory.createURI("https://doi.org/10.6084/m9.figshare.7718537.v1")

def text = new File(input).getText()
def data = parseCsv(text, separator: '\t')

data.each { line ->
  geneID = line["ENSID"]
  hgnc = line["HGNC"]
  genePMID = line["PMID Gene-disease"]
  disease = line["Disease name"]
  diseasePMID = line["PMID Disease"]
  omimID = line["Disease OMIM ID"]
  
  if (geneID.isEmpty()) return
  
  // the Disease NanoPub
  
  counter++
  nanopubIRI = "http://purl.org/nanopub/temp/np" + counter

  creator = new NanopubCreator(nanopubIRI)
  creator.addTimestamp(new Date())
  creator.addNamespace("np", "http://www.nanopub.org/nschema#")
  creator.addNamespace("orcid", "http://orcid.org/")
  creator.addNamespace("wdt", "http://www.wikidata.org/prop/direct/")
  creator.addNamespace("pav", "http://purl.org/pav/")
  creator.addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
  creator.addNamespace("dct", "http://purl.org/dc/terms/")
  creator.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema#")
  creator.addNamespace("prov", "http://www.w3.org/ns/prov#")
  creator.addNamespace("dc", "http://purl.org/dc/elements/1.1/")
  creator.addNamespace("pmid", "http://identifiers.org/pubmed/")
  creator.addNamespace("ens", "http://identifiers.org/ensembl/")
  creator.addNamespace("cczero", "http://creativecommons.org/publicdomain/zero/1.0/")
  creator.addNamespace("hgnc", "http://identifiers.org/hgnc/")
  creator.addNamespace("omim", "http://identifiers.org/omim/")
  creator.addNamespace("eco", "http://purl.obolibrary.org/obo/eco.owl#")
  creator.addNamespace("ncit", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#")
  creator.addNamespace("obo", "http://purl.obolibrary.org/obo/")
  creator.addNamespace("figshare", "https://doi.org/10.6084/")

  omimURI = "http://identifiers.org/omim/" + omimID
  omim = factory.createURI(omimURI)
  creator.addAssertionStatement(omim, RDF.TYPE, ncitC4873)
  creator.addAssertionStatement(omim, RDF.TYPE, DOID0050177)
  creator.addAssertionStatement(omim, RDFS.LABEL, factory.createLiteral(disease))
  
  creator.addProvenanceStatement(creator.getAssertionUri(), wasDerivedFrom, figshare)
  if (diseasePMID != null && !diseasePMID.isEmpty() && !diseasePMID.contains(",")) {
    try {
      pmidVal = Integer.parseInt(diseasePMID);
      diseasePMIDURI = factory.createURI("http://identifiers.org/pubmed/" + diseasePMID)
      creator.addProvenanceStatement(creator.getAssertionUri(), wasDerivedFrom, diseasePMIDURI)
    } catch (Exception exc) {} // ignore
  }
  creator.addProvenanceStatement(creator.getAssertionUri(), wasGeneratedBy, ECO000218)
  
  creator.addPubinfoStatement(rights, cczero)
  egon = creator.getOrcidUri("0000-0001-7542-0286")
  freddie = creator.getOrcidUri("0000-0002-7770-620X")
  creator.addCreator(freddie)
  creator.addCreator(egon)

  trustedPub = creator.finalizeTrustyNanopub()

  outputBuffer = new StringBuffer();
  outputBuffer.append(NanopubUtils.writeToString(trustedPub, RDFFormat.TRIG)).append("\n\n");
  println outputBuffer.toString()  
  
  // the Association NanoPub
  
  counter++
  nanopubIRI = "http://purl.org/nanopub/temp/np" + counter

  creator = new NanopubCreator(nanopubIRI)
  creator.addTimestamp(new Date())
  creator.addNamespace("np", "http://www.nanopub.org/nschema#")
  creator.addNamespace("orcid", "http://orcid.org/")
  creator.addNamespace("wdt", "http://www.wikidata.org/prop/direct/")
  creator.addNamespace("pav", "http://purl.org/pav/")
  creator.addNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#")
  creator.addNamespace("dct", "http://purl.org/dc/terms/")
  creator.addNamespace("xsd", "http://www.w3.org/2001/XMLSchema#")
  creator.addNamespace("prov", "http://www.w3.org/ns/prov#")
  creator.addNamespace("dc", "http://purl.org/dc/elements/1.1/")
  creator.addNamespace("pmid", "http://identifiers.org/pubmed/")
  creator.addNamespace("ens", "http://identifiers.org/ensembl/")
  creator.addNamespace("cczero", "http://creativecommons.org/publicdomain/zero/1.0/")
  creator.addNamespace("hgnc", "http://identifiers.org/hgnc/")
  creator.addNamespace("omim", "http://identifiers.org/omim/")
  creator.addNamespace("eco", "http://purl.obolibrary.org/obo/eco.owl#")
  creator.addNamespace("ncit", "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#")
  creator.addNamespace("obo", "http://purl.obolibrary.org/obo/")
  creator.addNamespace("figshare", "https://doi.org/10.6084/")
  creator.addNamespace("bc", "http://www.bigcat.unimaas.nl/rett/")
  creator.addNamespace("sio", "http://semanticscience.org/resource/")

  assocURI = "http://www.bigcat.unimaas.nl/rett/as" + Math.abs((geneID+omimID).hashCode())
  assoc = factory.createURI(assocURI)
  creator.addAssertionStatement(assoc, RDF.TYPE, SIO000983)
  geneURI = "http://identifiers.org/ensembl/" + geneID
  gene = factory.createURI(geneURI)
  creator.addAssertionStatement(assoc, SIO000628, gene)
  creator.addAssertionStatement(gene, RDFS.LABEL, factory.createLiteral(hgnc))
  omimURI = "http://identifiers.org/omim/" + omimID
  omim = factory.createURI(omimURI)
  creator.addAssertionStatement(assoc, SIO000628, omim)
  creator.addAssertionStatement(omim, RDFS.LABEL, factory.createLiteral(disease))

  creator.addProvenanceStatement(creator.getAssertionUri(), wasDerivedFrom, figshare)
  if (genePMID != null && !genePMID.isEmpty() && !genePMID.contains(",")) {
    genePMIDURI = factory.createURI("http://identifiers.org/pubmed/" + genePMID)
    creator.addProvenanceStatement(creator.getAssertionUri(), wasDerivedFrom, genePMIDURI)
  }
  creator.addProvenanceStatement(creator.getAssertionUri(), wasGeneratedBy, ECO000218)
  
  creator.addPubinfoStatement(rights, cczero)
  egon = creator.getOrcidUri("0000-0001-7542-0286")
  freddie = creator.getOrcidUri("0000-0002-7770-620X")
  creator.addCreator(freddie)
  creator.addCreator(egon)

  trustedPub = creator.finalizeTrustyNanopub()

  outputBuffer = new StringBuffer();
  outputBuffer.append(NanopubUtils.writeToString(trustedPub, RDFFormat.TRIG)).append("\n\n");
  println outputBuffer.toString()

}
