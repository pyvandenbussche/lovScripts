package org.lov.rdf2mongo.extractors;

import java.util.Iterator;

import org.jongo.MongoCollection;
import org.lov.SPARQLRunner;
import org.lov.objects.User;
import org.lov.rdf2mongo.describers.UserDescriber;
import org.lov.rdf2mongo.iterators.UsersDescriberIterator;

import com.hp.hpl.jena.query.Dataset;

/**
 * Extracts Users (LOV curators) from a dataset containing the LOV dump. 
 * Will create one document for each user.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class UsersExtractor implements Iterable<User> {
	private final SPARQLRunner source;
	private final MongoCollection agentCollection;
	
	public UsersExtractor(Dataset dataset, MongoCollection agentCollection) {
		this.source = new SPARQLRunner(dataset);
		this.agentCollection=agentCollection;
	}
	
	/**
	 * Extract only persons
	 */
	public Iterator<User> getcurators() {
		return createDescriptionIterator("list-lov-curators.sparql", "user", new UserDescriber(source,agentCollection));
	}
	
	@Override
	public Iterator<User> iterator() {return getcurators();}
	
	private Iterator<User> createDescriptionIterator(
			String sparqlFileName, String sparqlResultVariable, UserDescriber describer) {
		return new UsersDescriberIterator(
				source.getURIs(sparqlFileName, null, null, sparqlResultVariable),
				describer);
	}
}
