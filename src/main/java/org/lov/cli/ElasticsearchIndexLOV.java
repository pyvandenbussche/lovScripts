package org.lov.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.lov.vocidex.VocidexDocument;
import org.lov.vocidex.VocidexException;
import org.lov.vocidex.VocidexIndex;
import org.lov.vocidex.extract.AgentsExtractor;
import org.lov.vocidex.extract.LOVExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.shared.NotFoundException;

/**
 * A command line tool that indexes an LOV dump, adding all vocabularies
 * and their terms to the index. Uses {@link LOVExtractor}.
 * 
 * @author Richard Cyganiak
 */
public class ElasticsearchIndexLOV extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(ElasticsearchIndexLOV.class);
	
	public static void main(String... args) {
		new ElasticsearchIndexLOV(args).mainRun();
	}

	private String clusterName;
	private String hostName;
	private String indexName;
	private String lovDumpFile;
	
	public ElasticsearchIndexLOV(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("configFilePath", "absolute path for the configuration file  (e.g. /home/...)");
	}
	
	@Override
    protected String getCommandName() { return "index-lov"; }
	@Override
	protected String getSummary() { return getCommandName() + " clusterName hostname indexName lov.nq"; }

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 1) {
			doHelp();
		}
		String configFilePath = getPositionalArg(0);
		//load properties from the config file
		try {
			Properties lovConfig = new Properties();
			File file = new File(configFilePath);
			InputStream is = new FileInputStream(file);
			lovConfig.load(is);
			hostName= lovConfig.getProperty("ELASTICSEARCH_HOST");
			clusterName = lovConfig.getProperty("ELASTICSEARCH_CLUSTER");
			indexName = lovConfig.getProperty("ELASTICSEARCH_INDEX_NAME");
			lovDumpFile = lovConfig.getProperty("LOV_NQ_FILE_PATH");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void exec() {
		try {
			log.info("Loading LOV dump: " + lovDumpFile);
			Dataset dataset = RDFDataMgr.loadDataset(lovDumpFile, Lang.NQUADS);
			long graphCount = 1;
			long tripleCount = dataset.getDefaultModel().size();
			Iterator<String> it = dataset.listNames();
			while (it.hasNext()) {
				graphCount++;
				tripleCount += dataset.getNamedModel(it.next()).size();
			}
			log.info("Read " + tripleCount + " triples in " + graphCount + " graphs");

			VocidexIndex index = new VocidexIndex(clusterName, hostName, indexName);
			try {
				if (!index.exists()) {
					throw new VocidexException("Index '" + indexName + "' does not exist on the cluster. Create the index first!");
				}
				
				/* Process Agents */
				log.info("--Inserting Agents--");
				AgentsExtractor agentExtractor = new AgentsExtractor(dataset);
				int cpt=0;
				for (VocidexDocument document: agentExtractor) {
					index.addDocument(document);
					cpt++;
				}
				log.info(cpt+ " Agents inserted");
				
				LOVExtractor lovTransformer = new LOVExtractor(dataset);
				for (VocidexDocument document: lovTransformer) {
					//log.info("Indexing " + document.getId());
					index.addDocument(document);
				}
				log.info("Done!");
			} finally {
				index.close();
			}
		} catch (NotFoundException ex) {
			cmdError("Not found: " + ex.getMessage());
		} catch (VocidexException ex) {
			cmdError(ex.getMessage());
		}
	}
}
