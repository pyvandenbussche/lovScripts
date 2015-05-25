package org.lov.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.hp.hpl.jena.shared.NotFoundException;
import com.mongodb.MongoClient;

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
			log.info("Computing lov weekly evolution since: " + formatLatex.format(startingDate.getTime()));
						
			//bootstrap connection to MongoDB and create model
			Jongo jongo = new Jongo(new MongoClient(hostName).getDB(dbName));
			MongoCollection vocabCollection = jongo.getCollection("vocabularies");
			
			
			
					
			StringBuffer sb = new StringBuffer();
			//iterate until today
			while(startingDate.before(Calendar.getInstance())){
				long nbVocabs =  vocabCollection.count("{ createdInLOVAt: { $lte: # } }",startingDate.getTime());
				sb.append(formatLatex.format(startingDate.getTime())+"\t"+nbVocabs+"\n");
				startingDate.add(Calendar.WEEK_OF_YEAR, 1);
			}
			
			log.info("####### <Summary> #######");
			log.info(sb.toString());
			log.info("####### </Summary> #######");
			
			log.info("Done!");
		} catch (NotFoundException ex) {
			cmdError("Not found: " + ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
