/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.swarmreview.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;

import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.ui.swarmreview.PendingReviewManager;

/**
 * If shelved file exists still can update review 1.if has review and files == 0
 * return false full stop even shelved exists (no new no update) 2.if has review
 * and files > 0 then (can update existing only no new)
 * 
 * 3.if no review and files == 0 and shelved ==0 return false (no new no update)
 * 4.if no review and files == 0 and shelved > 0 then return true for (can
 * update and can new) 5.if no review and files > 0 and shelved >= 0 then (can
 * update can new)
 */
public abstract class PendingReviewAction
        extends ReviewAction<IP4PendingChangelist> {

    public PendingReviewAction() {
        super();
    }

    void initSwarmController(IP4PendingChangelist pendingChangeList,
            boolean clearState) {
        if (pendingChangeList != null) {
            (controller = PendingReviewManager
                    .getInstance(pendingChangeList.getConnection()))
                            .init(pendingChangeList, clearState);
        }
    }

    /**
     * Returns null if this.getSelection() == null or this.getSelection().size()
     * == 0
     */
    public IP4PendingChangelist getCurrentSelection() {
        if (this.getSelection() == null || this.getSelection().size() == 0) {
            return null;
        }
        return ((IP4PendingChangelist) this.getSelection().toList().get(0));

    }

    protected void revertFiles(IP4Resource[] checkedFiles, boolean wipe) {
        List<IP4Resource> selectedFiles = new ArrayList<IP4Resource>();
        for (int i = 0; i < checkedFiles.length; i++) {
            if (checkedFiles[i] instanceof IP4Resource) {
                selectedFiles.add((IP4Resource) checkedFiles[i]);
            }
        }
        P4Collection collectionSelection = createCollection(
                selectedFiles.toArray(new IP4Resource[0]));
        collectionSelection.revert(wipe);

        collectionSelection.refreshLocalResources(IResource.DEPTH_INFINITE);
        collectionSelection.resetStateValidation();
        updateActionState();
    }
}
