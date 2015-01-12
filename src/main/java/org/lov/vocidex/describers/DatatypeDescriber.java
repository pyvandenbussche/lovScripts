package org.lov.vocidex.describers;

import org.codehaus.jackson.node.ObjectNode;
import org.lov.SPARQLRunner;

import com.hp.hpl.jena.rdf.model.Resource;

public class DatatypeDescriber extends TermDescriber {
	public final static String TYPE = "datatype";

	public DatatypeDescriber(SPARQLRunner source, String prefix, String tag) {
		super(source, prefix, tag);
	}
	
	public void describe(Resource datatype, ObjectNode descriptionRoot) {
		super.describe(TYPE, datatype, descriptionRoot);
	}
}
