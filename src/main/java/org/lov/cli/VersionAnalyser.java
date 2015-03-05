package org.lov.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.jena.riot.Lang;
import org.lov.LovBotVocabAnalyser;
import org.lov.objects.VocabularySuggest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.google.gson.Gson;

/**
 * ...
 * 
 */
public class VersionAnalyser extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(VersionAnalyser.class);
	
	public static void main(String... args) {
		new VersionAnalyser(args).mainRun();
	}

	private String versionURI;
	private String vocabularyURI;
	private String vocabularyNsp;
	private Properties lovConfig;
	
	
	//versionFile.toURI().toString(), vocab.getUri(), vocab.getNsp(),Lang.N3, lovConfig
	
	public VersionAnalyser(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("versionURI", "URI of the version (e.g. http://...)");
		getUsage().addUsage("vocabularyURI", "URI of the vocabulary (e.g. http://...)");
		getUsage().addUsage("vocabularyNsp", "Namespace of the vocabulary (e.g. http://...)");
		getUsage().addUsage("configFilePath", "absolute path for the configuration file  (e.g. /home/...)");
	}
	
	@Override
    protected String getCommandName() {
		return "versionAnalyser";
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + "versionURI vocabularyURI vocabularyNsp configFilePath";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 4) {
			doHelp();
		}
		versionURI = getPositionalArg(0);
		vocabularyURI = getPositionalArg(1);
		vocabularyNsp = getPositionalArg(2);
		try {
			lovConfig = new Properties();
			File file = new File(getPositionalArg(3));
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
			VocabularySuggest vocabVersion = LovBotVocabAnalyser.analyseVersion(versionURI, vocabularyURI, vocabularyNsp,Lang.N3, lovConfig);
//			result.prettyPrint(log);
			Gson gson  =new Gson();
			System.out.println(gson.toJson(vocabVersion.toVocabularyVersionWrapper()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
