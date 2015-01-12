package org.lov.rdf2mongo.iterators;

import java.util.Collection;
import java.util.Iterator;

import org.lov.objects.Agent;
import org.lov.objects.Vocabulary;
import org.lov.rdf2mongo.describers.AgentDescriber;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Utility class that implements an iterator over indexable
 * {@link Vocabulary}s by calling a {@link Describer}
 * on every {@link Resource} in a collection.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class AgentsDescriberIterator implements Iterator<Agent> {
	private final Iterator<Resource> it;
	private final AgentDescriber describer;
	
	public AgentsDescriberIterator(Collection<Resource> resources, AgentDescriber describer) {
		this.it = resources.iterator();
		this.describer = describer;
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public Agent next() {
		Resource resource = it.next();
		return  describer.describe(resource);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
