package org.lov.objects;

import java.io.Serializable;

/**
 * Represents a Dataset: dataset using vocabularies
 * 
 * @author Pierre-Yves Vandenbussche
 *
 */
public class Dataset implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1561010940197708509L;
	private String uri;
	private String label;
	private int occurrences;
	
	
	
	public Dataset(){super();}
	public Dataset(String uri, String label, int occurrences){
		super();
		this.uri=uri;
		this.label=label;
		this.occurrences=occurrences;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public int getOccurrences() {
		return occurrences;
	}
	public void setOccurrences(int occurrences) {
		this.occurrences = occurrences;
	}
}
