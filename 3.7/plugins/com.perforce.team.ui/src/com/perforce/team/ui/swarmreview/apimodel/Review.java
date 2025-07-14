package com.perforce.team.ui.swarmreview.apimodel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Review {

	
	private Integer id;
	
	private String author;
	
	private List<Integer> changes = null;
	
	//private List<Object> comments;
	
	private List<Object> commits = null;
	
	private List<Object> commitStatus = null;
	
	private Integer created;
	
	private List<Object> deployDetails = null;
	
	private Object deployStatus;
	
	private String description;
	
	//private Participants participants;
	
	private Boolean pending;
	
	//private Projects projects = null;
	
	private String state;
	
	private String stateLabel;
	
	//private TestDetails testDetails = null;
	
	private Object testStatus;
	
	private String type;
	
	private Integer updated;
	
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	
	public Integer getId() {
		return id;
	}

	
	public void setId(Integer id) {
		this.id = id;
	}

	
	public String getAuthor() {
		return author;
	}

	
	public void setAuthor(String author) {
		this.author = author;
	}

	
	public List<Integer> getChanges() {
		return changes;
	}

	
	public void setChanges(List<Integer> changes) {
		this.changes = changes;
	}

	
//	public List<Object> getComments() {
//		return comments;
//	}
//
//	
//	public void setComments(List<Object> comments) {
//		this.comments = comments;
//	}

	
	public List<Object> getCommits() {
		return commits;
	}

	
	public void setCommits(List<Object> commits) {
		this.commits = commits;
	}

	
	public List<Object> getCommitStatus() {
		return commitStatus;
	}

	
	public void setCommitStatus(List<Object> commitStatus) {
		this.commitStatus = commitStatus;
	}

	
	public Integer getCreated() {
		return created;
	}

	
	public void setCreated(Integer created) {
		this.created = created;
	}

	
	public List<Object> getDeployDetails() {
		return deployDetails;
	}

	public void setDeployDetails(List<Object> deployDetails) {
		this.deployDetails = deployDetails;
	}

	
	public Object getDeployStatus() {
		return deployStatus;
	}

	
	public void setDeployStatus(Object deployStatus) {
		this.deployStatus = deployStatus;
	}

	
	public String getDescription() {
		return description;
	}

	
	public void setDescription(String description) {
		this.description = description;
	}

	
//	public Participants getParticipants() {
//		return participants;
//	}
//
//	
//	public void setParticipants(Participants participants) {
//		this.participants = participants;
//	}

	
	public Boolean getPending() {
		return pending;
	}


	public void setPending(Boolean pending) {
		this.pending = pending;
	}

	
//	public Projects getProjects() {
//		
//		return projects;
//	}
//
//	
//	public void setProjects(Projects projects) {
//		this.projects = projects;
//	}


	public String getState() {
		return state;
	}

	
	public void setState(String state) {
		this.state = state;
	}

	
	public String getStateLabel() {
		return stateLabel;
	}

	
	public void setStateLabel(String stateLabel) {
		this.stateLabel = stateLabel;
	}

	
//	public TestDetails getTestDetails() {
//		return testDetails;
//	}
//
//	
//	public void setTestDetails(TestDetails testDetails) {
//		this.testDetails = testDetails;
//	}

	
	public Object getTestStatus() {
		return testStatus;
	}

	
	public void setTestStatus(Object testStatus) {
		this.testStatus = testStatus;
	}

	
	public String getType() {
		return type;
	}

	
	public void setType(String type) {
		this.type = type;
	}

	
	public Integer getUpdated() {
		return updated;
	}

	
	public void setUpdated(Integer updated) {
		this.updated = updated;
	}

	
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}