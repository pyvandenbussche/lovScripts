package org.lov.cli;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.lov.LovAggregatorAgent;
import org.lov.objects.Vocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

/**
 * ...
 * 
 */
public class DownloadVersion extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(DownloadVersion.class);
	
	public static void main(String... args) {
		new DownloadVersion(args).mainRun();
	}

	private String vocabularyURI;
	private Properties lovConfig;
	
	public DownloadVersion(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("vocabularyURI", "URI of the vocabulary (e.g. http://...)");
		getUsage().addUsage("configFilePath", "absolute path for the configuration file  (e.g. /home/...)");
	}
	
	@Override
    protected String getCommandName() {
		return "downloadVersion";
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + "vocabularyURI configFilePath";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 2) {
			doHelp();
		}
		vocabularyURI = getPositionalArg(0);
		try {
			lovConfig = new Properties();
			File file = new File(getPositionalArg(1));
			InputStream is = new FileInputStream(file);
			lovConfig.load(is);			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void exec() {
		try {
			//
			Vocabulary vocab = new Vocabulary();
			vocab.setUri(vocabularyURI);
			List<LovAggregatorAgent> agents = new ArrayList<LovAggregatorAgent>(1);
			ExecutorService executor = Executors.newFixedThreadPool(1);
			agents.add(new LovAggregatorAgent(vocab));
			executor.invokeAll(agents, 15, TimeUnit.SECONDS);
			shutdownAndAwaitTermination(executor);
			
			//a version has been fetched
			if(agents.get(0).getVocabModel()!=null && agents.get(0).getVocabModel().size()>0){
				String uniqueID = UUID.randomUUID().toString();
				// store the file in .n3 locally
				File vocabVersionDir = new File(lovConfig.getProperty("VERSIONS_TEMP_PATH"));
				if(!vocabVersionDir.exists())vocabVersionDir.mkdir();
				File versionFile = new File(lovConfig.getProperty("VERSIONS_TEMP_PATH")+"/"+uniqueID+".n3");
				if(!versionFile.exists())versionFile.createNewFile();
				OutputStream fopn3 = new BufferedOutputStream(new FileOutputStream(versionFile));
				RDFDataMgr.write(fopn3, agents.get(0).getVocabModel(), Lang.N3) ;
				System.out.println(uniqueID+".n3");
			}
			else System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void shutdownAndAwaitTermination(ExecutorService pool) {
		   pool.shutdown(); // Disable new tasks from being submitted
		   try {
		     // Wait a while for existing tasks to terminate
		     if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
		       pool.shutdownNow(); // Cancel currently executing tasks
		       // Wait a while for tasks to respond to being cancelled
		       if (!pool.awaitTermination(5, TimeUnit.SECONDS)){}
		    	 
		     }
		   } catch (InterruptedException ie) {
		     // (Re-)Cancel if current thread also interrupted
		     pool.shutdownNow();
		     // Preserve interrupt status
		     Thread.currentThread().interrupt();
		   }
		}
}
