package org.lov.cli;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.lov.LovAggregatorAgent;
import org.lov.LovBotVocabAnalyser;
import org.lov.objects.Language;
import org.lov.objects.Vocabulary;
import org.lov.objects.VocabularySuggest;
import org.lov.objects.VocabularyVersionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.hp.hpl.jena.rdf.model.Model;
import com.mongodb.MongoClient;


//TODO check DNS Validity
/**
 * ...
 * 
 */
public class Aggregator extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(Aggregator.class);
	private static MongoCollection vocabCollection;
	private static Properties lovConfig;
	
	public static void main(String... args) {
		new Aggregator(args).mainRun();
	}

	private String configFilePath;
	
	public Aggregator(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("configFilePath", "absolute path for the configuration file  (e.g. /home/...)");
	}
	
	@Override
    protected String getCommandName() {
		return "aggregator";
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + " configFilePath (e.g. /home/...)";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 1) {
			doHelp();
		}
		configFilePath = getPositionalArg(0);
	}

	@Override
	protected void exec() {
		try {
			log.info("####### <Aggregator> #######");
			
			//load properties from the config file
			lovConfig = new Properties();
			File file = new File(configFilePath);
			InputStream is = new FileInputStream(file);
			lovConfig.load(is);
			
			/* get the list of vocabs (URI and isDefinedBy) and their last version file */
			//get the connection to mongodb and its vocabularies collection
			MongoClient mongoClient = new MongoClient( lovConfig.getProperty("MONGO_DB_HOST") , Integer.parseInt(lovConfig.getProperty("MONGO_DB_PORT")) );
			Jongo jongo = new Jongo(mongoClient.getDB( lovConfig.getProperty("MONGO_DB_INSTANCE") ));
			vocabCollection = jongo.getCollection("vocabularies");
			MongoCursor<Vocabulary> vocabs = vocabCollection.find().sort("{prefix:1}").as(Vocabulary.class);
			log.info("analyzing "+vocabs.count()+" vocabularies...");
			
			//create the pool
			ExecutorService executor = Executors.newFixedThreadPool(100);
			List<LovAggregatorAgent> agents = new ArrayList<LovAggregatorAgent>(vocabs.count());
			
			//for each vocab get the online version, compare with latest one
			while (vocabs.hasNext()) {
				Vocabulary vocab = (Vocabulary) vocabs.next();
				agents.add(new LovAggregatorAgent(vocab));
			}
			executor.invokeAll(agents, 180, TimeUnit.SECONDS);
			shutdownAndAwaitTermination(executor);
			
			int cptTerminated=0;
			int cptRetrieved=0;
			int cptUpdated=0;
			for (LovAggregatorAgent lovAggregatorAgent : agents) {
				if(lovAggregatorAgent.isTerminated())cptTerminated++;
				if(lovAggregatorAgent.isRetrieved())cptRetrieved++;
				if(compareAndUpdateVocabularyRecord(lovAggregatorAgent.isRetrieved(),lovAggregatorAgent.getVocabModel(),lovAggregatorAgent.getVocab()))cptUpdated++;
			}
			
			
			log.info(agents.size()+" vocabularies");
			log.info(cptTerminated+" vocabulary scanned");
			log.info(cptRetrieved+" vocabulary correctly retrieved");
			log.info( (agents.size()-cptRetrieved)+" vocabulary in error");
			log.info(cptUpdated+" vocabulary updated");
			log.info("####### </Aggregator> #######");
		} catch (FileNotFoundException e) {
			cmdError("Not found: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			cmdError("IO Exception: " + e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean compareAndUpdateVocabularyRecord(boolean isRetrieved, Model retrievedModel, Vocabulary vocab){
		boolean hasToBeUpdated=false;
		long dif = 0;
		// depending if vocabulary file has been downloaded or not, update status and date
		if(isRetrieved){
			/* Compare retrieved file with latest local file */
			//get the latest version file, if different, then store the file, update the record, update RDF Repository?
			if(vocab.getVersions()!=null && vocab.getVersions().size()>0){
				Collections.sort(vocab.getVersions());
				for (int i=0; i<vocab.getVersions().size();i++){
					if(vocab.getVersions().get(i).getFileURL()!=null){
						String versionFileURL = vocab.getVersions().get(i).getFileURL();
						//temporary fix used during migration 
						//TODO to be removed after migration
						versionFileURL = new File(lovConfig.getProperty("VERSIONS_DIR_PATH")+"/"+vocab.getId()+"/"
											+vocab.getId()+"_"+new SimpleDateFormat("yyyy-MM-dd").format(vocab.getVersions().get(i).getIssued())+".n3").toURI().toString();
						
						if(new File(lovConfig.getProperty("VERSIONS_DIR_PATH")+"/"+vocab.getId()+"/"
								+vocab.getId()+"_"+new SimpleDateFormat("yyyy-MM-dd").format(vocab.getVersions().get(i).getIssued())+".n3").exists()){
							Model lastVersionModel = RDFDataMgr.loadModel(versionFileURL,Lang.N3);
							if(!lastVersionModel.isIsomorphicWith(retrievedModel)){//not isomorphic ==> Vocab Changed
								log.info(vocab.getPrefix()+ " -> Modified "+(retrievedModel.size()-lastVersionModel.size()));
								dif = retrievedModel.size()-lastVersionModel.size();
								hasToBeUpdated=true;
							}
						}
						else {
							log.info(vocab.getPrefix()+ " -> Modified "+(retrievedModel.size()));
							dif = retrievedModel.size();
							hasToBeUpdated=true;
						}
						break;
					}
				}
			}else{	//no previous version ==> Vocab Changed
				log.info(vocab.getPrefix()+ " -> First Version "+(retrievedModel.size()));
				hasToBeUpdated=true;
			}
			if(hasToBeUpdated==true){//update the mongoDB record
				try {
//					VocabularyVersionWrapper lastVersion = vocab.getLastVersion();
//					if(dif==0 && lastVersion!=null){//TODO to be removed: only for the migrqtion qs jenq pqrser is different from sesqme
//						String versionAbsoluteLocalPAth = lovConfig.getProperty("VERSIONS_DIR_PATH")+"/"+vocab.getId()+"/"
//								+vocab.getId()+"_"+new SimpleDateFormat("yyyy-MM-dd").format(lastVersion.getIssued())+".n3";
//						
//						// store the file in .n3 locally
//						File vocabVersionDir = new File(lovConfig.getProperty("VERSIONS_DIR_PATH")+"/"+vocab.getId());
//						if(!vocabVersionDir.exists())vocabVersionDir.mkdir();
//						File versionFile = new File(versionAbsoluteLocalPAth);
//						if(!versionFile.exists())versionFile.createNewFile();
//						OutputStream fopn3 = new BufferedOutputStream(new FileOutputStream(versionFile));
//						RDFDataMgr.write(fopn3, retrievedModel, Lang.N3) ;
//										
//						
//					}
//					else{
						String versionAbsoluteLocalPAth = lovConfig.getProperty("VERSIONS_DIR_PATH")+"/"+vocab.getId()+"/"
								+vocab.getId()+"_"+new SimpleDateFormat("yyyy-MM-dd").format(new Date())+".n3";
						
						// store the file in .n3 locally
						File vocabVersionDir = new File(lovConfig.getProperty("VERSIONS_DIR_PATH")+"/"+vocab.getId());
						if(!vocabVersionDir.exists())vocabVersionDir.mkdir();
						File versionFile = new File(versionAbsoluteLocalPAth);
						if(!versionFile.exists())versionFile.createNewFile();
						OutputStream fopn3 = new BufferedOutputStream(new FileOutputStream(versionFile));
						RDFDataMgr.write(fopn3, retrievedModel, Lang.N3) ;
										
						//create version wrapper with file absolute public URL
						VocabularyVersionWrapper version = new VocabularyVersionWrapper();
						//read from the URI and load it in a model
						VocabularySuggest vocabVersion = LovBotVocabAnalyser.analyseVersion(versionFile.toURI().toString(), vocab.getUri(), vocab.getNsp(),Lang.N3, lovConfig);
						version.setName("v"+new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
						version.setIssued(new Date());
						version.setFileURL(lovConfig.getProperty("LOV_DATASET_URI")+"/vocabs/"+vocab.getPrefix()+"/versions/"+vocab.getPrefix()+"-"+new SimpleDateFormat("yyyy-MM-dd").format(new Date())+".n3");
						version.setClassNumber(vocabVersion.getNbClasses());
						version.setPropertyNumber(vocabVersion.getNbProperties());
						version.setInstanceNumber(vocabVersion.getNbInstances());
						version.setDatatypeNumber(vocabVersion.getNbDatatypes());
						if(vocabVersion.getRelMetadata()!=null){for (VocabularySuggest relNsp : vocabVersion.getRelMetadata()) {
								version.addRelMetadata(relNsp.getUri());}
						}
						if(vocabVersion.getRelDisjunc()!=null){for (VocabularySuggest relNsp : vocabVersion.getRelDisjunc()) {
							version.addRelDisjunc(relNsp.getUri());}
						}
						if(vocabVersion.getRelEquivalent()!=null){for (VocabularySuggest relNsp : vocabVersion.getRelEquivalent()) {
							version.addRelEquivalent(relNsp.getUri());}
						}
						if(vocabVersion.getRelExtends()!=null){for (VocabularySuggest relNsp : vocabVersion.getRelExtends()) {
							version.addRelExtends(relNsp.getUri());}
						}
						if(vocabVersion.getRelGeneralizes()!=null){for (VocabularySuggest relNsp : vocabVersion.getRelGeneralizes()) {
							version.addRelGeneralizes(relNsp.getUri());}
						}
						if(vocabVersion.getRelImports()!=null){for (VocabularySuggest relNsp : vocabVersion.getRelImports()) {
							version.addRelImports(relNsp.getUri());}
						}
						if(vocabVersion.getRelSpecializes()!=null){for (VocabularySuggest relNsp : vocabVersion.getRelSpecializes()) {
							version.addRelSpecializes(relNsp.getUri());}
						}
						
						//Languages
						if(vocabVersion.getLanguages()!=null){for (Language lang : vocabVersion.getLanguages()){
							version.addLanguageId(lang.getId());}
						}
						
						// update vocabulary info based on analysis of the version including last Modification.
						vocab.setLastModifiedInLOVAt(version.getIssued());
						vocab.setLastDeref(version.getIssued());
						vocab.setCommentDeref(null);
						//TODO add a status manuallyChecked=false ? to the version
						vocab.addVersion(version);
						
						// save the vocab object in MongoDB
						vocabCollection.save(vocab);
//					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else{ //not retrieved, mark down on vocab Status
			vocab.setCommentDeref("Problem while dereferencing or parsing the vocabulary");
			// save the vocab object in MongoDB
			vocabCollection.save(vocab);
		}
		return hasToBeUpdated;
	}
	
//	private String writeFile(Vocabulary vocab){
//		try {
//			/* MANAGE VERSION HISTORY */
//			//Create a file archive using the date of the day
//			File vocabArchivDirectory = new File(LOV_AGGREGATOR_ARCHIVES_PATH+"dir_"+vocab.getPrefix());
//			if(!vocabArchivDirectory.exists())vocabArchivDirectory.mkdir();
//			
//			File newFileArchive = new File(LOV_AGGREGATOR_ARCHIVES_PATH+"dir_"+vocab.getPrefix()+"/file_"+prefix+"_"+todayString+extension);
//			if(newFileArchive.exists())newFileArchive.delete();
//			newFileArchive.createNewFile();
//			
//			
//			String extension = ".n3";//vocab.getExtensionForFile();
//			File newFile = new File(LOV_AGGREGATOR_DOWNLOAD_PATH+prefix+extension);
//			if(newFile.exists())newFile.delete();
//			newFile.createNewFile();
//			
//			LovUtil.WriteRepositoryToFile(newFile, con,RDFFormat.N3);
//			
//			/* MANAGE VERSION HISTORY */
//			//Create a file archive using the date of the day
//			File vocabArchivDirectory = new File(LOV_AGGREGATOR_ARCHIVES_PATH+"dir_"+vocab.getPrefix());
//			if(!vocabArchivDirectory.exists())vocabArchivDirectory.mkdir();
//			
//			File newFileArchive = new File(LOV_AGGREGATOR_ARCHIVES_PATH+"dir_"+vocab.getPrefix()+"/file_"+prefix+"_"+todayString+extension);
//			if(newFileArchive.exists())newFileArchive.delete();
//			newFileArchive.createNewFile();
//			
//			LovUtil.WriteRepositoryToFile(newFileArchive, con,RDFFormat.N3);
//			
//			//Create a new FRBR Expression for the vocabulary
//			BNode expression = LovUtil.createBnode(lovConnection);
//			LovUtil.addStatement(vocab.getUri(), LovConstants.FRBR_FULL_REALIZATION, expression, lovConnection);
//			LovUtil.addStatement(expression, LovConstants.RDF_FULL_TYPE, LovUtil.createURI(LovConstants.FRBR_FULL_EXPRESSION,lovConnection), lovConnection);
//			LovUtil.addStatement(expression, LovConstants.DC_TERMS_FULL_DATE, todayString, LovUtil.createURI("http://www.w3.org/2001/XMLSchema#date", lovConnection), lovConnection);
//			LovUtil.addStatement(expression, LovConstants.RDFS_FULL_LABEL, "v"+todayString,"en", lovConnection);
//			LovUtil.addStatement(expression, LovConstants.DC_TERMS_FULL_DESCRIPTION, "<p>"+LovConstants.EXPRESSION_DESC_AUTO+ "</p>"+generateDescriptionFormDiff(diffAdded,diffDeleted), lovConnection);
//			String vocabArchivePublicURL = LOV_PUBLIC_URL+"agg/archives/dir_"+vocab.getPrefix()+"/file_"+prefix+"_"+todayString+extension;
//			URI manifestation = LovUtil.createURI(vocabArchivePublicURL, lovConnection);
//			LovUtil.addStatement(expression, LovConstants.FRBR_FULL_EMBODIMENT, manifestation, lovConnection);
//			LovUtil.addStatement(manifestation, LovConstants.RDF_FULL_TYPE, LovConstants.FRBR_FULL_MANIFESTATION, lovConnection);
//					
//			
//			return prefix+extension;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return "null";
//		}
//		
//	}
	
	
	private void shutdownAndAwaitTermination(ExecutorService pool) {
	   pool.shutdown(); // Disable new tasks from being submitted
	   System.out.println("pool.shutdown()");
	   try {
	     // Wait a while for existing tasks to terminate
	     if (!pool.awaitTermination(20, TimeUnit.SECONDS)) {
	       pool.shutdownNow(); // Cancel currently executing tasks
	       System.out.println("pool.shutdownNow()");
	       // Wait a while for tasks to respond to being cancelled
	       if (!pool.awaitTermination(20, TimeUnit.SECONDS)){}
	    	 
	     }
	   } catch (InterruptedException ie) {
	     // (Re-)Cancel if current thread also interrupted
	     pool.shutdownNow();
	     // Preserve interrupt status
	     Thread.currentThread().interrupt();
	   }
	}
	
}
