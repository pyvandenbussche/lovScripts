package org.lov.rdf2mongo.describers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lov.SPARQLRunner;
import org.lov.objects.Agent;

import com.hp.hpl.jena.rdf.model.Resource;

public abstract class AgentDescriber {
	private final SPARQLRunner source;
	
	public AgentDescriber(SPARQLRunner source) {
		this.source = source;
	}
	
	private String getName(Resource agent) {
		return source.getString("agent-name.sparql","agent", agent, "name");
	}
	
	private List<Resource> getAltUris(Resource agent) {
		return source.getURIs("agent-altUris.sparql","agent", agent, "altUri");
	}
	
	public abstract Agent describe(Resource resource);
	
	public Agent describe(String type, Resource uri) {
		Agent agent = new Agent();
		agent.setType(type);
		agent.setPrefUri(uri.getURI());
		agent.setName(getName(uri));
		List<String> altUris = new ArrayList<String>();
		for (Iterator<Resource> iterator = getAltUris(uri).iterator(); iterator.hasNext();) {
			Resource altUri = iterator.next();
			altUris.add(altUri.getURI());
		}
		agent.setAltUris(altUris);
		return agent;
	}
}
