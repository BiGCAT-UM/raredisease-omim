# convert2nps.groovy

Script to convert tabular input to nanopublications in Turtle format.

```shell
groovy convert2nps.groovy data.tsv > nanopubs.ttl
```

The `data.tsv` input looks like:

```tsv
ENSID	HGNC	PMID Gene-disease	Disease OMIM ID	Disease name	PMID Disease
ENSG00000188536	HBA2	1115799	604131	"Thalassemia, alpha-"	1115799
ENSG00000206172	HBA1	909779	604131	"Thalassemias, alpha-"	909779
ENSG00000052850	ALX4	11106354	609597	Parietal foramina 2 	11106354
ENSG00000102081	FMR1	1675488	300624	Fragile X syndrome 	1675488
```

