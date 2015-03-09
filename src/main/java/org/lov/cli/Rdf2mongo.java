package org.lov.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.lov.LOVException;
import org.lov.objects.Agent;
import org.lov.objects.Element;
import org.lov.objects.Language;
import org.lov.objects.User;
import org.lov.objects.Vocabulary;
import org.lov.rdf2mongo.ICom;
import org.lov.rdf2mongo.extractors.AgentsExtractor;
import org.lov.rdf2mongo.extractors.ElementsExtractor;
import org.lov.rdf2mongo.extractors.LanguagesExtractor;
import org.lov.rdf2mongo.extractors.UsersExtractor;
import org.lov.rdf2mongo.extractors.VocabulariesExtractor;
import org.lov.vocidex.extract.LOVExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.shared.NotFoundException;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClientURI;

/**
 * A command line tool that transform LOV dump to mongoDB. Uses {@link LOVExtractor}.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class Rdf2mongo extends CmdGeneral implements ICom {
	private final static Logger log = LoggerFactory.getLogger(Rdf2mongo.class);
	
	public static void main(String... args) {
		new Rdf2mongo(args).mainRun();
	}

	private String hostName;
	private String dbName;
	private String lovDumpFile;
	private String langDumpFile;
	private String metricsFile;
	private Properties lovConfig;
	private MongoCollection langCollection;
	private MongoCollection agentCollection;
	private MongoCollection elementCollection;
	private MongoCollection vocabCollection;
	
	public Rdf2mongo(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("configFilePath", "absolute path for the configuration file  (e.g. /home/...)");
	}
	
	@Override
    protected String getCommandName() {return "rdf2mongo";}	
	@Override
	protected String getSummary() {return getCommandName() + " configFilePath";}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 1) {
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
			lovDumpFile = lovConfig.getProperty("LOV_RDF_FILE_PATH");
			langDumpFile = lovConfig.getProperty("LANGUAGES_FILE_PATH");
			metricsFile = lovConfig.getProperty("METRICS_FILE_PATH");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void exec() {
		try {
			//parse languages.rdf
			log.info("Loading Languages dump: " + langDumpFile);
			Dataset langDataset = RDFDataMgr.loadDataset(langDumpFile, Lang.TTL);
			
			//parse metrics file 
			log.info("Loading metrics dump: " + metricsFile);
			Dataset metricsDataset = RDFDataMgr.loadDataset(Paths.get(metricsFile).toUri().toString(), Lang.N3);
						
			//parse LOV.rdf
			log.info("Loading LOV dump: " + lovDumpFile);
			Dataset dataset = RDFDataMgr.loadDataset(Paths.get(lovDumpFile).toUri().toString(), Lang.RDFXML);
			long graphCount = 1;
			long tripleCount = dataset.getDefaultModel().size();
//			Iterator<String> it = dataset.listNames();
//			while (it.hasNext()) {
//				graphCount++;
//				tripleCount += dataset.getNamedModel(it.next()).size();
//			}
			log.info("Read " + tripleCount + " triples in " + graphCount + " graphs");
						
			//bootstrap connection to MongoDB
			DB db = Mongo.Holder.singleton().connect(new MongoClientURI("mongodb://"+hostName)).getDB(dbName);
			Jongo jongo = new Jongo(db);
			
			/* Process Languages */
			log.info("--Inserting Languages--");
			
			langCollection = DropCreateCollection("languages",jongo);
			LanguagesExtractor langExtractor = new LanguagesExtractor(langDataset);
			int cpt=0;
			for (Language lang: langExtractor) {
				langCollection.insert(lang);
				cpt++;
			}
			langDataset.close();
			log.info(cpt+ " Languages inserted");
			
			/* Process Agents */
			/*log.info("--Inserting Agents--");
			agentCollection = DropCreateCollection("agents",jongo);
			AgentsExtractor agentExtractor = new AgentsExtractor(dataset);
			cpt=0;
			for (Agent agent: agentExtractor) {
				agentCollection.insert(agent);
				cpt++;
			}
			log.info(cpt+ " Agents inserted");*/
			
			
			/* Process Metrics for elements */
			/*log.info("--Inserting Elements--");
			elementCollection = DropCreateCollection("elements",jongo);
			ElementsExtractor elementsExtractor = new ElementsExtractor(metricsDataset);
			cpt=0;
			for (Element el: elementsExtractor) {
				elementCollection.insert(el);
				cpt++;
			}
			log.info(cpt+ " Vocabulary elements with metrics inserted");*/
						
			/* create users for LOV editors*/
			/*log.info("--Inserting Users--");
			MongoCollection usersCollection = DropCreateCollection("users",jongo);
			UsersExtractor userExtractor = new UsersExtractor(dataset,agentCollection);
			cpt=0;
			for (User user: userExtractor) {
				usersCollection.insert(user);
				cpt++;
			}
			log.info(cpt+ " Users inserted");*/
			
			
			
			/* Process Vocabularies */
			/*log.info("--Inserting Vocabularies--");
			vocabCollection = DropCreateCollection("vocabularies",jongo);	
			//clear versions folder
			File vocabFolder = new File(lovConfig.getProperty("VERSIONS_DIR_PATH"));
			deleteFolder(vocabFolder);
			vocabFolder.mkdir();
			
			VocabulariesExtractor vocabulariesExtractor = new VocabulariesExtractor(dataset, metricsDataset, this);
			cpt=0;
			for (Vocabulary vocab: vocabulariesExtractor) {
				vocabCollection.insert(vocab);				
				cpt++;
			}
			log.info(cpt+ " Vocabularies inserted");
			dataset.close();
			log.info("---Done---");*/
			
			
		} catch (UnknownHostException e){
			cmdError(e.getMessage());
		} catch (NotFoundException ex) {
			cmdError("Not found: " + ex.getMessage());
		} catch (LOVException ex) {
			cmdError(ex.getMessage());
		}
	}
	
	private MongoCollection DropCreateCollection(String collectionname, Jongo jongo){
		jongo.getCollection(collectionname).drop();
		return jongo.getCollection(collectionname);
	}

	@Override
	public Properties getLovConfig() {
		return lovConfig;
	}
	@Override
	public MongoCollection getLangCollection() {
		return langCollection;
	}
	@Override
	public MongoCollection getAgentCollection() {
		return agentCollection;
	}
	@Override
	public MongoCollection getVocabCollection() {
		return vocabCollection;
	}
	
	public static void deleteFolder(File folder) {
	    File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	                deleteFolder(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}
}
