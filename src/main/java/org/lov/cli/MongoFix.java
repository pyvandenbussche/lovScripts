package org.lov.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.lov.objects.Comment;
import org.lov.objects.Vocabulary;
import org.lov.objects.VocabularyVersionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.mongodb.MongoClient;

/**
 * A command line tool that applies fixes to MongoDB.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class MongoFix extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(MongoFix.class);
	
	public static void main(String... args) {
		new MongoFix(args).mainRun();
	}

	private String hostName;
	private String dbName;
	private Properties lovConfig;
//	private MongoCollection langCollection;
//	private MongoCollection agentCollection;
	private MongoCollection vocabCollection;
//	private MongoCollection elementCollection;
	
	public MongoFix(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("configFilePath", "absolute path for the configuration file  (e.g. /home/...)");
	}
	
	@Override
    protected String getCommandName() {return "fix";}	
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
			
			//bootstrap connection to MongoDB and create model
			Jongo jongo = new Jongo(new MongoClient(hostName).getDB(dbName));
//			langCollection = jongo.getCollection("languages");
//			agentCollection = jongo.getCollection("agents");
			vocabCollection = jongo.getCollection("vocabularies");
//			elementCollection = jongo.getCollection("elements");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void exec() {
		removeBotReviews();
		changeVersionFileURL();
	}
	
	private void removeBotReviews(){
		long startTime,estimatedTime;
		int cpt=0;	
		// Process Vocabularies
		startTime = System.currentTimeMillis();
		log.info("Removing bot generated reviews");
		MongoCursor<Vocabulary> vocabs = vocabCollection.find().as(Vocabulary.class);
		cpt=0;
		for (Vocabulary vocab : vocabs) {
			if(vocab.getReviews()!=null){
				boolean hasChanged=false;
				for (int i=vocab.getReviews().size()-1; i>-1; i--) {
					Comment comment = vocab.getReviews().get(i);
					if(comment.getAgentId().equals("54b2be048433ca9ccf1c1006")){
						hasChanged=true;
						cpt++;
						vocab.getReviews().remove(i);					
					}
				}
				if(hasChanged){//update the vocab
					vocabCollection.update("{prefix:#}",vocab.getPrefix()).with(vocab);
				}
			}
			else System.out.println("##### vocab without review: "+vocab.getPrefix());
		}
		estimatedTime = System.currentTimeMillis() - startTime;
		log.info(cpt+" bot reviews removed");
		log.info("---Done in "+String.format("%d sec, %d ms", TimeUnit.MILLISECONDS.toSeconds(estimatedTime), estimatedTime - TimeUnit.MILLISECONDS.toSeconds(estimatedTime))+" ---");
	}
	
	private void changeVersionFileURL(){
			long startTime,estimatedTime;
			int cpt=0;	
			// Process Vocabularies
			startTime = System.currentTimeMillis();
			log.info("Changing the version file URL for Vocabularies");
			MongoCursor<Vocabulary> vocabs = vocabCollection.find().as(Vocabulary.class);
			cpt=0;
			for (Vocabulary vocab : vocabs) {
				if(vocab.getVersions()!=null){
					boolean hasChanged=false;
					for (VocabularyVersionWrapper version : vocab.getVersions()) {
						if(version.getFileURL()!=null && version.getFileURL().length()>0){
							//then change the version file url for the new value
							int endCutIndex = indexOf(Pattern.compile("(\\d{4}-\\d{2}-\\d{2})"), version.getFileURL());
							int startCutIndex = version.getFileURL().lastIndexOf("/")+1;
							String newURL = version.getFileURL().substring(0, startCutIndex)+version.getFileURL().substring(endCutIndex, version.getFileURL().length());
							//System.out.println(version.getFileURL());
							//System.out.println(newURL);
							if(!newURL.equals(version.getFileURL())){
								hasChanged=true;
								cpt++;
							}
							version.setFileURL(newURL);							
						}
					}
					if(hasChanged){//update the vocab
						vocabCollection.update("{prefix:#}",vocab.getPrefix()).with(vocab);
					}
				}
				else System.out.println("##### vocab without version: "+vocab.getPrefix());
			}
			estimatedTime = System.currentTimeMillis() - startTime;
			log.info(cpt+" version file URL changed");
			log.info("---Done in "+String.format("%d sec, %d ms", TimeUnit.MILLISECONDS.toSeconds(estimatedTime), estimatedTime - TimeUnit.MILLISECONDS.toSeconds(estimatedTime))+" ---");
	}
	
	public static int indexOf(Pattern pattern, String s) {
	    Matcher matcher = pattern.matcher(s);
	    return matcher.find() ? matcher.start() : -1;
	}
	
	
	private String DateYMD(Date d){
		if(d==null)return null;
		return new SimpleDateFormat("yyyy-MM-dd").format(d);
	}

}
