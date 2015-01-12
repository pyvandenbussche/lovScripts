package org.lov.objects;

import java.io.Serializable;

import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.ObjectId;

public class Language implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7694579394508263739L;
	
	@Id @ObjectId
	private String id;
	private String uri;
	private String label;
	private String iso639P3PCode;
	private String iso639P1Code;
	
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
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getIso639P3PCode() {
		return iso639P3PCode;
	}
	public void setIso639P3PCode(String iso639p3pCode) {
		iso639P3PCode = iso639p3pCode;
	}
	public String getIso639P1Code() {
		return iso639P1Code;
	}
	public void setIso639P1Code(String iso639p1Code) {
		iso639P1Code = iso639p1Code;
	}
}
