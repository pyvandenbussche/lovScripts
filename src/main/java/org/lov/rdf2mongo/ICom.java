package org.lov.rdf2mongo;

import java.util.Properties;

import org.jongo.MongoCollection;

public interface ICom {
	public Properties getLovConfig();
	public MongoCollection getLangCollection();
	public MongoCollection getAgentCollection();
	public MongoCollection getVocabCollection();
}
