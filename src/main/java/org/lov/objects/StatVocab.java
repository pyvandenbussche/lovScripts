package org.lov.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.ObjectId;

/**
 * Represents statistics associated to a Vocabulary
 * 
 * @author Pierre-Yves Vandenbussche
 *
 */
public class StatVocab implements Serializable{

	private static final long serialVersionUID = -2642994886082964900L;
	@Id @ObjectId
	private String id;
	private String uri;
	private String nsp;
	private String prefix;
	
	private List<String> outRelMetadata;
	private List<String> outRelSpecializes;
	private List<String> outRelGeneralizes;
	private List<String> outRelExtends;
	private List<String> outRelEquivalent;
	private List<String> outRelDisjunc;
	private List<String> outRelImports;
	
	private List<String> incomRelMetadata;
	private List<String> incomRelSpecializes;
	private List<String> incomRelGeneralizes;
	private List<String> incomRelExtends;
	private List<String> incomRelEquivalent;
	private List<String> incomRelDisjunc;
	private List<String> incomRelImports;
	
	int nbIncomingLinks=0;
	int nbOutgoingLinks=0;
	
	public StatVocab(){super();}
	
	public StatVocab(String uri, String nsp, String prefix){
		super();
		this.uri=uri;
		this.nsp=nsp;
		this.prefix=prefix;
	}
	
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getNsp() {
		return nsp;
	}
	public void setNsp(String nsp) {
		this.nsp = nsp;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public List<String> getIncomRelMetadata() {
		return incomRelMetadata;
	}

	public void setIncomRelMetadata(List<String> incomRelMetadata) {
		this.incomRelMetadata = incomRelMetadata;
	}

	public List<String> getIncomRelSpecializes() {
		return incomRelSpecializes;
	}

	public void setIncomRelSpecializes(List<String> incomRelSpecializes) {
		this.incomRelSpecializes = incomRelSpecializes;
	}

	public List<String> getIncomRelGeneralizes() {
		return incomRelGeneralizes;
	}

	public void setIncomRelGeneralizes(List<String> incomRelGeneralizes) {
		this.incomRelGeneralizes = incomRelGeneralizes;
	}

	public List<String> getIncomRelExtends() {
		return incomRelExtends;
	}

	public void setIncomRelExtends(List<String> incomRelExtends) {
		this.incomRelExtends = incomRelExtends;
	}

	public List<String> getIncomRelEquivalent() {
		return incomRelEquivalent;
	}

	public void setIncomRelEquivalent(List<String> incomRelEquivalent) {
		this.incomRelEquivalent = incomRelEquivalent;
	}

	public List<String> getIncomRelDisjunc() {
		return incomRelDisjunc;
	}

	public void setIncomRelDisjunc(List<String> incomRelDisjunc) {
		this.incomRelDisjunc = incomRelDisjunc;
	}

	public List<String> getIncomRelImports() {
		return incomRelImports;
	}

	public void setIncomRelImports(List<String> incomRelImports) {
		this.incomRelImports = incomRelImports;
	}

	public int getNbIncomingLinks() {
		return nbIncomingLinks;
	}

	public void setNbIncomingLinks(int nbIncomingLinks) {
		this.nbIncomingLinks = nbIncomingLinks;
	}
	
	public void addIncomRelMetadata(String vocabUri, MongoCollection statsCollection) {
		if(incomRelMetadata==null)incomRelMetadata = new ArrayList<String>();
		addInRel(incomRelMetadata, vocabUri, statsCollection);
	}
	public void addIncomRelSpecializes(String vocabUri, MongoCollection statsCollection) {
		if(incomRelSpecializes==null)incomRelSpecializes = new ArrayList<String>();
		addInRel(incomRelSpecializes, vocabUri, statsCollection);
	}
	public void addIncomRelGeneralizes(String vocabUri, MongoCollection statsCollection) {
		if(incomRelGeneralizes==null)incomRelGeneralizes = new ArrayList<String>();
		addInRel(incomRelGeneralizes, vocabUri, statsCollection);
	}
	public void addIncomRelExtends(String vocabUri, MongoCollection statsCollection) {
		if(incomRelExtends==null)incomRelExtends = new ArrayList<String>();
		addInRel(incomRelExtends, vocabUri, statsCollection);
	}
	public void addIncomRelEquivalent(String vocabUri, MongoCollection statsCollection) {
		if(incomRelEquivalent==null)incomRelEquivalent = new ArrayList<String>();
		addInRel(incomRelEquivalent, vocabUri, statsCollection);
	}
	public void addIncomRelDisjunc(String vocabUri, MongoCollection statsCollection) {
		if(incomRelDisjunc==null)incomRelDisjunc = new ArrayList<String>();
		addInRel(incomRelDisjunc, vocabUri, statsCollection);
	}
	public void addIncomRelImports(String vocabUri, MongoCollection statsCollection) {
		if(incomRelImports==null)incomRelImports = new ArrayList<String>();
		addInRel(incomRelImports, vocabUri, statsCollection);
	}
	
	private void addInRel(List<String> inRel, String vocabUri, MongoCollection statsCollection){
		StatVocab vocab= statsCollection.findOne("{uri:#}", vocabUri).as(StatVocab.class);
		if(vocab!=null){
			inRel.add(vocab.getId());
			nbIncomingLinks++;
		}
	}

	public List<String> getOutRelMetadata() {
		return outRelMetadata;
	}

	public void setOutRelMetadata(List<String> outRelMetadata) {
		this.outRelMetadata = outRelMetadata;
	}

	public List<String> getOutRelSpecializes() {
		return outRelSpecializes;
	}

	public void setOutRelSpecializes(List<String> outRelSpecializes) {
		this.outRelSpecializes = outRelSpecializes;
	}

	public List<String> getOutRelGeneralizes() {
		return outRelGeneralizes;
	}

	public void setOutRelGeneralizes(List<String> outRelGeneralizes) {
		this.outRelGeneralizes = outRelGeneralizes;
	}

	public List<String> getOutRelExtends() {
		return outRelExtends;
	}

	public void setOutRelExtends(List<String> outRelExtends) {
		this.outRelExtends = outRelExtends;
	}

	public List<String> getOutRelEquivalent() {
		return outRelEquivalent;
	}

	public void setOutRelEquivalent(List<String> outRelEquivalent) {
		this.outRelEquivalent = outRelEquivalent;
	}

	public List<String> getOutRelDisjunc() {
		return outRelDisjunc;
	}

	public void setOutRelDisjunc(List<String> outRelDisjunc) {
		this.outRelDisjunc = outRelDisjunc;
	}

	public List<String> getOutRelImports() {
		return outRelImports;
	}

	public void setOutRelImports(List<String> outRelImports) {
		this.outRelImports = outRelImports;
	}
	
	public void addOutRelMetadata(List<String> vocabNsps, MongoCollection statsCollection) {
		if(outRelMetadata==null)outRelMetadata = new ArrayList<String>();
		addOutRel(outRelMetadata,vocabNsps,statsCollection);
	}
	public void addOutRelSpecializes(List<String> vocabNsps, MongoCollection statsCollection) {
		if(outRelSpecializes==null)outRelSpecializes = new ArrayList<String>();
		addOutRel(outRelSpecializes,vocabNsps,statsCollection);
	}
	public void addOutRelGeneralizes(List<String> vocabNsps, MongoCollection statsCollection) {
		if(outRelGeneralizes==null)outRelGeneralizes = new ArrayList<String>();
		addOutRel(outRelGeneralizes,vocabNsps,statsCollection);
	}
	public void addOutRelExtends(List<String> vocabNsps, MongoCollection statsCollection) {
		if(outRelExtends==null)outRelExtends = new ArrayList<String>();
		addOutRel(outRelExtends,vocabNsps,statsCollection);
	}
	public void addOutRelEquivalent(List<String> vocabNsps, MongoCollection statsCollection) {
		if(outRelEquivalent==null)outRelEquivalent = new ArrayList<String>();
		addOutRel(outRelEquivalent,vocabNsps,statsCollection);
	}
	public void addOutRelDisjunc(List<String> vocabNsps, MongoCollection statsCollection) {
		if(outRelDisjunc==null)outRelDisjunc = new ArrayList<String>();
		addOutRel(outRelDisjunc,vocabNsps,statsCollection);
	}
	public void addOutRelImports(List<String> vocabNsps, MongoCollection statsCollection) {
		if(outRelImports==null)outRelImports = new ArrayList<String>();
		addOutRel(outRelImports,vocabNsps,statsCollection);
	}
	
	private void addOutRel(List<String> outRel, List<String> vocabNsps, MongoCollection statsCollection){
		if(vocabNsps!=null){
			for (String vocabNsp : vocabNsps) {
				StatVocab vocab= statsCollection.findOne("{$or:[{nsp:#},{uri:#}]}", vocabNsp, vocabNsp).as(StatVocab.class);
				if(vocab!=null){
					outRel.add(vocab.getId());
					nbOutgoingLinks++;
				}
			}
		}		
	}
	
}
