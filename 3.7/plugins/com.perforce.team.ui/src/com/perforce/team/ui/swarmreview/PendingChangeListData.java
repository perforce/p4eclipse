package com.perforce.team.ui.swarmreview;

import java.util.Arrays;

import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;

public class PendingChangeListData {

	private IP4PendingChangelist pendingChangeList;
	private IP4ShelvedChangelist shelvedChangeList;
	private IP4Resource[] filesInPendingChangelist;
	private IP4Resource[] filesShelved;
	
	
	public PendingChangeListData(IP4PendingChangelist pendingChangeList, IP4ShelvedChangelist shelvedChangeList,
			IP4Resource[] filesInPendingChangelist, IP4Resource[] filesShelved) {
		super();
		this.pendingChangeList = pendingChangeList;
		this.filesInPendingChangelist = filesInPendingChangelist;
		this.shelvedChangeList = shelvedChangeList;
		this.filesShelved = filesShelved;
	}


	public IP4PendingChangelist getPendingChangeList() {
		return pendingChangeList;
	}


	public IP4Resource[] getFilesInPendingChangelist() {
		return filesInPendingChangelist;
	}
	
	public int getChangelistId(){
		return getPendingChangeList().getId();
	}
	
	
	
	public IP4Resource[] getFilesShelved() {
		return filesShelved;
	}


	public boolean hasShelvedFiles(){
		return filesShelved != null && filesShelved.length > 0;
	}


	public IP4ShelvedChangelist getShelvedChangeList() {
		return shelvedChangeList;
	}
	
	public boolean equals(Object obj){
		if(obj == null){
			return false;
		}
		if(obj instanceof PendingChangeListData){
			IP4Resource[] clientPending = ((PendingChangeListData)obj).filesInPendingChangelist;
			IP4Resource[] clientShelved = ((PendingChangeListData)obj).filesShelved;
			boolean pendingsame = Arrays.equals(filesInPendingChangelist, clientPending);
			boolean shelvedsame = Arrays.equals(filesShelved, clientShelved);
			return pendingsame && shelvedsame;
		}
		return false;
	}
	
	public int hashcode(){
		return shelvedChangeList.hashCode() + filesShelved.hashCode();
	}
	
}
