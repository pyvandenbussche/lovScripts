package org.lov.vocidex.extract;

import java.util.Iterator;

import org.lov.SPARQLRunner;
import org.lov.vocidex.VocidexDocument;
import org.lov.vocidex.describers.ClassDescriber;
import org.lov.vocidex.describers.DatatypeDescriber;
import org.lov.vocidex.describers.InstanceDescriber;
import org.lov.vocidex.describers.PropertyDescriber;
import org.lov.vocidex.describers.SPARQLDescriber;

import com.hp.hpl.jena.util.iterator.NiceIterator;

/**
 * Extracts all vocabulary terms (classes, properties, datatypes)
 * from a {@link SPARQLRunner}, describes them using the appropriate
 * describers, and packages them as indexable
 * {@link VocidexDocument}s.
 * 
 * @author Richard Cyganiak
 */
public class VocabularyTermExtractor implements Extractor {
	private final SPARQLRunner source;
	private final String prefix;
	private final String tag;
	
	/**
	 * @param source Model containing declarations of vocabulary terms
	 * @param prefix Prefix to be used for creating prefixed names; may be null
	 */
	public VocabularyTermExtractor(SPARQLRunner source, String prefix, String tag) {
		this.source = source;
		this.prefix = prefix;
		this.tag=tag;
	}
	
	/**
	 * Extract only classes
	 */
	public Iterator<VocidexDocument> classes() {
		return createDescriptionIterator("list-classes.sparql", "class", new ClassDescriber(source, prefix,tag));
	}
	
	/**
	 * Extract only properties
	 */
	public Iterator<VocidexDocument> properties() {
		return createDescriptionIterator("list-properties.sparql", "property", new PropertyDescriber(source, prefix,tag));
	}
	
	/**
	 * Extract only data types
	 */
	public Iterator<VocidexDocument> datatypes() {
		return createDescriptionIterator("list-datatypes.sparql", "datatype", new DatatypeDescriber(source, prefix,tag));
	}
	
	/**
	 * Extract only instances
	 */
	public Iterator<VocidexDocument> instances() {
		return createDescriptionIterator("list-instances.sparql", "instance", new InstanceDescriber(source, prefix,tag));
	}

	/**
	 * Extract all terms (classes, properties, datatypes)
	 */
	@Override
	public Iterator<VocidexDocument> iterator() {
		return NiceIterator.andThen(classes(), properties()).andThen(datatypes()).andThen(instances());
	}
	
	private Iterator<VocidexDocument> createDescriptionIterator(
			String sparqlFileName, String sparqlResultVariable, SPARQLDescriber describer) {
		return new DescriberIterator(
				source.getURIs(sparqlFileName, null, null, sparqlResultVariable),
				describer);
	}
}
