package org.lov.vocidex.describers;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.lov.SPARQLRunner;
import org.lov.vocidex.JSONHelper;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Produces a JSON description of a vocabulary instance.
 * 
 * @author Pierre-Yves Vandenbussche
 */
public class InstanceDescriber extends TermDescriber {
	public final static String TYPE = "instance";
	
	public InstanceDescriber(SPARQLRunner source, String prefix, String tag) {
		super(source, prefix, tag);
	}
	
	public void describe(Resource class_, ObjectNode descriptionRoot) {
		super.describe(TYPE, class_, descriptionRoot);
		
		//fetch the instance types (classes defined in the same vocabulary i.e. same namespace)
		ResultSet rs = getSource().getResultSet("instance-classes.sparql", "instance", class_);
		ArrayNode array = JSONHelper.createArray();
		while(rs.hasNext()){
			QuerySolution qs2 = rs.next();
			ObjectNode node = JSONHelper.createObject();
			putString(node, "class", qs2.get("class").asResource().getURI());
			array.add(node);
		}
		putURIArray(descriptionRoot, "classes", array);
		
	}
}
