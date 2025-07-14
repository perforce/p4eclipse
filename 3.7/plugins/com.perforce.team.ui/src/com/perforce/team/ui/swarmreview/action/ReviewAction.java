/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.swarmreview.action;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.ui.p4java.actions.P4Action;
import com.perforce.team.ui.swarmreview.ReviewManager;

/**
 * If shelved file exists still can update review 1.if has review and files == 0
 * return false full stop even shelved exists (no new no update) 2.if has review
 * and files > 0 then (can update existing only no new) 3.if no review and files
 * == 0 and shelved ==0 return false (no new no update) 4.if no review and files
 * == 0 and shelved > 0 then return true for (can update and can new) 5.if no
 * review and files > 0 and shelved >= 0 then (can update can new)
 */
public abstract class ReviewAction<T extends IP4Changelist> extends P4Action {
	ReviewManager<T> controller;

	public ReviewAction() {
		super();
	}

	abstract void initSwarmController(T pendingChangeList, boolean clearState);

	abstract T getCurrentSelection();

	/**
	 * Returns true if selected change list has review or else false;
	 * 
	 * @param pendingChangeList
	 * @return
	 */
	boolean isMenuEnabled(T pendingChangeList) {
		return hasReview(pendingChangeList);
	}

	/**
	 * Returns true if change list has a review, or else false.
	 * 
	 * @return
	 */
	private boolean hasReview(final T pendingChangeList) {
		boolean enabled = false;
		if (pendingChangeList == null) {
			return false;
		}
		if (pendingChangeList != null) {
			initSwarmController(pendingChangeList, false);
			if (controller != null) {
				T selectedChangelIst = controller.getP4ChangelistById(pendingChangeList);
				enabled = selectedChangelIst.hasReview();
			}
		}
		return enabled;
	}
}
