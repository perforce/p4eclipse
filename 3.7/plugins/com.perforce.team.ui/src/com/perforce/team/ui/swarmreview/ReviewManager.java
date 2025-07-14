package com.perforce.team.ui.swarmreview;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.gson.Gson;
import com.perforce.p4java.admin.IProperty;
import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IUserSummary;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.p4java.impl.mapbased.server.Server;
import com.perforce.p4java.option.server.GetPropertyOptions;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;
import com.perforce.team.ui.LogListener;
import com.perforce.team.ui.swarmreview.apimodel.Review;
import com.perforce.team.ui.swarmreview.apimodel.SwarmAPIVersions;
import com.perforce.team.ui.swarmreview.apimodel.SwarmReview;
import com.perforce.team.ui.swarmreview.apimodel.SwarmReviewList;
import com.perforce.team.ui.swarmreview.restapi.APIClient;
import com.perforce.team.ui.swarmreview.restapi.APIException;
import com.perforce.team.ui.swarmreview.restapi.AuthScheme;
import com.perforce.team.ui.swarmreview.restapi.JsonString;
import com.perforce.team.ui.swarmreview.restapi.Response;
import com.perforce.team.ui.swarmreview.restapi.Result;
import com.perforce.team.ui.swarmreview.util.SwarmReviewUtil;
import com.perforce.team.ui.views.PendingView;

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
public abstract class ReviewManager<T extends IP4Changelist> {

	/**
	 * Model representing a P4 Code Review review
	 */
	private static Properties eclipseExternalProps = new Properties();
	private static String VERSION = "9";
	private String AUTH_TOKEN;
	String SWARM_URL;
	protected IP4Connection connection;
	boolean canConnectToSwarm;
	Result<SwarmAPIVersions> swarmVersionResult;
	private LogListener logListener = new LogListener();

	static {
		String props = System.getProperty("user.home");
		try {
			eclipseExternalProps.load(new FileInputStream(new File(props, "/p4eclipse/p4eclipse.properties")));
			if (!eclipseExternalProps.isEmpty()) {
				VERSION = eclipseExternalProps.getProperty("swarm.version");
			}
		} catch (Exception e) {
			// ignore if file does not exist
		}
	}

	final String SWARM_API_PATH = "/api/v" + VERSION + "/reviews";
	boolean ignoreSSL;
	ReviewModel<T> model;

	/**
	 * Constructs a P4 Code Review review manager
	 */
	ReviewManager() {
		logListener.init();
	}

	
	public abstract boolean updateChangelist();
	public abstract Result<Boolean> isChangeListCandidateForNewReview(T currentSelection);

	public abstract Result<Boolean> isChangeListCandidateForUpdateReview(T currentSelection);

	public abstract Result<Boolean> isOpenChange(T currentSelection);

	public abstract Result<Boolean> isOpenReview(T currentSelection);
	public abstract void init(T pendingChangeList, boolean clearState);
	abstract void populateCurrentChangelistItems(T pendingChangeList);
  
	public String getChangelistDescription(){
		return model.getPendingChangeList().getDescription();
	}
	

	public IP4Connection getConnection() {
		return connection;
	}

	public String getExternalProperty(String key) {
		return eclipseExternalProps.getProperty(key);
	}

	public void log(String msg) {
		logListener.info(0, msg);
	}
	
	public Object[] getFilesInSelectedChangelist() {
		return model.getFilesInSelectedChangelist() == null ? new Object[0] : model.getFilesInSelectedChangelist();
	}
	
	public Object[] getReviewFiles(){
		if(model.getFilesInSelectedChangelist() == null || model.getFilesInSelectedChangelist().length == 0){
			updatePendingFiles();
		}
		return model.getFilesInSelectedChangelist() == null ? new Object[0] : model.getFilesInSelectedChangelist();
	}
	
	
	public Object[] getFilesInSelectedShelf() {
		return model.getFilesInSelectedShelf() == null ? new Object[0] : model.getFilesInSelectedShelf();
	}
	
	public void clearSelectedChangelistData() {
		model.setSelectedChangelistData(new Object[0]);
	}

	public List<Integer> getChangeListIdsLinkedToSwarm() {
		return model.getChangeListIdsLinkedToSwarm();
	}

	public Integer getSwarmReviewIdFor(Integer changeListId) {
		return model.getSwarmReviewIdFor(changeListId);
	}

	public ReviewerUserList getSelectedReviewerUsers() {
		return model.getSelectedReviewerUserList();
	}
	
	public Object[] getShelvedReviewFiles(){
		if(model.getShelvedChangeList() == null || model.getFilesInSelectedShelf().length == 0){
			updatePendingFiles();
		}
		return model.getFilesInSelectedShelf() == null ? new Object[0] : model.getFilesInSelectedShelf();
	}

	private void updatePendingFiles() {
		if (model.getShelvedChangeList() != null) {
			IP4ShelvedChangelist shelvedChange = model.getShelvedChangeList();
			IP4Resource[] shelvedFiles = shelvedChange.getFiles();
			if (shelvedFiles.length == 0) {
				shelvedChange.refresh();
				shelvedFiles = shelvedChange.getFiles();
			}
			model.setFilesInSelectedShelf(shelvedFiles);
			IP4Resource[] pendingfiles = (IP4Resource[]) model.getFilesInSelectedChangelist();

			IP4Resource[] allFiles = new IP4Resource[pendingfiles.length
					+ ((model.getFilesInSelectedShelf()) != null ? model.getFilesInSelectedShelf().length : 0)];

			List<IP4Resource> shelved = new ArrayList<IP4Resource>(
					Arrays.asList((IP4Resource[]) model.getFilesInSelectedShelf()));
			List<IP4Resource> pending = new ArrayList<IP4Resource>(Arrays.asList(pendingfiles));
			pending.addAll(shelved);
			allFiles = pending.toArray(allFiles);
			//model.setFilesInSelectedChangelist(allFiles);
		}
	}

	public int getPendingFilesCount() {
		int x = 0;
		if (model.getFilesInSelectedChangelist() != null && model.getFilesInSelectedChangelist().length > 0) {
			IP4Resource[] files = (IP4Resource[]) model.getFilesInSelectedChangelist();

			for (IP4Resource file : files) {
				if (!(file instanceof IP4ShelveFile)) {
					x++;
				}
			}
		}
		return x;
	}

	
	/**
	 * Extracts all users who are reviewers
	 * 
	 * @return
	 */
	public ReviewerUserList getReviewerUsers() {
		ReviewerUserList users = model.getReviewerUsers();
		if (users == null || users.getSize() == 0) {
			initReviewers();
		}
		return model.getReviewerUsers();
	}

	/**
	 * Converts
	 * 
	 * @param resources
	 * @return
	 */
	private IP4Resource[] getIP4Resources(Object[] resources) {
		IP4Resource[] resourceFiles = new IP4Resource[resources.length];
		if (resources != null) {
			int x = 0;
			for (Object resource : resources) {
				if (resource instanceof IP4File) {
					resourceFiles[x] = (IP4File) resource;
				}
				x++;
			}
		}
		return resourceFiles;
	}


	/**
	 * Clears the reviewers
	 */
	public void clearReviewers() {
		if (model.getReviewerUsers() != null) {
			// model.getReviewerUsers().clearList();
		}
		getSelectedReviewerUsers().clearList();
		model.setSearchedSwarmReviewId(null);
	}

	/**
	 * Clears the current state of the singleton
	 */
	public void clearState() {
		AUTH_TOKEN = null;
		if (!canConnectToSwarm) {
			SWARM_URL = null;
		}
		// canConnectToSwarm = false;
		// swarmVersionResult = null;
		model.setSelectedChangelistData(null);
		model.setSwarmSpecificChangelist(null);

		model.setFilesInSelectedChangelist(null);
		model.setPendingChangeList(null);
		model.setShelvedChangeList(null);
		model.setFilesInSelectedShelf(null);
		clearReviewers();
	}

	public void clearSwarmState() {
		SWARM_URL = null;
		canConnectToSwarm = false;
		swarmVersionResult = null;
	}

	public void clearCache() {
		model.clearSwarmLinkedChangeCache();
	}

	String getBasicAuthTicket() {
		if (AUTH_TOKEN == null) {
			updateAuthToken();
		}
		return AUTH_TOKEN;
	}

	private void updateAuthToken() {
		AUTH_TOKEN = connection.getServer().getUserName() + ":" + connection.getServer().getAuthTicket();
	}

	public Result<String> getSwarmURL() {
		Result<String> urlResult = new Result<String>(true);
		try {
			if (SWARM_URL == null) {
				GetPropertyOptions propOpts = new GetPropertyOptions();
				String key = "P4.Swarm.URL";
				propOpts.setName(key);
				if (connection != null && connection.getServer() != null) {
					List<IProperty> values = connection.getServer().getProperty(propOpts);
					for (IProperty prop : values) {
						if (key.equals(prop.getName())) {
							SWARM_URL = prop.getValue();
							if (SWARM_URL != null) {
								if (SWARM_URL.endsWith("/")) {
									SWARM_URL = SWARM_URL.substring(0, (SWARM_URL.lastIndexOf("/")));
								}
							}
							urlResult.withSuccess("P4 Code Review URL located", SWARM_URL);
							return urlResult;
						}
					}
				}
			}
			boolean found = SWARM_URL != null;
			if (found && SWARM_URL.endsWith("/")) {
				SWARM_URL = SWARM_URL.substring(0, (SWARM_URL.lastIndexOf("/")));
			}
			urlResult.withValues(found, true, found ? "P4 Code Review URL located" : "P4 Code Review not found", false, SWARM_URL);
		} catch (P4JavaException ex) {
			urlResult.withValues(false, true, "P4 Code Review not found", true, SWARM_URL);
			return urlResult;
		}
		return urlResult;
	}

	/**
	 * Initialises system with the users who qualify as reviewers
	 * 
	 * @throws P4JavaException
	 */
	public void initReviewers() {
		try {
			Server server = ((Server) connection.getServer());
			List<IUserSummary> users = server.getUsers(null, -1);

			ReviewerUserList reviewerUsers = new ReviewerUserList(users);
			model.setReviewerUsers(reviewerUsers);
		} catch (Exception ex) {
			throw new P4JavaError(ex);
		}
	}

	/**
	 * Adds multiple reviewers at once
	 * 
	 * @param reviewers
	 */
	public void addReviewerUsers(ReviewerUserList reviewers) {
		ReviewerUserList selectedReviewerUsers = model.getSelectedReviewerUserList();
		selectedReviewerUsers.addUnique(reviewers);

		model.setSelectedReviewerUserList(selectedReviewerUsers);
		mapReviewerToChangelist(model.getPendingChangeList().getId(), model.getSelectedReviewerUserList());
	}

	/**
	 * Removes a reviewer given the users indexes extracted from as it appears
	 * in the UI
	 * 
	 * @param reviewerIndexes
	 */
	public void removeReviewer(ReviewerUserList reviewers) {
		try {
			ReviewerUserList alreadySelected = model.getSelectedReviewerUserList();
			if (alreadySelected != null && alreadySelected.getSize() > 0) {
				alreadySelected.removeReviewers(reviewers);
				model.setSelectedReviewerUserList(alreadySelected);
				removeReviewerMappedToChangelist(model.getPendingChangeList().getId(), reviewers);
			}
		} catch (Exception ex) {
			throw new P4JavaError(ex);
		}
	}

	/**
	 * Adds a reviewer searched by name
	 * 
	 * @param reviewerName
	 */
	public Result<String> addReviewerByName(String reviewerName) {
		Result<String> result = new Result<String>(true, "Successfully added");
		try {
			List<String> reviewerNameList = new ArrayList<String>();
			reviewerNameList.add(reviewerName);
			Server server = ((Server) connection.getServer());
			IUserSummary user = server.getUser(reviewerName);

			if (user != null) {
				ReviewerUserList users = new ReviewerUserList(user);
				addReviewerUsers(users);
			} else {
				result.withNoSuccess("User " + reviewerName + " does not exist", false, reviewerName);

			}
		} catch (Exception ex) {
			result.withNoSuccess(ex.getMessage(), true, reviewerName);
		}
		return result;
	}

	/**
	 * Maintains a collection of Reviewers mapping them to a change list id
	 * 
	 * @param changeId
	 * @param reviewer
	 */
	private void mapReviewerToChangelist(Integer changeId, ReviewerUserList reviewers) {
		ReviewerUserList rlist = model.getChangeListIdToReviewerUserMap().get(changeId);
		if (rlist == null) {
			rlist = new ReviewerUserList();
			rlist.addReviewers(reviewers);
			model.getChangeListIdToReviewerUserMap().put(changeId, rlist);
		}
		rlist.addUnique(reviewers);
		model.getChangeListIdToReviewerUserMap().put(changeId, rlist);
	}

	private void removeReviewerMappedToChangelist(Integer changeId, ReviewerUserList reviewers) {
		ReviewerUserList rlist = model.getChangeListIdToReviewerUserMap().get(changeId);
		rlist.removeReviewers(reviewers);
	}

	/**
	 * Returns the description for a given review
	 * 
	 * @param reviewId
	 * @return
	 */
	public String getChangeListDescriptionFor(String reviewId) {
		String description = "Review ID not found";
		try {
			model.setSwarmSpecificChangelist(null);
			model.setSearchedSwarmReviewId(null);
			Result<Response<JsonString>> apiResult = callAPI("GET", SWARM_API_PATH + "/" + reviewId, null, null);
			if (!apiResult.isSuccessful()) {
				return description;
			}

			Response<JsonString> response = apiResult.getResultData();
			boolean ok = response.isSuccessful();

			if (ok) {
				SwarmReview swarmreview = SwarmReviewUtil
						.generateSwarmReviewFromJSON(response.getResponseBody().getJasonString());
				description = swarmreview.getReview().getDescription();
				model.setSearchedSwarmReviewId(Integer.valueOf(swarmreview.getReview().getId()));
			}
		} catch (NumberFormatException ex) {
			description = "Error - input value must be a number";
		} catch (APIException ex) {
			description = "Error - " + ex.getMessage();
		} catch (P4JavaException ex) {
			description = "Error - " + ex.getMessage();
		}
		return description;
	}

	/**
	 * Indicates that the selected change list should be treated as an update to
	 * an existing review in P4 Code Review  rather than a new review in P4 Code Review
	 * 
	 * @return
	 */
	public boolean isUpdateReview() {
		Result<Boolean> isChangelistLinkedToSwarmReview = extractReviewResultForChangeList(
				model.getPendingChangeList().getId());
		if (isChangelistLinkedToSwarmReview.isSuccessful()) {
			return isChangelistLinkedToSwarmReview.getResultData().booleanValue();
		}
		return model.getSwarmSpecificChangelist() != null || model.getSearchedSwarmReviewId() != null;
	}

	/**
	 * Extracts a List of IFileSpec(s) from the selected Change list items
	 * 
	 * @return
	 */
	List<IFileSpec> extractFileSpecList() {
		IP4Resource[] resources = getIP4Resources(model.getSelectedChangelistData());

		List<String> paths = new ArrayList<String>();
		if (resources != null && resources.length > 0) {
			for (IP4Resource resource : resources) {
				if (resource != null) {
					String path = resource.getActionPath();
					if (path != null) {
						paths.add(path);
					}
				}
			}
		}
		List<IFileSpec> specs = P4FileSpecBuilder.makeFileSpecList(paths.toArray(new String[paths.size()]));
		return specs;
	}

	public Result<Boolean> extractReviewResultForChangeList(int changeListId) {
		Result<Boolean> result = isChangeListLinkedToSwarmReview(changeListId);
		return result;
	}

	/**
	 * Check whether change list id is in cache
	 * 
	 * @param changeListId
	 * @return
	 */
	private boolean changeListInReviewFound(Integer changeListId) {
		model.setSwarmSpecificChangelist(null);
		boolean changelistInSwarm = model.isChangeInReview(changeListId);
		if (changelistInSwarm) {
			Integer reviewIdInSwarm = model.getSwarmReviewIdFor(changeListId);
			model.setSwarmSpecificChangelist(reviewIdInSwarm);
		}
		return changelistInSwarm;
	}

	/**
	 * Determines whether a given change list id is linked to a corresponding
	 * P4 Code Review specific review id
	 * 
	 * @param changeListId
	 * @return
	 */
	public Result<Boolean> isChangeListLinkedToSwarmReview(Integer changeListId) {
		Result<Boolean> result = new Result<Boolean>(true, "");

		if (changeListInReviewFound(changeListId)) {
			return result.withSuccess("Changelist has a corresponding review in swarm", Boolean.TRUE);
		}

		if (changeListId > -1) {
			try {
				Result<SwarmReviewList> reviewListResult = extractReviewsForChangeList(changeListId);
				if (!reviewListResult.isError() && reviewListResult != null && reviewListResult.isSuccessful()) {
					boolean isEmpty = reviewListResult.getResultData().isEmpty();
					if (!isEmpty) {
						Iterator<Review> it = reviewListResult.getResultData().getReviews().iterator();

						while (it.hasNext()) {
							Review review = it.next();
							model.addChangeIdsLinkedToSwarm(review.getId(), review.getChanges());
						}
						result.withSuccess("P4 Code Review, reviews for change " + changeListId + " successfully retrieved",
								Boolean.TRUE);
					} else {
						result.withNoSuccess("No reviews found for change " + changeListId, false, Boolean.FALSE);
					}
				} else {
					result.withNoSuccess(reviewListResult.getMessage(), reviewListResult.isError(), Boolean.FALSE);
				}
			} catch (Exception ex) {
				result.withNoSuccess(ex.getMessage(), Boolean.FALSE, Boolean.FALSE);
			}
		}
		return result;
	}

	/**
	 * Returns a list of Ids from the given list of IP4Changelist items
	 * 
	 * @param lists
	 *            Anything that extends from IP4Changelist
	 * @return
	 */
	List<Integer> extractIdsFromPendingChangeLists(T[] lists) {
		List<Integer> clIdList = new ArrayList<Integer>();
		if (lists != null) {
			for (T list : lists) {
				if (list != null) {
					model.addP4Changelist(list);
					clIdList.add(list.getId());
				}
			}
		}
		return clIdList;
	}

	void refreshChangelist(T[] lists) {
		if (lists != null) {
			for (T list : lists) {
				if (list != null) {
					if (list instanceof IP4PendingChangelist) {
						IP4ShelvedChangelist shelved = ((IP4PendingChangelist) list).getShelvedChanges();
						list.refresh();
					}
				}
			}
		}
	}

	/**
	 * Returns IP4Changelist for a given Id if it exists in the local cache
	 * 
	 * @param id
	 * @return
	 */
	public T getP4ChangelistById(T cl) {
		return model.getP4ChangeListIdToChangeListMap().get(cl.getId() + "@" + cl.getClientName());
	}

	/**
	 * Caches the changes linked to P4 Code Review found in lists
	 * 
	 * @param lists
	 */
	public void cacheChangesLinkedToSwarm(final T[] lists) {
		model.setAllP4ChangeLists(new ArrayList<T>(Arrays.asList(lists)));
		List<Integer> clIdsList = extractIdsFromPendingChangeLists(lists);
		doCacheChangesInReview(clIdsList.toArray(new Integer[clIdsList.size()]));
		updateReviewStatusOfChangelists(lists);
		P4Runner.schedule(new P4Runnable() {
			@Override
			public void run(IProgressMonitor monitor) {
				// refreshChangelist(lists);
			}
		});
	}

	public void createCache(final T[] lists) {
		clearSwarmState();
		final Result<Boolean> result = canWeConnectToSwarm();
		if (lists != null && lists.length > 0) {
			if (result.isSuccessful()) {
				cacheChangesLinkedToSwarm(lists);
			}
		}
	}

	public List<T> getAllP4ChangeLists() {
		return model.getAllP4ChangeLists();
	}

	/**
	 * Updates the Pending change lists' in-review status
	 * 
	 * @param lists
	 */
	private void updateReviewStatusOfChangelists(T[] lists) {
		List<Integer> changesInReview = getChangeListIdsLinkedToSwarm();
		if (changesInReview != null) {
			for (Integer id : changesInReview) {
				for (T list : lists) {
					if (id.equals(list.getId())) {
						list.setHasReview(true);
						break;
					}
				}
			}
		}
	}

	public void doCacheChangesInReview(Integer... changeListIds) {
		Result<Boolean> result = new Result<Boolean>(true, "");
		List<Integer> changeIdsNotInCache = new ArrayList<Integer>();

		for (Integer cl : changeListIds) {
			if ((model.isChangeInReview(cl))) {
			} else {
				changeIdsNotInCache.add(cl);
			}
		}
		if (!changeIdsNotInCache.isEmpty()) {
			try {
				Result<SwarmReviewList> reviewListResult = extractReviewsForChangeList(
						changeIdsNotInCache.toArray(new Integer[changeIdsNotInCache.size()]));

				if (!reviewListResult.isError() && reviewListResult != null && reviewListResult.isSuccessful()) {
					boolean isEmpty = reviewListResult.getResultData().isEmpty();
					if (!isEmpty) {
						Iterator<Review> it = reviewListResult.getResultData().getReviews().iterator();
						while (it.hasNext()) {
							Review review = it.next();
							model.addChangeIdsLinkedToSwarm(review.getId(), review.getChanges());
						}
					}
				}
			} catch (Exception ex) {
				result.withNoSuccess(ex.getMessage(), Boolean.FALSE, Boolean.FALSE);
			}
		}
	}

	/**
	 * Opens the review in a web browser
	 * 
	 * @return
	 * @throws Exception
	 */
	public void openSwarmReviewOnline() throws Exception {
		Integer chglistid = model.getPendingChangeList().getId();
		Result<String> onlineURL = getSwarmURL();
		if (onlineURL.isSuccessful()) {
			String URL = onlineURL.getResultData() + "/reviews/" + model.getSwarmReviewIdFor(chglistid);
			open(URL);
		}
	}

	/**
	 * Opens the change in a web browser
	 * 
	 * @return
	 * @throws Exception
	 */
	public void openSwarmChangeOnline() throws Exception {
		Integer chglistid = model.getPendingChangeList().getId();
		Result<String> onlineURL = getSwarmURL();
		if (onlineURL.isSuccessful()) {
			String URL = onlineURL.getResultData() + "/changes/" + chglistid;
			open(URL);
		}
	}
	
	private void open(String URL){
		//Desktop.getDesktop().browse(new URI(URL));
		org.eclipse.swt.program.Program.launch(URL);
	}

	/**
	 * Extracts the review id from P4 Code Review for a given change id This method uses the
	 * following API url to limit the number of fields in the response
	 * http://{SWARM_API_PATH} + "?change[]=" + {changeListStr} + "&fields=id"
	 * 
	 * This method only returns a Result<SwarmReviewList>, where SwarmReviewList
	 * is the parsed JSON of reviews returned as part of the API call.
	 * 
	 * It may be a successful API call with a response code of 200 but with an
	 * empty list of reviews
	 * 
	 * Its the callers responsibility to handle such scenarios
	 * 
	 * @param changeListId
	 * @return
	 * @throws Exception
	 */
	private Result<SwarmReviewList> extractReviewsForChangeList(Integer... changeListIds) {

		SwarmReviewList reviewList = new SwarmReviewList();
		Result<SwarmReviewList> result = new Result<SwarmReviewList>(true, "");
		String clString = "";
		if (changeListIds != null) {
			for (Integer cl : changeListIds) {
				clString += "change[]=" + cl + "&";
			}
		}
		try {
			Result<Boolean> canConnectResult = canWeConnectToSwarm();
			if (!canConnectResult.isSuccessful()) {
				return result.withNoSuccess(canConnectResult.getMessage(), canConnectResult.isError(), reviewList);
			}
			Result<Response<JsonString>> appiResult = callAPI("GET", SWARM_API_PATH, clString, "fields=id,changes");
			if (!appiResult.isSuccessful()) {
				return result.withNoSuccess(appiResult.isReportInSilence(), appiResult.getMessage(),
						appiResult.isError(), reviewList);
			}
			Response<JsonString> response = appiResult.getResultData();
			boolean ok = appiResult.isSuccessful() && response.isSuccessful();
			if (ok) {
				reviewList = SwarmReviewUtil
						.generateSwarmReviewListFromJSON(response.getResponseBody().getJasonString());
				result.withSuccess("Reviews successfully retrieved", reviewList);
			} else {
				result.withNoSuccess("No reviews found for change ", false, reviewList);
			}
		} catch (P4JavaException ex) {
			result.withNoSuccess(ex.getMessage(), true, reviewList);
		} catch (APIException ex) {
			result.withNoSuccess(ex.getLocalisedMessage(), true, reviewList);
		} finally {
			if (result == null || !result.isSuccessful()) {
				clearCache();
			}
		}
		return result;
	}

	/**
	 * Makes a GET API call given the args. This picks up the URL and path from
	 * external variables.
	 * 
	 * Ensure that each of the arguments a re formed according to that supported
	 * by the API
	 * 
	 * @param urlPart
	 *            Part comes after http://{domainname} e.g
	 *            http://{domainname}/{urlpart} where urlPaert can be formed
	 *            similar to {urlpart1/urlPart2} etc.
	 * @param queryParams
	 *            Query parameters key/values sent along the with the request
	 *            tht comes after '?' e.g
	 *            http://{domainname}/{urlpart}?{queryParams}
	 * @param filter
	 *            Any filter options e.g
	 *            http://{domainname}/{urlpart}?{queryParams}&{filter}
	 * @return
	 * @throws P4JavaException
	 */
	Result<Response<JsonString>> callAPI(String method, String urlPart, String queryParams, String filter)
			throws P4JavaException {
		Result<Response<JsonString>> responseResult = new Result<Response<JsonString>>(true);
		Response<JsonString> response;

		Result<String> URLResult = getSwarmURL();
		if (URLResult == null || !URLResult.isSuccessful()) {
			return responseResult.withNoSuccess(true,
					"P4 Code Review not configured. Unable to find P4 Code Review URL" + URLResult.getMessage(), true, null);
		}
		if (URLResult != null && URLResult.getResultData() != null && URLResult.getResultData().isEmpty()) {
			return responseResult.withNoSuccess(false, "Unble to connect to P4 Code Review, Invalid URL", true, null);
		}
		String URL = URLResult.getResultData();
		if (urlPart != null && !urlPart.isEmpty()) {
			URL += urlPart;
		}
		response = new APIClient().secure(ignoreSSL).withValues(method, URL, queryParams, filter)
				.withAuth(AuthScheme.Basic, getBasicAuthTicket()).asJsonStringResponse();

		responseResult.withValues(response.isOK(), false, response.getResponseMessage(), false, response);
		return responseResult;
	}

	Result<Boolean> canContinue() {
		Result<String> urlCheck = getSwarmURL();
		if (!urlCheck.isSuccessful()) {
			return new Result<Boolean>(urlCheck, urlCheck.isSuccessful());
		}
		Result<Boolean> swarmConnectResult = canWeConnectToSwarm();
		if (!swarmConnectResult.isSuccessful()) {
			return new Result<Boolean>(swarmConnectResult, swarmConnectResult.isSuccessful());
		}
		return new Result<Boolean>(true).withSuccess("Can connect to Swarm", true);
	}

	public Result<Boolean> canWeConnectToSwarm() {
		try {
			if (!canConnectToSwarm) {
				swarmVersionResult = checkVersion(VERSION);
				canConnectToSwarm = swarmVersionResult.isSuccessful();
			}
			log(swarmVersionResult.getMessage());
		} catch (Exception ex) {
			return new Result<Boolean>(false, ex.getMessage());
		}
		return new Result<Boolean>(swarmVersionResult, swarmVersionResult.isSuccessful());
	}

	/**
	 * Checks whether server supports the minimum version
	 * 
	 * @param expectedVersion
	 * @return
	 */
	public Result<SwarmAPIVersions> checkVersion(String expectedVersion) {
		Result<SwarmAPIVersions> result = new Result<SwarmAPIVersions>(true, "");
		String apiVersion = expectedVersion != null ? expectedVersion : VERSION;
		SwarmAPIVersions swarmAPIVersions = new SwarmAPIVersions();
		try {
			Result<Response<JsonString>> apiresult = callAPI("GET", "/api/version", null, null);
			if (!apiresult.isSuccessful()) {
				return result.withNoSuccess(apiresult.isReportInSilence(),
						"Could not connect to Swarm. " + apiresult.getMessage(), apiresult.isError(), swarmAPIVersions);
			}
			Response<JsonString> res = apiresult.getResultData();
			if (res.isOK()) {
				String response = res.getResponseBody().getJasonString();
				Gson gson = new Gson();
				swarmAPIVersions = gson.fromJson(response, SwarmAPIVersions.class);
				boolean versionSupported = swarmAPIVersions.isVersionSupported(apiVersion);
				if (versionSupported) {
					result.withSuccess("API Version: " + apiVersion + " is currently supported by P4 Code Review",
							swarmAPIVersions);
				} else {
					result.withNoSuccess("P4 Code Review does not support API Version: " + apiVersion
							+ " Currently supported version is v" + VERSION, false, swarmAPIVersions);
				}
			} else {
				result.withNoSuccess(res.getResponseMessage(), false, swarmAPIVersions);
			}

		} catch (P4JavaException e) {
			result.withNoSuccess(e.getMessage(), true, swarmAPIVersions);
		} catch (APIException e) {
			result.withNoSuccess(e.getMessage(), true, swarmAPIVersions);
		} finally {
			swarmVersionResult = result;
			canConnectToSwarm = swarmVersionResult.isSuccessful();
		}
		return result;
	}

	boolean isChangelistInReview(final T pendingChangeList) {
		boolean inReview = false;
		if (pendingChangeList == null) {
			return false;
		}
		if (pendingChangeList != null) {
			T selectedChangelIst = getP4ChangelistById(pendingChangeList);
			//if(selectedChangelIst != null){
				inReview = selectedChangelIst.hasReview();
			//}
		}
		return inReview;
	}

	/**
	 * Requests that the checkedFiles are submitted for review
	 * 
	 * @param checkedFiles
	 * @param description
	 */
	public Result<Boolean> requestReview(Object[] checkedFiles, String description) {
		IClient client = null;
		IChangelist changelist = null;
		boolean isNewReview = true;

		Result<Boolean> result = new Result<Boolean>(true, "");
		try {
			result = canWeConnectToSwarm();
			if (!result.isSuccessful()) {
				return result;
			}
			if (checkedFiles == null) {
				checkedFiles = getFilesInSelectedChangelist();
				model.setSelectedChangelistData(checkedFiles);
			} else {
				model.setSelectedChangelistData(checkedFiles);
			}
			final IServer server = connection.getServer();
			client = server.getCurrentClient();
			final List<IFileSpec> specs = extractFileSpecList();
			changelist = model.getPendingChangeList().getChangelist();
			if (changelist == null) {
				// maintain the user to change map here........................
				// + extractFormattedHashReviewString() +
				// extractFormattedSelectedReviewers()
				changelist = client.createChangelist(new Changelist(IChangelist.UNKNOWN, client.getName(),
						server.getUserName(), ChangelistStatus.NEW, null, description, false, (Server) server));
			} else {
				String currDescription = changelist.getDescription();
				if(description != null && !description.isEmpty()){
					//currDescription += description;
					changelist.setDescription(description);
					changelist.update(false);
				}
				Result<Boolean> resultInSwarm = isChangeListLinkedToSwarmReview(changelist.getId());// extractReviewResultForChangeList(changelist.getId());
				isNewReview = !(!resultInSwarm.isError() && resultInSwarm.isSuccessful()
						&& resultInSwarm.getResultData().booleanValue());
				if (resultInSwarm.isError()) {
					result.withNoSuccess(resultInSwarm.getMessage(), resultInSwarm.isError(),
							resultInSwarm.getResultData());
					return result;
				}
			}

			List<IFileSpec> shelvedFiles = new ArrayList<IFileSpec>();
			if (!specs.isEmpty()) {
				@SuppressWarnings("unused")
				List<IFileSpec> reopened = client.reopenFiles(specs, changelist.getId(), null);
				changelist.refresh();
				shelvedFiles = client.shelveChangelist(changelist.getId(), specs, true, false, false);
			}

			// If a review exists for numbered change list just shelve the
			// files. no need to call the API
			// If review is for default then whether new review or update to an
			// existing review call the API
			if (shelvedFiles.size() > 0
					|| (model.getFilesInSelectedShelf() != null && model.getFilesInSelectedShelf().length > 0)) {
				String shelveStatusMsg = "";
				for (IFileSpec spec : shelvedFiles) {
					boolean error = (spec.getOpStatus() == FileSpecOpStatus.ERROR);
					if (error) {
						shelveStatusMsg += spec.getStatusMessage();
					}
				}
				if (!shelveStatusMsg.trim().isEmpty()) {
					return result.withNoSuccess(shelveStatusMsg, true, false);
				}

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
						param += "&" + reviewerParam;
						param += "&" + requiredReviewerParam;
					}
					Result<Response<JsonString>> apiResult = callAPI("POST", path, param, null);
					if (!apiResult.isSuccessful()) {
						return result.withNoSuccess("Unable to create review: " + apiResult.getMessage(),
								apiResult.isError(), false);
					}
					Response<JsonString> response = apiResult.getResultData();
					boolean ok = response.isOK();
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
		} catch (P4JavaException ex) {
			result.withNoSuccess(ex.getMessage(), true, false);
		} finally {
			clearState();
		}
		return result;
	}
}
