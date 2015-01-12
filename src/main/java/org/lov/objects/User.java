package org.lov.objects;

import java.io.Serializable;

import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.ObjectId;

/**
 * Represents a User who register to LOV WebSite
 * 
 * @author Pierre-Yves Vandenbussche
 *
 */
public class User implements Serializable{
	
	public static String CATEGORY_ADMIN = "admin";
	public static String CATEGORY_CURATOR = "curator";
	public static String CATEGORY_USER = "user";

	private static final long serialVersionUID = -9095302190165421462L;
	
	@Id @ObjectId
	private String id;
	private String name;
	private String email;
	private String category;
	private String callBackFn;
	private String apiKey;
	private String organization;
	private boolean activated=false;
	private ClaimAgent agent;
	
	
	public User(){super();}
	
	 
    public String getId() {
	  return id;
	}
	  
	public void setId(String id) {
	  this.id = id;
	}

	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public ClaimAgent getAgent() {
		return agent;
	}
	public void setAgent(ClaimAgent claimAgent) {
		this.agent = claimAgent;
	}
	public void setAgent(String agentUri, MongoCollection agentCollection) {
		this.agent = new ClaimAgent();
		agent.setAgentId(agentUri, agentCollection);
		agent.setStatus(ClaimAgent.STATUS_ACCEPTED);
	}
	public boolean isActivated() {
		return activated;
	}
	public void setActivated(boolean activated) {
		this.activated = activated;
	}


	//	public void setAgent(String agentId) {
//		this.agent = new ClaimAgent();
//		agent.setAgent(agentId);
//		agent.setStatus(ClaimAgent.STATUS_ACCEPTED);
//	}
	public String getCallBackFn() {
		return callBackFn;
	}
	public void setCallBackFn(String callBackFn) {
		this.callBackFn = callBackFn;
	}	
}
