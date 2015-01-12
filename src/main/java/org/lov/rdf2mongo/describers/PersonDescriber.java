package org.lov.rdf2mongo.describers;

import org.lov.SPARQLRunner;
import org.lov.objects.Agent;

import com.hp.hpl.jena.rdf.model.Resource;

public class PersonDescriber extends AgentDescriber {
	public final static String TYPE = "person";
	
	public PersonDescriber(SPARQLRunner source) {
		super(source);
	}
	
	public Agent describe(Resource uri) {
		return super.describe(TYPE, uri);
	}
}
