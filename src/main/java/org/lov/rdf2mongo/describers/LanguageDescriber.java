package org.lov.rdf2mongo.describers;

import org.lov.SPARQLRunner;
import org.lov.objects.Language;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Resource;

public class LanguageDescriber {
	private final SPARQLRunner source;
	
	public LanguageDescriber(SPARQLRunner source) {
		this.source=source;
	}
	
	private QuerySolution getInformation(Resource lang) {
		return source.getOneSolution("language-information.sparql","lang", lang);
	}
	
	public Language describe(Resource uri) {
		Language lang = new Language();
		lang.setUri("http://id.loc.gov/vocabulary/iso639-2/"+uri.getURI().substring(uri.getURI().lastIndexOf("/")+1) );//use Library of Congress URI
		QuerySolution qs = getInformation(uri);
		lang.setIso639P3PCode(qs.get("iso639P3PCode").asLiteral().getLexicalForm());
		lang.setIso639P1Code(qs.get("iso639P1Code").asLiteral().getLexicalForm());
		lang.setLabel(qs.get("label").asLiteral().getLexicalForm());
		return lang;
	}
}
