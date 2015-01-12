package org.lov.objects;

import java.io.Serializable;
import java.util.Date;

/**
 * Represents an Comment: could be an official review or a user comment
 * 
 * @author Pierre-Yves Vandenbussche
 *
 */
public class Comment implements Serializable{

	private static final long serialVersionUID = 2983039987588333727L;
	private String body;
	private Date createdAt;
	private String agentId;
	
	
	public Comment(){super();}
	public Comment(String agentId, Date createdAt, String body){
		super();
		this.agentId=agentId;
		this.createdAt=createdAt;
		this.body=body;
	}


	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public String getAgentId() {
		return agentId;
	}
	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
}
