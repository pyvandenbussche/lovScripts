package org.lov.vocidex.describers;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.node.ObjectNode;
import org.lov.SPARQLRunner;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Produces a JSON description of a vocabulary, using the metadata present in LOV.
 * 
 * @author Richard Cyganiak, Pierre-Yves Vandenbussche
 */
public class LOVVocabularyDescriber extends SPARQLDescriber {
	public final static String TYPE = "vocabulary";

	public LOVVocabularyDescriber(SPARQLRunner source) {
		super(source);
	}
	
	public void describe(Resource vocabulary, ObjectNode descriptionRoot) {
		QuerySolution qs = getSource().getOneSolution("describe-lov-vocab.sparql", "vocab", vocabulary);
		descriptionRoot.put("type", TYPE);
		putString(descriptionRoot, "uri", vocabulary.getURI());
		System.out.println(vocabulary.getURI());
		putString(descriptionRoot, "prefix", qs.get("prefix").asLiteral().getLexicalForm()); 
		
		ResultSet rsTitles = getSource().getResultSet("describe-lov-vocab-titles.sparql", "vocab", vocabulary);
		while(rsTitles.hasNext()){
			QuerySolution qs2 = rsTitles.next();
			String property = "http://purl.org/dc/terms/title";
			String value = qs2.get("title").asLiteral().getLexicalForm();
			String lang = qs2.get("title").asLiteral().getLanguage();
			String propLang = (lang!=null&&lang.length()>0 ? property+"@"+lang : property );
			putString(descriptionRoot, propLang, value);
		}
		
		ResultSet rsComments = getSource().getResultSet("describe-lov-vocab-descs.sparql", "vocab", vocabulary);
		while(rsComments.hasNext()){
			QuerySolution qs2 = rsComments.next();
			String property = "http://purl.org/dc/terms/description";
			String value = qs2.get("description").asLiteral().getLexicalForm();
			String lang = qs2.get("description").asLiteral().getLanguage();
			String propLang = (lang!=null&&lang.length()>0 ? property+"@"+lang : property );
			putString(descriptionRoot, propLang, value);
		}
		
		ResultSet rsKeywords = getSource().getResultSet("lov-term-tags.sparql", "vocab", vocabulary);
		List<String> tags = new ArrayList<String>();
		while(rsKeywords.hasNext()){
			QuerySolution qs2 = rsKeywords.next();
			tags.add(qs2.get("tag").asLiteral().getLexicalForm());
		}
		putArrayString(descriptionRoot, "tags", tags);
		
		ResultSet rslangs = getSource().getResultSet("lov-vocabulary-languages.sparql", "vocab", vocabulary);
		List<String> langs = new ArrayList<String>();
		while(rslangs.hasNext()){
			QuerySolution qs2 = rslangs.next();
			langs.add(qs2.get("lang").asLiteral().getLexicalForm());
		}
		putArrayString(descriptionRoot, "langs", langs);
	}	
}