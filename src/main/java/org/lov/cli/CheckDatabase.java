package org.lov.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.lov.LOVException;
import org.lov.objects.Vocabulary;
import org.lov.objects.VocabularyVersionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.hp.hpl.jena.shared.NotFoundException;
import com.mongodb.MongoClient;

/**
 * A command line tool to check inconsistencies in the vocabularies database (like date issues or mention of object properties in LanguageIds)
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class CheckDatabase extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(CheckDatabase.class);
	
	public static void main(String... args) {
		new CheckDatabase(args).mainRun();
	}

	private String hostName;
	private String dbName;
	private Properties lovConfig;
	private MongoCollection vocabCollection;
	
	public CheckDatabase(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("configFilePath", "absolute path for the configuration file  (e.g. /home/...)");
	}
	
	@Override
    protected String getCommandName() {return "checkDatabase";}	
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
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void exec() {
		try {
			//bootstrap connection to MongoDB and create model
			Jongo jongo = new Jongo(new MongoClient(hostName).getDB(dbName));
			vocabCollection = jongo.getCollection("vocabularies");
			
			int cptErrorDate=0;
			int cptErrorLangId=0;
			int cpt=0;
			long startTime,estimatedTime;
			
			
			
			
			// Process Vocabularies
			startTime = System.currentTimeMillis();
			log.info("Processing Vocabularies");
			MongoCursor<Vocabulary> vocabs = vocabCollection.find().as(Vocabulary.class);
			
			for (Vocabulary vocab : vocabs) {
				cpt++;
				
				// checking for errors in Date (using the wrong timezone as it should have a day granularity (hours, minutes and seconds should be 0))
				Calendar cal = Calendar.getInstance(Locale.UK);
				
				
				if(vocab.getVersions()!=null){
					
					
					for (int i=0; i<vocab.getVersions().size();i++){
						VocabularyVersionWrapper version = vocab.getVersions().get(i);
						
						cal.setTime(version.getIssued());
						if(cal.get(Calendar.HOUR_OF_DAY)>0){
							cptErrorDate++;
							System.out.println("[version date error] "+vocab.getPrefix()+" - "+DateYMD(cal.getTime()));
						}
						
						if(version.getLanguageIds()!=null){
							for(String langId: version.getLanguageIds()){
								if(langId.contains("ObjectId")){
									cptErrorLangId++;
									System.out.println("[version lang error] "+vocab.getPrefix());
								}
							}
						}
						
					}
				}
			}
			estimatedTime = System.currentTimeMillis() - startTime;
			log.info("=> "+cpt+" Vocabularies processed in "+String.format("%d min, %d sec", 
				    TimeUnit.MILLISECONDS.toMinutes(estimatedTime),
				    TimeUnit.MILLISECONDS.toSeconds(estimatedTime) - 
				    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(estimatedTime))
				));
			
			
			
			//Add LOV namedgraph to Dataset and write to file
			System.out.println("Number of date error:"+cptErrorDate);
			System.out.println("Number of Language Ids error:"+cptErrorLangId);
			
			log.info("---Done---");
			
		} catch (UnknownHostException e){
			cmdError(e.getMessage());
		} catch (NotFoundException ex) {
			cmdError("Not found: " + ex.getMessage());
		} catch (LOVException ex) {
			cmdError(ex.getMessage());
		}
	}
	
	
	private String DateYMD(Date d){
		if(d==null)return null;
		return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(d);
	}
}
