package org.lov.rdf2mongo.describers;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.jongo.MongoCollection;
import org.lov.SPARQLRunner;
import org.lov.objects.User;

import com.hp.hpl.jena.rdf.model.Resource;

public class UserDescriber {
	private final SPARQLRunner source;
	private SecureRandom random = new SecureRandom();
	private final MongoCollection agentCollection;
	
	public UserDescriber(SPARQLRunner source, MongoCollection agentCollection) {
		this.source=source;
		this.agentCollection=agentCollection;
	}
	
	private String getName(Resource agent) {
		return source.getString("agent-name.sparql","agent", agent, "name");
	}
	
	public User describe(Resource uri) {
		User user = new User();
		user.setName(getName(uri));
		user.setActivated(true);
		user.setApiKey(generateApiKey());
		user.setAgent(uri.getURI(), agentCollection);
		
		user.setEmail(getEmailFromPrefURI(uri.getURI()));
		if(user.getEmail()==null)user.setCategory(User.CATEGORY_CURATOR);
		else user.setCategory(User.CATEGORY_ADMIN);
		return user;
	}

	public String generateApiKey(){
	  return new BigInteger(130, random).toString(49);
	}
	
	private String getEmailFromPrefURI(String prefURI){
		if(prefURI.equals("http://google.com/+BernardVatant")) return "bernard.vatant@mondeca.com";
		if(prefURI.equals("http://google.com/+GhislainAtemezing")) return "auguste.atemezing@eurecom.fr";
		if(prefURI.equals("http://google.com/+MaríaPovedaVillalón")) return "mpoveda@fi.upm.es";
		if(prefURI.equals("http://google.com/+PierreYvesVandenbussche")) return "py.vandenbussche@gmail.com";
		return null;
	}
}
