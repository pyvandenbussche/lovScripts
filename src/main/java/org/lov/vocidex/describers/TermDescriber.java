package org.lov.vocidex.describers;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.node.ObjectNode;
import org.lov.SPARQLRunner;

import com.hp.hpl.jena.rdf.model.Literal;
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
	
	private List<Literal> getRDFSLabels(Resource term) {
		return getSource().getLiterals("term-labels.sparql","label", "term", term);
	}
	private String getLabelFromSource(Resource term) {
		return getSource().getLangString("term-label.sparql", term, "label");
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
		
//		List<Literal> labels = getRDFSLabels(term);
//		List<String> labelsString = new ArrayList<String>();
//		if(labels!=null){
//			for (Literal label : labels) {
//				labelsString.add(label.getLexicalForm());
//			}
//			if(labelsString.size()>0)putArrayString(descriptionRoot, "labelsWithoutLang", labelsString);
//		}
		//putString(descriptionRoot, "labelsWithoutLang", getLabelFromSource(term));
		if(tag!=null)putArrayString(descriptionRoot, "tags", tag);
		// Adds "label" key
		strLiteralDescriber.describe(term, descriptionRoot);
		//labelDescriber.describe(term, descriptionRoot);
		//putString(descriptionRoot, "comment", getComment(term));
	}
}
