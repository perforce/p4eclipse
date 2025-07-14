package com.perforce.team.ui.swarmreview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;

/**
 * The model for facilitating a P4 Code Review review
 * 
 */
public abstract class ReviewModel<T extends IP4Changelist> {
	/**
	 * List of files contained in the selected change list view
	 */
	private IP4Resource[] filesInSelectedChangelist;
	
	private IP4Resource[] filesInSelectedShelf;
	/**
	 * All possible users that can be selected as a reviewer
	 */
	private ReviewerUserList reviewerUsers;
	/**
	 * The list of users chosen to be reviewers
	 */
	private ReviewerUserList selectedReviewerUsers = new ReviewerUserList();
	
	private List<T> allP4ChangeLists = new ArrayList<T>();
	/**
	 * 
	 */
	private Object[] selectedChangelistData;
	/**
	 * Review description
	 */
	private String description;
	/**
	 * The pending changelist instance
	 */
	protected T changeList;
	
	protected IP4ShelvedChangelist shelvedChangeList;
	/**
	 * Changes attached to a review created in swarm
	 */
	private Map<Integer, Integer> changeIdToSwarmReviewIdMap = new HashMap<Integer, Integer>();
	private List<Integer> changeIdsLinkedToSwarm = new ArrayList<Integer>();
	private Map<String, T> p4ChangeListIdToChangeListMap = new HashMap<String, T>();
	private Map<String, PendingChangeListData> changeListDataMap = new HashMap<String, PendingChangeListData>();
	
	private Map<Integer, ReviewerUserList> changeListIdToReviewerUserMap = new HashMap<Integer, ReviewerUserList>();
	
	private Integer searchedSwarmReviewId;
	/**
	 * 
	 */
	private Integer swarmSpecificChangelistId;
	
	ReviewModel(){
		
	}
	
	public abstract T getPendingChangeList();
	public abstract void setPendingChangeList(T pendingChangeList);
	
	
	public Map<String, PendingChangeListData> getChangeListDataMap(){
		return changeListDataMap;
	}
	
	public void addChangeListData(PendingChangeListData cld){
		changeListDataMap.put(String.valueOf(cld.getChangelistId()) + "@" + cld.getPendingChangeList().getClientName(), cld);
	}
	
	public PendingChangeListData getChangeListDataById(IP4PendingChangelist cld){
		return changeListDataMap.get(String.valueOf(cld.getId()) + "@" + cld.getClientName());
	}
	
	public void setShelvedChangeList(IP4ShelvedChangelist shelvedChangeList){
		this.shelvedChangeList = shelvedChangeList;
	}
	
	public IP4ShelvedChangelist getShelvedChangeList(){
		return shelvedChangeList;
	}
	
	public void setChangeListIdToReviewerUserMap(Map<Integer, ReviewerUserList> rMap){
		this.changeListIdToReviewerUserMap = rMap;
	}
	
	public Map<Integer, ReviewerUserList> getChangeListIdToReviewerUserMap(){
		return this.changeListIdToReviewerUserMap;
	}

	public List<T> getAllP4ChangeLists() {
		return allP4ChangeLists;
	}

	public void setAllP4ChangeLists(List<T> allChangeLists) {
		this.allP4ChangeLists = allChangeLists;
	}
	
	

	public Map<String, T> getP4ChangeListIdToChangeListMap() {
		return p4ChangeListIdToChangeListMap;
	}

	public void setP4ChangeListIdToChangeListMap(Map<String, T> changeListIdToChangeListMap) {
		this.p4ChangeListIdToChangeListMap = changeListIdToChangeListMap;
	}
	
	public void addP4Changelist(T changeList){
		p4ChangeListIdToChangeListMap.put(String.valueOf(changeList.getId()) + "@" + changeList.getClientName(), changeList);
	}

	/**
	 * Maintains a map of change ids to P4 Code Review ids
	 * @param swarmId
	 * @param id
	 */
	public void addChangeIdsLinkedToSwarm(Integer swarmId, List<Integer> changes){
		changeIdsLinkedToSwarm.addAll(changes);
		if(changes != null){
			for(Integer changeid : changes){
				changeIdToSwarmReviewIdMap.put(changeid, swarmId);
			}
		}
	}
	
	public void clearSwarmLinkedChangeCache(){
		changeIdsLinkedToSwarm.clear();
		changeIdToSwarmReviewIdMap.clear();
	}
	
	public Integer getSwarmReviewIdFor(Integer pendingChangeId){
		return changeIdToSwarmReviewIdMap.get(pendingChangeId);
	}
	/**
	 * Whether the given change(id) is in review
	 * @param changeId
	 * @return
	 */
	public boolean isChangeInReview(Integer changeId){
		return changeIdsLinkedToSwarm.contains(changeId);
	}
	/**
	 * Returns all the change list ids that has a corresponding
	 * review
	 * @return
	 */
	public List<Integer> getChangeListIdsLinkedToSwarm(){
		return changeIdsLinkedToSwarm;
	}
	
	public IP4Resource[] getFilesInSelectedChangelist() {
		return filesInSelectedChangelist;
	}
	public void setFilesInSelectedChangelist(IP4Resource[] changelistData) {
		this.filesInSelectedChangelist = changelistData;
	}
	
	
	public IP4Resource[] getFilesInSelectedShelf() {
		return filesInSelectedShelf;
	}
	public void setFilesInSelectedShelf(IP4Resource[] changelistData) {
		this.filesInSelectedShelf = changelistData;
	}
	
	
	public void setReviewerUsers(ReviewerUserList reviewerUserData) {
		this.reviewerUsers = reviewerUserData;
	}
	public ReviewerUserList getReviewerUsers() {
		return reviewerUsers;
	}
	public ReviewerUserList getSelectedReviewerUserList() {
		return selectedReviewerUsers;
	}

	public void setSelectedReviewerUserList(ReviewerUserList selectedReviewerUser) {
		this.selectedReviewerUsers = selectedReviewerUser;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	public Object[] getSelectedChangelistData() {
		return selectedChangelistData;
	}

	public void setSelectedChangelistData(Object[] selectedChangelistData) {
		this.selectedChangelistData = selectedChangelistData;
	}

//	public T getPendingChangeList() {
//		return changeList;
//	}
//
//	public void setPendingChangeList(T pendingChangeList) {
//		this.changeList = pendingChangeList;
//	}

	public Integer getSwarmSpecificChangelist() {
		return swarmSpecificChangelistId;
	}

	public void setSwarmSpecificChangelist(Integer updateReviewChangelist) {
		this.swarmSpecificChangelistId = updateReviewChangelist;
	}

	public Integer getSearchedSwarmReviewId() {
		return searchedSwarmReviewId;
	}

	public void setSearchedSwarmReviewId(Integer searchedSwarmReviewId) {
		this.searchedSwarmReviewId = searchedSwarmReviewId;
	}
}
