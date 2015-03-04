package org.lov.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jongo.MongoCollection;

/**
 * Represents a Vocabulary version: with actual link to terms etc.
 * 
 * @author Pierre-Yves Vandenbussche
 *
 */
public class VocabularyVersionWrapper implements Serializable, Comparable<VocabularyVersionWrapper>{

	private static final long serialVersionUID = 1672110638875440226L;
	private String name;
	private String fileURL;
	private Date issued;
	private int classNumber;
	private int propertyNumber;
	private int instanceNumber;
	private int datatypeNumber;
	private List<String> relMetadata;
	private List<String> relSpecializes;
	private List<String> relGeneralizes;
	private List<String> relExtends;
	private List<String> relEquivalent;
	private List<String> relDisjunc;
	private List<String> relImports;
	private List<String> languageIds;
	private boolean isReviewed=false;
	
	public VocabularyVersionWrapper(){super();}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getIssued() {
		return issued;
	}
	public void setIssued(Date issued) {
		this.issued = issued;
	}
	public String getFileURL() {
		return fileURL;
	}
	public void setFileURL(String fileURL) {
		this.fileURL = fileURL;
	}

	public int getClassNumber() {
		return classNumber;
	}

	public void setClassNumber(int classNumber) {
		this.classNumber = classNumber;
	}

	public int getPropertyNumber() {
		return propertyNumber;
	}

	public void setPropertyNumber(int propertyNumber) {
		this.propertyNumber = propertyNumber;
	}

	public int getInstanceNumber() {
		return instanceNumber;
	}

	public void setInstanceNumber(int instanceNumber) {
		this.instanceNumber = instanceNumber;
	}

	public int getDatatypeNumber() {
		return datatypeNumber;
	}

	public void setDatatypeNumber(int datatypeNumber) {
		this.datatypeNumber = datatypeNumber;
	}

	public List<String> getRelMetadata() {
		return relMetadata;
	}

	public void setRelMetadata(List<String> relMetadata) {
		this.relMetadata = relMetadata;
	}

	public List<String> getRelSpecializes() {
		return relSpecializes;
	}
	public void addRelMetadata(String vocabUriOrNsp) {
		if(relMetadata==null)relMetadata = new ArrayList<String>();
		relMetadata.add(vocabUriOrNsp);
	}

	public void setRelSpecializes(List<String> relSpecializes) {
		this.relSpecializes = relSpecializes;
	}
	public void addRelSpecializes(String vocabUriOrNsp) {
		if(relSpecializes==null)relSpecializes = new ArrayList<String>();
		relSpecializes.add(vocabUriOrNsp);
	}

	public List<String> getRelGeneralizes() {
		return relGeneralizes;
	}
	public void addRelGeneralizes(String vocabUriOrNsp) {
		if(relGeneralizes==null)relGeneralizes = new ArrayList<String>();
		relGeneralizes.add(vocabUriOrNsp);
	}

	public void setRelGeneralizes(List<String> relGeneralizes) {
		this.relGeneralizes = relGeneralizes;
	}

	public List<String> getRelExtends() {
		return relExtends;
	}

	public void setRelExtends(List<String> relExtends) {
		this.relExtends = relExtends;
	}
	public void addRelExtends(String vocabUriOrNsp) {
		if(relExtends==null)relExtends = new ArrayList<String>();
		relExtends.add(vocabUriOrNsp);
	}

	public List<String> getRelEquivalent() {
		return relEquivalent;
	}

	public void setRelEquivalent(List<String> relEquivalent) {
		this.relEquivalent = relEquivalent;
	}
	public void addRelEquivalent(String vocabUriOrNsp) {
		if(relEquivalent==null)relEquivalent = new ArrayList<String>();
		relEquivalent.add(vocabUriOrNsp);
	}

	public List<String> getRelDisjunc() {
		return relDisjunc;
	}

	public void setRelDisjunc(List<String> relDisjunc) {
		this.relDisjunc = relDisjunc;
	}
	public void addRelDisjunc(String vocabUriOrNsp) {
		if(relDisjunc==null)relDisjunc = new ArrayList<String>();
		relDisjunc.add(vocabUriOrNsp);
	}

	public List<String> getRelImports() {
		return relImports;
	}

	public void setRelImports(List<String> relImports) {
		this.relImports = relImports;
	}	
	public void addRelImports(String vocabUriOrNsp) {
		if(relImports==null)relImports = new ArrayList<String>();
		relImports.add(vocabUriOrNsp);
	}

	public void addLanguageId(String languageURI, MongoCollection langCollection) {
		if(languageIds==null)languageIds = new ArrayList<String>();
		Language lang = langCollection.findOne("{uri:#}", languageURI).as(Language.class);
		if(lang!=null) languageIds.add(lang.getId());
	}
	
	public void addLanguageId(String languageId) {
		if(languageIds==null)languageIds = new ArrayList<String>();
		if(languageId!=null) languageIds.add(languageId);
	}
	public List<String> getLanguageIds() {
		return languageIds;
	}

	public void setLanguageIds(List<String> languageIds) {
		this.languageIds = languageIds;
	}

	public boolean isReviewed() {
		return isReviewed;
	}

	public void setReviewed(boolean isReviewed) {
		this.isReviewed = isReviewed;
	}

	@Override
	public int compareTo(VocabularyVersionWrapper o) {
		return o.getIssued().compareTo(this.getIssued());//made to be sorting desc
	}
	
}
