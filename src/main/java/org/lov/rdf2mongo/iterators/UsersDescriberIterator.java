package org.lov.rdf2mongo.iterators;

import java.util.Collection;
import java.util.Iterator;

import org.lov.objects.User;
import org.lov.rdf2mongo.describers.UserDescriber;

import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Utility class that implements an iterator over indexable
 * {@link User}s by calling a {@link Describer}
 * on every {@link Resource} in a collection.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class UsersDescriberIterator implements Iterator<User> {
	private final Iterator<Resource> it;
	private final UserDescriber describer;
	
	public UsersDescriberIterator(Collection<Resource> resources, UserDescriber describer) {
		this.it = resources.iterator();
		this.describer = describer;
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public User next() {
		Resource resource = it.next();
		return  describer.describe(resource);
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
