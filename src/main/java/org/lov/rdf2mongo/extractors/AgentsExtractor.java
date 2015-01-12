package org.lov.rdf2mongo.extractors;

import java.util.Iterator;

import org.lov.SPARQLRunner;
import org.lov.objects.Agent;
import org.lov.rdf2mongo.describers.AgentDescriber;
import org.lov.rdf2mongo.describers.OrganizationDescriber;
import org.lov.rdf2mongo.describers.PersonDescriber;
import org.lov.rdf2mongo.iterators.AgentsDescriberIterator;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.util.iterator.NiceIterator;

/**
 * Extracts Agents from a dataset containing the LOV dump. 
 * Will create one document for each agent.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class AgentsExtractor implements Iterable<Agent> {
	private final SPARQLRunner source;
	
	public AgentsExtractor(Dataset dataset) {
		this.source = new SPARQLRunner(dataset);
	}
	
	/**
	 * Extract only persons
	 */
	public Iterator<Agent> persons() {
		return createDescriptionIterator("list-persons.sparql", "agent", new PersonDescriber(source));
	}
	
	/**
	 * Extract only organisations
	 */
	public Iterator<Agent> organizations() {
		return createDescriptionIterator("list-organizations.sparql", "agent", new OrganizationDescriber(source));
	}
	
	/**
	 * Extract only Software Agents
	 * @return
	 */
	public Iterator<Agent> swAgents() {
		return createDescriptionIterator("list-swagents.sparql", "agent", new OrganizationDescriber(source));
	}
	
	
	@Override
	public Iterator<Agent> iterator() {
		return NiceIterator.andThen(persons(), organizations()).andThen(swAgents());
	}
	
	private Iterator<Agent> createDescriptionIterator(
			String sparqlFileName, String sparqlResultVariable, AgentDescriber describer) {
		return new AgentsDescriberIterator(
				source.getURIs(sparqlFileName, null, null, sparqlResultVariable),
				describer);
	}
}
