package org.lov.rdf2mongo.iterators;

import java.util.Collection;
import java.util.Iterator;

import org.lov.objects.Element;
import org.lov.rdf2mongo.describers.ElementDescriber;
import org.lov.vocidex.describers.Describer;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Utility class that implements an iterator over indexable
 * {@link Element}s by calling a {@link Describer}
 * on every {@link Resource} in a collection.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class ElementsDescriberIterator implements Iterator<Element> {
	private final Iterator<Resource> it;
	private final ElementDescriber describer;
	
	public ElementsDescriberIterator(Collection<Resource> resources, ElementDescriber describer) {
		this.it = resources.iterator();
		this.describer = describer;
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public Element next() {
		Resource resource = it.next();
		return  describer.describe(resource);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
