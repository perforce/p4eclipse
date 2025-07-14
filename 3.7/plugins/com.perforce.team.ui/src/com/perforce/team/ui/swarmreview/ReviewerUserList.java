package com.perforce.team.ui.swarmreview;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.perforce.p4java.core.IUserSummary;

/**
 * This class maintains a list of ReviwerUsers and exposes some
 * common List operations
 */

public class ReviewerUserList implements Serializable{

	/**
	 * List of Reviewers in this listr
	 */
	private List<ReviewerUser> users = new ArrayList<ReviewerUser>();
	
	/**
	 * Constructs a ReviewerUserList
	 */
	public ReviewerUserList() {
		
	}
	
	/**
	 * Constructs a ReviewerUserList with given array of ReviewerUser(s)
	 * @param users
	 */
	public ReviewerUserList(ReviewerUser... users) {
		this.users.addAll(new ArrayList<ReviewerUser>(Arrays.asList(users)));
	}
	
	public ReviewerUserList(IUserSummary... users) {
		if(users != null && users.length>0){
			for(IUserSummary user : users){
				ReviewerUser ruser = new ReviewerUser(user);
				addReviewer(ruser);
			}
		}
	}
	/**
	 * Constructs a ReviewerUserList with given List of IUserSummary
	 * @param users
	 */
	public ReviewerUserList(List<IUserSummary> users) {
		if(users != null && users.size()>0){
			for(IUserSummary user : users){
				ReviewerUser ruser = new ReviewerUser(user);
				addReviewer(ruser);
			}
		}
	}
	/**
	 * Returns an iterator of the list of users held here
	 * @return
	 */
	public Iterator<ReviewerUser> iterator(){
		return this.users.iterator();
	}
	/**
	 * Adds a Reviewer user
	 * @param reviwer
	 */
	public void addReviewer(ReviewerUser reviwer){
		this.users.add(reviwer);
	}
	/**
	 * Add all Reviewer users in the passed reviewer
	 * @param reviewer
	 */
	public void addReviewers(ReviewerUserList reviewer){
		this.users.addAll(reviewer.users);
	}
	/**
	 * Removes all RevierwerUsers in the given ReviewerUserList 
	 * @param reviewer
	 */
	public void removeReviewers(ReviewerUserList reviewer){
		this.users.removeAll(reviewer.users);
	}
	/**
	 * Removes a single ReviewerUser
	 * @param reviewer
	 */
	public void removeReviewer(ReviewerUser reviewer){
		this.users.remove(reviewer);
	}
	/**
	 * Removes ReviewerUser that exists on the goven index
	 * @param reviewerIndex
	 */
	public void removeReviewer(int reviewerIndex){
		this.users.remove(reviewerIndex);
	}
	/**
	 * `clears this list
	 */
	public void clearList(){
		this.users.clear();
	}

	/**
	 * Returns number if ReviewerUser(s) in this list
	 * @return
	 */
	public int getSize(){
		return this.users.size();
	}
	
	public boolean isEmpty(){
		return this.users.isEmpty();
	}
	/**
	 * Converts the contents in to an array
	 * @return
	 */
	public ReviewerUser[] toArray(){
		ReviewerUser [] ruArray = new ReviewerUser[this.users.size()];
		ruArray = this.users.toArray(ruArray);
		return ruArray;
	}
	
	public List<String> getRequiredReviewers(){
		List<String> reuiredUsers = new ArrayList<String>();
		Iterator<ReviewerUser> it = iterator();
		while(it.hasNext()){
			ReviewerUser user = it.next();
			if(user.isRequired()){
				reuiredUsers.add(user.getLogin());
			}
		}
		return reuiredUsers;
	}
	
	public List<String> getReviewersNotRequired(){
		List<String> notReuiredUsers = new ArrayList<String>();
		Iterator<ReviewerUser> it = iterator();
		while(it.hasNext()){
			ReviewerUser user = it.next();
			if(!user.isRequired()){
				notReuiredUsers.add(user.getLogin());
			}
		}
		return notReuiredUsers;
	}
	/**
	 * Adds only unique ReviewerUser(s) in the given reviewers list
	 * who does not exist in this list
	 * @param reviewers
	 */
	public void addUnique(ReviewerUserList reviewers){
		for(ReviewerUser ruser : reviewers.users ){
			boolean found = false;
			for(ReviewerUser newuser : this.users){
				if(newuser.equals(ruser)){
					found = true;
					break;
				}
			}
			if(!found){
				users.add(ruser);
			}
		}
	}
}
