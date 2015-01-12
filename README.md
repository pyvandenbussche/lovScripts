# Linked Open Vocabularies (LOV) - Scripts

This is the [Linked Open Vocabularies (LOV) Scripts code repository](http://lov.okfn.org/dataset/lov/). LOV provides a choice of several hundreds of such vocabularies, based on quality requirements including URI stability and availability on the Web, use of standard formats and publication best practices, quality metadata and documentation, identifiable and trustable publication body, proper versioning policy.

Scripts include:

**Aggregator** - check for vocabulary updates and update vocabulary records in case something has changed or the LOV Bot has not been able to access or parse the file.

**Elasticsearch** - set of 2 scripts for creating the index and index LOV data using the NQuads file

**Mongo2RDF** - Export the LOV data from MongoDB to RDF representation (N3 file for LOV records only and NQuads for LOV records + latest version of each vocabulary)

**Rdf2mongo** - For the LOV migration from v2 to v3

**Stats** - Generates summary stats for vocabulaies, tags, etc. stored in MongoDB.

**Suggest** - Access to LOV bot for getting an analysis of a URI.



## Install

**NOTE:** You need to have node.js, mongodb and elasticsearch installed and running.

```sh
  $ git clone git://github.com/pyvandenbussche/lovScripts.git
  $ mvn install
```


## License
 LOV code and dataset are licensed under a [Creative Commons Attribution 4.0 International License]( https://creativecommons.org/licenses/by/4.0/).
