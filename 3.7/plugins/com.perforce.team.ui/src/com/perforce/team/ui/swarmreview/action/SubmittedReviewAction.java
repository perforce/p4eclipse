/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.swarmreview.action;

import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.ui.swarmreview.SubmittedReviewManager;

public abstract class SubmittedReviewAction extends ReviewAction<IP4SubmittedChangelist> {

	public SubmittedReviewAction() {
		super();
	}

	/**
	 * Returns null if this.getSelection() == null or this.getSelection().size()
	 * == 0
	 */
	public IP4SubmittedChangelist getCurrentSelection() {
		if (this.getSelection() == null || this.getSelection().size() == 0) {
			return null;
		}
		return ((IP4SubmittedChangelist) this.getSelection().toList().get(0));

	}

	void initSwarmController(IP4SubmittedChangelist pendingChangeList, boolean clearState) {
		if (pendingChangeList != null) {
			(controller = SubmittedReviewManager.getInstance(pendingChangeList.getConnection()))
					.init(pendingChangeList, clearState);
		}
	}
}
