package org.lov.vocidex.describers;

import org.codehaus.jackson.node.ObjectNode;
import org.lov.SPARQLRunner;

import com.hp.hpl.jena.rdf.model.Resource;

public class OrganizationDescriber extends AgentDescriber {
	public final static String TYPE = "organization";
	
	public OrganizationDescriber(SPARQLRunner source) {
		super(source);
	}
	
	public void describe(Resource class_, ObjectNode descriptionRoot) {
		super.describe(TYPE, class_, descriptionRoot);
	}
}
