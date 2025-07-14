package com.perforce.team.ui.swarmreview;

import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;



public class SubmittedReviewModel extends ReviewModel<IP4SubmittedChangelist> {
	
	
	private SubmittedReviewModel(){
	}
	
	public static SubmittedReviewModel getNewInstance(){
		return new SubmittedReviewModel();
	}
	
	@Override
	public IP4SubmittedChangelist getPendingChangeList() {
		return changeList;
	}

	@Override
	public void setPendingChangeList(IP4SubmittedChangelist changeList) {
		this.changeList = changeList;
	}
}
