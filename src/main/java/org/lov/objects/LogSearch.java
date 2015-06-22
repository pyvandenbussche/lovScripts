package org.lov.objects;

import java.io.Serializable;
import java.util.Date;

public class LogSearch implements Serializable {
	public static String CAT_TERM = "termSearch";
	public static String CAT_VOCAB = "vocabularySearch";
	public static String CAT_AGENT = "agentSearch";
	
	public static String METH_UI = "ui";
	public static String METH_API = "api";
	
	
	private String searchWords;
	private String searchURL;
	private LOVDate date;
	private String category;
	private String method;
	private String nbResults;
	
	
	
	
	public String getSearchWords() {
		return searchWords;
	}
	public void setSearchWords(String searchWords) {
		this.searchWords = searchWords;
	}
	public String getSearchURL() {
		return searchURL;
	}
	public void setSearchURL(String searchURL) {
		this.searchURL = searchURL;
	}
	public long getDate() {
		return date.get$date();
	}
	public void setDate(LOVDate date) {
		this.date = date;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getNbResults() {
		return nbResults;
	}
	public void setNbResults(String nbResults) {
		this.nbResults = nbResults;
	}
	
	class LOVDate {
	    private long $date;

		public long get$date() {
			return $date;
		}

		public void set$date(long $date) {
			this.$date = $date;
		}
	}
}
