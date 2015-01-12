package org.lov.rdf2mongo.extractors;

import java.util.Iterator;

import org.lov.SPARQLRunner;
import org.lov.objects.Language;
import org.lov.rdf2mongo.describers.LanguageDescriber;
import org.lov.rdf2mongo.iterators.LanguagesDescriberIterator;

import com.hp.hpl.jena.query.Dataset;

/**
 * Extracts Languages from a languages dataset.
 * Will create one document for each language.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class LanguagesExtractor implements Iterable<Language> {
	private final SPARQLRunner source;
	
	public LanguagesExtractor(Dataset dataset) {
		this.source = new SPARQLRunner(dataset);
	}
	
	/**
	 * Extract languages
	 */
	public Iterator<Language> getLanguages() {
		return createDescriptionIterator("list-languages.sparql", "lang", new LanguageDescriber(source));
	}
	
	@Override
	public Iterator<Language> iterator() {return getLanguages();}
	
	private Iterator<Language> createDescriptionIterator(
			String sparqlFileName, String sparqlResultVariable, LanguageDescriber describer) {
		return new LanguagesDescriberIterator(
				source.getURIs(sparqlFileName, null, null, sparqlResultVariable),
				describer);
	}
}
