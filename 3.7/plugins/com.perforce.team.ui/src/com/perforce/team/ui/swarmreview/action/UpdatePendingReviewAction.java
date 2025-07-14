/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.swarmreview.action;

import org.eclipse.core.runtime.IProgressMonitor;

import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.swarmreview.dialogs.UpdatePendingReviewDialog;
import com.perforce.team.ui.swarmreview.dialogs.UpdateReviewDialog;
import com.perforce.team.ui.swarmreview.restapi.Result;

public class UpdatePendingReviewAction extends PendingReviewAction {
	/**
	 * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
	 */
	@Override
	protected void runAction() {
		try {
			final IP4PendingChangelist pendingChangeList = ((IP4PendingChangelist) this.getSelection().toList().get(0));
			final IP4Resource[] selectedItems = pendingChangeList.getAllMembers();

			IP4Runnable runnable = new P4Runnable() {
				@Override
				public void run(IProgressMonitor monitor) {
					showNoOpenedFilesDialog(pendingChangeList);
				}
			};
			runRunnable(runnable);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void showNoOpenedFilesDialog(final IP4PendingChangelist pendingChangeList) {
		PerforceUIPlugin.syncExec(new Runnable() {
			public void run() {
				initSwarmController(pendingChangeList, true);
				UpdateReviewDialog<IP4PendingChangelist> usrd = new UpdatePendingReviewDialog(getShell());
				usrd.setController(controller);
				if (UpdateReviewDialog.OK != usrd.open()) {
					return;
				}
				if (usrd.isRevertChecked()) {
					revertFiles(usrd.getCheckedFiles(), usrd.isRemoveChecked());
				}
			}
		});
	}

	@Override
	public boolean isEnabledEx() {
		boolean enabled = false;
		IP4PendingChangelist pendingChangeList = this.getCurrentSelection();
		if (pendingChangeList != null) {
			initSwarmController(pendingChangeList, false);
			Result<Boolean> result = controller.isChangeListCandidateForUpdateReview(pendingChangeList);
			enabled = result.isSuccessful();
			if (!result.isSuccessful() && !result.isReportInSilence()) {
				// SwarmReviewUtil.setStatus(result.getMessage(), false);
			}
		}
		return enabled;
	}
}
