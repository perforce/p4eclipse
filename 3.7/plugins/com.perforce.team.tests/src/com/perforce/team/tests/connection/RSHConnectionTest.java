package com.perforce.team.tests.connection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Assert;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.p4java.impl.mapbased.server.Server;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.swarmreview.LocalTestUtil;

public class RSHConnectionTest extends ConnectionBasedTestCase {

	public static final String RESOURCES = "servers/";
	private static LocalTestUtil localTestUtil = LocalTestUtil.newInstance();
	private String p4d;

	private void init(String p4dVersion) throws IOException {
		String p4d = localTestUtil.getP4dPath(p4dVersion);
		this.p4d = p4d;
	}
	
	public void testP4dPath(){
		try{
			init("2017.1");
			Assert.assertTrue(this.p4d.endsWith("servers/2017.1/macosx/x86_64/p4d"));
		}catch(Exception ex){
			Assert.fail(ex.getMessage());
		}
		
	}
	/**
	 * Test whether URL entered by user is correctly translated
	 * "p4jrsh://" + getP4d() + " -r " + getPathToRoot() + " -L log -i --java"
	 */
	public void testURLConversion(){
		try{
			//Test for RSH is cnverted correctly
			init("2017.1");
			File testDir = getTestDir();
			String root =testDir.getAbsolutePath()  + "/testURLConversion";
			if(!Files.exists(Paths.get(root))){
				Files.createDirectories(Paths.get(root));
			}
			ConnectionParameters connParams = localTestUtil.getConnectionParameters("tpethiyagoda", "", "rsh:" + this.p4d + " -r " + root  + " -i --java", null, false, "eclipse-plugin-test-ws");
			IP4Connection conn = P4Workspace.getWorkspace().tryToConnect(connParams, null);
			
			Assert.assertNotNull(conn);
			Assert.assertEquals(((Server)conn.getServer()).getServerAddressDetails().getUri(), "p4jrsh://" + this.p4d + " -r " + root  + " -i --java");
			
			IP4Connection con = new P4Connection(connParams);
			IServer  server = ((P4Connection)con).createServer(null);
			
			Assert.assertNotNull(conn);
			Assert.assertTrue(((Server)server).getServerAddressDetails().getUri().contains("p4jrsh"));
			Assert.assertEquals(((Server)server).getServerAddressDetails().getUri(), "p4jrsh://" + this.p4d + " -r " + root  +  " -i --java");
			
			//Test for SSL
			connParams = localTestUtil.getConnectionParameters("tpethiyagoda", "", "ssl:p4prod.perforce.com:1666", null, false, "p4eclipseuk");
			con = new P4Connection(connParams);
			server = ((P4Connection)con).createServer(null);
			
			Assert.assertNotNull(conn);
			Assert.assertTrue(((Server)server).getServerAddressDetails().getUri().contains("ssl"));
			Assert.assertEquals(((Server)server).getServerAddressDetails().getUri(), "p4jrpcssl://p4prod.perforce.com:1666");
			
		}catch(Exception ex){
			Assert.fail(ex.getMessage());
		}
	}
	/**
	 * Test successful connection via RSH
	 */
	public void testRSHConnection(){
		try{
			//Test for RSH is cnverted correctly
			init("2017.1");
			File testDir = getTestDir();
			String root =testDir.getAbsolutePath()  + "/testRSHConnection";
			if(!Files.exists(Paths.get(root))){
				Files.createDirectories(Paths.get(root));
			}
			ConnectionParameters connParams = localTestUtil.getConnectionParameters("tpethiyagoda", "", "rsh:" + this.p4d + " -r " + root  + " -i --java", null, false, "eclipse-plugin-test-ws");
			IP4Connection conn = P4Workspace.getWorkspace().tryToConnect(connParams, null);
			
			Assert.assertNotNull(conn);
			Assert.assertEquals(((Server)conn.getServer()).getServerAddressDetails().getUri(), "p4jrsh://" + this.p4d + " -r " + root  + " -i --java");
			String clientName = conn.getClientName();
			Assert.assertEquals("eclipse-plugin-test-ws", clientName);
			
			String[] paths = {"//depot/... //eclipse-plugin-test-ws/..."};
			IClient client = Client.newClient(((IOptionsServer)conn.getServer()), "eclipse-plugin-test-ws", "eclipse-plugin-test-ws", ".", paths);
			conn.createClient(client);
			
			String[] paths1 = {"//depot/... //RSHClient/..."};
			client = Client.newClient(((IOptionsServer)conn.getServer()), "RSHClient", "RSHClient", ".", paths1);
			conn.createClient(client);
			
			IClient testClientFromServer = conn.getServer().getClient("RSHClient");
			Assert.assertNotNull(testClientFromServer);
			Assert.assertEquals("RSHClient", testClientFromServer.getName());
			
			conn.getServer().setCurrentClient(testClientFromServer);
			conn.connect();
			boolean isLoggedIn = conn.login("");
			Assert.assertTrue(isLoggedIn);
			
			String clientRoot = conn.getClientRoot();
			Assert.assertEquals("RSHClient", testClientFromServer.getName());
			
		}catch(Exception ex){
			Assert.fail(ex.getMessage());
		}
	}
	
	/**
	 * Test to validate the URL as entered via the UI
	 * @throws IOException 
	 */
	public void testURLValidation() throws IOException{
		init("2017.1");
		String regex = "((rsh)?(:{1})[^:]+[a-zA-Z:]?.*)";//"((rsh:)?[^:]?.*):;// |  
		
		String URL = "rsh:" + this.p4d + " -i --java";
		boolean matches = URL.matches(regex);
		Assert.assertTrue(matches);
		
		URL = "rsh::" + this.p4d + " -i --java";
		matches = URL.matches(regex);
		Assert.assertFalse(matches);
		
		URL = "rsh:c:/Users/perforce/p4d -i --java";
		matches = URL.matches(regex);
		Assert.assertTrue(matches);
		
		
		String regexSSL = "(ssl:)?[^:]+:[1-9]\\d*";
		URL = "ssl:p4prod.perforce.com:1666";
		matches = URL.matches(regexSSL);
		Assert.assertTrue(matches);
		
		URL = "p4prod.perforce.com:1666";
		matches = URL.matches(regexSSL);
		Assert.assertTrue(matches);
		
		URL = "ssl::p4prod.perforce.com:1666";
		matches = URL.matches(regexSSL);
		Assert.assertFalse(matches);
		
		
		boolean isShortCircuitORMatches = URL.matches(regex) || URL.matches(regexSSL);
		Assert.assertFalse(isShortCircuitORMatches);
		
		URL = "rsh:" + this.p4d + " -i --java";
		isShortCircuitORMatches = URL.matches(regex) || URL.matches(regexSSL);
		Assert.assertTrue(isShortCircuitORMatches);
		
		URL = "rsh:c:/Users/perforce/p4d -i --java";
		isShortCircuitORMatches = URL.matches(regex) || URL.matches(regexSSL);
		Assert.assertTrue(isShortCircuitORMatches);
		
		URL = "ssl:p4prod.perforce.com:1666";
		isShortCircuitORMatches = URL.matches(regex) || URL.matches(regexSSL);
		Assert.assertTrue(isShortCircuitORMatches);
		
		URL = "p4prod.perforce.com:1666";
		isShortCircuitORMatches = URL.matches(regex) || URL.matches(regexSSL);
		Assert.assertTrue(isShortCircuitORMatches);
		
	}
}
