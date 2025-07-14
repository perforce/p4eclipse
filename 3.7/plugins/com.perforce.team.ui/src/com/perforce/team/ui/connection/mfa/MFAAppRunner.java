package com.perforce.team.ui.connection.mfa;


import com.perforce.team.core.p4java.IP4Connection;

public class MFAAppRunner {
	
	private FactoredAuthFactory authFactory = FactoredAuthFactory.instance();
	
	/**
	 * 
	 * @param conn
	 * @param port
	 * @param user
	 * @param appName
	 * @param appLocation
	 * @return
	 */
	public AuthResult<Integer> run (IP4Connection  conn, String port, String user, String appName, String appLocation){
		MFAuthenticator mfaApp = authFactory
				.getNewMFAAuthenticator()
				.withPort(port)
				.withUser(user)
				.with2FAApp(appName)
				.with2FAAppLocatedAt(appLocation)
				.withConnection(conn);
		
		AuthResult<Integer> res = mfaApp.authenticate();
		return res;
	}
	/**
	 * Use this only for testing
	 * @param conn
	 * @param port
	 * @param user
	 * @param appName
	 * @param appLocation
	 * @return
	 */
	@Deprecated
	public AuthResult<Integer> runForTesting (IP4Connection  conn, String port, String user, String appName, String appLocation){
		MFAuthenticator mfaApp = authFactory
				.getNewMFAAuthenticator()
				.withPort(port)
				.withUser(user)
				.with2FAApp(appName)
				.with2FAAppLocatedAt(appLocation)
				.withConnection(conn)
				.forTesting();
		AuthResult<Integer> res = mfaApp.authenticate();
		return res;
	}
}
