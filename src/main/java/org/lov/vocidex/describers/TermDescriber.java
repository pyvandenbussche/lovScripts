package org.lov.vocidex.describers;

import org.codehaus.jackson.node.ObjectNode;
import org.lov.SPARQLRunner;

import com.hp.hpl.jena.rdf.model.Resource;

public abstract class TermDescriber extends SPARQLDescriber {
	protected final StrLiteralDescriber strLiteralDescriber;
	private final String prefix;
	private final String tag;
	
	public TermDescriber(SPARQLRunner source, String prefix, String tag) {
		super(source);
		this.prefix = prefix;
		this.tag=tag;
		this.strLiteralDescriber = new StrLiteralDescriber(source);
	}
	
	public String getURI(Resource term) {
		return term.getURI();
	}
	
	public String getLocalName(Resource term) {
		return term.getLocalName();
	}

//	public String getComment(Resource term) {
//		return getSource().getLangString("term-comment.sparql", term, "comment");
//	}

	public void describe(String type, Resource term, ObjectNode descriptionRoot) {
		descriptionRoot.put("type", type);
		putString(descriptionRoot, "uri", getURI(term));
		if (prefix != null) {
			descriptionRoot.put("prefix", prefix);
			descriptionRoot.put("prefixedName", prefix + ":" + getLocalName(term));
		};
		putString(descriptionRoot, "localName", getLocalName(term));
		
		if(tag!=null)putArrayString(descriptionRoot, "tags", tag);
		// Adds "label" key
		strLiteralDescriber.describe(term, descriptionRoot);
		//labelDescriber.describe(term, descriptionRoot);
		//putString(descriptionRoot, "comment", getComment(term));
	}
}