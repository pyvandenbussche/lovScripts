package org.lov.rdf2mongo.extractors;

import java.util.Iterator;

import org.lov.SPARQLRunner;
import org.lov.objects.Vocabulary;
import org.lov.rdf2mongo.ICom;
import org.lov.rdf2mongo.describers.VocabularyDescriber;
import org.lov.rdf2mongo.iterators.VocabulariesDescriberIterator;

import com.hp.hpl.jena.query.Dataset;

/**
 * Extracts indexable {@link Vocabulary} instances from vocabularies 
 * declared in the LOV dump.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class VocabulariesExtractor implements Iterable<Vocabulary> {
	private final SPARQLRunner source;
	private final SPARQLRunner metrics;
	private final ICom iCom;
	
	public VocabulariesExtractor(Dataset dataset, Dataset metricsDataset, ICom iCom) {
		this.source = new SPARQLRunner(dataset);
		this.metrics = new SPARQLRunner(metricsDataset);
		this.iCom = iCom;
	}
	
	public Iterator<Vocabulary> getVocabularies() {
		return createDescriptionIterator("list-lov-vocabularies.sparql", "vocab", new VocabularyDescriber(source, metrics, iCom));
	}
	
	@Override
	public Iterator<Vocabulary> iterator() {return getVocabularies();}
	
	private Iterator<Vocabulary> createDescriptionIterator(
			String sparqlFileName, String sparqlResultVariable, VocabularyDescriber describer) {
		return new VocabulariesDescriberIterator(
				source.getURIs(sparqlFileName, null, null, sparqlResultVariable),
				describer);
	}
}
