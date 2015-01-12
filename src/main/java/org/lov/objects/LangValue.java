package org.lov.objects;

import java.io.Serializable;

/**
 * Represents a Lang Value e.g. "Paris"@en
 * 
 * @author Pierre-Yves Vandenbussche
 *
 */
public class LangValue implements Serializable{
	
	private static final long serialVersionUID = 2324053070053233584L;
	private String value;
	private String lang;
	
	
	public LangValue(){}
	public LangValue(String value, String lang){
		this.value=value;
		this.lang=lang;
	}

	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}	
}
