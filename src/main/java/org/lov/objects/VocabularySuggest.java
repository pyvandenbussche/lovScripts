package org.lov.objects;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;

/**
 * Represents an Vocabulary: contains the metadata and pointers to versions
 * 
 * @author Pierre-Yves Vandenbussche
 *
 */
public class VocabularySuggest{

	private long nbTriplesWithoutInf;
	
	//deal with variety and complexity to get URI / namespace  and prefix information
	private String uri; // LOV best URI guess
		private String uriInputSearch; // URI given as input by the user
		private String uriDeclared; // URI declared as of type ontology
	private String nsp; // LOV best namespace guess
		private String nspMostUsed; // namespace most used in the vocabulary file
		private String nspVannPref; // namespace declared as vann preferred namespace URI 
		private String nspClosest; // closest namespace from the list of declared namespaces to the input URI (using levenshtein distance)
		private String nspDefault; // default namespace (xml:base) without prefix
	private String prefix; // LOV best prefix guess
		private String prefixVannPref; // prefix declared as vann preferred prefix
		private String prefixAssociatedNsp;  // prefix associated to the LOV best namespace guess 
		
	private int nbClasses;
	private int nbProperties;
	private int nbInstances;
	private int nbDatatypes;
	private List<Language> languages;
	private List<LangValue> titles;
	private List<LangValue> descriptions;
	private String dateIssued;
	private String dateModified;
	private List<Agent> creators;
	private List<Agent> contributors;
	private List<Agent> publishers;
	
	private List<VocabularySuggest> relMetadata;
	private List<VocabularySuggest> relSpecializes;
	private List<VocabularySuggest> relGeneralizes;
	private List<VocabularySuggest> relExtends;
	private List<VocabularySuggest> relEquivalent;
	private List<VocabularySuggest> relDisjunc;
	private List<VocabularySuggest> relImports;
	
	public VocabularySuggest(){super();}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getUriInputSearch() {
		return uriInputSearch;
	}
	public void setUriInputSearch(String uriInputSearch) {
		this.uriInputSearch = uriInputSearch;
	}
	public String getUriDeclared() {
		return uriDeclared;
	}
	public void setUriDeclared(String uriDeclared) {
		this.uriDeclared = uriDeclared;
	}
	public String getNsp() {
		return nsp;
	}
	public void setNsp(String nsp) {
		this.nsp = nsp;
	}
	public String getNspMostUsed() {
		return nspMostUsed;
	}
	public void setNspMostUsed(String nspMostUsed) {
		this.nspMostUsed = nspMostUsed;
	}
	public String getNspVannPref() {
		return nspVannPref;
	}
	public void setNspVannPref(String nspVannPref) {
		this.nspVannPref = nspVannPref;
	}
	public String getNspClosest() {
		return nspClosest;
	}
	public void setNspClosest(String nspClosest) {
		this.nspClosest = nspClosest;
	}
	public String getNspDefault() {
		return nspDefault;
	}
	public void setNspDefault(String nspDefault) {
		this.nspDefault = nspDefault;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getPrefixVannPref() {
		return prefixVannPref;
	}
	public void setPrefixVannPref(String prefixVannPref) {
		this.prefixVannPref = prefixVannPref;
	}
	public String getPrefixAssociatedNsp() {
		return prefixAssociatedNsp;
	}
	public void setPrefixAssociatedNsp(String prefixAssociatedNsp) {
		this.prefixAssociatedNsp = prefixAssociatedNsp;
	}
	public int getNbClasses() {
		return nbClasses;
	}
	public void setNbClasses(int nbClasses) {
		this.nbClasses = nbClasses;
	}
	public int getNbProperties() {
		return nbProperties;
	}
	public void setNbProperties(int nbProperties) {
		this.nbProperties = nbProperties;
	}
	public int getNbInstances() {
		return nbInstances;
	}
	public void setNbInstances(int nbInstances) {
		this.nbInstances = nbInstances;
	}
	public int getNbDatatypes() {
		return nbDatatypes;
	}
	public void setNbDatatypes(int nbDatatypes) {
		this.nbDatatypes = nbDatatypes;
	}
	public List<Language> getLanguages() {
		return languages;
	}
	public void setLanguages(List<Language> languages) {
		this.languages = languages;
	}
	public List<LangValue> getTitles() {
		return titles;
	}
	public void setTitles(List<LangValue> titles) {
		this.titles = titles;
	}
	public List<LangValue> getDescriptions() {
		return descriptions;
	}
	public void setDescriptions(List<LangValue> descriptions) {
		this.descriptions = descriptions;
	}
	public String getDateIssued() {
		return dateIssued;
	}
	public void setDateIssued(String dateIssued) {
		this.dateIssued = dateIssued;
	}
	public String getDateModified() {
		return dateModified;
	}
	public void setDateModified(String dateModified) {
		this.dateModified = dateModified;
	}
	public long getNbTriplesWithoutInf() {
		return nbTriplesWithoutInf;
	}

	public void setNbTriplesWithoutInf(long nbTriplesWithoutInf) {
		this.nbTriplesWithoutInf = nbTriplesWithoutInf;
	}

	public List<Agent> getCreators() {
		return creators;
	}

	public void setCreators(List<Agent> creators) {
		this.creators = creators;
	}

	public List<Agent> getContributors() {
		return contributors;
	}

	public void setContributors(List<Agent> contributors) {
		this.contributors = contributors;
	}

	public List<Agent> getPublishers() {
		return publishers;
	}

	public void setPublishers(List<Agent> publishers) {
		this.publishers = publishers;
	}

	public List<VocabularySuggest> getRelMetadata() {
		return relMetadata;
	}
	public void setRelMetadata(List<VocabularySuggest> relMetadata) {
		this.relMetadata = relMetadata;
	}
	public List<VocabularySuggest> getRelSpecializes() {
		return relSpecializes;
	}
	public void setRelSpecializes(List<VocabularySuggest> relSpecializes) {
		this.relSpecializes = relSpecializes;
	}
	public List<VocabularySuggest> getRelGeneralizes() {
		return relGeneralizes;
	}
	public void setRelGeneralizes(List<VocabularySuggest> relGeneralizes) {
		this.relGeneralizes = relGeneralizes;
	}
	public List<VocabularySuggest> getRelExtends() {
		return relExtends;
	}
	public void setRelExtends(List<VocabularySuggest> relExtends) {
		this.relExtends = relExtends;
	}
	public List<VocabularySuggest> getRelEquivalent() {
		return relEquivalent;
	}
	public void setRelEquivalent(List<VocabularySuggest> relEquivalent) {
		this.relEquivalent = relEquivalent;
	}
	public List<VocabularySuggest> getRelDisjunc() {
		return relDisjunc;
	}
	public void setRelDisjunc(List<VocabularySuggest> relDisjunc) {
		this.relDisjunc = relDisjunc;
	}
	public List<VocabularySuggest> getRelImports() {
		return relImports;
	}
	public void setRelImports(List<VocabularySuggest> relImports) {
		this.relImports = relImports;
	}
	
	public void prettyPrint(Logger log){
		log.info("####### <Summary> #######");
		log.info("Nb Triples: " + nbTriplesWithoutInf);
		log.info("## URI --> " + uri);
		log.info("\t Input search URI " + uriInputSearch);
		log.info("\t Declared URI " + uriDeclared);
		
		log.info("## Namespace --> " + nsp);
		log.info("\t Most used namespace " + nspMostUsed);
		log.info("\t Vann preferred namespace " + nspVannPref);
		log.info("\t Closest namespace " + nspClosest);
		log.info("\t Default namespace " + nspDefault);
		
		log.info("## Prefix --> " + prefix);
		log.info("\t Vann preferred prefix " + prefixVannPref);
		log.info("\t Prefix associated to the namespace " + prefixAssociatedNsp);
				
		log.info("");
		log.info("Nb Classes: " + nbClasses);
		log.info("Nb Properties: " + nbProperties);
		log.info("Nb Instances: " + nbInstances);
		log.info("Nb Datatypes: " + nbDatatypes);
		
		log.info("");
		if(languages!=null){
			log.info("Langs ["+languages.size()+"]");
			for (Iterator<Language> iterator = languages.iterator(); iterator.hasNext();) {
				Language lang = (Language) iterator.next();
				log.info("\t "+(lang.getLabel()!=null? lang.getLabel():"##unknown##")+ " ("+lang.getIso639P1Code()+")");
			}
		}
		log.info("Date Modified: "+dateModified);
		log.info("Date Issued: "+dateIssued);
		if(titles!=null){
			log.info("Titles ["+titles.size()+"]");
			for (Iterator<LangValue> iterator = titles.iterator(); iterator.hasNext();) {
				LangValue langValue = (LangValue) iterator.next();
				log.info("\t "+langValue.getValue()+(langValue.getLang()!=null? " @"+langValue.getLang():""));
			}
		}
		if(descriptions!=null){
			log.info("Descriptions ["+descriptions.size()+"]");
			for (Iterator<LangValue> iterator = descriptions.iterator(); iterator.hasNext();) {
				LangValue langValue = (LangValue) iterator.next();
				log.info("\t "+langValue.getValue()+(langValue.getLang()!=null? " @"+langValue.getLang():""));
			}
		}
		if(creators!=null){
			for (Agent agent : creators) {
				log.info("Creators: "+agent.getPrefUri()+(agent.getName()!=null?" ("+agent.getName()+")":""));
			}
		}
		if(contributors!=null){
			for (Agent agent : contributors) {
				log.info("Contributors: "+agent.getPrefUri()+(agent.getName()!=null?" ("+agent.getName()+")":""));
			}
		}
		if(publishers!=null){
			for (Agent agent : publishers) {
				log.info("Publishers: "+agent.getPrefUri()+(agent.getName()!=null?" ("+agent.getName()+")":""));
			}
		}
		log.info("");
		
		if(relMetadata!=null){
			for (VocabularySuggest relNsp : relMetadata) {
				log.info("uses Metadata: "+relNsp.getUri()+(relNsp.getPrefix()!=null?" ("+relNsp.getPrefix()+")":""));
			}
		}
		if(relSpecializes!=null){
			for (VocabularySuggest relNsp : relSpecializes) {
				log.info("specializes: "+relNsp.getUri()+(relNsp.getPrefix()!=null?" ("+relNsp.getPrefix()+")":""));
			}
		}
		if(relGeneralizes!=null){
			for (VocabularySuggest relNsp : relGeneralizes) {
				log.info("generalizes: "+relNsp.getUri()+(relNsp.getPrefix()!=null?" ("+relNsp.getPrefix()+")":""));
			}
		}
		if(relExtends!=null){
			for (VocabularySuggest relNsp : relExtends) {
				log.info("extends: "+relNsp.getUri()+(relNsp.getPrefix()!=null?" ("+relNsp.getPrefix()+")":""));
			}
		}
		if(relEquivalent!=null){
			for (VocabularySuggest relNsp : relEquivalent) {
				log.info("has equivalences with: "+relNsp.getUri()+(relNsp.getPrefix()!=null?" ("+relNsp.getPrefix()+")":""));
			}
		}
		if(relDisjunc!=null){
			for (VocabularySuggest relNsp : relDisjunc) {
				log.info("has disjunctions with: "+relNsp.getUri()+(relNsp.getPrefix()!=null?" ("+relNsp.getPrefix()+")":""));
			}
		}
		if(relImports!=null){
			for (VocabularySuggest relNsp : relImports) {
				log.info("imports: "+relNsp.getUri()+(relNsp.getPrefix()!=null?" ("+relNsp.getPrefix()+")":""));
			}
		}
		
		log.info("####### </Summary> #######");
		
	}
}
