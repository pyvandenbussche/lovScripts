package org.lov.rdf2mongo.describers;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.riot.Lang;
import org.bson.types.ObjectId;
import org.lov.LovBotVocabAnalyser;
import org.lov.SPARQLRunner;
import org.lov.objects.Agent;
import org.lov.objects.Comment;
import org.lov.objects.Dataset;
import org.lov.objects.LangValue;
import org.lov.objects.Language;
import org.lov.objects.Vocabulary;
import org.lov.objects.VocabularySuggest;
import org.lov.objects.VocabularyVersionWrapper;
import org.lov.rdf2mongo.ICom;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

public class VocabularyDescriber {
	private final SPARQLRunner source;
	private final SPARQLRunner metrics;
	private final ICom iCom;
	private final String VERSIONS_DIR_PATH;
	private final String LOV_DATASET_URI;
	
	public VocabularyDescriber(SPARQLRunner source, SPARQLRunner metrics, ICom iCom) {
		this.source=source;
		this.metrics=metrics;
		this.iCom=iCom;
		VERSIONS_DIR_PATH=iCom.getLovConfig().getProperty("VERSIONS_DIR_PATH");
		LOV_DATASET_URI=iCom.getLovConfig().getProperty("LOV_DATASET_URI");
	}
	
	/*
	 * Fetch nsp, prefix, issued, modified, homepage, isDefinedBy
	 */
	private QuerySolution getBasicInformation(Resource vocab) {
		return source.getOneSolution("vocabulary-BasicInformation.sparql","vocab", vocab);
	}
	private ResultSet getTitles(Resource vocab) {
		return source.getResultSet("vocabulary-titles.sparql","vocab", vocab);
	}
	private ResultSet getDescriptions(Resource vocab) {
		return source.getResultSet("vocabulary-descriptions.sparql","vocab", vocab);
	}
	private ResultSet getTags(Resource vocab) {
		return source.getResultSet("vocabulary-tags.sparql","vocab", vocab);
	}
	private List<Resource> getCreators(Resource vocab) {
		return source.getURIs("vocabulary-creators.sparql","vocab", vocab, "agent");
	}
	private List<Resource> getContributors(Resource vocab) {
		return source.getURIs("vocabulary-contributors.sparql","vocab", vocab, "agent");
	}
	private List<Resource> getPublishers(Resource vocab) {
		return source.getURIs("vocabulary-publishers.sparql","vocab", vocab, "agent");
	}
	private ResultSet getReviews(Resource vocab) {
		return source.getResultSet("vocabulary-reviews.sparql","vocab", vocab);
	}
	private ResultSet getVersions(Resource vocab) {
		return source.getResultSet("vocabulary-versions.sparql","vocab", vocab);
	}
	private ResultSet getMetrics(Resource vocab) {
		return metrics.getResultSet("vocabulary-metrics.sparql","vocab", vocab);
	}
	
	public Vocabulary describe(Resource uri) {
		Vocabulary vocab = new Vocabulary();
		//set the vocab document id for further reference before creation
		vocab.setId(new ObjectId().toString());
		vocab.setUri(uri.getURI());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		//getBasic Information
		QuerySolution qs = getBasicInformation(uri);
		vocab.setPrefix(qs.get("prefix").asLiteral().getLexicalForm());
		vocab.setNsp(qs.get("nsp").asLiteral().getLexicalForm()); 
		if(qs.get("isDefinedBy")!=null){
			vocab.setIsDefinedBy(qs.get("isDefinedBy").asResource().getURI());
		}
		if(qs.get("homepage")!=null){
			vocab.setHomepage(qs.get("homepage").asResource().getURI());
		}
		try {
			if(qs.get("issued")!=null){
				vocab.setIssuedAt(sdf.parse(qs.get("issued").asLiteral().getLexicalForm()));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		//get Titles
		ResultSet rs = getTitles(uri);
		while (rs.hasNext()) {
			QuerySolution qsReview = rs.next();
			vocab.addTitle(new LangValue(
					qsReview.get("title").asLiteral().getLexicalForm(),
					qsReview.get("title").asLiteral().getLanguage()));
		}
		
		//get Descriptions
		rs = getDescriptions(uri);
		while (rs.hasNext()) {
			QuerySolution qsReview = rs.next();
			vocab.addDescription(new LangValue(
					qsReview.get("description").asLiteral().getLexicalForm(),
					qsReview.get("description").asLiteral().getLanguage()));
		}
		
		//get Tags
		rs = getTags(uri);
		while (rs.hasNext()) {
			QuerySolution qsReview = rs.next();
			vocab.addTag(qsReview.get("tag").asLiteral().getLexicalForm());
		}
		
		//Agents
		for (Iterator<Resource> iterator = getCreators(uri).iterator(); iterator.hasNext();) {
			Resource agent = iterator.next();
			vocab.addCreatorId(agent.getURI(), iCom.getAgentCollection());
		}
		for (Iterator<Resource> iterator = getContributors(uri).iterator(); iterator.hasNext();) {
			Resource agent = iterator.next();
			vocab.addContributorId(agent.getURI(), iCom.getAgentCollection());
		}
		for (Iterator<Resource> iterator = getPublishers(uri).iterator(); iterator.hasNext();) {
			Resource agent = iterator.next();
			vocab.addPublisherId(agent.getURI(), iCom.getAgentCollection());
		}
		
				
		//Reviews
		rs = getReviews(uri);
		while (rs.hasNext()) {
			QuerySolution qsReview = rs.next();
			String date = qsReview.get("date").asLiteral().getLexicalForm();
			String body = qsReview.get("text").asLiteral().getLexicalForm();
			if(!body.equals("Vocabulary inserted into the LOV ecosystem.")){
				try {
					Agent agent = iCom.getAgentCollection().findOne("{prefUri:#}", qsReview.get("user").asResource().getURI()).as(Agent.class);
					if(agent!=null){
						vocab.addReview(new Comment(agent.getId(), sdf.parse(date), body));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		//Versions
		rs = getVersions(uri);
		while (rs.hasNext()) {
			try {
				QuerySolution qsReview = rs.next();
				String versionLabel = qsReview.get("versionLabel").asLiteral().getLexicalForm();
				String versionLink = null;
				if(qsReview.get("versionLink")!=null)versionLink = qsReview.get("versionLink").asResource().toString();
				String versionDate = qsReview.get("versionDate").asLiteral().getLexicalForm();
				VocabularyVersionWrapper version = new VocabularyVersionWrapper();
				version.setName(versionLabel);
				version.setIssued(sdf.parse(versionDate));
				
				
				if(versionLink !=null){
					
					//copy the file in the new history version files locally
					//if folder for the vocabulary does not exist, create it
					File vocabFolder = new File(VERSIONS_DIR_PATH+"/"+vocab.getId());
					if(!vocabFolder.exists())vocabFolder.mkdir();
					
					URL website = new URL(versionLink);
					ReadableByteChannel rbc = Channels.newChannel(website.openStream());
					File versionFile = new File(VERSIONS_DIR_PATH+"/"+vocab.getId()+"/"+vocab.getId()+"_"+versionDate+".n3");
					if(!versionFile.exists())versionFile.createNewFile();
					FileOutputStream fos = new FileOutputStream(versionFile);
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					//we change the URL to fit the new URL pattern
					version.setFileURL(LOV_DATASET_URI+"/vocabs/"+vocab.getPrefix()+"/versions/"+vocab.getPrefix()+"-"+versionDate+".n3");
					
					//read from the URI and load it in a model
					VocabularySuggest vocabVersion = LovBotVocabAnalyser.analyseVersion(versionLink, vocab.getUri(), vocab.getNsp(),Lang.N3, iCom.getLovConfig());
					
					version.setClassNumber(vocabVersion.getNbClasses());
					version.setPropertyNumber(vocabVersion.getNbProperties());
					version.setInstanceNumber(vocabVersion.getNbInstances());
					version.setDatatypeNumber(vocabVersion.getNbDatatypes());
					if(vocabVersion.getRelMetadata()!=null){for (VocabularySuggest relNsp : vocabVersion.getRelMetadata()) {
							version.addRelMetadata(relNsp.getUri());}
					}
					if(vocabVersion.getRelDisjunc()!=null){for (VocabularySuggest relNsp : vocabVersion.getRelDisjunc()) {
						version.addRelDisjunc(relNsp.getUri());}
					}
					if(vocabVersion.getRelEquivalent()!=null){for (VocabularySuggest relNsp : vocabVersion.getRelEquivalent()) {
						version.addRelEquivalent(relNsp.getUri());}
					}
					if(vocabVersion.getRelExtends()!=null){for (VocabularySuggest relNsp : vocabVersion.getRelExtends()) {
						version.addRelExtends(relNsp.getUri());}
					}
					if(vocabVersion.getRelGeneralizes()!=null){for (VocabularySuggest relNsp : vocabVersion.getRelGeneralizes()) {
						version.addRelGeneralizes(relNsp.getUri());}
					}
					if(vocabVersion.getRelImports()!=null){for (VocabularySuggest relNsp : vocabVersion.getRelImports()) {
						version.addRelImports(relNsp.getUri());}
					}
					if(vocabVersion.getRelSpecializes()!=null){for (VocabularySuggest relNsp : vocabVersion.getRelSpecializes()) {
						version.addRelSpecializes(relNsp.getUri());}
					}
					
					//Languages
					if(vocabVersion.getLanguages()!=null){for (Language lang : vocabVersion.getLanguages()){
						version.addLanguageId(lang.getId());}
					}
					
				}
				vocab.addVersion(version);
			} catch (Exception e) {
				e.printStackTrace();
			}continue;
		}
		
		//set creation and last modification dates in LOV (approximated from the reviews and versions)
		//is considered creation in LOV the first review date
		//is considered the last modification in LOV either the last review or the last version date
		if(vocab.getReviews()!=null){
			for (Comment review : vocab.getReviews()) {
				if( vocab.getCreatedInLOVAt()==null || vocab.getCreatedInLOVAt().after(review.getCreatedAt()))vocab.setCreatedInLOVAt(review.getCreatedAt());
				if( vocab.getLastModifiedInLOVAt()==null || vocab.getLastModifiedInLOVAt().before(review.getCreatedAt()))vocab.setLastModifiedInLOVAt(review.getCreatedAt());
			}
		}
		if(vocab.getVersions()!=null){
			for (VocabularyVersionWrapper version : vocab.getVersions()) {
				if( vocab.getLastModifiedInLOVAt()==null || vocab.getLastModifiedInLOVAt().before(version.getIssued()))vocab.setLastModifiedInLOVAt(version.getIssued());
			}
		}
		if(vocab.getCreatedInLOVAt()==null)vocab.setCreatedInLOVAt(new Date());
		if(vocab.getLastModifiedInLOVAt()==null)vocab.setLastModifiedInLOVAt(new Date());
		if(vocab.getLastModifiedInLOVAt()==null)vocab.setLastModifiedInLOVAt(new Date());
		vocab.setLastDeref(vocab.getLastModifiedInLOVAt());
		
		//Metrics
		rs = getMetrics(uri);
		while (rs.hasNext()) {
			QuerySolution qsReview = rs.next();
			String datasetLabel = qsReview.get("datasetLabel").asLiteral().getLexicalForm();
			String datasetUri = qsReview.get("dataset").asResource().toString();
			String occurrences = qsReview.get("occurrences").asLiteral().getLexicalForm();
			
			Dataset dataset = new Dataset(datasetUri, datasetLabel, Integer.parseInt(occurrences));
							
			vocab.addDataset(dataset);
		}
		
		return vocab;
	}
}
