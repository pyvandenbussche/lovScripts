package org.lov.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.ObjectId;

/**
 * Represents a Vocabulary: contains the metadata and pointers to versions
 * 
 * @author Pierre-Yves Vandenbussche
 *
 */
public class Vocabulary implements Serializable{

	private static final long serialVersionUID = 6767088673597849172L;
	@Id @ObjectId
	private String id;
	private String uri;
	private String nsp;
	private String prefix;
	private List<LangValue> titles;
	private List<LangValue> descriptions;
	private List<String> tags;
	
	/*distinguish first issued and created/modified in LOV*/
	private Date createdInLOVAt; //creation of the record in LOV
	private Date lastModifiedInLOVAt; //last modification of the record in LOV (either by the BOT or a curator)
	private Date issuedAt; //first publication of the vocabulary on the WEB (not in LOV)
	private Date lastDeref; //Last date of successful dereferentiation by the BOT
	private String commentDeref; //if !=null means there has been an error during the dereferentiation
	
	private String homepage;
	private String isDefinedBy;
	private List<String> creatorIds;
	private List<String> contributorIds;
	private List<String> publisherIds;
	
	private List<Comment> reviews;
	private List<Comment> comments;
	private String status; // TODO for deprecated?
	private List<VocabularyVersionWrapper> versions;
	private List<Dataset> datasets;
	
	
	public Vocabulary(){super();}
	
	
	
	
    public String getId() {
	  return id;
	}
	public void setId(String id) {
	  this.id = id;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getNsp() {
		return nsp;
	}
	public void setNsp(String nsp) {
		this.nsp = nsp;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}	
	public Date getLastDeref() {
		return lastDeref;
	}
	public void setLastDeref(Date lastDeref) {
		this.lastDeref = lastDeref;
	}
	public String getCommentDeref() {
		return commentDeref;
	}
	public void setCommentDeref(String commentDeref) {
		this.commentDeref = commentDeref;
	}
	public void addTitle(LangValue title){
		if(titles==null)titles = new ArrayList<LangValue>();
		titles.add(title);
	}
	public List<LangValue> getTitles() {
		return titles;
	}
	public void setTitles(List<LangValue> titles) {
		this.titles = titles;
	}
	public List<LangValue> getDescriptions() {
		return descriptions;
	}
	public void setDescriptions(List<LangValue> descriptions) {
		this.descriptions = descriptions;
	}
	public void addDescription(LangValue description){
		if(descriptions==null)descriptions = new ArrayList<LangValue>();
		descriptions.add(description);
	}
	public List<String> getTags() {
		return tags;
	}
	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	public void addTag(String tag){
		if(tags==null)tags = new ArrayList<String>();
		tags.add(tag);
	}
	
	public Date getCreatedInLOVAt() {
		return createdInLOVAt;
	}

	public void setCreatedInLOVAt(Date createdInLOVAt) {
		this.createdInLOVAt = createdInLOVAt;
	}

	public Date getLastModifiedInLOVAt() {
		return lastModifiedInLOVAt;
	}

	public void setLastModifiedInLOVAt(Date lastModifiedInLOVAt) {
		this.lastModifiedInLOVAt = lastModifiedInLOVAt;
	}

	public Date getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(Date issuedAt) {
		this.issuedAt = issuedAt;
	}

	public String getHomepage() {
		return homepage;
	}
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}
	public String getIsDefinedBy() {
		return isDefinedBy;
	}
	public void setIsDefinedBy(String isDefinedBy) {
		this.isDefinedBy = isDefinedBy;
	}
	public List<String> getCreatorIds() {
		return creatorIds;
	}
	public void setCreatorIds(List<String> creatorIds) {
		this.creatorIds = creatorIds;
	}
	public Agent addCreatorId(String agentPrefURI, MongoCollection userCollection) {
		if(creatorIds==null)creatorIds = new ArrayList<String>();
		Agent agent = userCollection.findOne("{prefUri:#}", agentPrefURI).as(Agent.class);
		if(agent!=null){
			creatorIds.add(agent.getId());
			return agent;
		}
		else return null;
	}
	public void addContributorId(String agentPrefURI, MongoCollection userCollection) {
		if(contributorIds==null)contributorIds = new ArrayList<String>();
		Agent agent = userCollection.findOne("{prefUri:#}", agentPrefURI).as(Agent.class);
		if(agent!=null) contributorIds.add(agent.getId());
	}
	public void addPublisherId(String agentPrefURI, MongoCollection userCollection) {
		if(publisherIds==null)publisherIds = new ArrayList<String>();
		Agent agent = userCollection.findOne("{prefUri:#}", agentPrefURI).as(Agent.class);
		if(agent!=null) publisherIds.add(agent.getId());
	}
	

	public List<String> getContributorIds() {
		return contributorIds;
	}

	
	public List<VocabularyVersionWrapper> getVersions() {
		return versions;
	}
	
	public void addVersion(VocabularyVersionWrapper version) {
		if(versions==null)versions = new ArrayList<VocabularyVersionWrapper>();
		versions.add(version);
	}


	public void setVersions(List<VocabularyVersionWrapper> versions) {
		this.versions = versions;
	}


	public void setContributorIds(List<String> contributorIds) {
		this.contributorIds = contributorIds;
	}


	public List<String> getPublisherIds() {
		return publisherIds;
	}


	public void setPublisherIds(List<String> publisherIds) {
		this.publisherIds = publisherIds;
	}

	public List<Comment> getReviews() {
		return reviews;
	}
	public void setReviews(List<Comment> reviews) {
		this.reviews = reviews;
	}
	public void addReview(Comment comment){
		if(reviews==null)reviews = new ArrayList<Comment>();
		reviews.add(comment);
	}
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	public List<Dataset> getDatasets() {
		return datasets;
	}

	public void setDatasets(List<Dataset> datasets) {
		this.datasets = datasets;
	}
	public void addDataset(Dataset dataset) {
		if(datasets==null)datasets = new ArrayList<Dataset>();
		datasets.add(dataset);
	}
	
	public VocabularyVersionWrapper getLastVersion(){
		if(getVersions()!=null && getVersions().size()>0){
			Collections.sort(this.getVersions());
			return getVersions().get(0);
		}
		return null;
	}
}
