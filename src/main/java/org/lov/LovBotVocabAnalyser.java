package org.lov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.lov.objects.Agent;
import org.lov.objects.LangValue;
import org.lov.objects.Language;
import org.lov.objects.VocabularySuggest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.mongodb.MongoClient;


public class LovBotVocabAnalyser {
	private final static Logger log = LoggerFactory.getLogger(LovBotVocabAnalyser.class);
	
	/**
	 * This function analyse a vocabulary version using previous vocabulary information
	 **/
	public static VocabularySuggest analyseVersion(String vocabularyversionURL, String vocabularyURI, String vocabularyNsp, Lang lang, Properties lovConfig) throws Exception{
		log.info("LOV BOT is analysing the version file : " + vocabularyversionURL);
		return analyse(vocabularyversionURL, vocabularyURI, vocabularyNsp, lang, lovConfig);
	}
	
	/**
	 * This function dereferentiate the vocabulary URI adn get all information possible without any previous knowledge 
	 **/
	public static VocabularySuggest analyseVocabURI(String vocabularyURI, Properties lovConfig) throws Exception{
		//log.info("LOV BOT is analysing the vocabulary URI: " + vocabularyURI);
		return analyse(vocabularyURI, null, null,null, lovConfig);
	}
	
	private static VocabularySuggest analyse(String uriToAnalayse,String vocabURI, String vocabNsp, Lang lang, Properties lovConfig) throws Exception{
			
			MongoClient mongoClient = new MongoClient( lovConfig.getProperty("MONGO_DB_HOST") , Integer.parseInt(lovConfig.getProperty("MONGO_DB_PORT")) );
			Jongo jongo = new Jongo(mongoClient.getDB( lovConfig.getProperty("MONGO_DB_INSTANCE") ));
			//read from the URI and load it in a model
			Model vocab;
			if(lang!=null)vocab= RDFDataMgr.loadModel(uriToAnalayse,lang); //try to read from the URI and load it in a model
			else vocab= RDFDataMgr.loadModel(uriToAnalayse);
			
			
			VocabularySuggest result = new VocabularySuggest();
			result.setNbTriplesWithoutInf(vocab.size());
//			try {
//				File outputFile = new File("C:/Users/vandenbusschep/Desktop/output.n3");
//				if (outputFile.exists())outputFile.delete();
//				outputFile.createNewFile();
//				FileOutputStream fop = new FileOutputStream(outputFile);
//				RDFDataMgr.write(fop, vocab, RDFFormat.NT) ;
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			
			//use RDFS inference
			vocab = ModelFactory.createRDFSModel(vocab);
			
			//run queries over the vocabulary
			SPARQLRunner sparqlRunner = new SPARQLRunner(vocab);
						
			/* Vocabulary terms */
			List<Resource> classes = ((vocabNsp!=null)? //use namespace if we have it to filter out the element not member of that vocabulary
					sparqlRunner.getURIs("list-classes-with-nsp.sparql", "nsp", ResourceFactory.createResource(vocabNsp), "class")
					:sparqlRunner.getURIs("list-classes.sparql", null, null, "class"));
			List<Resource> properties = ((vocabNsp!=null)? 
					sparqlRunner.getURIs("list-properties-with-nsp.sparql", "nsp", ResourceFactory.createResource(vocabNsp), "property")
					:sparqlRunner.getURIs("list-properties.sparql", null, null, "property"));
			List<Resource> instances = ((vocabNsp!=null)?
					sparqlRunner.getURIs("list-instances-with-nsp.sparql", "nsp", ResourceFactory.createResource(vocabNsp), "instance")
					:sparqlRunner.getURIs("list-instances.sparql", null, null, "instance"));
			List<Resource> datatypes = ((vocabNsp!=null)?
					sparqlRunner.getURIs("list-datatypes-with-nsp.sparql", "nsp", ResourceFactory.createResource(vocabNsp), "datatype")
					:sparqlRunner.getURIs("list-datatypes.sparql", null, null, "datatype"));
			List<Resource> allTerms = new ArrayList<Resource>();
			allTerms.addAll(classes);
			allTerms.addAll(properties);
			allTerms.addAll(instances);
			allTerms.addAll(datatypes);
			
			//countVocabTerms(properties, nsp)
			
			/* URI, Namespace and prefixes */
			String declaredUri = sparqlRunner.getURI("vocab-ontology-uri.sparql", "uri",null, null);
			result.setUri(declaredUri);
			result.setUriDeclared(declaredUri);
			result.setUriInputSearch(uriToAnalayse);
			result.setNspVannPref(sparqlRunner.getString("vocab-preferredNamespaceUri.sparql", "nsp","uri", (declaredUri!=null?ResourceFactory.createResource(declaredUri):ResourceFactory.createResource((vocabURI!=null)?vocabURI:uriToAnalayse))));
			result.setNspDefault(vocab.getNsPrefixURI(""));
			result.setNspClosest(getClosestNamespace(vocab.getNsPrefixMap(),(vocabURI!=null)?vocabURI:uriToAnalayse));
			result.setNspMostUsed(getMostUsedNamespace(allTerms));
			String nsp = result.getNspMostUsed(); // strategy to get the namespace: 1) most used one
			if(nsp==null)nsp = result.getNspVannPref(); // 2) declared as vann preferred Namespace
			if(nsp==null)nsp = result.getNspClosest(); // 3) the closest lexical value from the declared namespaces
			if(nsp==null)nsp = result.getNspDefault(); // 4) the default namespace (without prefix)
			result.setNsp(nsp);
			result.setPrefixVannPref(sparqlRunner.getString("vocab-preferredNamespacePrefix.sparql", "nsp","uri", (declaredUri!=null?ResourceFactory.createResource(declaredUri):ResourceFactory.createResource((vocabURI!=null)?vocabURI:uriToAnalayse))));
			result.setPrefixAssociatedNsp(vocab.getNsURIPrefix(nsp));
			String prefix = result.getPrefixVannPref(); // strategy to get the prefix: 1) declared as vann preferred prefix
			if(prefix==null)prefix= result.getPrefixAssociatedNsp(); // 2) the prefix associated to the namespace
			result.setPrefix(prefix);
			
			result.setNbClasses((vocabNsp!=null)?classes.size() : countVocabTerms(classes,result.getNsp()));
			result.setNbProperties((vocabNsp!=null)?properties.size() : countVocabTerms(properties,result.getNsp()));
			result.setNbInstances((vocabNsp!=null)?instances.size() : countVocabTerms(instances,result.getNsp()));
			result.setNbDatatypes((vocabNsp!=null)?datatypes.size() : countVocabTerms(datatypes,result.getNsp()));
			
			/* other metadata */ 
			result.setLanguages(getlanguages(sparqlRunner.getLiterals("vocab-langs.sparql", "lang", null, null), jongo));
			result.setDateModified(sparqlRunner.getString("vocab-modified.sparql", "modified","uri", ResourceFactory.createResource((vocabURI!=null)?vocabURI:uriToAnalayse)));
			result.setDateIssued(sparqlRunner.getString("vocab-issued.sparql", "issued","uri", ResourceFactory.createResource((vocabURI!=null)?vocabURI:uriToAnalayse)));
			result.setTitles(fromLiteralToLangValue(sparqlRunner.getLiterals("vocab-titles.sparql", "title", "uri", ResourceFactory.createResource((vocabURI!=null)?vocabURI:uriToAnalayse))));
			result.setDescriptions(fromLiteralToLangValue(sparqlRunner.getLiterals("vocab-descriptions.sparql", "description", "uri", ResourceFactory.createResource((vocabURI!=null)?vocabURI:uriToAnalayse))));
			result.setCreators(getAgents(sparqlRunner.getResultSet("vocab-creators.sparql", "uri", ResourceFactory.createResource((vocabURI!=null)?vocabURI:uriToAnalayse)),jongo));
			result.setContributors(getAgents(sparqlRunner.getResultSet("vocab-contributors.sparql", "uri", ResourceFactory.createResource((vocabURI!=null)?vocabURI:uriToAnalayse)),jongo));
			result.setPublishers(getAgents(sparqlRunner.getResultSet("vocab-publishers.sparql", "uri", ResourceFactory.createResource((vocabURI!=null)?vocabURI:uriToAnalayse)),jongo));
			
			/* VOAF relations */
			result.setRelMetadata(getVocabularies(sparqlRunner.getURIs("vocab-rel-metadata.sparql", "nsp", ResourceFactory.createPlainLiteral((vocabNsp!=null)?vocabNsp : nsp), "elem"), jongo));
			result.setRelSpecializes(getVocabularies(sparqlRunner.getURIs("vocab-rel-specializes.sparql", "nsp", ResourceFactory.createPlainLiteral((vocabNsp!=null)?vocabNsp : nsp), "elem"), jongo));
			result.setRelGeneralizes(getVocabularies(sparqlRunner.getURIs("vocab-rel-generalizes.sparql", "nsp", ResourceFactory.createPlainLiteral((vocabNsp!=null)?vocabNsp : nsp), "elem"), jongo));
			result.setRelExtends(getVocabularies(sparqlRunner.getURIs("vocab-rel-extends.sparql", "nsp", ResourceFactory.createPlainLiteral((vocabNsp!=null)?vocabNsp : nsp), "elem"), jongo));
			result.setRelEquivalent(getVocabularies(sparqlRunner.getURIs("vocab-rel-equiv.sparql", "nsp", ResourceFactory.createPlainLiteral((vocabNsp!=null)?vocabNsp : nsp), "elem"), jongo));
			result.setRelDisjunc(getVocabularies(sparqlRunner.getURIs("vocab-rel-disj.sparql", "nsp", ResourceFactory.createPlainLiteral((vocabNsp!=null)?vocabNsp : nsp), "elem"), jongo));
			result.setRelImports(getVocabularies(sparqlRunner.getURIs("vocab-rel-imports.sparql", "uri", ResourceFactory.createResource((vocabURI!=null)?vocabURI:uriToAnalayse), "relVocab"), jongo));
			
			
			return result;
	}
	
	
	private static List<Language> getlanguages(List<Literal> langsLiteral, Jongo jongo){
		if(langsLiteral==null)return null;
		MongoCollection coll = jongo.getCollection("languages");		
		List<Language> langs = new ArrayList<>();
		for (Literal lit : langsLiteral) {
			//get the language with its label using the ISO639P1 code
			Language l = coll.findOne("{iso639P1Code:#}", lit.getLexicalForm()).as(Language.class);
			if(l==null){
				l = new Language();
				l.setIso639P1Code(lit.getLexicalForm());
			}
			langs.add(l);
		}
		return langs;
	}
	
	private static List<Agent> getAgents(ResultSet rs, Jongo jongo){
		if(!rs.hasNext())return null;
		MongoCollection coll = jongo.getCollection("agents");
		List<Agent> agents = new ArrayList<Agent>();
		while (rs.hasNext()) {
			QuerySolution qs = (QuerySolution) rs.next();
			//get agents using prefUri and altUris
			Agent a = coll.findOne("{ $or: [ {prefUri:#}, { altUris: # } ] }", qs.get("agent").asResource().toString(), qs.get("agent").asResource().toString()).as(Agent.class);
			if(a==null){
				a = new Agent(qs.get("agent").asResource().toString(),(qs.contains("agentLbl")?qs.get("agentLbl").asLiteral().getLexicalForm():null) );
			}
			agents.add(a);
		}
		return agents;		
	}
	
	private static List<LangValue> fromLiteralToLangValue(List<Literal> lits){
		if(lits==null)return null;
		List<LangValue> lvs = new ArrayList<LangValue>();
		for (Literal lit : lits) {
			lvs.add(new LangValue(lit.getLexicalForm(), lit.getLanguage()));
		}		
		return lvs;
	}
	
	private static List<VocabularySuggest> getVocabularies(List<Resource> res, Jongo jongo){
		MongoCollection coll = jongo.getCollection("vocabularies");
		if(res==null)return null;
		List<VocabularySuggest> vocabs = new ArrayList<VocabularySuggest>();
		for (String nsp : getDistinctNspsFromURIs(res)) {
			VocabularySuggest a = coll.findOne("{ $or: [ {nsp:#}, { uri: # } ] }", nsp, nsp).projection("{uri:1,nsp:1,prefix:1}").as(VocabularySuggest.class);
			if(a==null){
				a = new VocabularySuggest();
				a.setUri(nsp);
			}
			vocabs.add(a);
		}	
		return vocabs;
	}
	
	private static int countVocabTerms(List<Resource> terms, String vocabNsp){
		int cpt=0;
		for (Iterator<Resource> iterator = terms.iterator(); iterator.hasNext();) {
			Resource resource = (Resource) iterator.next();
			if(resource.getNameSpace().equals(vocabNsp))cpt++;
		}
		return cpt;		
	}
	
	/**
	 * Frequency approach to detect the most used namespace
	 * 
	 * @param allTerms
	 * @return
	 */
	private static String getMostUsedNamespace(List<Resource> allTerms){
		Map<String, String> URIs = new HashMap<String, String>();
		
		for (Iterator<Resource> iterator = allTerms.iterator(); iterator.hasNext();) {
			Resource resource = (Resource) iterator.next();
			String rBase = resource.getNameSpace();
			if(!rBase.equals(LovConstants.NSP_RDF)&& !rBase.equals(LovConstants.NSP_RDFS) && !rBase.equals(LovConstants.NSP_OWL)){
				if(URIs.get(rBase)!=null){
					int cptUri = Integer.parseInt(URIs.get(rBase));
					URIs.remove(rBase);
					URIs.put(rBase, ""+(cptUri+1));
				}
				else{
					URIs.put(rBase, "1");
				}
			}
		}
		String URIMostUsed=null;
		int URIMostUsedCPT=1;
		for ( Iterator<Entry<String, String>> it = URIs.entrySet().iterator(); it.hasNext(); ) {
			Entry<String, String> entry = it.next();
			if(Integer.parseInt(entry.getValue())>URIMostUsedCPT){
				URIMostUsed = entry.getKey();
				URIMostUsedCPT= Integer.parseInt(entry.getValue());
			}
		}	
		return URIMostUsed;
	}
	
	private static String getClosestNamespace(Map<String, String> nsps, String vocabUri){
		String closestNamespace = null;
		int lowestLevenshtein = 1000;
		for ( Iterator<Entry<String, String>> it = nsps.entrySet().iterator(); it.hasNext(); ) {
			Entry<String, String> entry = it.next();
			String nsp = entry.getValue();
			if(nsp.contains(vocabUri)){
				int levenDis = LovUtil.getLevenshteinDistance(vocabUri, nsp);
				if(levenDis<lowestLevenshtein){//this is the closet
					closestNamespace= nsp;
					lowestLevenshtein=levenDis;
				}
			}
		}
		return closestNamespace;		
	}
	
	private static List<String> getDistinctNspsFromURIs(List<Resource> uris){
		List<String> result = new ArrayList<>();
		for (Resource r : uris) {
			if(!result.contains(r.getNameSpace())) result.add(r.getNameSpace());
		}
		return result;
	}
}
