package org.lov.objects;

import java.io.Serializable;
import java.util.List;

import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.ObjectId;


/**
 * Represents an Agent: Person or Organisation having a role in vocabulary design or publication
 * 
 * @author Pierre-Yves Vandenbussche
 *
 */
public class Agent implements Serializable{

	private static final long serialVersionUID = -5260350321135219966L;
	
	@Id @ObjectId
	private String id;
	private String prefUri;
	private String name;
	private List<String> altUris;
	private String type;
	private List<String> hasRoleInVocab;
	
	
	public Agent(){}
	
	public Agent(String prefUri, String name){
		super();
		this.prefUri=prefUri;
		this.name=name;
	}
	 
    public String getId() {
	  return this.id;
	}
	public void setId(String id) {
	  this.id = id;
	}
	public String getPrefUri() {
		return prefUri;
	}
	public void setPrefUri(String prefUri) {
		this.prefUri = prefUri;
	}
	public List<String> getAltUris() {
		return altUris;
	}
	public void setAltUris(List<String> altUris) {
		this.altUris = altUris;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getHasRoleInVocab() {
		return hasRoleInVocab;
	}
	public void setHasRoleInVocab(List<String> hasRoleInVocab) {
		this.hasRoleInVocab = hasRoleInVocab;
	}
}
