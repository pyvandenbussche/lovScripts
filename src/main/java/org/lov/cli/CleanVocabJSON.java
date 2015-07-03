package org.lov.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.jongo.MongoCollection;
import org.lov.LOVException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.hp.hpl.jena.shared.NotFoundException;

/**
 * A command line tool to fix inconsistencies in the vocabularies database (like date issues or mention of object properties in LanguageIds)
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class CleanVocabJSON extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(CleanVocabJSON.class);
	
	public static void main(String... args) {
		new CleanVocabJSON(args).mainRun();
	}

	private File vocabulariesJsonFile;
	private String dbName;
	private Properties lovConfig;
	private MongoCollection vocabCollection;
	
	public CleanVocabJSON(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("configFilePath", "absolute path for the configuration file  (e.g. /home/...)");
	}
	
	@Override
    protected String getCommandName() {return "cleanvocabjson";}	
	@Override
	protected String getSummary() {return getCommandName() + " vocabulariesJsonFilePath";}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 1) {
			doHelp();
		}
		vocabulariesJsonFile = new File(getPositionalArg(0));
	}

	@Override
	protected void exec() {
			int cpt=0;
			int cptErrorDate=0;
			int cptErrorLangId=0;
			long startTime,estimatedTime;
			
			
			
			
			// Process Vocabularies
			startTime = System.currentTimeMillis();
			log.info("Processing Vocabularies");
			String prefix=null;
			try (BufferedReader br = new BufferedReader(new FileReader(vocabulariesJsonFile))) {
			    String line;
			    while ((line = br.readLine()) != null) {
			    	cpt++;
			    	JsonElement jelement = new JsonParser().parse(line);
					JsonObject  jobject = jelement.getAsJsonObject();
					prefix = jobject.get("prefix").getAsString();
					
					JsonArray versions = jobject.getAsJsonArray("versions");
					if(versions!=null){
						for (int i = 0; i < versions.size(); i++) {
							JsonObject  version = versions.get(i).getAsJsonObject();
							if(version !=null){
								JsonElement languageIds = version.getAsJsonArray("languageIds");
								if(languageIds!=null && languageIds instanceof JsonArray){
									for (int j = 0; j < ((JsonArray)languageIds).size(); j++) {
										if(((JsonArray)languageIds).get(j).isJsonObject()){
											cptErrorLangId++;
										}
									}
								}
							}
						}
					}
					
								    }
			} catch (JsonSyntaxException e) {
				// TODO Auto-generated catch block
				System.out.println(prefix);
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println(prefix);
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println(prefix);
				e.printStackTrace();
			}
			
			
			
			
			
			
//			JsonObject obj = new JsonObject(" .... ");
//			String pageName = obj.getJSONObject("pageInfo").getString("pageName");
//
//			JSONArray arr = obj.getJSONArray("posts");
//			for (int i = 0; i < arr.length(); i++)
//			{
//			    String post_id = arr.getJSONObject(i).getString("post_id");
//			    ......
//			}
			estimatedTime = System.currentTimeMillis() - startTime;
			log.info("=> "+cpt+" Vocabularies processed in "+String.format("%d min, %d sec", 
				    TimeUnit.MILLISECONDS.toMinutes(estimatedTime),
				    TimeUnit.MILLISECONDS.toSeconds(estimatedTime) - 
				    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(estimatedTime))
				));
			
			
			
			
			log.info("Number of date error:"+cptErrorDate);
			log.info("Number of Language Ids error:"+cptErrorLangId);
			
	}
	
	
	private String DateYMD(Date d){
		if(d==null)return null;
		return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(d);
	}
}
