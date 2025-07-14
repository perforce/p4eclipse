package com.perforce.team.ui.swarmreview;

import java.io.Serializable;

import com.perforce.p4java.core.IUserSummary;

/**
 * This class represents a ReviewerUser
 *
 */
public class ReviewerUser implements Serializable{

	private IUserSummary userSummary;
	private boolean required;
	private String email;
	private String login;
	
	public ReviewerUser(IUserSummary userSummary, boolean required) {
		this.userSummary = userSummary;
		email = userSummary.getEmail();
		login = userSummary.getLoginName();
		this.required = required;
	}
	
	public ReviewerUser(IUserSummary userSummary) {
		this(userSummary, false);
	}

	public IUserSummary getUserSummary() {
		return userSummary;
	}

	public boolean isRequired() {
		return required;
	}
		

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getLogin(){
		return login;
	}
	
	public String getFullName(){
		return userSummary.getFullName();
	}
	
	public String getEmail(){
		return userSummary.getEmail();
	}
	@Override
	public boolean equals(Object source){
		if(this == source){return true;}
		if(source == null){return false;}
		if((source.getClass() != ReviewerUser.class)){return false;}
		return ((ReviewerUser)source).login.equals(this.login);
	}
	
	public int hashcode(){
		return this.login.hashCode();
	}
}
