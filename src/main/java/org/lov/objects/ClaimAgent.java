package org.lov.objects;

import java.io.Serializable;

import org.jongo.MongoCollection;

/**
 * Represents a user claim for agent identification
 * 
 * @author Pierre-Yves Vandenbussche
 *
 */
public class ClaimAgent implements Serializable{
	public static String STATUS_PENDING= "pending";
	public static String STATUS_ACCEPTED= "accepted";

	private static final long serialVersionUID = 4506053246800425853L;
	private String status;
	private String agentId;
	
	
	public ClaimAgent(){super();}

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getAgentId() {
		return agentId;
	}
	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
	public void setAgentId(String agentPrefUri,MongoCollection agentCollection) {
//		System.out.println("{prefUri: '"+agentPrefUri+"'}");
		Agent agent = agentCollection.findOne("{prefUri: #}",agentPrefUri).as(Agent.class);
		this.agentId = agent.getId();
	}
}
