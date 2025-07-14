package com.perforce.team.ui.swarmreview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.prefs.BackingStoreException;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4SubmittedFile;
import com.perforce.team.ui.swarmreview.restapi.APIClient;
import com.perforce.team.ui.swarmreview.restapi.APIException;
import com.perforce.team.ui.swarmreview.restapi.AuthScheme;
import com.perforce.team.ui.swarmreview.restapi.JsonString;
import com.perforce.team.ui.swarmreview.restapi.Response;
import com.perforce.team.ui.swarmreview.restapi.Result;
import com.perforce.team.ui.views.SubmittedView;

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
public class SubmittedReviewManager extends ReviewManager<IP4SubmittedChangelist> {

	private static ReviewManager<IP4SubmittedChangelist> managerInstance = new SubmittedReviewManager();

	public static ReviewManager<IP4SubmittedChangelist> getInstance(IP4Connection connection) {
		managerInstance.connection = connection;
		return managerInstance;
	}

	/**
	 * Initialises the manager Clears existing state
	 * 
	 * @param pendingChangeList
	 * @throws IOException
	 * @throws BackingStoreException
	 */
	public void init(IP4SubmittedChangelist pendingChangeList, boolean clearState) {
		if (this.model == null) {
			this.model = SubmittedReviewModel.getNewInstance();
		}
		this.clearState();
		populateCurrentChangelistItems(pendingChangeList);
		if (this.connection == null && pendingChangeList != null) {
			this.connection = pendingChangeList.getConnection();
		}
		ignoreSSL = this.connection.getParameters().isIgnoreSSLValidation();
	}

	/**
	 * Stores the selected change list in memory
	 * 
	 * @param pendingChangeList
	 */
	void populateCurrentChangelistItems(IP4SubmittedChangelist pendingChangeList) {
		List<P4SubmittedFile> ip4Changes = new ArrayList<P4SubmittedFile>();
		if (pendingChangeList != null) {
			Object[] changeListData = pendingChangeList.getFiles();
			if (changeListData != null) {
				for (Object file : changeListData) {
					if (file instanceof P4SubmittedFile) {
						ip4Changes.add((P4SubmittedFile) file);
					}
				}
			}
			P4SubmittedFile[] ip4Array = ip4Changes.toArray(new P4SubmittedFile[ip4Changes.size()]);
			model.setFilesInSelectedChangelist(ip4Array);
			model.setPendingChangeList(pendingChangeList);
		}
	}

	public Result<Boolean> requestReview(Object[] checkedFiles, String description) {
		
		IChangelist changelist = null;
		boolean isNewReview = true;

		Result<Boolean> result = new Result<Boolean>(true, "");
		try {
			result = canWeConnectToSwarm();
			if (!result.isSuccessful()) {
				return result;
			}
			model.setSelectedChangelistData(checkedFiles);
			{
				changelist = model.getPendingChangeList().getChangelist();
				String path = SWARM_API_PATH + "/";
				String param = "change=" + changelist.getId();
				ReviewerUserList reviewers = model.getSelectedReviewerUserList();

				String reviewerParam = "";
				String requiredReviewerParam = "";

				for (String reviewer : reviewers.getReviewersNotRequired()) {
					reviewerParam += "reviewers[]=" + reviewer + "&";
				}
				for (String reviewer : reviewers.getRequiredReviewers()) {
					requiredReviewerParam += "requiredReviewers[]=" + reviewer + "&";
				}
				if (isNewReview) {
					Integer searchedReviewId = null;
					// to update existing P4 Code Review review with a change note how
					// the path format changes from above
					if ((searchedReviewId = model.getSearchedSwarmReviewId()) != null) {
						path += searchedReviewId + "/changes/";
					} else {
						if(!reviewerParam.isEmpty()){
							param += "&" + reviewerParam;
						}
						if(!requiredReviewerParam.isEmpty()){
							param += "&" + requiredReviewerParam;
						}
					}
					Result<Response<JsonString>> apiResult = callAPI("POST", path, param, null);
					if (!apiResult.isSuccessful()) {
						return result.withNoSuccess("Unable to create review: " + apiResult.getMessage(),
								apiResult.isError(), false);
					}
					Response<JsonString> response = apiResult.getResultData();

					boolean ok = response.isSuccessful();
					if (ok) {
						result.withSuccess("Review created", Boolean.TRUE);
					} else {
						result.withNoSuccess("Unable to create review: " + response.getResponseMessage(), false, false);
					}
				} else {
					result.withSuccess("Review updated", Boolean.TRUE);
				}
			}
		} catch (APIException ex) {
			result.withNoSuccess(ex.getMessage(), true, false);
		} catch (Exception ex) {
			Exception e = new APIException("Unknown error",ex, -1);
			result.withNoSuccess(e.getMessage(), true, false);
		} finally {
			clearState();
		}
		return result;
	}

	@Override
	public boolean updateChangelist() {
		return false;
		
	}

	@Override
	public Result<Boolean> isChangeListCandidateForNewReview(IP4SubmittedChangelist currentSelection) {
		IP4SubmittedChangelist pendingChangeList = currentSelection;
		Result<Boolean> canContinue = canContinue();
		if (!canContinue.isSuccessful()) {
			return new Result<Boolean>(canContinue, canContinue.isSuccessful());
		}
    	boolean hasReview = !isChangelistInReview(pendingChangeList);
    	return new Result<Boolean>(hasReview).withValues(hasReview, false, "New Review support status: " + hasReview, false, Boolean.valueOf(hasReview));
	}

	@Override
	public Result<Boolean> isChangeListCandidateForUpdateReview(IP4SubmittedChangelist currentSelection) {
		IP4SubmittedChangelist pendingChangeList = currentSelection;
		Result<Boolean> canContinue = canContinue();
		if (!canContinue.isSuccessful()) {
			return new Result<Boolean>(canContinue, canContinue.isSuccessful());
		}
    	boolean hasReview = !isChangelistInReview(pendingChangeList);
    	return new Result<Boolean>(hasReview).withValues(hasReview, false, "Upate Review support status: " + hasReview, false, Boolean.valueOf(hasReview));
	}

	@Override
	public Result<Boolean> isOpenChange(IP4SubmittedChangelist currentSelection) {
		Result<Boolean> canContinue = canContinue();
		if (!canContinue.isSuccessful()) {
			return new Result<Boolean>(canContinue, canContinue.isSuccessful());
		}
		return new Result<Boolean>(true).withValues(true, false, "Open change supported", false, Boolean.TRUE);
	}

	@Override
	public Result<Boolean> isOpenReview(IP4SubmittedChangelist currentSelection) {
		IP4SubmittedChangelist pendingChangeList = currentSelection;
		Result<Boolean> canContinue = canContinue();
		if (!canContinue.isSuccessful()) {
			return new Result<Boolean>(canContinue, canContinue.isSuccessful());
		}
    	boolean finalResult = isChangelistInReview(pendingChangeList);
    	return new Result<Boolean>(finalResult).withValues(finalResult, false, "Open review support status: " + finalResult, false, Boolean.valueOf(finalResult));
	}
}
