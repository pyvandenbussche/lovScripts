package org.lov.rdf2mongo.describers;

import org.lov.SPARQLRunner;
import org.lov.objects.Element;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Resource;

public class ElementDescriber {
	private final SPARQLRunner source;
	
	public ElementDescriber(SPARQLRunner source) {
		this.source = source;
	}
	
	private QuerySolution getMetrics(Resource element) {
		return source.getOneSolution("elements-metrics.sparql","element", element);
	}
	
	public Element describe(Resource uri) {
		Element el = new Element();
		el.setUri(uri.getURI());
		QuerySolution qs = getMetrics(uri);
		el.setOccurrencesInDatasets(Integer.parseInt(qs.get("occurrencesInDatasets").asLiteral().getLexicalForm()));
		el.setReusedByDatasets(Integer.parseInt(qs.get("reusedByDatasets").asLiteral().getLexicalForm()));
		return el;
	}
}
