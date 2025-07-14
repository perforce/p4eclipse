package com.perforce.team.tests.swarmreview;

import org.junit.Assert;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.swarmreview.PendingReviewManager;
import com.perforce.team.ui.swarmreview.ReviewManager;
import com.perforce.team.ui.swarmreview.restapi.Result;

public class SwarmConnectionTest extends ConnectionBasedTestCase {

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
	 * Tests whether P4 Code Review can be reached on an incorrect/invalid host
	 * 
	 * @throws Exception
	 */
	public void testSwarmConnectionCheckOnInvalidURL() throws Exception {
		IP4Connection connection = localP4d.getExistingServerConnection(params);
		Assert.assertNotNull(connection);
		Assert.assertEquals(false, connection.getParameters().isIgnoreSSLValidation());

		try {
			ReviewManager<IP4PendingChangelist> swarmReviewManager = PendingReviewManager.getInstance(connection);
			swarmReviewManager.clearSwarmState();
			swarmReviewManager.init(null,true);
			Result<Boolean> versionResult = swarmReviewManager.canWeConnectToSwarm();
			Assert.assertEquals(versionResult.getMessage(), false, versionResult.isSuccessful());
		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		} finally {

		}
	}

	/**
	 * Tests whether P4 Code Review can be reached on a correct/valid host
	 * 
	 * @throws Exception
	 */
	public void testSwarmConnectionCheckOnvalidURL() throws Exception {
		IP4Connection connection = localP4d.getExistingServerConnection(params);
		localP4d.login(connection, "super", "Passw0rd");
		connection.getServer().setProperty("P4.Swarm.URL", "http://localhost:5080", null);

		Assert.assertNotNull(connection);
		Assert.assertEquals(false, connection.getParameters().isIgnoreSSLValidation());

		try {
			ReviewManager<IP4PendingChangelist> swarmReviewManager = PendingReviewManager.getInstance(connection);
			swarmReviewManager.init(null,true);
			Thread.sleep(100);
			Result<Boolean> versionResult = swarmReviewManager.canWeConnectToSwarm();

			Assert.assertEquals(versionResult.getMessage(), true, versionResult.isSuccessful());

		} catch (Exception ex) {
			Assert.fail(ex.getMessage());
		} finally {
			connection.getServer().setProperty("P4.Swarm.URL", "http://swarm.helix:5080", null);
		}
	}
}