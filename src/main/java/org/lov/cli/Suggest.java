package org.lov.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.lov.LovBotVocabAnalyser;
import org.lov.objects.VocabularySuggest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import arq.cmdline.CmdGeneral;

/**
 * ...
 * 
 */
public class Suggest extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(Suggest.class);
	
	public static void main(String... args) {
		new Suggest(args).mainRun();
	}

	private String vocabularyURI;
	private Properties lovConfig;
	
	public Suggest(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("vocabularyURI", "URI of the vocabulary (e.g. http://...)");
		getUsage().addUsage("configFilePath", "absolute path for the configuration file  (e.g. /home/...)");
	}
	
	@Override
    protected String getCommandName() {
		return "suggest";
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
			VocabularySuggest result = LovBotVocabAnalyser.analyseVocabURI(vocabularyURI, lovConfig);
//			result.prettyPrint(log);
			Gson gson  =new Gson();
			System.out.println(gson.toJson(result));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
