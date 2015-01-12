package org.lov.vocidex.describers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.node.ObjectNode;
import org.lov.SPARQLRunner;
import org.lov.objects.Agent;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

public abstract class AgentDescriber extends SPARQLDescriber {
	
	public AgentDescriber(SPARQLRunner source) {
		super(source);
	}
	
	private String getName(Resource agent) {
		return this.getSource().getString("agent-name.sparql","agent", agent, "name");
	}
	
	private List<Resource> getAltUris(Resource agent) {
		return this.getSource().getURIs("agent-altUris.sparql","agent", agent, "altUri");
	}
	
	private List<Resource> getRelatedVocabs(Resource agent) {
		return this.getSource().getURIs("list-agent-related-vocab.sparql","agent", agent, "vocab");
	}
	
	
	public void describe(String type, Resource agent, ObjectNode descriptionRoot) {
		descriptionRoot.put("type", type);
		putString(descriptionRoot, "uri", agent.getURI());
		
		List<String> altURIs = new ArrayList<String>();
		for (Iterator<Resource> iterator = getAltUris(agent).iterator(); iterator.hasNext();) {
			Resource altUri = iterator.next();
			altURIs.add(altUri.getURI());
		}
		putArrayString(descriptionRoot, "alturis", altURIs);
		
		String name = getName(agent);
		if (name != null) {
			descriptionRoot.put("name", name);
		};
		
		Map<String, Integer> tags = new HashMap<String,Integer>();
		List<String[]> vocabs = new ArrayList<String[]>();
		for (Iterator<Resource> iterator = getRelatedVocabs(agent).iterator(); iterator.hasNext();) {
			Resource vocab = iterator.next();
			ResultSet rsKeywords = getSource().getResultSet("lov-term-tags.sparql", "vocab", vocab);
			while(rsKeywords.hasNext()){
				QuerySolution qs2 = rsKeywords.next();
				String tag = qs2.get("tag").asLiteral().getLexicalForm();
				if(tags.get(tag)==null)tags.put(tag,1);
				else tags.put(tag, tags.get(tag)+1);
			}
			QuerySolution qs = getSource().getOneSolution("describe-lov-vocab.sparql", "vocab", vocab);
			vocabs.add(new String[]{vocab.getURI(),qs.get("prefix").asLiteral().getLexicalForm()});
			
		}
		putTagLabelOcc(descriptionRoot, "tags", tags);
		putVocabURIPrefix(descriptionRoot, "vocabs", vocabs);
	}
}
