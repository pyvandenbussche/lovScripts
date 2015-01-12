package org.lov.objects;

import java.io.Serializable;

import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.ObjectId;

/**
 * Represents a Vocabulary Element: property, class, datatype or instance
 * 
 * @author Pierre-Yves Vandenbussche
 *
 */
public class Element implements Serializable{

	private static final long serialVersionUID = -892193066562727135L;
	@Id @ObjectId
	private String id;
	private String uri;
	private int occurrencesInDatasets;
	private int reusedByDatasets;
	
	
	public Element(){super();}


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


	public int getOccurrencesInDatasets() {
		return occurrencesInDatasets;
	}


	public void setOccurrencesInDatasets(int occurrencesInDatasets) {
		this.occurrencesInDatasets = occurrencesInDatasets;
	}


	public int getReusedByDatasets() {
		return reusedByDatasets;
	}


	public void setReusedByDatasets(int reusedByDatasets) {
		this.reusedByDatasets = reusedByDatasets;
	}
	
}
