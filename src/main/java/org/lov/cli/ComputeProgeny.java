package org.lov.cli;

import org.apache.jena.riot.RDFDataMgr;
import org.lov.SPARQLRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.NotFoundException;

/**
 * A command line tool that indexes an LOV dump, adding all vocabularies
 * and their terms to the index. Uses.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class ComputeProgeny extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(ComputeProgeny.class);
	
	public static void main(String... args) {
		new ComputeProgeny(args).mainRun();
	}
	private String namespace;
	private String vocabDump;
	private String uri;
	
	public ComputeProgeny(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("namespace", "namespace of the vocabulary");
		getUsage().addUsage("vocabDump", "dump of the vocabulary");
		getUsage().addUsage("uri", "uri from which to compute progeny (subclasses and instances)");
	}
	
	@Override
    protected String getCommandName() {
		return "progeny";
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + " namespace vocabDump uri";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 3) {
			doHelp();
		}
		namespace = getPositionalArg(0);
		vocabDump = getPositionalArg(1);
		uri = getPositionalArg(2);
	}

	@Override
	protected void exec() {
		try {
			//sendPost(namespace);
			log.info("Computing description of vocabulary dump: " + vocabDump);
			Dataset dataset = RDFDataMgr.loadDataset(vocabDump);
			Model modelInference = ModelFactory.createRDFSModel(dataset.getDefaultModel());
			
			SPARQLRunner sparqlRunner = new SPARQLRunner(modelInference);
			int nbProgeny = sparqlRunner.getCount("subclasses-instances-count.sparql",ResourceFactory.createResource(uri), "nbItems", "ns", ResourceFactory.createPlainLiteral(namespace));
			
			
			log.info("####### <Summary> #######");
			log.info("Nb Classes and Instances Progeny: " + nbProgeny);
			log.info("####### </Summary> #######");
			
			log.info("Done!");
		} catch (NotFoundException ex) {
			cmdError("Not found: " + ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
//	
//	
//	private String prefixMe(String uri){
//		if(uri.contains("http://schema.org/"))return uri.replace("http://schema.org/", "schema:");
//		if(uri.contains("http://www.w3.org/2000/01/rdf-schema#"))return uri.replace("http://www.w3.org/2000/01/rdf-schema#", "rdfs:");
//		if(uri.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#"))return uri.replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:");
//		if(uri.contains("http://purl.org/dc/terms/"))return uri.replace("http://purl.org/dc/terms/", "dcterms:");
//		if(uri.contains("http://www.w3.org/2001/XMLSchema#"))return uri.replace("http://purl.org/dc/terms/", "xsd:");
//		return uri;
//	}
//	private String getLocalName(String uri){
//		if(uri.lastIndexOf("#")>-1)return uri.substring(uri.lastIndexOf("#")+1);
//		return uri.substring(uri.lastIndexOf("/")+1);
//	}
}
