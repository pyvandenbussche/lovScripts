package org.lov.vocidex.describers;

import org.codehaus.jackson.node.ObjectNode;
import org.lov.SPARQLRunner;
import org.lov.vocidex.JSONHelper;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Produces a JSON description of metrics for a vocabulary term, using the metrics metadata present in LOV.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class LOVTermMetricsDescriber extends SPARQLDescriber {

	public LOVTermMetricsDescriber(SPARQLRunner source) {
		super(source);
	}
	
	public void describe(Resource term, ObjectNode descriptionRoot) {
		QuerySolution qs = getSource().getOneSolution("lov-term-metrics.sparql", "term", term);
		
		ObjectNode v = JSONHelper.createObject();
				
		if(qs!=null && qs.get("occurrencesInVocabularies")!=null) 
			putLong(v, "occurrencesInVocabularies", Long.parseLong(qs.get("occurrencesInVocabularies").asLiteral().getLexicalForm()));
		else putLong(v, "occurrencesInVocabularies", new Long(0));
		
		if(qs!=null && qs.get("occurrencesInDatasets")!=null) 
			putLong(v, "occurrencesInDatasets", Long.parseLong(qs.get("occurrencesInDatasets").asLiteral().getLexicalForm()));
		else putLong(v, "occurrencesInDatasets", new Long(0));
		
		if(qs!=null && qs.get("reusedByVocabularies")!=null) 
			putLong(v, "reusedByVocabularies", Long.parseLong(qs.get("reusedByVocabularies").asLiteral().getLexicalForm()));
		else putLong(v, "reusedByVocabularies", new Long(0));
		
		if(qs!=null && qs.get("reusedByDatasets")!=null) 
			putLong(v, "reusedByDatasets", Long.parseLong(qs.get("reusedByDatasets").asLiteral().getLexicalForm()));
		else putLong(v, "reusedByDatasets", new Long(0));
		
		descriptionRoot.put("metrics", v);
	}	
}
