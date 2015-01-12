package org.lov;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LovConstants {
	
	/* NAMESPACES */
	public static String NSP_BIBO="http://purl.org/ontology/bibo/";
	public static String NSP_DC="http://purl.org/dc/elements/1.1/";
	public static String NSP_DC_TERMS="http://purl.org/dc/terms/";
	public static String NSP_DCAT="http://www.w3.org/ns/dcat#";
	public static String NSP_FOAF="http://xmlns.com/foaf/0.1/";
	public static String NSP_FRBR="http://purl.org/vocab/frbr/core#";
	public static String NSP_LEXVO="http://lexvo.org/ontology#";
	public static String NSP_LOV="http://lov.okfn.org/dataset/lov/";
	public static String NSP_MOAT="http://moat-project.org/ns#";
	public static String NSP_MREL="http://id.loc.gov/vocabulary/relators/";
	public static String NSP_OWL="http://www.w3.org/2002/07/owl#";
	public static String NSP_RDF="http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static String NSP_RDFS="http://www.w3.org/2000/01/rdf-schema#";
	public static String NSP_REV="http://purl.org/stuff/rev#";
	public static String NSP_SCHEMA="http://schema.org/";
	public static String NSP_SKOS="http://www.w3.org/TR/skos-reference/#";
	public static String NSP_TAGS="http://www.holygoat.co.uk/owl/redwood/0.1/tags/";
	public static String NSP_VANN="http://purl.org/vocab/vann/";
	public static String NSP_VOAF="http://purl.org/vocommons/voaf#";
	public static String NSP_VOID="http://rdfs.org/ns/void#";
	public static String NSP_XSD="http://www.w3.org/2001/XMLSchema#";
	
	
	
	
	
	/* LOV Dataset */
	public static String LOV_VOCABULARYSPACE ="lov:LOV";
	public static String LOV_FULL_VOCABULARYSPACE =NSP_LOV+"LOV";
	public static String LOV_DATASET_URI ="http://lov.okfn.org/dataset/lov/lov.rdf";
	public static String LOV_BOT_URI = LOV_DATASET_URI+"#bot";
	public static String LOV_BOT_NAME = "[Bot] automatic vocabulary analyzer";
	
	
	/* XSD / RDF / RDFS / OWL */
	public static String XSD_FULL_DATE = NSP_XSD+"date";
	public static String XSD_FULL_DATETIME = NSP_XSD+"dateTime";
	public static String XSD_FULL_BOOLEAN = NSP_XSD+"boolean";
	public static String XSD_FULL_INTEGER = NSP_XSD+"integer";
	public static String RDF_TYPE = "rdf:type";
	public static String RDF_FULL_TYPE = NSP_RDF+"type";
	public static String RDFS_LABEL = "rdfs:label";
	public static String RDFS_FULL_LABEL = NSP_RDFS+"label";
	public static String OWL_IMPORTS="owl:imports";
	public static String OWL_FULL_IMPORTS=NSP_OWL+"imports";
	public static String RDFS_FULL_IS_DEFINED_BY = NSP_RDFS+"isDefinedBy";
	public static String OWL_VERSION_INFO="owl:versionInfo";
	public static String OWL_FULL_VERSION_INFO=NSP_OWL+"versionInfo";
	public static String OWL_FULL_SAMEAS=NSP_OWL+"sameAs";
	
	
	
	/* DC / FOAF / VOID */
	public static String DC_TERMS_CREATOR="dcterms:creator";
	public static String DC_TERMS_FULL_CREATOR = NSP_DC_TERMS+"creator";
	public static String DC_TERMS_CONTRIBUTOR="dcterms:contributor";
	public static String DC_TERMS_FULL_CONTRIBUTOR = NSP_DC_TERMS+"contributor";
	public static String DC_TERMS_DATE = "dcterms:date";
	public static String DC_TERMS_FULL_DATE = NSP_DC_TERMS+"date";
	public static String DC_TERMS_DESCRIPTION = "dcterms:description";
	public static String DC_TERMS_FULL_DESCRIPTION = NSP_DC_TERMS+"description";
	public static String DC_TERMS_HAS_PART="dcterms:hasPart";
	public static String DC_TERMS_FULL_IDENTIFIER = NSP_DC_TERMS+"identifier";
	public static String DC_TERMS_FULL_ISSUED=NSP_DC_TERMS+"issued";
	public static String DC_TERMS_FULL_LANGUAGE = NSP_DC_TERMS+"language";
	public static String DC_TERMS_FULL_LICENSE = NSP_DC_TERMS+"license";
	public static String DC_TERMS_FULL_MODIFIED=NSP_DC_TERMS+"modified";
	public static String DC_TERMS_PUBLISHER="dcterms:publisher";
	public static String DC_TERMS_FULL_PUBLISHER = NSP_DC_TERMS+"publisher";
	public static String DC_TERMS_TITLE = "dcterms:title";
	public static String DC_TERMS_FULL_TITLE = NSP_DC_TERMS+"title";
	
	public static String FOAF_FULL_AGENT = NSP_FOAF+"Agent";
	public static String FOAF_FULL_HOMEPAGE = NSP_FOAF+"homepage";
	public static String FOAF_FULL_NAME = NSP_FOAF+"name";
	public static String FOAF_FULL_PERSON = NSP_FOAF+"Person";
	public static String FOAF_FULL_PRIMARY_TOPIC = NSP_FOAF+"primaryTopic";
	public static String FOAF_FULL_ORGANIZATION = NSP_FOAF+"Organization";
		
	public static String VOID_FULL_DATASET = NSP_VOID+"Dataset";
	public static String VOID_FULL_CLASS_PARTITION = NSP_VOID+"classPartition";
	public static String VOID_FULL_PROPERTY_PARTITION = NSP_VOID+"propertyPartition";
	public static String VOID_FULL_CLASS = NSP_VOID+"class";
	public static String VOID_FULL_PROPERTY = NSP_VOID+"property";
	public static String VOID_FULL_TRIPLES = NSP_VOID+"triples";
	public static String VOID_FULL_SPARQL_ENDPOINT = NSP_VOID+"sparqlEndpoint";
	
	
	
	/* MREL / REV / BIBO / VANN*/
	public static String MREL_FULL_REV = NSP_MREL+"rev";
	
	public static String REV_FULL_HAS_REVIEW=NSP_REV+"hasReview";
	public static String REV_FULL_REVIEW=NSP_REV+"Review";
	public static String REV_FULL_TEXT=NSP_REV+"text";
	
	public static String BIBO_SHORT_TITLE="bibo:shortTitle";
	
	public static String VANN_PREFERRED_NAMESPACE_PREFIX="vann:preferredNamespacePrefix";
	public static String VANN_FULL_PREFERRED_NAMESPACE_PREFIX=NSP_VANN+"preferredNamespacePrefix";
	public static String VANN_FULL_PREFERRED_NAMESPACE_URI=NSP_VANN+"preferredNamespaceUri";
	
	
	
	
	/* VOAF */
	public static String VOAF_TODO_LIST="voaf:toDoList";
	public static String VOAF_FULL_TODO_LIST=NSP_VOAF+"toDoList";
	public static String VOAF_VOCABULARY_SPACE="voaf:VocabularySpace";
	public static String VOAF_FULL_VOCABULARY_SPACE=NSP_VOAF+"VocabularySpace";
	public static String VOAF_FULL_PROPERTY_NUMBER=NSP_VOAF+"propertyNumber";
	public static String VOAF_FULL_DATATYPE_NUMBER=NSP_VOAF+"datatypeNumber";
	public static String VOAF_FULL_INSTANCE_NUMBER=NSP_VOAF+"instanceNumber";
	public static String VOAF_FULL_CLASS_NUMBER=NSP_VOAF+"classNumber";
	public static String VOAF_FULL_DATASET=NSP_VOAF+"dataset";
	public static String VOAF_VOCABULARY="voaf:Vocabulary";
	public static String VOAF_FULL_VOCABULARY=NSP_VOAF+"Vocabulary";
	public static String VOAF_RELIES_ON="voaf:reliesOn";
	public static String VOAF_USED_BY="voaf:usedBy";
	public static String VOAF_METADATA_VOC="voaf:metadataVoc";
	public static String VOAF_EXTENDS="voaf:extends";
	public static String VOAF_SPECIALIZES="voaf:specializes";
	public static String VOAF_GENERALIZES="voaf:generalizes";
	public static String VOAF_HAS_EQUIVALENCES_WITH="voaf:hasEquivalencesWith";
	public static String VOAF_HAS_DISJUNCTIONS_WITH="voaf:hasDisjunctionsWith";
	public static String VOAF_SIMILAR="voaf:similar";
	public static String VOAF_FULL_USED_BY=NSP_VOAF+"usedBy";
	public static String VOAF_FULL_SIMILAR=NSP_VOAF+"similar";
	public static String VOAF_FULL_RELIES_ON=NSP_VOAF+"reliesOn";
	public static String VOAF_FULL_GENERALIZES=NSP_VOAF+"generalizes";
	public static String VOAF_FULL_SPECIALIZES=NSP_VOAF+"specializes";
	public static String VOAF_FULL_HAS_EQUIVALENCES_WITH=NSP_VOAF+"hasEquivalencesWith";
	public static String VOAF_FULL_HAS_DISJUNCTIONS_WITH=NSP_VOAF+"hasDisjunctionsWith";
	public static String VOAF_FULL_EXTENDS=NSP_VOAF+"extends";
	public static String VOAF_FULL_METADATA_VOC=NSP_VOAF+"metadataVoc";
	public static String VOAF_FULL_OCCURRENCES_IN_VOCABULARIES=NSP_VOAF+"occurrencesInVocabularies";
	public static String VOAF_FULL_OCCURRENCES_IN_DATASETS=NSP_VOAF+"occurrencesInDatasets";
	public static String VOAF_FULL_REUSED_BY_VOCABULARIES=NSP_VOAF+"reusedByVocabularies";
	public static String VOAF_FULL_REUSED_BY_DATASETS=NSP_VOAF+"reusedByDatasets";
	public static String VOAF_FULL_USAGE_IN_DATASET=NSP_VOAF+"usageInDataset";
	public static String VOAF_FULL_DATASET_OCCURRENCES=NSP_VOAF+"DatasetOccurrences";
	public static String VOAF_FULL_IN_DATASET=NSP_VOAF+"inDataset";
	public static String VOAF_FULL_OCCURRENCES=NSP_VOAF+"occurrences";
	
	
	/* DCAT */
	public static String DCAT_FULL_CATALOG=NSP_DCAT+"Catalog";
	public static String DCAT_FULL_CATALOG_RECORD=NSP_DCAT+"CatalogRecord";
	public static String DCAT_FULL_DISTRIBUTION_CLASS=NSP_DCAT+"Distribution";
	public static String DCAT_FULL_DISTRIBUTION_PROP=NSP_DCAT+"distribution";
	public static String DCAT_FULL_KEYWORD=NSP_DCAT+"keyword";
	public static String DCAT_FULL_RECORD=NSP_DCAT+"record";
	
	/* FRBR */
	public static String FRBR_REALIZATION="frbr:realization";
	public static String FRBR_EXPRESSION="frbr:Expression";
	public static String FRBR_EMBODIMENT="frbr:embodiment";
	public static String FRBR_MANIFESTATION="frbr:Manifestation";
	public static String FRBR_FULL_REALIZATION=NSP_FRBR+"realization";
	public static String FRBR_FULL_EXPRESSION=NSP_FRBR+"Expression";
	public static String FRBR_FULL_EMBODIMENT=NSP_FRBR+"embodiment";
	public static String FRBR_FULL_MANIFESTATION=NSP_FRBR+"Manifestation";
	
	/* Schema */
	public static String SCHEMA_FULL_KEYWORDS=NSP_SCHEMA+"keywords";
	
	
	/* Other Vocabularies */
	public static String LEXVO_FULL_REPRESENTED_BY= NSP_LEXVO+"representedBy";
	public static String LEXVO_FULL_ISO639P3PCODE= NSP_LEXVO+"iso639P3PCode";
	public static String LEXVO_FULL_ISO639P1CODE= NSP_LEXVO+"iso639P1Code";
	public static String LEXVO_FULL_LANGUAGE= NSP_LEXVO+"Language";
	public static String MOAT_FULL_TAG= NSP_MOAT+"Tag";
	public static String TAGS_FULL_NAME= NSP_TAGS+"name";
	
	
	
	
	/* User URIs */
	public static String PYV_URI = "http://data.semanticweb.org/person/pierre-yves-vandenbussche";
	public static String BV_URI = "http://data.semanticweb.org/person/bernard-vatant";
	
	
	
	
	
	
	
	//http://html-color-codes.info/
	public static String ORANGE= "#ff7f0e";
	public static String YELLOW= "#f5e135";
	public static String GREEN= "#8cc800";
	public static String PINK= "#ffc0ff";
	public static String BLUE_LIGHT= "#6c9fc5";
	public static String BLUE_DARK= "#9ed1ec";
	public static String GRAY= "#dedede";
	public static String GRAY_DARK= "#b3b3b3";
	public static String PURPLE= "#fd72e7";
	public static String ORANGE_LIGHT="#ffdd82";
	public static String RED_LIGHT="#F78181";
	
	public static String EXPRESSION_DESC_AUTO="This version has been automatically fetched.";
		
	public static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'+01:00'";
	public static final DateFormat DATE_FORMAT_RSS = new SimpleDateFormat("EEE, d MMM yyyy HH:mm Z", Locale.US);
	
	
	/* User Management*/
	public static String USER = "USER";
	public static String USER_LOGIN="_LOGIN";
	public static String USER_URI="_URI";
	public static String USER_PWD="_PWD";
	public static String USER_EMAIL="_EMAIL";
	public static String USER_RIGHT="_RIGHT";
	
	

	public static String PREFIXES = "PREFIX rdf:<"+LovConstants.NSP_RDF+"> \n"+
	"PREFIX xsd:<"+LovConstants.NSP_XSD+"> \n"+
	"PREFIX dc:<"+LovConstants.NSP_DC+"> \n"+
	"PREFIX rdfs:<"+LovConstants.NSP_RDFS+"> \n"+
	"PREFIX owl:<"+LovConstants.NSP_OWL+"> \n"+
	"PREFIX skos:<"+LovConstants.NSP_SKOS+"> \n"+
	"PREFIX foaf:<"+LovConstants.NSP_FOAF+"> \n"+
	"PREFIX dcterms:<"+LovConstants.NSP_DC_TERMS+"> \n"+
	"PREFIX bibo:<"+LovConstants.NSP_BIBO+"> \n"+
	"PREFIX vann:<"+LovConstants.NSP_VANN+"> \n"+
	"PREFIX voaf:<"+LovConstants.NSP_VOAF+"> \n"+
	"PREFIX frbr:<"+LovConstants.NSP_FRBR+"> \n"+
	"PREFIX void:<"+LovConstants.NSP_VOID+"> \n"+
	"PREFIX lov:<"+LovConstants.NSP_LOV+"> \n";
	
	
	public static Map<String, String> getPrefixes(){
		Map<String, String> prefixes = new HashMap<String, String>();
		prefixes.put("xsd", NSP_XSD);
		prefixes.put("owl", NSP_OWL);
		prefixes.put("rdfs", NSP_RDFS);
		prefixes.put("rdf", NSP_RDF);
		prefixes.put("foaf", NSP_FOAF);
		prefixes.put("lov", NSP_LOV);
		return prefixes;
	}
	
}
