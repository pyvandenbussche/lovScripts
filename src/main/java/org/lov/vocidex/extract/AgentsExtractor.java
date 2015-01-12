package org.lov.vocidex.extract;

import java.util.Iterator;

import org.lov.SPARQLRunner;
import org.lov.vocidex.VocidexDocument;
import org.lov.vocidex.describers.AgentDescriber;
import org.lov.vocidex.describers.OrganizationDescriber;
import org.lov.vocidex.describers.PersonDescriber;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.util.iterator.NiceIterator;

/**
 * Extracts Agents from a dataset containing the LOV dump. 
 * Will create one document for each agent.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class AgentsExtractor  implements Extractor {
	private final SPARQLRunner source;
	
	public AgentsExtractor(Dataset dataset) {
		this.source = new SPARQLRunner(dataset,"rdf2es");
	}
	
	/**
	 * Extract only persons
	 */
	public Iterator<VocidexDocument> persons() {
		return createDescriptionIterator("list-lov-persons.sparql", "agent", new PersonDescriber(source));
	}
	
	/**
	 * Extract only organisations
	 */
	public Iterator<VocidexDocument> organizations() {
		return createDescriptionIterator("list-lov-organizations.sparql", "agent", new OrganizationDescriber(source));
	}
	
	/**
	 * Extract only Software Agents
	 * @return
	 */
	public Iterator<VocidexDocument> swAgents() {
		return createDescriptionIterator("list-lov-swagents.sparql", "agent", new OrganizationDescriber(source));
	}
	
	
	@Override
	public Iterator<VocidexDocument> iterator() {
		return NiceIterator.andThen(persons(), organizations()).andThen(swAgents());
	}
	
	private Iterator<VocidexDocument> createDescriptionIterator(
			String sparqlFileName, String sparqlResultVariable, AgentDescriber describer) {
		return new DescriberIterator(
				source.getURIs(sparqlFileName, null, null, sparqlResultVariable),
				describer);
	}
}
