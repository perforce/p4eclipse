package com.perforce.team.ui.swarmreview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.osgi.service.prefs.BackingStoreException;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.impl.mapbased.server.cmd.ResultListBuilder;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.P4DefaultChangelist;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.core.p4java.P4PendingChangelist;
import com.perforce.team.core.p4java.P4ShelveFile;
import com.perforce.team.core.p4java.P4ShelvedChangelist;
import com.perforce.team.ui.swarmreview.restapi.Result;

/**
 * This class manages the functionality surrounding the P4 Code Review review process
 * This uses the P4 Code Review API to perform review related tasks as follows:
 * 
 * 1. To extract a review for known review id via (GET) -
 * http(s)://{URL}/api/v6/reviews/{changeIdInSwarm} - returns a single Review in
 * JSON format
 * 
 * 2. To extract a review for a given change in P4 Server via (GET) -
 * http(s)://{URL}/api/v6/reviews?change[]={changeListStr} - this will return an
 * array of Reviews in JSON format
 * 
 * 3. To extract a review for a given change(s) in P4 Server via (GET) (note appending
 * changes via &) -
 * http(s)://{URL}/api/v6/reviews?change[]={changeListStr}&change[]={changeListStr1}
 * - this will return an array of Reviews in JSON format
 * 
 * For more info please refer to
 * https://swarm.perforce.com/docs/api.endpoints.html
 * 
 */
public class PendingReviewManager extends ReviewManager<IP4PendingChangelist> {

	private static ReviewManager<IP4PendingChangelist> managerInstance = new PendingReviewManager();

	public static ReviewManager<IP4PendingChangelist> getInstance(IP4Connection connection) {
		managerInstance.connection = connection;
		// managerInstance.init(null);
		return managerInstance;
	}

	/**
	 * Initialises the manager Clears existing state
	 * 
	 * @param pendingChangeList
	 * @throws IOException
	 * @throws BackingStoreException
	 */
	public void init(IP4PendingChangelist pendingChangeList, boolean clearState) {
		if (this.model == null) {
			this.model = PendingReviewModel.getNewInstance();
		}
		if (clearState) {
			this.clearState();
		}
		initSelectedChangelist(pendingChangeList);
		if (this.connection == null && pendingChangeList != null) {
			this.connection = pendingChangeList.getConnection();
		}
		ignoreSSL = this.connection.getParameters().isIgnoreSSLValidation();
	}

	@Override
	public boolean updateChangelist() {
		boolean changeListModified = false;
		try {
			IChangelist cl = (IChangelist) getConnection().getServer()
					.getChangelist(model.getPendingChangeList().getId());
			IP4PendingChangelist pendingCl = new P4PendingChangelist(getConnection(), cl, true);
			changeListModified = cl.getFiles(true).size() != getFilesInSelectedChangelist().length;
			if (changeListModified) {
				List<IFileSpec> files = cl.getFiles(true);
				Iterator<IFileSpec> it = files.iterator();
				while (it.hasNext()) {
					IFileSpec fs = it.next();
					IP4File p4File = new P4File(fs, getConnection());
					pendingCl.addFile(p4File);
				}
				populateCurrentChangelistItems(pendingCl);
			}
		} catch (Exception ex) {

		}
		return changeListModified;
	}

	/**
	 * Stores the selected change list in memory
	 * 
	 * @param pendingChangeList
	 * @throws Exception
	 */
	void populateCurrentChangelistItems(IP4PendingChangelist pendingChangeList) {

		List<IP4Resource> ip4Changes = new ArrayList<IP4Resource>();
		if (pendingChangeList != null) {
			if (model.getPendingChangeList() != null
					&& model.getPendingChangeList().getId() == pendingChangeList.getId()) {
				return;
			}
			Object[] changeListData = pendingChangeList.getAllMembers();
			if (changeListData != null) {
				for (Object file : changeListData) {
					if (file instanceof IP4File) {
						ip4Changes.add((IP4Resource) file);
					}

					if (file instanceof P4ShelvedChangelist) {
						P4ShelvedChangelist shelvedChange = ((P4ShelvedChangelist) file);
						IP4Resource[] shelvedFiles = shelvedChange.getFiles();
						List<IP4Resource> shelvedFilesList = new ArrayList<IP4Resource>();
						if (shelvedFiles.length == 0) {
							//shelvedChange.refresh();
							shelvedFiles = shelvedFilesList.toArray(shelvedFiles);
						}
						model.setShelvedChangeList(shelvedChange);         
						model.setFilesInSelectedShelf(shelvedFiles);
					}
				}
			}
			if (model.getFilesInSelectedShelf() != null && model.getFilesInSelectedShelf().length > 0) {
				IP4Resource[] shelvedFiles = (IP4Resource[]) model.getFilesInSelectedShelf();
				for (IP4Resource res : shelvedFiles) {
					if (res instanceof IP4ShelveFile) {
						//ip4Changes.add(res);
					}
				}
			}
			IP4Resource[] ip4Array = ip4Changes.toArray(new IP4Resource[ip4Changes.size()]);
			model.setFilesInSelectedChangelist(ip4Array);
			model.setPendingChangeList(pendingChangeList);
		}
	}

	
	
	public void initSelectedChangelist(IP4PendingChangelist pendingChangeList) {
		if (pendingChangeList != null) {
			IP4PendingChangelist[] newcl = new IP4PendingChangelist[1];
			newcl[0] = pendingChangeList;
			populateChangelistData(newcl);
			model.addP4Changelist(pendingChangeList);
			PendingChangeListData pclData = model.getChangeListDataById(pendingChangeList);
			if(pclData != null){
				model.setFilesInSelectedChangelist(pclData.getFilesInPendingChangelist());
				model.setPendingChangeList(pclData.getPendingChangeList());
				model.setFilesInSelectedShelf(pclData.getFilesShelved());
				model.setShelvedChangeList(pclData.getShelvedChangeList()); 
			}
		}
	}

	public void populateChangelistData(IP4PendingChangelist[] pendingChangeListItems) {
		for (IP4PendingChangelist cl : pendingChangeListItems) {
			populateCurrentChangelistItems(cl);
			PendingChangeListData clData = new PendingChangeListData(model.getPendingChangeList(), cl.getShelvedChanges(),
					 model.getFilesInSelectedChangelist(), (IP4Resource[]) model.getFilesInSelectedShelf());
			model.addChangeListData(clData);
		}
	}
	
	public IP4PendingChangelist getP4ChangelistById(IP4PendingChangelist cl) {
		IP4PendingChangelist selectedCl = model.getP4ChangeListIdToChangeListMap().get(cl.getId() + "@" + cl.getClientName());
		if(selectedCl == null){
			IP4PendingChangelist[] newcl = new IP4PendingChangelist[1];
			newcl[0] = cl;
			populateChangelistData(newcl);
			model.addP4Changelist(cl);
		}
		return model.getP4ChangeListIdToChangeListMap().get(cl.getId() + "@" + cl.getClientName());
	}

	public void createCache(final IP4PendingChangelist[] lists) {
		super.createCache(lists);
		populateChangelistData(lists);
	}

	/**
	 * Returns true if change list is candidate for a new review
	 * 
	 * @param currentSelection
	 * @return
	 */
	public Result<Boolean> isChangeListCandidateForNewReview(IP4PendingChangelist currentSelection) {
		IP4PendingChangelist pendingChangeList = currentSelection;
		Result<Boolean> canContinue = canContinue();
		if (!canContinue.isSuccessful()) {
			return new Result<Boolean>(canContinue, canContinue.isSuccessful());
		}
		if (pendingChangeList == null) {
			return new Result<Boolean>(false).withNoSuccess("New Review not supported, Empty changelist",
					Boolean.FALSE);
		}
		int validContentCount = getFilesInSelectedChangelist().length;
		int validShelfCount = getFilesInSelectedShelf().length;

		if (pendingChangeList instanceof P4DefaultChangelist && validContentCount > 0) {
			return new Result<Boolean>(true).withSuccess("New Review supported", Boolean.TRUE);
		}
		boolean hasReview = isChangelistInReview(pendingChangeList);
		if (hasReview) {
			return new Result<Boolean>(false).withNoSuccess("New Review not supported. Already in review",
					Boolean.FALSE);
		} else if ((validContentCount == 0) && (validShelfCount == 0)) {
			//return new Result<Boolean>(false).withNoSuccess("New Review not supported. Empty content", Boolean.FALSE);
		}
		boolean finalResult = !hasReview;
		return new Result<Boolean>(finalResult).withValues(finalResult, false,
				"New Review support status: " + finalResult, false, Boolean.valueOf(finalResult));
	}

	/**
	 * Returns true if change list is candidate for a review update
	 * 
	 * @param currentSelection
	 * @return
	 */
	public Result<Boolean> isChangeListCandidateForUpdateReview(IP4PendingChangelist currentSelection) {
		final IP4PendingChangelist pendingChangeList = currentSelection;
		Result<Boolean> canContinue = canContinue();
		if (!canContinue.isSuccessful()) {
			return new Result<Boolean>(canContinue, canContinue.isSuccessful());
		}
		if (pendingChangeList == null) {
			return new Result<Boolean>(false).withNoSuccess("Update not supported, Empty changelist", Boolean.FALSE);
		}
		int validContentCount = getPendingFilesCount();
		int shelvedFiles = getFilesInSelectedShelf() == null ? 0 : getFilesInSelectedShelf().length;

		if (pendingChangeList instanceof P4DefaultChangelist && validContentCount > 0) {
			return new Result<Boolean>(true).withSuccess("Update supported", Boolean.TRUE);
		}
		boolean hasReview = isChangelistInReview(pendingChangeList);
		if (hasReview) {
			if (validContentCount > 0) {
				return new Result<Boolean>(true).withSuccess("Update supported", Boolean.TRUE);
			}
		}
		boolean finalResult = !hasReview;// && (validContentCount > 0 || shelvedFiles > 0);
		return new Result<Boolean>(finalResult).withValues(finalResult, false,
				"Update Review support status: " + finalResult, false, Boolean.valueOf(finalResult));
	}

	@Override
	public Result<Boolean> isOpenChange(IP4PendingChangelist currentSelection) {
		Result<Boolean> canContinue = canContinue();
		if (!canContinue.isSuccessful()) {
			return new Result<Boolean>(canContinue, canContinue.isSuccessful());
		}
		if (currentSelection instanceof P4DefaultChangelist) {
			return new Result<Boolean>(false).withNoSuccess("Not supported, Default changelist", Boolean.FALSE);
		}
		return new Result<Boolean>(true).withSuccess("Open change supported", Boolean.TRUE);
	}

	@Override
	public Result<Boolean> isOpenReview(IP4PendingChangelist currentSelection) {
		IP4PendingChangelist pendingChangeList = currentSelection;
		Result<Boolean> canContinue = canContinue();
		if (!canContinue.isSuccessful()) {
			return new Result<Boolean>(canContinue, canContinue.isSuccessful());
		}
		if (pendingChangeList instanceof P4DefaultChangelist) {
			return new Result<Boolean>(false).withNoSuccess("Not supported, Default changelist", Boolean.FALSE);
		}
		boolean finalResult = isChangelistInReview(pendingChangeList);

		return new Result<Boolean>(finalResult).withValues(finalResult, false,
				"Open Review support status: " + finalResult, false, Boolean.valueOf(finalResult));
	}
}
