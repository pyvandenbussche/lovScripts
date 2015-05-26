package org.lov.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.lov.objects.Language;
import org.lov.objects.Vocabulary;
import org.lov.objects.VocabularyVersionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.hp.hpl.jena.shared.NotFoundException;
import com.mongodb.MongoClient;

import org.lov.objects.*;

/**
 * A command line tool that output inline the number of vocabularies per week based on the archives analysis.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class ComputeLOVWeekEvolDesc extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(ComputeLOVWeekEvolDesc.class);
	private final static SimpleDateFormat formatLatex = new SimpleDateFormat("yyyy-MM-dd");
	
	private Calendar startingDate;
	private String hostName;
	private String dbName;
	private Properties lovConfig;
	
	
	
	public static void main(String... args) {
		new ComputeLOVWeekEvolDesc(args).mainRun();
	}	
	
	public ComputeLOVWeekEvolDesc(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("startingDate", "format: yyyy-MM-dd");
		getUsage().addUsage("configFilePath", "absolute path for the configuration file  (e.g. /home/...)");
	}
	
	@Override
    protected String getCommandName() {
		return "computelovevol";
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + "configFilePath startingDate";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 2) {
			doHelp();
		}
		String configFilePath = getPositionalArg(0);
		//load properties from the config file
		try {
			lovConfig = new Properties();
			File file = new File(configFilePath);
			InputStream is = new FileInputStream(file);
			lovConfig.load(is);
			hostName= lovConfig.getProperty("MONGO_DB_HOST")+":"+lovConfig.getProperty("MONGO_DB_PORT");
			dbName = lovConfig.getProperty("MONGO_DB_INSTANCE");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// parse the requested starting date
		try {
			startingDate = Calendar.getInstance();
			startingDate.setTime(formatLatex.parse(getPositionalArg(1)));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void exec() {
		try {
			//sendPost(namespace);
						
			//bootstrap connection to MongoDB and create model
			Jongo jongo = new Jongo(new MongoClient(hostName).getDB(dbName));
			MongoCollection vocabCollection = jongo.getCollection("vocabularies");
			MongoCollection langCollection = jongo.getCollection("languages");
			
			computeElements(vocabCollection);
			computeEvolNbVocabsWeekly(vocabCollection);
			
			computeEvolVocabCreationModif(vocabCollection);
			computeExpressivity(vocabCollection);
			computeLang(vocabCollection,langCollection);
			
			computeVoafRel(vocabCollection);
			
			log.info("Done!");
		} catch (NotFoundException ex) {
			cmdError("Not found: " + ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/*
	 * Elastic Search Count number elements per type:
	curl -XGET "http://localhost:9200/lov/_search?search_type=count" -d '{
    	"facets": {
	        "count_by_type": {
	            "terms": {
	                "field": "_type"
	            }
	        }
    	}
	}'
	 */
	
	private void computeEvolNbVocabsWeekly(MongoCollection vocabCollection){
		log.info("####### cumu evol Nb vocabs weekly from: "+ formatLatex.format(startingDate.getTime())+"  #######");	
		StringBuffer sb = new StringBuffer();
		//iterate until today
		while(startingDate.before(Calendar.getInstance())){
			long nbVocabs =  vocabCollection.count("{ createdInLOVAt: { $lte: # } }",startingDate.getTime());
			sb.append(formatLatex.format(startingDate.getTime())+"\t"+nbVocabs+"\n");
			startingDate.add(Calendar.WEEK_OF_YEAR, 1);
		}				
		log.info(sb.toString());
		log.info("########################\n");
	}
	
	private void computeEvolVocabCreationModif(MongoCollection vocabCollection){
		//get list of vocabs
		MongoCursor<Vocabulary> vocabs = vocabCollection.find().as(Vocabulary.class);
		SortedMap<String, Integer> dateBucketCreation = new TreeMap<String, Integer>();
		SortedMap<String, Integer> dateBucketModification = new TreeMap<String, Integer>();
		for (int i = 1998; i < 2015; i++) {
			dateBucketCreation.put(i+"-01-01", 0);
			dateBucketModification.put(i+"-01-01", 0);
			dateBucketCreation.put(i+"-07-01", 0);
			dateBucketModification.put(i+"-07-01", 0);
		}
		
		//for each vocabs, extract creation date / last modif date
		for (Vocabulary vocab : vocabs) {
			if(vocab.getIssuedAt()!=null){
				Calendar issuedAt = Calendar.getInstance();
				issuedAt.setTime(vocab.getIssuedAt());
				// ########################
				// CAREFUL the year has been decremented by one year ... was not able to figure out how to do that properly...
				// ########################
				String issuedAtString = issuedAt.get(Calendar.YEAR)-1+((issuedAt.get(Calendar.MONTH)>5)?"-07-01":"-01-01");
				//store in time buckets
				dateBucketCreation.put(issuedAtString, dateBucketCreation.get(issuedAtString)+1);
			}
			if(vocab.getVersions()!=null && vocab.getVersions().size()>0){
				Collections.sort(vocab.getVersions());
				VocabularyVersionWrapper version = vocab.getVersions().get(0);// <-- this is the latest version
				Calendar issuedAt = Calendar.getInstance();
				issuedAt.setTime(version.getIssued());
				String issuedAtString = issuedAt.get(Calendar.YEAR)-1+((issuedAt.get(Calendar.MONTH)>5)?"-07-01":"-01-01");
				dateBucketModification.put(issuedAtString, dateBucketModification.get(issuedAtString)+1);
			}
		}
		//output
		StringBuffer sb = new StringBuffer();
		for ( Entry<String, Integer> timeBucket : dateBucketCreation.entrySet()) {
			sb.append("("+timeBucket.getKey()+","+timeBucket.getValue()+")\n");
		}
		log.info("####### evol creation vocabs #######");	
		log.info(sb.toString());
		log.info("########################\n");
		sb= new StringBuffer();
		for ( Entry<String, Integer> timeBucket : dateBucketModification.entrySet()) {
			sb.append("("+timeBucket.getKey()+","+timeBucket.getValue()+")\n");
		}
		log.info("####### evol modifications vocabs #######");	
		log.info(sb.toString());
		log.info("########################\n");
	}
	
	private void computeExpressivity(MongoCollection vocabCollection){
		//get list of vocabs
		MongoCursor<Vocabulary> vocabs = vocabCollection.find().as(Vocabulary.class);
		int rdf = 0;
		int rdfs = 0;
		int owl = 0;
		
		//for each vocabs, extract creation date / last modif date
		for (Vocabulary vocab : vocabs) {
			if(vocab.getVersions()!=null && vocab.getVersions().size()>0){
				Collections.sort(vocab.getVersions());
				VocabularyVersionWrapper version = vocab.getVersions().get(0);// <-- this is the latest version
				if(version.getRelMetadata()!=null && version.getRelMetadata().contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#"))rdf++;
				if(version.getRelMetadata()!=null && version.getRelMetadata().contains("http://www.w3.org/2000/01/rdf-schema#"))rdfs++;
				if(version.getRelMetadata()!=null && version.getRelMetadata().contains("http://www.w3.org/2002/07/owl#"))owl++;
			}
		}
		//output
		log.info("####### expressivity #######");	
//		log.info(""+rdf);	
		log.info((float)((float)rdf/(float)(vocabs.count()))+"/rdf, "+(float)((float)rdfs/(float)(vocabs.count()))+"/rdfs, "+(float)((float)owl/(float)(vocabs.count()))+"/owl");
		log.info("########################\n");
	}
	
	private void computeLang(MongoCollection vocabCollection, MongoCollection langCollection){
				
		//get list of langs
		MongoCursor<Language> langs = langCollection.find().as(Language.class);
		Map<String, String> langMap = new HashMap<String, String>(); 
		for (Language lang : langs) {
			langMap.put(lang.getId(), lang.getLabel());
		}
		
		
		Map<String, Integer> res = new HashMap<String, Integer>(); 
		TreeMap<Integer,Integer> numbPerLang = new TreeMap<Integer,Integer>();
		
		//get list of vocabs
		
		MongoCursor<Vocabulary> vocabs = vocabCollection.find().as(Vocabulary.class);
		
		for (Vocabulary vocab : vocabs) {
			if(vocab.getVersions()!=null && vocab.getVersions().size()>0){
				Collections.sort(vocab.getVersions());
				VocabularyVersionWrapper version = vocab.getVersions().get(0);// <-- this is the latest version
				if(version.getLanguageIds()!=null){
					for (String langId : version.getLanguageIds()) {
						String lang = langMap.get(langId);
						if(res.get(lang)!=null)res.put(lang, res.get(lang)+1);
						else res.put(lang, 1);
					}
					int nbLangs = version.getLanguageIds().size();
					numbPerLang.put(nbLangs, (numbPerLang.get(nbLangs)!=null?numbPerLang.get(nbLangs):0)+1); 
				}
				else numbPerLang.put(0, (numbPerLang.get(0)!=null?numbPerLang.get(0):0)+1); 
				
			}
			
			
		}
		
		// sort
		ValueComparator bvc =  new ValueComparator(res);
		TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
		sorted_map.putAll(res);
		//output
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(2);
		StringBuffer sb = new StringBuffer();
		for ( Entry<String, Integer> item : sorted_map.entrySet()) {
			sb.append(item.getKey()+" & "+item.getValue()+" & "+ df.format((float)item.getValue()/(double)vocabs.count()*100) +"\\%      \\\\ \n");
		}
		log.info("####### Vocab per lang #######");	
		log.info(sb.toString());
		log.info("########################\n");
		
		//output
		sb = new StringBuffer();
		for ( Entry<Integer, Integer> item : numbPerLang.entrySet()) {
			sb.append("("+item.getKey()+","+item.getValue()+") \n");
		}
		log.info("####### vocabs per lang nb #######");	
		log.info(sb.toString());
		log.info("########################\n");
	}
	
//	private void computeVocabsAgent(MongoCollection vocabCollection){
//		//get list of vocabs
//		MongoCursor<Vocabulary> vocabs = vocabCollection.find().as(Vocabulary.class);
//		int vocWithAgent = 0; // creator / contrib / publisher
//		
//		//for each vocabs, extract creation date / last modif date
//		for (Vocabulary vocab : vocabs) {
//			if(vocab.getCreatorIds()!=null && vocab.getCreatorIds().size()>0 ||
//					vocab.getContributorIds()!=null && vocab.getContributorIds().size()>0 ||
//					vocab.getPublisherIds()!=null && vocab.getPublisherIds().size()>0 )
//		}
//		//output
//		log.info("####### expressivity #######");	
////		log.info(""+rdf);	
//		log.info((float)((float)rdf/(float)(vocabs.count()))+"/rdf, "+(float)((float)rdfs/(float)(vocabs.count()))+"/rdfs, "+(float)((float)owl/(float)(vocabs.count()))+"/owl");
//		log.info("########################\n");
//	}
	
	private void computeVoafRel(MongoCollection vocabCollection){
		
		
		//get list of vocabs
		MongoCursor<Vocabulary> vocabs = vocabCollection.find().as(Vocabulary.class);
		Map<String,Integer> voafRel = new HashMap<String,Integer>();
		voafRel.put("voaf:metadataVoc", 0);
		voafRel.put("voaf:extends", 0);
		voafRel.put("voaf:specializes", 0);
		voafRel.put("voaf:generalizes", 0);
		voafRel.put("voaf:hasEquivalencesWith", 0);
		voafRel.put("voaf:hasDisjunctionsWith", 0);
		voafRel.put("owl:imports", 0);
		
		for (Vocabulary vocab : vocabs) {
			if(vocab.getVersions()!=null && vocab.getVersions().size()>0){
				Collections.sort(vocab.getVersions());
				VocabularyVersionWrapper version = vocab.getVersions().get(0);// <-- this is the latest version
				if(version.getRelMetadata()!=null) voafRel.put("voaf:metadataVoc", voafRel.get("voaf:metadataVoc")+version.getRelMetadata().size());
				if(version.getRelExtends()!=null) voafRel.put("voaf:extends", voafRel.get("voaf:extends")+version.getRelExtends().size());
				if(version.getRelSpecializes()!=null) voafRel.put("voaf:specializes", voafRel.get("voaf:specializes")+version.getRelSpecializes().size());
				if(version.getRelGeneralizes()!=null) voafRel.put("voaf:generalizes", voafRel.get("voaf:generalizes")+version.getRelGeneralizes().size());
				if(version.getRelEquivalent()!=null) voafRel.put("voaf:hasEquivalencesWith", voafRel.get("voaf:hasEquivalencesWith")+version.getRelEquivalent().size());
				if(version.getRelDisjunc()!=null) voafRel.put("voaf:hasDisjunctionsWith", voafRel.get("voaf:hasDisjunctionsWith")+version.getRelDisjunc().size());
				if(version.getRelImports()!=null) voafRel.put("owl:imports", voafRel.get("owl:imports")+version.getRelImports().size());
			}			
		}
		// sort
				ValueComparator bvc =  new ValueComparator(voafRel);
				TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
				sorted_map.putAll(voafRel);
		//output
		StringBuffer sb = new StringBuffer();
		for ( Entry<String, Integer> item : sorted_map.entrySet()) {
			sb.append(item.getKey()+" & "+item.getValue()+" \\\\ \n");
		}
		log.info("####### vocabs per lang nb #######");	
		log.info(sb.toString());
		log.info("########################\n");
	}
	
	
	class ValueComparator implements Comparator<String> {

	    Map<String, Integer> base;
	    public ValueComparator(Map<String, Integer> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}
	
	
	private void computeElements(MongoCollection vocabCollection){
		//get list of vocabs
		MongoCursor<Vocabulary> vocabs = vocabCollection.find().as(Vocabulary.class);
		
		int nbVocabs = 0;
		
		int nbClasses = 0;
		int nbProperties = 0;
		int nbDatatypes = 0;
		int nbInstances = 0;
		
		int[] classes = new int[vocabs.count()];
		int[] properties = new int[vocabs.count()];
		int[] datatypes = new int[vocabs.count()];
		int[] instances = new int[vocabs.count()];
		
		int[] versions = new int[vocabs.count()];
		int nbVersions = 0;
		
		//for each vocabs, extract creation date / last modif date
		int cpt=0;
		for (Vocabulary vocab : vocabs) {
			if(vocab.getVersions()!=null && vocab.getVersions().size()>0){
				versions[cpt]=vocab.getVersions().size();
				nbVersions+=vocab.getVersions().size();
				
				Collections.sort(vocab.getVersions());
				VocabularyVersionWrapper version = vocab.getVersions().get(0);// <-- this is the latest version
				nbClasses+=version.getClassNumber();
				classes[cpt]=version.getClassNumber();
				
				nbProperties+=version.getPropertyNumber();
				properties[cpt]=version.getPropertyNumber();
				
				nbDatatypes+=version.getDatatypeNumber();
				datatypes[cpt]=version.getDatatypeNumber();
				
				nbInstances+=version.getInstanceNumber();
				instances[cpt]=version.getInstanceNumber();
			}
			else versions[cpt]=0;
			cpt++;
		}
		nbVocabs=vocabs.count();
		
		
		StringBuffer sb = new StringBuffer();
		sb.append("Vocabularies & "+nbVocabs+ " & \\\\ \n");	
		sb.append("Classes & "+nbClasses+ " & "+getMedian(classes)+" \\\\ \n");	
		sb.append("Properties & "+nbProperties+ " & "+getMedian(properties)+" \\\\ \n");	
		sb.append("Instances & "+nbInstances+ " & "+getMedian(instances)+" \\\\ \n");	
		sb.append("Datatypes & "+nbDatatypes+ " & "+getMedian(datatypes)+" \\\\ \n\n");	
		
		sb.append("Versions nb / avg / median : "+nbVersions+ " / "+ ((float)nbVersions/(float)vocabs.count()) + " / "+getMedian(versions)+" \\\\ \n");	
		//output
		log.info("####### stats #######");	
		log.info(sb.toString());
		log.info("########################\n");
	}
	
	private double getMedian(int[] numArray){
		
		Arrays.sort(numArray);
		double median;
		if (numArray.length % 2 == 0)
		    median = ((double)numArray[numArray.length/2] + (double)numArray[numArray.length/2 - 1])/2;
		else
		    median = (double) numArray[numArray.length/2];
		return median;
	}
	
}
