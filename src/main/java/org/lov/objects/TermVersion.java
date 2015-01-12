package org.lov.objects;

import java.io.Serializable;
import java.util.List;

import org.jongo.marshall.jackson.oid.Id;


/**
 * Represents a Vocabulary version: with actual link to terms etc.
 * 
 * @author Pierre-Yves Vandenbussche
 *
 */
public class TermVersion implements Serializable{

	private static final long serialVersionUID = 1672110638875440226L;
	private String id;
	private String uri;
	private List<String> vocabularyVersions;
	//to be completed
	
	
	public TermVersion(){super();}
	
	 
	@Id
    public String getId() {
	  return id;
	}
	  
	@Id
	public void setId(String id) {
	  this.id = id;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
}
