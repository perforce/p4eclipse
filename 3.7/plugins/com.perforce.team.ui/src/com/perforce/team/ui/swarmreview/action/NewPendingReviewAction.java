/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.swarmreview.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;

import com.perforce.p4java.server.IServer;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.swarmreview.dialogs.NewPendingReviewDialog;
import com.perforce.team.ui.swarmreview.dialogs.NewReviewDialog;
import com.perforce.team.ui.swarmreview.restapi.Result;
import com.perforce.team.ui.swarmreview.util.SwarmReviewUtil;

/**
 * If shelved file exists still can update review 1.if has review and files == 0
 * return false full stop even shelved exists (no new no update)
 * 
 * 2.if has review and files > 0 then (can update existing only no new) 3.if no
 * review and files == 0 and shelved ==0 return false (no new no update)
 * 
 * 4.if no review and files == 0 and shelved > 0 then return true (can update
 * and can new) 5.if no review and files > 0 and shelved >= 0 then (can update
 * can new)
 */
public class NewPendingReviewAction extends PendingReviewAction {

	public NewPendingReviewAction() {
		super();
	}

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
					showNoOpenedFilesDialog(pendingChangeList, selectedItems, null);
				}
			};
			runRunnable(runnable);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void showNoOpenedFilesDialog(final IP4PendingChangelist pendingChangeList,
			final IP4Resource[] selectedItems, final IServer iserver) {
		PerforceUIPlugin.syncExec(new Runnable() {

			public void run() {
				initSwarmController(pendingChangeList, true);
				NewReviewDialog<IP4PendingChangelist> srd = new NewPendingReviewDialog(getShell());
				srd.setController(controller);
				if (NewReviewDialog.OK != srd.open()) {
					return;
				}
				if (srd.isRevertChecked()) {
					revertFiles(srd.getCheckedFiles(), srd.isRemoveChecked());
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
			Result<Boolean> result = controller.isChangeListCandidateForNewReview(pendingChangeList);
			enabled = result.isSuccessful();
			if (!result.isSuccessful() && !result.isReportInSilence()) {
				// SwarmReviewUtil.setStatus(result.getMessage(), false);
			}
		}
		return enabled;
	}
}
