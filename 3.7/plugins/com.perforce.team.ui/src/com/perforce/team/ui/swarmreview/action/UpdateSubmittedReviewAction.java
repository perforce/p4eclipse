/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.swarmreview.action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;

import com.perforce.p4java.server.IServer;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.swarmreview.dialogs.UpdateReviewDialog;
import com.perforce.team.ui.swarmreview.dialogs.UpdateSubmittedReviewDialog;
import com.perforce.team.ui.swarmreview.restapi.Result;
import com.perforce.team.ui.swarmreview.util.SwarmReviewUtil;

public class UpdateSubmittedReviewAction extends SubmittedReviewAction {

	/**
	 * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
	 */
	@Override
	protected void runAction() {
		try {
			final IP4SubmittedChangelist pendingChangeList = ((IP4SubmittedChangelist) this.getSelection().toList()
					.get(0));
			final IP4Resource[] selectedItems = pendingChangeList.getFiles();
			IP4Runnable runnable = new P4Runnable() {
				@Override
				public void run(IProgressMonitor monitor) {
					if (selectedItems.length > 0) {
						IServer iserver = selectedItems[0].getServer();

						showNoOpenedFilesDialog(pendingChangeList, selectedItems, iserver);
					} else {
						MessageDialog.openConfirm(null, "No Changes", "No Changes Found");
					}
				}
			};
			runRunnable(runnable);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void showNoOpenedFilesDialog(final IP4SubmittedChangelist pendingChangeList,
			final IP4Resource[] selectedItems, final IServer iserver) {
		PerforceUIPlugin.syncExec(new Runnable() {
			public void run() {
				initSwarmController(pendingChangeList,true);
				UpdateReviewDialog<IP4SubmittedChangelist> usrd = new UpdateSubmittedReviewDialog(getShell());
				usrd.setController(controller);
				usrd.open();
			}
		});
	}

	@Override
	public boolean isEnabledEx() {
		boolean enabled = false;
		IP4SubmittedChangelist pendingChangeList = this.getCurrentSelection();
		if (pendingChangeList != null) {
			initSwarmController(pendingChangeList,false);
			Result<Boolean> result = controller.isChangeListCandidateForUpdateReview(pendingChangeList);
			enabled = result.isSuccessful();
			if (!result.isSuccessful() && !result.isReportInSilence()) {
				//SwarmReviewUtil.setStatus(result.getMessage(), false);
			}
		}
		return enabled;
	}
}
