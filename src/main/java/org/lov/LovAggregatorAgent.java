package org.lov;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.concurrent.Callable;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotNotFoundException;
import org.lov.objects.Vocabulary;
import org.openrdf.model.Resource;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.n3.N3Writer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class LovAggregatorAgent implements Callable<Model> {
	private final static Logger log = LoggerFactory.getLogger(LovAggregatorAgent.class);
	private Model vocabModel = null;
	private Vocabulary vocab=null;
	private String vocabUrl=null;
	private boolean isTerminated=false;
	private boolean isRetrieved=false;
	
	   public LovAggregatorAgent(Vocabulary vocab) {
		  this.vocab=vocab;
		  //use the isdefinedby if exists otherwise use the uri
	      this.vocabUrl=(vocab.getIsDefinedBy()!=null? vocab.getIsDefinedBy() : vocab.getUri());
	   }
	   
	   public Model call() throws Exception {
//			long start = System.currentTimeMillis();
//		 	String responseTime="";
		   /* load the ontology (content negotiation, etc.) in a model */
			try {
				vocabModel = RDFDataMgr.loadModel(vocabUrl,Lang.RDFXML);
				log.info("vocabulary: "+vocabUrl+" ("+vocabModel.size()+")");
			
			} catch(RiotNotFoundException e){//URL not accessible
				String errorMsg = "";
				if( e.getMessage()!=null) errorMsg = e.getMessage().split(System.getProperty("line.separator"))[0];
				log.error("Failed to parse vocabulary: "+ vocabUrl +" "+ errorMsg);
				isTerminated=true;
				return vocabModel;
			} catch (Exception e) {//other exception (failed parsing) 
				try {// try using openrdf library
					Repository rep = LovUtil.LoadRepositoryFromURL(new URL(vocabUrl));
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					RDFWriter writer=new N3Writer(out);
					RepositoryConnection connec = rep.getConnection();
					if(connec.size((Resource)null)<1)throw new Exception("Unable to parse the content");
					connec.export(writer);
					connec.close();
					rep.shutDown();
					ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
					vocabModel = ModelFactory.createDefaultModel();//transform openrdf repository to jena model
					RDFDataMgr.read(vocabModel, in, Lang.N3);
					out.close();
					in.close();
					log.info("vocabulary: "+vocabUrl+" ("+vocabModel.size()+")");
				} catch (Exception e2) {
					String errorMsg = "";
					if( e.getMessage()!=null) errorMsg = e2.getMessage().split(System.getProperty("line.separator"))[0];
					log.error("Failed to parse vocabulary: "+ vocabUrl +" "+ errorMsg);
					isTerminated=true;
					return vocabModel;
				}
			}
			isTerminated=true;
			isRetrieved=true;
			return vocabModel;
	   }

	public Model getVocabModel() {
		return vocabModel;
	}

	public void setVocabModel(Model vocabModel) {
		this.vocabModel = vocabModel;
	}
	
	public Vocabulary getVocab() {
		return vocab;
	}

	public void setVocab(Vocabulary vocab) {
		this.vocab = vocab;
	}

	public boolean isTerminated() {
		return isTerminated;
	}

	public void setTerminated(boolean isTerminated) {
		this.isTerminated = isTerminated;
	}

	public boolean isRetrieved() {
		return isRetrieved;
	}

	public void setRetrieved(boolean isRetrieved) {
		this.isRetrieved = isRetrieved;
	}
}
