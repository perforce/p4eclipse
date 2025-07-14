package com.perforce.team.tests.swarmreview;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;

import com.perforce.p4java.admin.IProperty;
import com.perforce.p4java.option.server.GetPropertyOptions;
import com.perforce.p4java.option.server.PropertyOptions;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.swarmreview.PendingReviewManager;
import com.perforce.team.ui.swarmreview.ReviewManager;
import com.perforce.team.ui.swarmreview.ReviewerUserList;
import com.perforce.team.ui.swarmreview.SubmittedReviewManager;
import com.perforce.team.ui.swarmreview.apimodel.SwarmAPIVersions;
import com.perforce.team.ui.swarmreview.restapi.Result;

public class RequestNewSwarmReviewTest extends ConnectionBasedTestCase {

	private ConnectionParameters params;
	private static LocalTestUtil localP4d = LocalTestUtil.newInstance();
	
	public void setUp() throws Exception {
		params = localP4d.getConnectionParameters("tpethiyagoda", "", "localhost:4000", null, false, "eclipse-plugin-test-ws");
		localP4d.initDocker(false);
	}
	
	public void tearDown() throws Exception {
		localP4d.dockerDown(false);
		localP4d.destroyDockerProcess();
	}

	public IP4Connection getConnection(ConnectionParameters params, boolean rpc, boolean check) throws Exception {
		IP4Connection connection = createConnection(params, rpc, check);
		return connection;
	}

	/**
	 * Test reviews can be found for changes already in review
	 */
	public void testSwarmReviewsLinkedToChange() {

		try {
			IP4Connection connection = localP4d.getExistingServerConnection(params);
			localP4d.login(connection, "super", "Passw0rd");
			connection.getServer().setProperty("P4.Swarm.URL", "http://localhost:5080", null);

			Assert.assertNotNull(connection);
			Assert.assertEquals(false, connection.getParameters().isIgnoreSSLValidation());

			List<IP4PendingChangelist> cl = new ArrayList<IP4PendingChangelist>();
			IP4PendingChangelist changelist = connection.getPendingChangelist(0);
			cl.add(changelist);
			cl.add(connection.getPendingChangelist(132));
			cl.add(connection.getPendingChangelist(135));

			if (changelist != null) {
				connection.getParameters().setIgnoreSSLValidation(true);

				ReviewManager<IP4PendingChangelist> swarmReviewManager = PendingReviewManager.getInstance(connection);
				swarmReviewManager.init(changelist,true);

				Result<SwarmAPIVersions> versionResult = swarmReviewManager.checkVersion("9");
				Assert.assertEquals(true, versionResult.isSuccessful());

				swarmReviewManager.doCacheChangesInReview(132, 141);
				List<Integer> swarmChanges = swarmReviewManager.getChangeListIdsLinkedToSwarm();
				Assert.assertTrue(!swarmChanges.isEmpty());

				swarmReviewManager.doCacheChangesInReview(134, 135);
				swarmChanges = swarmReviewManager.getChangeListIdsLinkedToSwarm();
				Assert.assertTrue(!swarmChanges.isEmpty());
				Assert.assertNull(swarmReviewManager.getSwarmReviewIdFor(134));
				Assert.assertNull(swarmReviewManager.getSwarmReviewIdFor(135));

				swarmReviewManager.clearCache();
				Assert.assertTrue(swarmChanges.isEmpty());

				IP4PendingChangelist[] parr = cl.toArray(new IP4PendingChangelist[cl.size()]);
				swarmReviewManager.cacheChangesLinkedToSwarm(parr);
				swarmChanges = swarmReviewManager.getChangeListIdsLinkedToSwarm();

				Assert.assertTrue(!swarmChanges.isEmpty());
				Assert.assertNull(swarmReviewManager.getSwarmReviewIdFor(134));
				Assert.assertNotNull(swarmReviewManager.getSwarmReviewIdFor(132));

				ReviewerUserList users = swarmReviewManager.getReviewerUsers();
				Assert.assertTrue(!users.isEmpty());
			}
		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		} finally {

		}
	}

	public void testGetSwarmURL() {
		try {
			IP4Connection connection = localP4d.getExistingServerConnection(params);
			localP4d.login(connection, "super", "Passw0rd");
			connection.getServer().setProperty("P4.Swarm.URL", "http://localhost:5080", null);

			Assert.assertNotNull(connection);
			Assert.assertEquals(false, connection.getParameters().isIgnoreSSLValidation());
			
			ReviewManager<IP4PendingChangelist> swarmReviewManager = PendingReviewManager.getInstance(connection);
			swarmReviewManager.init(null,true);
			
			Result<String> swarmURL = swarmReviewManager.getSwarmURL();
			Assert.assertEquals(swarmURL.getMessage(), "http://localhost:5080", swarmURL.getResultData());
			
			
			connection.getServer().setProperty("P4.Swarm.URL", "http://localhost:5080/", null);
			
			GetPropertyOptions gopts = new GetPropertyOptions();
			gopts.setName("P4.Swarm.URL");
			List<IProperty> props = connection.getServer().getProperty(gopts);
			Assert.assertEquals("http://localhost:5080/", props.get(0).getValue());
			
			swarmURL = swarmReviewManager.getSwarmURL();
			Assert.assertEquals(swarmURL.getMessage(), "http://localhost:5080", swarmURL.getResultData());
			
		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		} finally {

		}

	}

	/**
	 * Tests P4 Code Review version check over SSL, with a self signed certificate. This
	 * fails as self signed accept is disabled.
	 * 
	 * Ignoring test because SSL is not enabled in P4 Code Review in the P4 Code Review docker container.
	 * 
	 * @throws Exception
	 */
	@Ignore
//	public void testVersionCheckOverSSLWithSelfSignedCertificateValidationEnabled() throws Exception {
//
//		IP4Connection connection = localP4d.getExistingServerConnection(params);
//		localP4d.login(connection, "super", "Passw0rd");
//		
//		Assert.assertNotNull(connection);
//		Assert.assertEquals(false, connection.getParameters().isIgnoreSSLValidation());
//
//		try {
//			IP4PendingChangelist changelist = connection.getPendingChangelist(0);
//			if (changelist != null) {
//				ReviewManager<IP4PendingChangelist> swarmReviewManager = PendingReviewManager.getInstance(connection);
//				swarmReviewManager.init(changelist,true);
//
//				Result<SwarmAPIVersions> versionResult = swarmReviewManager.checkVersion("4");
//				Assert.assertEquals(true, versionResult.isSuccessful());
//				// Assert.assertTrue(versionResult.getMessage().contains("SSL
//				// handshake failed"));
//			}
//
//		} catch (Exception ex) {
//			Assert.fail(ex.getMessage());
//		} finally {
//			System.setProperty("disableAuth", "false");
//		}
//	}

	/**
	 * Tests P4 Code Review version check over SSL, with a self signed certificate. This
	 * passes as self signed accept is enabled.
	 *
	 * @throws Exception
	 */
	public void testVesionCheckOverSSLWithSelfSignedCertificateValidationIgnored() throws Exception {

		IP4Connection connection = localP4d.getExistingServerConnection(params);
		localP4d.login(connection, "super", "Passw0rd");
		Assert.assertNotNull(connection);
		Assert.assertEquals(false, connection.getParameters().isIgnoreSSLValidation());

		try {
			connection.getParameters().setIgnoreSSLValidation(true);
			Assert.assertEquals(true, connection.getParameters().isIgnoreSSLValidation());

			IP4PendingChangelist changelist = connection.getPendingChangelist(0);
			if (changelist != null) {
				ReviewManager<IP4PendingChangelist> swarmReviewManager = PendingReviewManager.getInstance(connection);
				swarmReviewManager.init(changelist,true);

				Result<SwarmAPIVersions> versionResult = swarmReviewManager.checkVersion("9");
				Assert.assertEquals(true, versionResult.isSuccessful());
			}

		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		} finally {

		}
	}

	/**
	 * Tests if P4 Code Review is not configured that eclipse deals with it quietly
	 *
	 * @throws Exception
	 */
	public void testFailInSilenceIfSwarmNotConfigured() throws Exception {

		IP4Connection connection = localP4d.getExistingServerConnection(params);
		Assert.assertNotNull(connection);
		Assert.assertEquals(false, connection.getParameters().isIgnoreSSLValidation());

		try {
			localP4d.login(connection, "super", "Passw0rd");

			deleteProperty(connection);

			connection.getParameters().setIgnoreSSLValidation(true);
			Assert.assertEquals(true, connection.getParameters().isIgnoreSSLValidation());

			IP4PendingChangelist changelist = connection.getPendingChangelist(0);

			ReviewManager<IP4PendingChangelist> swarmReviewManager = PendingReviewManager.getInstance(connection);
			swarmReviewManager.init(changelist,true);
			swarmReviewManager.clearSwarmState();

			Result<SwarmAPIVersions> versionResult = swarmReviewManager.checkVersion("9");
			Assert.assertEquals(false, versionResult.isSuccessful());
			Assert.assertEquals(true, versionResult.isReportInSilence());
			Assert.assertTrue(versionResult.getMessage().contains("P4 Code Review not configured"));

			versionResult = swarmReviewManager.checkVersion("9");
			Assert.assertEquals(false, versionResult.isSuccessful());
			Assert.assertEquals(true, versionResult.isReportInSilence());
			Assert.assertTrue(versionResult.getMessage().contains("P4 Code Review not configured"));
		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		} finally {
			connection.getServer().setProperty("P4.Swarm.URL", "http://localhost:5080", null);
		}
	}

	/**
	 * Deletes property recursively until all properties are deleted. Had to do
	 * this as for some reason, deleting one would auto create another.
	 * 
	 * 
	 * @param connection
	 * @throws Exception
	 */
	private void deleteProperty(IP4Connection connection) throws Exception {
		GetPropertyOptions gopts = new GetPropertyOptions();
		gopts.setName("P4.Swarm.URL");
		List<IProperty> properties = connection.getServer().getProperty(gopts);
		for (IProperty ip : properties) {
			PropertyOptions opts = new PropertyOptions();
			opts.setName("P4.Swarm.URL");
			opts.setSequence(ip.getSequence().equals("none") ? "0" : ip.getSequence());
			connection.getServer().deleteProperty("P4.Swarm.URL", opts);
		}
		properties = connection.getServer().getProperty(gopts);
		if (!properties.isEmpty()) {
			deleteProperty(connection);
		}
	}

	/**
	 * Tests whether null change list makes it candidate for new review
	 *
	 * @throws Exception
	 */
	public void testIsCandidateForNewReviewWithNullSelectedChangelist() throws Exception {

		try {
			IP4Connection connection = localP4d.getExistingServerConnection(params);
			localP4d.login(connection, "super", "Passw0rd");
			Assert.assertNotNull(connection);
			Assert.assertEquals(false, connection.getParameters().isIgnoreSSLValidation());

			IP4PendingChangelist changelist = connection.getPendingChangelist(10);
			

			ReviewManager<IP4PendingChangelist> swarmReviewManager = PendingReviewManager.getInstance(connection);
			swarmReviewManager.init(changelist,true);

			Result<Boolean> enabled = swarmReviewManager.isChangeListCandidateForNewReview(changelist);
			Assert.assertEquals(false, enabled.isSuccessful());

		} finally {

		}
	}

	/**
	 * Tests whether default change list makes it candidate for update/new
	 * review Can update and request new review Can not open change or review
	 *
	 * @throws Exception
	 */
	public void testIsDefaultPendingChangelistCandidateForUpdateReview() throws Exception {
		params.setUser("super");
		IP4Connection connection = localP4d.getExistingServerConnection(params);
		localP4d.login(connection, "super", "Passw0rd");
		Assert.assertNotNull(connection);
		Assert.assertEquals(false, connection.getParameters().isIgnoreSSLValidation());

		connection.getServer().setProperty("P4.Swarm.URL", "http://localhost:5080", null);
		IP4PendingChangelist changelist = connection.getPendingChangelist(0);

		ReviewManager<IP4PendingChangelist> swarmReviewManager = PendingReviewManager.getInstance(connection);
		changelist.refresh();
		swarmReviewManager.init(changelist,true);
		swarmReviewManager.createCache(new IP4PendingChangelist[]{changelist});

		Result<Boolean> enabled = swarmReviewManager.isChangeListCandidateForUpdateReview(changelist);
		Assert.assertEquals(true, enabled.isSuccessful());

		enabled = swarmReviewManager.isChangeListCandidateForNewReview(changelist);
		Assert.assertEquals(true, enabled.isSuccessful());

		enabled = swarmReviewManager.isOpenChange(changelist);
		Assert.assertEquals(false, enabled.isSuccessful());

		enabled = swarmReviewManager.isOpenReview(changelist);
		Assert.assertEquals(false, enabled.isSuccessful());
	}

	/**
	 * If change in review and no files in change list, cannot request new or
	 * update review Can open change and review
	 *
	 * @throws Exception
	 */
	public void testIsEmptyChangelistAlreadyInReviewCandidateForNewReview() throws Exception {

		params.setUser("super");
		IP4Connection connection = localP4d.getExistingServerConnection(params);
		localP4d.login(connection, "super", "Passw0rd");
		Assert.assertNotNull(connection);
		Assert.assertEquals(false, connection.getParameters().isIgnoreSSLValidation());

		connection.getServer().setProperty("P4.Swarm.URL", "http://localhost:5080", null);

		IP4PendingChangelist changelist = connection.getPendingChangelist(35);

		ReviewManager<IP4PendingChangelist> swarmReviewManager = PendingReviewManager.getInstance(connection);
		swarmReviewManager.init(changelist,true);
		swarmReviewManager.cacheChangesLinkedToSwarm(new IP4PendingChangelist[] { changelist });

		Result<Boolean> enabled = swarmReviewManager.isChangeListCandidateForNewReview(changelist);
		Assert.assertEquals(false, enabled.isSuccessful());

		enabled = swarmReviewManager.isChangeListCandidateForUpdateReview(changelist);
		Assert.assertEquals(false, enabled.isSuccessful());

		enabled = swarmReviewManager.isOpenChange(changelist);
		Assert.assertEquals(true, enabled.isSuccessful());

		enabled = swarmReviewManager.isOpenReview(changelist);
		Assert.assertEquals(true, enabled.isSuccessful());

	}

	/**
	 * If change is in review and has files then can update Can open review Can
	 * open change
	 *
	 * @throws Exception
	 */
	public void testIsEmptyChangelistAlreadyInReviewCandidateForUpdateReview() throws Exception {

		IP4Connection connection = localP4d.getExistingServerConnection(params);
		localP4d.login(connection, "super", "Passw0rd");
		Assert.assertNotNull(connection);
		Assert.assertEquals(false, connection.getParameters().isIgnoreSSLValidation());

		connection.getServer().setProperty("P4.Swarm.URL", "http://localhost:5080", null);

		IP4PendingChangelist changelist = connection.getPendingChangelist(144);

		ReviewManager<IP4PendingChangelist> swarmReviewManager = PendingReviewManager.getInstance(connection);
		swarmReviewManager.init(changelist,true);
		swarmReviewManager.createCache(new IP4PendingChangelist[]{changelist});

		Result<Boolean> enabled = swarmReviewManager.isChangeListCandidateForUpdateReview(changelist);
		Assert.assertEquals(true, enabled.isSuccessful());

		enabled = swarmReviewManager.isChangeListCandidateForNewReview(changelist);
		Assert.assertEquals(false, enabled.isSuccessful());

		enabled = swarmReviewManager.isOpenChange(changelist);
		Assert.assertEquals(true, enabled.isSuccessful());

		enabled = swarmReviewManager.isOpenReview(changelist);
		Assert.assertEquals(true, enabled.isSuccessful());
	}

	/**
	 * Tests whether new and update P4 Code Review menus are enabled when P4 Code Review is not
	 * configured Can not open review or change
	 * 
	 * @throws Exception
	 */
	public void testIsChangelistCandidateForReviewWhenSwarmNotConfigured() throws Exception {

		IP4Connection connection = localP4d.getExistingServerConnection(params);
		try {
			localP4d.login(connection, "super", "Passw0rd");
			deleteProperty(connection);

			Assert.assertNotNull(connection);
			Assert.assertEquals(false, connection.getParameters().isIgnoreSSLValidation());

			Assert.assertNotNull(connection);
			Assert.assertEquals(false, connection.getParameters().isIgnoreSSLValidation());

			IP4PendingChangelist changelist = connection.getPendingChangelist(0);

			ReviewManager<IP4PendingChangelist> swarmReviewManager = PendingReviewManager.getInstance(connection);
			swarmReviewManager.init(changelist,true);
			swarmReviewManager.createCache(new IP4PendingChangelist[]{changelist});

			Result<Boolean> enabled = swarmReviewManager.isChangeListCandidateForNewReview(changelist);
			Assert.assertEquals(false, enabled.isSuccessful());

			enabled = swarmReviewManager.isChangeListCandidateForUpdateReview(changelist);
			Assert.assertEquals(false, enabled.isSuccessful());

			enabled = swarmReviewManager.isOpenChange(changelist);
			Assert.assertEquals(false, enabled.isSuccessful());

			enabled = swarmReviewManager.isOpenReview(changelist);
			Assert.assertEquals(false, enabled.isSuccessful());

		} finally {
			connection.getServer().setProperty("P4.Swarm.URL", "http://localhost:5080", null);
		}
	}
	
	public void testIsShelvedChangelistCandidateForReview() throws Exception {

		IP4Connection connection = localP4d.getExistingServerConnection(params);
		try {
			params.setUser("super");
			localP4d.login(connection, "super", "Passw0rd");
			Assert.assertNotNull(connection);
			Assert.assertEquals(false, connection.getParameters().isIgnoreSSLValidation());

			connection.getServer().setProperty("P4.Swarm.URL", "http://localhost:5080", null);
			IP4PendingChangelist changelist = connection.getPendingChangelist(164);

			ReviewManager<IP4PendingChangelist> swarmReviewManager = PendingReviewManager.getInstance(connection);
			changelist.refresh();
			swarmReviewManager.init(changelist,true);
			swarmReviewManager.createCache(new IP4PendingChangelist[]{changelist});

			Result<Boolean> enabled = swarmReviewManager.isChangeListCandidateForUpdateReview(changelist);
			Assert.assertEquals(true, enabled.isSuccessful());

			enabled = swarmReviewManager.isChangeListCandidateForNewReview(changelist);
			Assert.assertEquals(true, enabled.isSuccessful());

			enabled = swarmReviewManager.isOpenChange(changelist);
			Assert.assertEquals(true, enabled.isSuccessful());

			enabled = swarmReviewManager.isOpenReview(changelist);
			Assert.assertEquals(false, enabled.isSuccessful());
		} finally {
			connection.getServer().setProperty("P4.Swarm.URL", "http://localhost:5080", null);
		}
	}

	/**
	 * Test for requesting a review for a submitted changelist.
	 */
	public void testRequestReviewSubmittedChangelist() {
		try {
			IP4Connection connection = localP4d.getExistingServerConnection(params);
			localP4d.login(connection, "super", "Passw0rd");

			Assert.assertNotNull(connection);
			Assert.assertEquals(false, connection.getParameters().isIgnoreSSLValidation());

			localP4d.login(connection, "super", "Passw0rd");
			connection.getServer().setProperty("P4.Swarm.URL", "http://localhost:5080", null);

			List<IP4SubmittedChangelist> cl = new ArrayList<IP4SubmittedChangelist>();
			IP4SubmittedChangelist changelist = connection.getSubmittedChangelistById(3);
			cl.add(changelist);

			connection.getParameters().setIgnoreSSLValidation(true);

			ReviewManager<IP4SubmittedChangelist> swarmReviewManager = SubmittedReviewManager.getInstance(connection);
			changelist.refresh();
			swarmReviewManager.init(changelist, false);

			Result<Boolean> res = swarmReviewManager.requestReview(swarmReviewManager.getFilesInSelectedChangelist(),
					"Review for submitted");
			Assert.assertEquals(res.getMessage(), true, res.isSuccessful());

		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		} finally {

		}
	}
}