package org.lov.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.riot.RDFDataMgr;
import org.lov.SPARQLRunner;
import org.lov.dif.describers.ResourceChangeDescriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.NotFoundException;

/**
 * A command line tool that indexes an LOV dump, adding all vocabularies
 * and their terms to the index. Uses.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class ComputeDif extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(ComputeDif.class);
	
	public static void main(String... args) {
		new ComputeDif(args).mainRun();
	}
	private String namespace;
	private String vocabDumpv1;
	private String vocabDumpv2;
	
	public ComputeDif(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("namespace", "namespace of the vocabulary");
		getUsage().addUsage("vocabDumpv1", "v1 of a vocabulary");
		getUsage().addUsage("vocabDumpv2", "v2 of the same vocabulary");
	}
	
	@Override
    protected String getCommandName() {
		return "dif";
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + " namespace vocabDumpv1 vocabDumpv2";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().size() < 3) {
			doHelp();
		}
		namespace = getPositionalArg(0);
		vocabDumpv1 = getPositionalArg(1);
		vocabDumpv2 = getPositionalArg(2);
	}

	@Override
	protected void exec() {
		try {
			//sendPost(namespace);
			log.info("Computing dif between dump(v1): " + vocabDumpv1 + " and dump(v2): " + vocabDumpv2);
			Dataset datasetv1 = RDFDataMgr.loadDataset(vocabDumpv1);
			Dataset datasetv2 = RDFDataMgr.loadDataset(vocabDumpv2);
			
			Model diffv1v2 = datasetv1.getDefaultModel().difference(datasetv2.getDefaultModel());
			Model diffv2v1 = datasetv2.getDefaultModel().difference(datasetv1.getDefaultModel());
			SPARQLRunner sparqlRunnerv1 = new SPARQLRunner(datasetv1);
			SPARQLRunner sparqlRunnerv2 = new SPARQLRunner(datasetv2);
			
			log.info("####### <Summary> #######");
			log.info("Deletion of " + diffv1v2.size() + " triples");
			log.info("Creation of " + diffv2v1.size() + " triples");
			log.info("####### </Summary> #######");
			
			List<ResourceChangeDescriber> modifList = new ArrayList<ResourceChangeDescriber>();
			List<ResourceChangeDescriber> delList = new ArrayList<ResourceChangeDescriber>();
			List<ResourceChangeDescriber> creatList = new ArrayList<ResourceChangeDescriber>();
			
			StmtIterator it = diffv1v2.listStatements();
			while (it.hasNext()) {
				Statement stmt      = it.nextStatement();  // get next statement
			    Resource  subject   = stmt.getSubject();     // get the subject
			    //Property  predicate = stmt.getPredicate();   // get the predicate
			    //RDFNode   object    = stmt.getObject();      // get the object
			    //log.info(subject.toString() + "\t"+ predicate.toString() + "\t"+ object.toString());
			    
			    // ignore blank node and consider only named resources
			    if(subject.isURIResource() && subject.toString().startsWith(namespace)){
				    if(sparqlRunnerv2.askUri("uri-ask.sparql", "uri", subject)){
				    	if(!modifList.contains(new ResourceChangeDescriber(subject))){
				    		ResourceChangeDescriber desc = new ResourceChangeDescriber(subject);
				    		desc.getPoDeleteList().add(stmt);
				    		modifList.add(desc);
				    	}
				    	else{
				    		modifList.get(modifList.indexOf(new ResourceChangeDescriber(subject))).getPoDeleteList().add(stmt);
				    	}
				    }
				    else{
				    	if(!delList.contains(new ResourceChangeDescriber(subject))){
				    		ResourceChangeDescriber desc = new ResourceChangeDescriber(subject);
				    		desc.getPoDeleteList().add(stmt);
				    		delList.add(desc);
				    	}
				    	else{
				    		delList.get(delList.indexOf(new ResourceChangeDescriber(subject))).getPoDeleteList().add(stmt);
				    	}
				    }
			    }
			 // ignore blank node and consider only named resources
//			    if(object.isURIResource() && object.toString().startsWith(namespace)){
//				    if(sparqlRunnerv2.askUri("uri-ask.sparql", "uri", object.asResource())){
//				    	if(!modifList.contains(new ResourceChangeDescriber(object.asResource()))){
//				    		ResourceChangeDescriber desc = new ResourceChangeDescriber(object.asResource());
//				    		desc.getSpDeleteList().add(stmt);
//				    		modifList.add(desc);
//				    	}
//				    	else{
//				    		modifList.get(modifList.indexOf(new ResourceChangeDescriber(object.asResource()))).getSpDeleteList().add(stmt);
//				    	}
//				    }
//				    else{
//				    	if(!delList.contains(new ResourceChangeDescriber(object.asResource()))){
//				    		ResourceChangeDescriber desc = new ResourceChangeDescriber(object.asResource());
//				    		desc.getSpDeleteList().add(stmt);
//				    		delList.add(desc);
//				    	}
//				    	else{
//				    		delList.get(delList.indexOf(new ResourceChangeDescriber(object.asResource()))).getSpDeleteList().add(stmt);
//				    	}
//				    }
//			    }
			}
			
			StmtIterator it2 = diffv2v1.listStatements();
			while (it2.hasNext()) {
				Statement stmt      = it2.nextStatement();  // get next statement
			    Resource  subject   = stmt.getSubject();     // get the subject
			    //Property  predicate = stmt.getPredicate();   // get the predicate
			    //RDFNode   object    = stmt.getObject();      // get the object
			    //log.info(subject.toString() + "\t"+ predicate.toString() + "\t"+ object.toString());
			    
			    if(subject.isURIResource() && subject.toString().startsWith(namespace)){
				    if(sparqlRunnerv1.askUri("uri-ask.sparql", "uri", subject)){
				    	if(!modifList.contains(new ResourceChangeDescriber(subject))){
				    		ResourceChangeDescriber desc = new ResourceChangeDescriber(subject);
				    		desc.getPoAddList().add(stmt);
				    		modifList.add(desc);
				    	}
				    	else{
				    		modifList.get(modifList.indexOf(new ResourceChangeDescriber(subject))).getPoAddList().add(stmt);
				    	}
				    }
				    else{
				    	if(!creatList.contains(new ResourceChangeDescriber(subject))){
				    		ResourceChangeDescriber desc = new ResourceChangeDescriber(subject);
				    		desc.getPoAddList().add(stmt);
				    		creatList.add(desc);
				    	}
				    	else{
				    		creatList.get(creatList.indexOf(new ResourceChangeDescriber(subject))).getPoAddList().add(stmt);
				    	}
				    }
			    }
			 // ignore blank node and consider only named resources
//			    if(object.isURIResource() && object.toString().startsWith(namespace)){
//				    if(sparqlRunnerv1.askUri("uri-ask.sparql", "uri", object.asResource())){
//				    	if(!modifList.contains(new ResourceChangeDescriber(object.asResource()))){
//				    		ResourceChangeDescriber desc = new ResourceChangeDescriber(object.asResource());
//				    		desc.getSpAddList().add(stmt);
//				    		modifList.add(desc);
//				    	}
//				    	else{
//				    		modifList.get(modifList.indexOf(new ResourceChangeDescriber(object.asResource()))).getSpAddList().add(stmt);
//				    	}
//				    }
//				    else{
//				    	if(!creatList.contains(new ResourceChangeDescriber(object.asResource()))){
//				    		ResourceChangeDescriber desc = new ResourceChangeDescriber(object.asResource());
//				    		desc.getSpAddList().add(stmt);
//				    		creatList.add(desc);
//				    	}
//				    	else{
//				    		creatList.get(creatList.indexOf(new ResourceChangeDescriber(object.asResource()))).getSpAddList().add(stmt);
//				    	}
//				    }
//			    }
			}
			Collections.sort(creatList);
			Collections.sort(delList);
			Collections.sort(modifList);
			
			
			outputHTML(modifList,delList,creatList);
			
			
			log.info("Done!");
		} catch (NotFoundException ex) {
			cmdError("Not found: " + ex.getMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	// HTTP POST request
//		private void sendPost(String content) throws Exception {
//	 
//			String url = "http://localhost:3000/";
//			String USER_AGENT = "Mozilla/5.0";
//			URL obj = new URL(url);
//			HttpURLConnection httpcon = (HttpURLConnection) obj.openConnection();
//	 
//			//add reuqest header
//			httpcon.setRequestMethod("POST");
//			httpcon.setDoOutput(true);
//			httpcon.setRequestProperty("Content-type","application/json; charset=utf-8"); 
//			httpcon.setRequestProperty("Accept", "application/json");
//			httpcon.setUseCaches(false); 
//			httpcon.connect();
//	 
//			// Send post request
//			String json = "";//"{'value': 7.5}";
//			OutputStreamWriter  os = new OutputStreamWriter (httpcon.getOutputStream());
//			os.write(content);
//			os.close();
//	 
//			int responseCode = httpcon.getResponseCode();
//			System.out.println("\nSending 'POST' request to URL : " + url);
//			System.out.println("Response Code : " + responseCode);	 
//			httpcon.disconnect();
//		}
	
	private void outputHTML(List<ResourceChangeDescriber> modifList, List<ResourceChangeDescriber> delList, List<ResourceChangeDescriber> creatList){
		String html="<div style=\"margin-top:10px;\"><span style=\"font-size:18px;\">Summary:</span>" +
				"<div style=\"margin-left:20px; margin-right:20px;\">" +
				"<table id=\"metadataTable\" class=\"table\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\">" +
				"<tr><td><span><a href=\"#creationTable\">Resources Created ("+creatList.size()+")</a></span></td><td><p>";
		Integer capitalLetter=null;
		for (Iterator<ResourceChangeDescriber> iterator = creatList.iterator(); iterator.hasNext();) {
			ResourceChangeDescriber resourceChangeDescriber = (ResourceChangeDescriber) iterator.next();
			if(capitalLetter==null || capitalLetter>0 &&  Character.isLowerCase(getLocalName(resourceChangeDescriber.getRes().toString()).charAt(0)) || capitalLetter<0 &&  Character.isUpperCase(getLocalName(resourceChangeDescriber.getRes().toString()).charAt(0))){
				html+="</p><p>";
				if(Character.isLowerCase(getLocalName(resourceChangeDescriber.getRes().toString()).charAt(0)))capitalLetter=-1;
				else capitalLetter=1;
			}
			html+="<a href=\"#" + getLocalName(resourceChangeDescriber.getRes().toString()) + "\">" + getLocalName(resourceChangeDescriber.getRes().toString()) + "</a>";
			if(iterator.hasNext())html+=", ";
		}
		html+="</p></td></tr><tr class=\"row_odd\"><td><span><a href=\"#deletionTable\">Resources Deleted ("+delList.size()+")</a></span></td><td><p>";
		for (Iterator<ResourceChangeDescriber> iterator = delList.iterator(); iterator.hasNext();) {
			ResourceChangeDescriber resourceChangeDescriber = (ResourceChangeDescriber) iterator.next();
			if(capitalLetter==null || capitalLetter>0 &&  Character.isLowerCase(getLocalName(resourceChangeDescriber.getRes().toString()).charAt(0)) || capitalLetter<0 &&  Character.isUpperCase(getLocalName(resourceChangeDescriber.getRes().toString()).charAt(0))){
				html+="</p><p>";
				if(Character.isLowerCase(getLocalName(resourceChangeDescriber.getRes().toString()).charAt(0)))capitalLetter=-1;
				else capitalLetter=1;
			}
			html+="<a href=\"#" + getLocalName(resourceChangeDescriber.getRes().toString()) + "\">" + getLocalName(resourceChangeDescriber.getRes().toString()) + "</a>";
			if(iterator.hasNext())html+=", ";
		}
		html+="</p></td></tr><tr><td><span><a href=\"#modificationTable\">Resources Modified ("+modifList.size()+")</a></span></td><td><p>";
		for (Iterator<ResourceChangeDescriber> iterator = modifList.iterator(); iterator.hasNext();) {
			ResourceChangeDescriber resourceChangeDescriber = (ResourceChangeDescriber) iterator.next();
			if(capitalLetter==null || capitalLetter>0 &&  Character.isLowerCase(getLocalName(resourceChangeDescriber.getRes().toString()).charAt(0)) || capitalLetter<0 &&  Character.isUpperCase(getLocalName(resourceChangeDescriber.getRes().toString()).charAt(0))){
				html+="</p><p>";
				if(Character.isLowerCase(getLocalName(resourceChangeDescriber.getRes().toString()).charAt(0)))capitalLetter=-1;
				else capitalLetter=1;
			}
			html+="<a href=\"#" + getLocalName(resourceChangeDescriber.getRes().toString()) + "\">" + getLocalName(resourceChangeDescriber.getRes().toString()) + "</a>";
			if(iterator.hasNext())html+=", ";
		}
		html+="</p></td></tr></table></div>\n";
		
		
		
		/* DETAILS */
		html+="<div id=\"creationTable\" style=\"margin-top:10px;\"><span style=\"font-size:18px;\">Creations (" + creatList.size() + "):</span>" +
				"<div style=\"margin-left:20px; margin-right:20px;\">" +
				"<table class=\"table\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\">";
		for (int i = 0; i < creatList.size(); i++) {
			ResourceChangeDescriber res = creatList.get(i);
			html+="<tr";
			if(i%2==1)html+=" class=\"row_odd\"";
			html+="><td><span id=\""+ getLocalName(res.getRes().toString()) +"\"><a href=\"" + res.getRes().toString() + "\" target=\"_blank\">"+prefixMe(res.getRes().toString())+"</a></span></td><td>";
			for (Iterator<Statement> iterator2 = res.getPoAddList().iterator(); iterator2.hasNext();) {
				Statement stmt = iterator2.next();
				html+="<p style=\"color:#060;\"> + "+ prefixMe(stmt.getPredicate().toString())+ " "+ decorateObject(stmt.getObject()) + "</p>";
			}
			html+="</td></tr>";
		}
		html+="</tr></table></div>\n";
		
		html+="<div id=\"deletionTable\" style=\"margin-top:10px;\"><span style=\"font-size:18px;\">Deletions (" + delList.size() + "):</span>" +
				"<div style=\"margin-left:20px; margin-right:20px;\">" +
				"<table class=\"table\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\">";
		for (int i = 0; i < delList.size(); i++) {
			ResourceChangeDescriber res = delList.get(i);
			html+="<tr";
			if(i%2==1)html+=" class=\"row_odd\"";
			html+="><td><span id=\""+ getLocalName(res.getRes().toString()) +"\"><a href=\"" + res.getRes().toString() + "\" target=\"_blank\">"+prefixMe(res.getRes().toString())+"</a></span></td><td>";
			for (Iterator<Statement> iterator2 = res.getPoDeleteList().iterator(); iterator2.hasNext();) {
				Statement stmt = iterator2.next();
				html+="<p style=\"color:#900;\"> - "+ prefixMe(stmt.getPredicate().toString())+ " "+ decorateObject(stmt.getObject()) + "</p>";
			}
			html+="</td></tr>";
		}
		html+="</tr></table></div>\n";
		
		html+="<div id=\"modificationTable\" style=\"margin-top:10px;\"><span style=\"font-size:18px;\">Modifications (" + modifList.size() + "):</span>" +
				"<div style=\"margin-left:20px; margin-right:20px;\">" +
				"<table class=\"table\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"100%\">";
		for (int i = 0; i < modifList.size(); i++) {
			ResourceChangeDescriber res = modifList.get(i);
			html+="<tr";
			if(i%2==1)html+=" class=\"row_odd\"";
			html+="><td><span id=\""+ getLocalName(res.getRes().toString()) +"\"><a href=\"" + res.getRes().toString() + "\" target=\"_blank\">"+prefixMe(res.getRes().toString())+"</a></span></td><td>";
			for (Iterator<Statement> iterator2 = res.getPoAddList().iterator(); iterator2.hasNext();) {
				Statement stmt = iterator2.next();
				html+="<p style=\"color:#060;\"> + "+ prefixMe(stmt.getPredicate().toString())+ " "+ decorateObject(stmt.getObject()) + "</p>";
			}
			for (Iterator<Statement> iterator2 = res.getPoDeleteList().iterator(); iterator2.hasNext();) {
				Statement stmt = iterator2.next();
				html+="<p style=\"color:#900;\"> - "+ prefixMe(stmt.getPredicate().toString())+ " "+ decorateObject(stmt.getObject()) + "</p>";
			}
			html+="</td></tr>";
		}
		html+="</tr></table></div>\n";
		
		try {
			 
			File file = new File("D:/Sources/pyv_repository/vocsync/target/vocsync-cli/vocsync/bin/output.txt");
			// if file doesnt exists, then create it
						if (file.exists()) {
							file.delete();
						} 
						file.createNewFile();
			FileOutputStream fop = new FileOutputStream(file);
 
			
 
			// get the content in bytes
			byte[] contentInBytes = html.getBytes();
 
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	private void outputConsole(List<ResourceChangeDescriber> modifList, List<ResourceChangeDescriber> delList, List<ResourceChangeDescriber> creatList){
//		log.info("## Creations (" + creatList.size() + ")");
//		for (Iterator<ResourceChangeDescriber> iterator = creatList.iterator(); iterator.hasNext();) {
//			ResourceChangeDescriber resource = iterator.next();
//			log.info(resource.getRes().toString());
//			
//			for (Iterator<Statement> iterator2 = resource.getPoAddList().iterator(); iterator2.hasNext();) {
//				Statement stmt = iterator2.next();
//				log.info("\t + " + prefixMe(stmt.getPredicate().toString())+ " "+ decorateObject(stmt.getObject()));
//			}
//		}
//		log.info("## Deletions (" + delList.size() + ")");
//		for (Iterator<ResourceChangeDescriber> iterator = delList.iterator(); iterator.hasNext();) {
//			ResourceChangeDescriber resource = iterator.next();
//			log.info(resource.getRes().toString());
//			
//			for (Iterator<Statement> iterator2 = resource.getPoDeleteList().iterator(); iterator2.hasNext();) {
//				Statement stmt = iterator2.next();
//				log.info("\t - " + prefixMe(stmt.getPredicate().toString())+ " "+ decorateObject(stmt.getObject()));
//			}
//		}
//		log.info("## Modifications (" + modifList.size() + ")");
//		for (Iterator<ResourceChangeDescriber> iterator = modifList.iterator(); iterator.hasNext();) {
//			ResourceChangeDescriber resource = iterator.next();
//			log.info(resource.getRes().toString());
//			
//			for (Iterator<Statement> iterator2 = resource.getPoDeleteList().iterator(); iterator2.hasNext();) {
//				Statement stmt = iterator2.next();
//				log.info("\t - " + prefixMe(stmt.getPredicate().toString())+ " "+ decorateObject(stmt.getObject()));
//			}
//			for (Iterator<Statement> iterator2 = resource.getPoAddList().iterator(); iterator2.hasNext();) {
//				Statement stmt = iterator2.next();
//				log.info("\t + " + prefixMe(stmt.getPredicate().toString())+ " "+ decorateObject(stmt.getObject()));
//			}
//		}
//	}
	
	private String decorateObject(RDFNode object){
		if(object.isURIResource())return "<a href=\""+ object.toString()+"\">"+ prefixMe(object.toString())+ "</a>";
		if(object.isLiteral()){
			String output="\""+object.toString()+"\"";
			if( ((Literal)object).getLanguage()!=null && ((Literal)object).getLanguage().length()>0 )return output+="@"+((Literal)object).getLanguage();
			if( ((Literal)object).getDatatypeURI()!=null )return output+="^^"+((Literal)object).getDatatypeURI();
			return output;
		}
		return object.toString();
		
	}
	
	private String prefixMe(String uri){
		if(uri.contains("http://schema.org/"))return uri.replace("http://schema.org/", "schema:");
		if(uri.contains("http://www.w3.org/2000/01/rdf-schema#"))return uri.replace("http://www.w3.org/2000/01/rdf-schema#", "rdfs:");
		if(uri.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#"))return uri.replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:");
		if(uri.contains("http://purl.org/dc/terms/"))return uri.replace("http://purl.org/dc/terms/", "dcterms:");
		if(uri.contains("http://www.w3.org/2001/XMLSchema#"))return uri.replace("http://purl.org/dc/terms/", "xsd:");
		return uri;
	}
	private String getLocalName(String uri){
		if(uri.lastIndexOf("#")>-1)return uri.substring(uri.lastIndexOf("#")+1);
		return uri.substring(uri.lastIndexOf("/")+1);
	}
}
