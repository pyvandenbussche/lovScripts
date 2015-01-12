package org.lov.rdf2mongo.extractors;

import java.util.Iterator;

import org.lov.SPARQLRunner;
import org.lov.objects.Element;
import org.lov.rdf2mongo.describers.ElementDescriber;
import org.lov.rdf2mongo.iterators.ElementsDescriberIterator;

import com.hp.hpl.jena.query.Dataset;

/**
 * Extracts indexable {@link Element} instances from vocabularies 
 * declared in the LOV metrics dump.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class ElementsExtractor implements Iterable<Element> {
	private final SPARQLRunner metrics;
	
	public ElementsExtractor(Dataset metricsDataset) {
		this.metrics = new SPARQLRunner(metricsDataset);
	}
	
	public Iterator<Element> getElements() {
		return createDescriptionIterator("list-elements-metrics.sparql", "element", new ElementDescriber(metrics));
	}
	
	@Override
	public Iterator<Element> iterator() {return getElements();}
	
	private Iterator<Element> createDescriptionIterator(
			String sparqlFileName, String sparqlResultVariable, ElementDescriber describer) {
		return new ElementsDescriberIterator(
				metrics.getURIs(sparqlFileName, null, null, sparqlResultVariable),
				describer);
	}
}
