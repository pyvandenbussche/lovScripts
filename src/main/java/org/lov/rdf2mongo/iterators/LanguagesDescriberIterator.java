package org.lov.rdf2mongo.iterators;

import java.util.Collection;
import java.util.Iterator;

import org.lov.objects.Language;
import org.lov.rdf2mongo.describers.LanguageDescriber;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Utility class that implements an iterator over indexable
 * {@link Language}s by calling a {@link LanguageDescriber}
 * on every {@link Resource} in a collection.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class LanguagesDescriberIterator implements Iterator<Language> {
	private final Iterator<Resource> it;
	private final LanguageDescriber describer;
	
	public LanguagesDescriberIterator(Collection<Resource> resources, LanguageDescriber describer) {
		this.it = resources.iterator();
		this.describer = describer;
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public Language next() {
		Resource resource = it.next();
		return  describer.describe(resource);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
