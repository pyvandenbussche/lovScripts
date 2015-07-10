package org.lov.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.lov.objects.StatVocab;
import org.lov.objects.Vocabulary;
import org.lov.objects.VocabularyVersionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.mongodb.MongoClient;


/**
 * ...
 * 
 */
public class StatsOneVocab extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(StatsOneVocab.class);
	
	private String hostName;
	private String dbName;
	private String vocabularyURI;
	private static MongoCollection vocabCollection;
	private static Jongo jongo;
	
	public static void main(String... args) {
		new StatsOneVocab(args).mainRun();
	}

	private String configFilePath;
	
	public StatsOneVocab(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("configFilePath", "absolute path for the configuration file  (e.g. /home/...)");
		getUsage().addUsage("vocabularyURI", "URI of the vocabulary as defined in LOV");
	}
	
	@Override
    protected String getCommandName() {
		return "statsonevocab";
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + " configFilePath (e.g. /home/...)";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 2) {
			doHelp();
		}
		configFilePath = getPositionalArg(0);
		vocabularyURI = getPositionalArg(1);
		//load properties from the config file
		try {
			Properties lovConfig = new Properties();
			File file = new File(configFilePath);
			InputStream is = new FileInputStream(file);
			lovConfig.load(is);
			hostName= lovConfig.getProperty("MONGO_DB_HOST")+":"+lovConfig.getProperty("MONGO_DB_PORT");
			dbName = lovConfig.getProperty("MONGO_DB_INSTANCE");
			jongo = new Jongo(new MongoClient(hostName).getDB(dbName));
			vocabCollection = jongo.getCollection("vocabularies");
			
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	@Override
	protected void exec() {
			try {
				long startTime = System.currentTimeMillis();
				log.info("####### <Stats for one Vocab> #######");
				
				MongoCollection statCollection = getCollection("statvocabularies",jongo);
				
				/* Generate number incoming links per vocabulary */
				//get the list of vocabularies and their relationships to others
				MongoCursor<Vocabulary> vocabs = vocabCollection.find().as(Vocabulary.class);
				List<Vocabulary> vocabList = new ArrayList<Vocabulary>();
				StatVocab currentStatVocab = null;
				VocabularyVersionWrapper lVersion = null;
				for (Vocabulary vocab : vocabs) {
					if(vocab.getUri().equals(vocabularyURI)){
						currentStatVocab = new StatVocab(vocab.getUri(),vocab.getNsp(), vocab.getPrefix());
						lVersion = vocab.getLastVersion();
					}
					vocabList.add(vocab);
				}
							
				if(currentStatVocab!=null){
						if(lVersion!=null){
							currentStatVocab.addOutRelDisjunc(lVersion.getRelDisjunc(), statCollection);
							currentStatVocab.addOutRelEquivalent(lVersion.getRelEquivalent(), statCollection);
							currentStatVocab.addOutRelExtends(lVersion.getRelExtends(), statCollection);
							currentStatVocab.addOutRelGeneralizes(lVersion.getRelGeneralizes(), statCollection);
							currentStatVocab.addOutRelImports(lVersion.getRelImports(), statCollection);
							currentStatVocab.addOutRelMetadata(lVersion.getRelMetadata(), statCollection);
							currentStatVocab.addOutRelSpecializes(lVersion.getRelSpecializes(), statCollection);
							
						}
						for (Vocabulary voc : vocabList) {
							VocabularyVersionWrapper lastVersion = voc.getLastVersion();
							if(lastVersion!=null){
								if(lastVersion.getRelDisjunc()!=null && (lastVersion.getRelDisjunc().contains(currentStatVocab.getNsp()) || lastVersion.getRelDisjunc().contains(currentStatVocab.getUri()) ))currentStatVocab.addIncomRelDisjunc(voc.getUri(),statCollection);
								if(lastVersion.getRelEquivalent()!=null && (lastVersion.getRelEquivalent().contains(currentStatVocab.getNsp()) || lastVersion.getRelEquivalent().contains(currentStatVocab.getUri()) ))currentStatVocab.addIncomRelEquivalent(voc.getUri(),statCollection);
								if(lastVersion.getRelExtends()!=null && (lastVersion.getRelExtends().contains(currentStatVocab.getNsp()) || lastVersion.getRelExtends().contains(currentStatVocab.getUri()) ))currentStatVocab.addIncomRelExtends(voc.getUri(),statCollection);
								if(lastVersion.getRelGeneralizes()!=null && (lastVersion.getRelGeneralizes().contains(currentStatVocab.getNsp()) || lastVersion.getRelGeneralizes().contains(currentStatVocab.getUri()) ))currentStatVocab.addIncomRelGeneralizes(voc.getUri(),statCollection);
								if(lastVersion.getRelImports()!=null && (lastVersion.getRelImports().contains(currentStatVocab.getNsp()) || lastVersion.getRelImports().contains(currentStatVocab.getUri()) ))currentStatVocab.addIncomRelImports(voc.getUri(),statCollection);
								if(lastVersion.getRelMetadata()!=null && (lastVersion.getRelMetadata().contains(currentStatVocab.getNsp()) || lastVersion.getRelMetadata().contains(currentStatVocab.getUri()) ))currentStatVocab.addIncomRelMetadata(voc.getUri(),statCollection);
								if(lastVersion.getRelSpecializes()!=null && (lastVersion.getRelSpecializes().contains(currentStatVocab.getNsp()) || lastVersion.getRelSpecializes().contains(currentStatVocab.getUri()) ))currentStatVocab.addIncomRelSpecializes(voc.getUri(),statCollection);
							}					
						}
					
					//update statVocabs
					statCollection.update("{uri:#}", currentStatVocab.getUri()).upsert().with(currentStatVocab);
					log.info("Stat inserted");
				}
				
				log.info("####### </Stats> #######");
				long estimatedTime = System.currentTimeMillis() - startTime;
				log.info("=> Stats processed in "+String.format("%d sec, %d ms", 
					    TimeUnit.MILLISECONDS.toSeconds(estimatedTime),
					    estimatedTime - TimeUnit.MILLISECONDS.toSeconds(estimatedTime)
					));
			} catch (Exception e) {
				log.error(e.getMessage());
			}
	}
	
	private MongoCollection getCollection(String collectionname, Jongo jongo){
		return jongo.getCollection(collectionname);
	}
	
}
