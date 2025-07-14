/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.swarmreview.action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;

import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.swarmreview.restapi.Result;
import com.perforce.team.ui.swarmreview.util.SwarmReviewUtil;

public class OpenPendingChangeOnlineAction extends PendingReviewAction {

	public OpenPendingChangeOnlineAction() {
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
					if (selectedItems.length > 0) {
						showOpenedBrowser(pendingChangeList);
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

	private void showOpenedBrowser(final IP4PendingChangelist pendingChangeList) {
		PerforceUIPlugin.syncExec(new Runnable() {
			public void run() {
				initSwarmController(pendingChangeList, true);
				try {
					controller.openSwarmChangeOnline();
				} catch (Exception e) {
					MessageDialog.openError(null, "Open Swarm", "Unable to open");
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
			Result<Boolean> result = controller.isOpenChange(pendingChangeList);
			enabled = result.isSuccessful();
			if (!result.isSuccessful() && !result.isReportInSilence()) {
				//SwarmReviewUtil.setStatus(result.getMessage(), false);
			}
		}
		return enabled;
	}
}
