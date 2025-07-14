package com.perforce.team.ui.swarmreview;

import com.perforce.team.core.p4java.IP4PendingChangelist;

/**
 * The model for facilitating a P4 Code Review review
 * 
 */
public class PendingReviewModel extends ReviewModel<IP4PendingChangelist> {
	
	
	private PendingReviewModel(){
	}
	
	public static PendingReviewModel getNewInstance(){
		return new PendingReviewModel();
	}

	@Override
	public IP4PendingChangelist getPendingChangeList() {
		return changeList;
	}

	@Override
	public void setPendingChangeList(IP4PendingChangelist changeList) {
		this.changeList = changeList;
	}
}
