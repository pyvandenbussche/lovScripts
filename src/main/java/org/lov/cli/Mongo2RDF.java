package org.lov.cli;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.bson.types.ObjectId;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.lov.LOVException;
import org.lov.LovConstants;
import org.lov.StatementHelper;
import org.lov.objects.Agent;
import org.lov.objects.Comment;
import org.lov.objects.Element;
import org.lov.objects.LangValue;
import org.lov.objects.Language;
import org.lov.objects.Vocabulary;
import org.lov.objects.VocabularyVersionWrapper;
import org.lov.vocidex.extract.LOVExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.cmdline.CmdGeneral;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.LabelExistsException;
import com.hp.hpl.jena.rdf.arp.JenaReader;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFReader;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.NotFoundException;
import com.mongodb.MongoClient;

/**
 * A command line tool that transform LOV mongoDB in LOV Dump. Uses {@link LOVExtractor}.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class Mongo2RDF extends CmdGeneral {
	private final static Logger log = LoggerFactory.getLogger(Mongo2RDF.class);
	
	public static void main(String... args) {
		new Mongo2RDF(args).mainRun();
	}

	private String hostName;
	private String dbName;
	private String lovNQDumpFile;
	private String lovN3DumpFile;
	private String lovDatasetURI;
	private Properties lovConfig;
	private MongoCollection langCollection;
	private MongoCollection agentCollection;
	private MongoCollection vocabCollection;
	private MongoCollection elementCollection;
	
	public Mongo2RDF(String[] args) {
		super(args);
		getUsage().startCategory("Arguments");
		getUsage().addUsage("configFilePath", "absolute path for the configuration file  (e.g. /home/...)");
	}
	
	@Override
    protected String getCommandName() {return "mongo2rdf";}	
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
			lovNQDumpFile = lovConfig.getProperty("LOV_NQ_FILE_PATH");
			lovN3DumpFile = lovConfig.getProperty("LOV_N3_FILE_PATH");
			lovDatasetURI = lovConfig.getProperty("LOV_DATASET_URI");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void exec() {
		try {
			//bootstrap connection to MongoDB and create model
			Jongo jongo = new Jongo(new MongoClient(hostName).getDB(dbName));
			langCollection = jongo.getCollection("languages");
			agentCollection = jongo.getCollection("agents");
			vocabCollection = jongo.getCollection("vocabularies");
			elementCollection = jongo.getCollection("elements");
			Model lovModel = ModelFactory.createDefaultModel();
			lovModel.setNsPrefixes(LovConstants.getPrefixes());//allowed in n3 file
			StatementHelper sthlp = new StatementHelper(lovModel);
			int cpt;
			long startTime,estimatedTime;
			File file = new File(lovNQDumpFile);
			if(file.exists())file.delete();
			file.createNewFile();
			File filen3 = new File(lovN3DumpFile);
			if(filen3.exists())filen3.delete();
			filen3.createNewFile();
			
			
			// Process languages
			startTime = System.currentTimeMillis();
			log.info("Processing Languages");
			MongoCursor<Language> langs = langCollection.find().as(Language.class);
			cpt=0;
			for (Language lang : langs) {
				cpt++;
				sthlp.addResourceStatement(lang.getUri(), LovConstants.RDF_FULL_TYPE, LovConstants.LEXVO_FULL_LANGUAGE);
				sthlp.addLiteralStatement(lang.getUri(), LovConstants.RDFS_FULL_LABEL, lang.getLabel(), null, "en");
				sthlp.addLiteralStatement(lang.getUri(), LovConstants.LEXVO_FULL_ISO639P3PCODE, lang.getIso639P3PCode(), null, null);
				sthlp.addLiteralStatement(lang.getUri(), LovConstants.LEXVO_FULL_ISO639P1CODE, lang.getIso639P1Code(), null, null);				
			}
			estimatedTime = System.currentTimeMillis() - startTime;
			log.info("=> "+cpt+" Languages processed in "+String.format("%d sec, %d ms", 
				    TimeUnit.MILLISECONDS.toSeconds(estimatedTime),
				    estimatedTime - TimeUnit.MILLISECONDS.toSeconds(estimatedTime)
				));
			
			
			// Process Agents
			startTime = System.currentTimeMillis();
			log.info("Processing Agents");
			MongoCursor<Agent> agents = agentCollection.find().as(Agent.class);
			cpt=0;
			for (Agent agent : agents) {
				cpt++;
				sthlp.addResourceStatement(agent.getPrefUri(), LovConstants.RDF_FULL_TYPE, 
						(agent.getType().equals("person"))?LovConstants.FOAF_FULL_PERSON: 
							(agent.getType().equals("organization"))?LovConstants.FOAF_FULL_ORGANIZATION: LovConstants.FOAF_FULL_AGENT);
				sthlp.addLiteralStatement(agent.getPrefUri(), LovConstants.FOAF_FULL_NAME, agent.getName(), null, null);
				for (String altURI : agent.getAltUris()) {
					sthlp.addResourceStatement(agent.getPrefUri(), LovConstants.OWL_FULL_SAMEAS, altURI);
				}
			}
			estimatedTime = System.currentTimeMillis() - startTime;
			log.info("=> "+cpt+" Agents processed in "+String.format("%d sec, %d ms", 
				    TimeUnit.MILLISECONDS.toSeconds(estimatedTime),
				    estimatedTime - TimeUnit.MILLISECONDS.toSeconds(estimatedTime)
				));
			
			
			// Process Elements metrics
			startTime = System.currentTimeMillis();
			log.info("Processing Elements Metrics");
			MongoCursor<Element> elements = elementCollection.find().as(Element.class);
			cpt=0;
			for (Element element : elements) {
				cpt++;
				sthlp.addLiteralStatement(element.getUri(), LovConstants.VOAF_FULL_OCCURRENCES_IN_DATASETS, ""+element.getOccurrencesInDatasets(), XSDDatatype.XSDint, null);
				sthlp.addLiteralStatement(element.getUri(), LovConstants.VOAF_FULL_REUSED_BY_DATASETS, ""+element.getReusedByDatasets(), XSDDatatype.XSDint, null);
			}
			estimatedTime = System.currentTimeMillis() - startTime;
			log.info("=> "+cpt+" Elements Metrics processed in "+String.format("%d sec, %d ms", 
				    TimeUnit.MILLISECONDS.toSeconds(estimatedTime),
				    estimatedTime - TimeUnit.MILLISECONDS.toSeconds(estimatedTime)
				));
			
			
			// Process Vocabularies
			startTime = System.currentTimeMillis();
			log.info("Processing Vocabularies");
			MongoCursor<Vocabulary> vocabs = vocabCollection.find().as(Vocabulary.class);
			cpt=0;
			// add metadata on the LOV Catalog
			sthlp.addResourceStatement(lovDatasetURI, LovConstants.RDF_FULL_TYPE, LovConstants.DCAT_FULL_CATALOG);
			sthlp.addResourceStatement(lovDatasetURI, LovConstants.DC_TERMS_FULL_LICENSE, "https://creativecommons.org/licenses/by/4.0/");
			sthlp.addLiteralStatement(lovDatasetURI, LovConstants.DC_TERMS_FULL_MODIFIED, DateYMD(new Date()), XSDDatatype.XSDdate, null);
			sthlp.addLiteralStatement(lovDatasetURI, LovConstants.DC_TERMS_FULL_TITLE, "The Linked Open Vocabularies (LOV) Catalog", null, "en");
			sthlp.addLiteralStatement(lovDatasetURI, LovConstants.DC_TERMS_FULL_DESCRIPTION, "The LOV Catalog is a collection of RDFS and OWL ontologies designed to be reused to describe Data on the Web.", null, "en");
			for (Vocabulary vocab : vocabs) {
				System.out.println("Now processing: "+vocab.getPrefix());
				cpt++;
				String vocabUriLov = lovDatasetURI+"/vocabs/"+vocab.getPrefix();
				sthlp.addResourceStatement(vocabUriLov, LovConstants.RDF_FULL_TYPE, LovConstants.DCAT_FULL_CATALOG_RECORD);
				sthlp.addResourceStatement(lovDatasetURI, LovConstants.DCAT_FULL_RECORD, vocabUriLov);
				sthlp.addLiteralStatement(vocabUriLov, LovConstants.DC_TERMS_FULL_ISSUED, DateYMD(vocab.getCreatedInLOVAt()), XSDDatatype.XSDdate, null);
				sthlp.addLiteralStatement(vocabUriLov, LovConstants.DC_TERMS_FULL_MODIFIED, DateYMD(vocab.getLastModifiedInLOVAt()), XSDDatatype.XSDdate, null);
				if(vocab.getTitles()!=null){
					for(LangValue lv: vocab.getTitles()){
						sthlp.addLiteralStatement(vocabUriLov, LovConstants.DC_TERMS_FULL_TITLE, lv.getValue(), null, lv.getLang());
					}
				}
				sthlp.addResourceStatement(vocabUriLov, LovConstants.FOAF_FULL_PRIMARY_TOPIC, vocab.getUri());
				
				sthlp.addResourceStatement(vocab.getUri(), LovConstants.RDF_FULL_TYPE, LovConstants.VOAF_FULL_VOCABULARY);
				sthlp.addLiteralStatement(vocab.getUri(), LovConstants.VANN_FULL_PREFERRED_NAMESPACE_URI, vocab.getNsp(), null, null);
				sthlp.addLiteralStatement(vocab.getUri(), LovConstants.VANN_FULL_PREFERRED_NAMESPACE_PREFIX, vocab.getPrefix(), null, null);
				sthlp.addLiteralStatement(vocab.getUri(), LovConstants.DC_TERMS_FULL_ISSUED, DateYMD(vocab.getIssuedAt()), XSDDatatype.XSDdate, null);
				
				if(vocab.getTitles()!=null){
					for(LangValue lv: vocab.getTitles()){
						sthlp.addLiteralStatement(vocab.getUri(), LovConstants.DC_TERMS_FULL_TITLE, lv.getValue(), null, lv.getLang());
					}
				}
				if(vocab.getDescriptions()!=null){
					for(LangValue lv: vocab.getDescriptions()){
						sthlp.addLiteralStatement(vocab.getUri(), LovConstants.DC_TERMS_FULL_DESCRIPTION, lv.getValue(), null, lv.getLang());
					}
				}
				if(vocab.getTags()!=null){
					for (String tag : vocab.getTags()) {
						sthlp.addLiteralStatement(vocab.getUri(), LovConstants.DCAT_FULL_KEYWORD, tag, null, null);
					}
				}
				
				sthlp.addLiteralStatement(vocab.getUri(), LovConstants.FOAF_FULL_HOMEPAGE, vocab.getHomepage(), null, null);
				sthlp.addResourceStatement(vocab.getUri(), LovConstants.RDFS_FULL_IS_DEFINED_BY, vocab.getIsDefinedBy());
				
				if(vocab.getCreatorIds()!=null){
					for(String agentId: vocab.getCreatorIds()){
						Agent agent = agentCollection.findOne("{_id:#}", new ObjectId(agentId)).as(Agent.class);
						sthlp.addResourceStatement(vocab.getUri(), LovConstants.DC_TERMS_FULL_CREATOR, agent.getPrefUri());
					}
				}
				if(vocab.getContributorIds()!=null){
					for(String agentId: vocab.getContributorIds()){
						Agent agent = agentCollection.findOne("{_id:#}", new ObjectId(agentId)).as(Agent.class);
						sthlp.addResourceStatement(vocab.getUri(), LovConstants.DC_TERMS_FULL_CONTRIBUTOR, agent.getPrefUri());
					}
				}
				if(vocab.getPublisherIds()!=null){
					for(String agentId: vocab.getPublisherIds()){
						Agent agent = agentCollection.findOne("{_id:#}", new ObjectId(agentId)).as(Agent.class);
						sthlp.addResourceStatement(vocab.getUri(), LovConstants.DC_TERMS_FULL_PUBLISHER, agent.getPrefUri());
					}
				}
				
				if(vocab.getReviews()!=null){
					for(Comment rev: vocab.getReviews()){
						Resource bn = ResourceFactory.createResource();
						sthlp.addResourceStatement(vocab.getUri(), LovConstants.REV_FULL_HAS_REVIEW, bn);
						sthlp.addResourceStatement(bn, LovConstants.RDF_FULL_TYPE, LovConstants.REV_FULL_REVIEW);
						sthlp.addLiteralStatement(bn, LovConstants.REV_FULL_TEXT, rev.getBody(), null, null);
						sthlp.addLiteralStatement(bn, LovConstants.DC_TERMS_DATE, DateYMD(rev.getCreatedAt()), XSDDatatype.XSDdate, null);
												
						Agent agent = agentCollection.findOne("{_id:#}", new ObjectId(rev.getAgentId())).as(Agent.class);
						sthlp.addResourceStatement(bn, LovConstants.DC_TERMS_CREATOR, agent.getPrefUri());
					}
				}
				
				if(vocab.getDatasets()!=null){
					int sumOccurences =0;
					for(org.lov.objects.Dataset dataset: vocab.getDatasets()){
						Resource bn_datasetOcc = ResourceFactory.createResource();
						sthlp.addResourceStatement(vocab.getUri(), LovConstants.VOAF_FULL_USAGE_IN_DATASET, bn_datasetOcc);
						sthlp.addResourceStatement(bn_datasetOcc, LovConstants.RDF_FULL_TYPE, LovConstants.VOAF_FULL_DATASET_OCCURRENCES);
						sthlp.addResourceStatement(bn_datasetOcc, LovConstants.VOAF_FULL_IN_DATASET, dataset.getUri());
						sthlp.addLiteralStatement(bn_datasetOcc, LovConstants.VOAF_FULL_OCCURRENCES, ""+dataset.getOccurrences(), XSDDatatype.XSDint, null);
						sthlp.addLiteralStatement(dataset.getUri(), LovConstants.RDFS_LABEL, dataset.getLabel(), null, "en");
						sumOccurences+=dataset.getOccurrences();
					}
					//attach aggregation metrics
					sthlp.addLiteralStatement(vocab.getUri(), LovConstants.VOAF_FULL_OCCURRENCES_IN_DATASETS, ""+sumOccurences, XSDDatatype.XSDint, null);
					sthlp.addLiteralStatement(vocab.getUri(), LovConstants.VOAF_FULL_REUSED_BY_DATASETS, ""+vocab.getDatasets().size(), XSDDatatype.XSDint, null);
					
					//TODO occurrences in vocabularies ?? OR not ...
					// reused by vocabularies
					 long reusedByVocabs= vocabCollection.count("{$or: [" +
					 		"{'versions.relMetadata':#}, " +
					 		"{'versions.relSpecializes':#}, " +
					 		"{'versions.relGeneralizes':#}, " +
					 		"{'versions.relExtends':#}, " +
					 		"{'versions.relEquivalent':#}, " +
					 		"{'versions.relDisjunc':#}, " +
					 		"{'versions.relImports':#} ]}", vocab.getNsp(),vocab.getNsp(),vocab.getNsp(),vocab.getNsp(),vocab.getNsp(),vocab.getNsp(),vocab.getUri());
					 sthlp.addLiteralStatement(vocab.getUri(), LovConstants.VOAF_FULL_REUSED_BY_VOCABULARIES, ""+reusedByVocabs, XSDDatatype.XSDint, null);
				}
				
				
				if(vocab.getVersions()!=null){
					Collections.sort(vocab.getVersions());
					for (int i=0; i<vocab.getVersions().size();i++){
						VocabularyVersionWrapper version = vocab.getVersions().get(i);
						// handle the case we don't have any file for the version --> blank node as subject
						Resource versionURI = (version.getFileURL()!=null) ? ResourceFactory.createResource(version.getFileURL()): ResourceFactory.createResource();
						if(i==0 && version.getFileURL()!=null){ // <-- this is the latest version
							//add the version in the dataset in its own namedgraph
							Dataset dataset = DatasetFactory.createMem();
//							Model m = ModelFactory.createDefaultModel();
//							JenaReader jr = m.getReader(Lang.N3.getLabel());
//							RDFReader rdr=m.getReader(Lang.N3.getLabel());
//							rdr.
//						    rdr.setErrorHandler(new RDFErrorHandler() {
//								
//								@Override
//								public void warning(Exception arg0) {
//									log.error("warning$$$$$: "+arg0.getMessage());
//									System.out.println("warning$$$$$: "+arg0.getMessage());
//								}
//								
//								@Override
//								public void fatalError(Exception arg0) {
//									log.error("fatalError$$$$$: "+arg0.getMessage());
//									
//								}
//								
//								@Override
//								public void error(Exception arg0) {
//									log.error("error$$$$$: "+arg0.getMessage());
//									
//								}
//							});
//						    rdr.read(m,version.getFileURL());
											        
					        
//									RDFDataMgr.read(m, version.getFileURL(), Lang.N3);
							
							try {
								dataset.addNamedModel(vocab.getUri(),RDFDataMgr.loadModel(version.getFileURL(), Lang.N3));
							} catch (Exception e) {
								log.error("Error accessing and or parsing vocabulary version :"+vocab.getUri()+" - "+version.getFileURL());
								e.printStackTrace();
							}
							
							OutputStream fop = new BufferedOutputStream(new FileOutputStream(file,true));
							RDFDataMgr.write(fop, dataset, Lang.NQ) ;
							fop.close();
							dataset.close();
							
							sthlp.addLiteralStatement(vocab.getUri(), LovConstants.DC_TERMS_FULL_MODIFIED, DateYMD(version.getIssued()), XSDDatatype.XSDdate, null);
							if(version.getLanguageIds()!=null){
								for(String langId: version.getLanguageIds()){
									Language lang = langCollection.findOne("{_id:#}", new ObjectId(langId)).as(Language.class);
									sthlp.addResourceStatement(vocab.getUri(), LovConstants.DC_TERMS_FULL_LANGUAGE, lang.getUri());
								}
							}
						}
						sthlp.addResourceStatement(versionURI, LovConstants.RDF_FULL_TYPE, LovConstants.DCAT_FULL_DISTRIBUTION_CLASS);
						sthlp.addResourceStatement(vocab.getUri(), LovConstants.DCAT_FULL_DISTRIBUTION_PROP, versionURI);
						sthlp.addLiteralStatement(versionURI, LovConstants.DC_TERMS_FULL_TITLE, version.getName(), null, null);
						sthlp.addLiteralStatement(versionURI, LovConstants.DC_TERMS_FULL_ISSUED, DateYMD(version.getIssued()), XSDDatatype.XSDdate, null);
						
						if(version.getFileURL()!=null){
							sthlp.addLiteralStatement(versionURI, LovConstants.VOAF_FULL_CLASS_NUMBER, ""+version.getClassNumber(), XSDDatatype.XSDint, null);
							sthlp.addLiteralStatement(versionURI, LovConstants.VOAF_FULL_PROPERTY_NUMBER, ""+version.getPropertyNumber(), XSDDatatype.XSDint, null);
							sthlp.addLiteralStatement(versionURI, LovConstants.VOAF_FULL_DATATYPE_NUMBER, ""+version.getDatatypeNumber(), XSDDatatype.XSDint, null);
							sthlp.addLiteralStatement(versionURI, LovConstants.VOAF_FULL_INSTANCE_NUMBER, ""+version.getInstanceNumber(), XSDDatatype.XSDint, null);
						}
						if(version.getLanguageIds()!=null){
							for(String langId: version.getLanguageIds()){
								Language lang = langCollection.findOne("{_id:#}", new ObjectId(langId)).as(Language.class);
								sthlp.addResourceStatement(versionURI, LovConstants.DC_TERMS_FULL_LANGUAGE, lang.getUri());
							}
						}
						if(version.getRelDisjunc()!=null){
							for(String relNsp: version.getRelDisjunc()){
								Vocabulary relVocab = vocabCollection.findOne("{nsp:#}", relNsp).as(Vocabulary.class);
								sthlp.addResourceStatement(versionURI, LovConstants.VOAF_FULL_HAS_DISJUNCTIONS_WITH, (relVocab!=null ? relVocab.getUri(): relNsp) );
							}
						}
						if(version.getRelEquivalent()!=null){
							for(String relNsp: version.getRelEquivalent()){
								Vocabulary relVocab = vocabCollection.findOne("{nsp:#}", relNsp).as(Vocabulary.class);
								sthlp.addResourceStatement(versionURI, LovConstants.VOAF_FULL_HAS_EQUIVALENCES_WITH, (relVocab!=null ? relVocab.getUri(): relNsp) );
							}
						}
						if(version.getRelExtends()!=null){
							for(String relNsp: version.getRelExtends()){
								Vocabulary relVocab = vocabCollection.findOne("{nsp:#}", relNsp).as(Vocabulary.class);
								sthlp.addResourceStatement(versionURI, LovConstants.VOAF_FULL_EXTENDS, (relVocab!=null ? relVocab.getUri(): relNsp) );
							}
						}
						if(version.getRelGeneralizes()!=null){
							for(String relNsp: version.getRelGeneralizes()){
								Vocabulary relVocab = vocabCollection.findOne("{nsp:#}", relNsp).as(Vocabulary.class);
								sthlp.addResourceStatement(versionURI, LovConstants.VOAF_FULL_GENERALIZES, (relVocab!=null ? relVocab.getUri(): relNsp) );
							}
						}
						if(version.getRelImports()!=null){
							for(String relURI: version.getRelImports()){
								sthlp.addResourceStatement(versionURI, LovConstants.OWL_FULL_IMPORTS, relURI);
							}
						}
						if(version.getRelMetadata()!=null){
							for(String relNsp: version.getRelMetadata()){
								Vocabulary relVocab = vocabCollection.findOne("{nsp:#}", relNsp).as(Vocabulary.class);
								sthlp.addResourceStatement(versionURI, LovConstants.VOAF_FULL_METADATA_VOC, (relVocab!=null ? relVocab.getUri(): relNsp) );
							}
						}
						if(version.getRelSpecializes()!=null){
							for(String relNsp: version.getRelSpecializes()){
								Vocabulary relVocab = vocabCollection.findOne("{nsp:#}", relNsp).as(Vocabulary.class);
								sthlp.addResourceStatement(versionURI, LovConstants.VOAF_FULL_SPECIALIZES, (relVocab!=null ? relVocab.getUri(): relNsp) );
							}
						}
					}
				}
				if(cpt%100==0){
					estimatedTime = System.currentTimeMillis() - startTime;
					log.info("=> "+cpt+" Vocabularies processed in "+String.format("%d min, %d sec",
					    TimeUnit.MILLISECONDS.toMinutes(estimatedTime),
					    TimeUnit.MILLISECONDS.toSeconds(estimatedTime) - 
					    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(estimatedTime))
					));
				}
			}
			estimatedTime = System.currentTimeMillis() - startTime;
			log.info("=> "+cpt+" Vocabularies processed in "+String.format("%d min, %d sec", 
				    TimeUnit.MILLISECONDS.toMinutes(estimatedTime),
				    TimeUnit.MILLISECONDS.toSeconds(estimatedTime) - 
				    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(estimatedTime))
				));
			
			
			
			//Add LOV namedgraph to Dataset and write to file
			Dataset lovDataset = DatasetFactory.createMem();
			lovDataset.addNamedModel(lovDatasetURI, lovModel);
			OutputStream fop = new BufferedOutputStream(new FileOutputStream(file,true));
			RDFDataMgr.write(fop, lovDataset, Lang.NQ) ;
			OutputStream fopn3 = new BufferedOutputStream(new FileOutputStream(filen3));
			RDFDataMgr.write(fopn3, lovModel, Lang.N3) ;
			fop.close();
			fopn3.close();
			lovDataset.close();
			gzipIt(file);
			gzipIt(filen3);
			
			log.info("---Done---");
			
		} catch (UnknownHostException e){
			cmdError(e.getMessage());
		} catch (NotFoundException ex) {
			cmdError("Not found: " + ex.getMessage());
		} catch (LOVException ex) {
			cmdError(ex.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private String DateYMD(Date d){
		if(d==null)return null;
		return new SimpleDateFormat("yyyy-MM-dd").format(d);
	}
	private void gzipIt(File fileToBeZipped){
	     byte[] buffer = new byte[1024];	 
	     try{	 
	    	 File gzipFile = new File(fileToBeZipped.getAbsolutePath()+".gz");
	    	 if(gzipFile.exists())gzipFile.delete();
	    	 GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(fileToBeZipped.getAbsolutePath()+".gz"));
	         FileInputStream in = new FileInputStream(fileToBeZipped);	 
	         int len;
	         while ((len = in.read(buffer)) > 0) {
	         	gzos.write(buffer, 0, len);
	         }	 
	         in.close();	 
	    	 gzos.finish();
	    	 gzos.close(); 
	    }catch(IOException ex){
	       ex.printStackTrace();   
	    }
	   }
}
