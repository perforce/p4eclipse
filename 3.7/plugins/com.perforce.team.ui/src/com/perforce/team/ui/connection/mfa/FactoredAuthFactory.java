package com.perforce.team.ui.connection.mfa;

public class FactoredAuthFactory {

	private static FactoredAuthFactory instance = new FactoredAuthFactory();
	
	
	private FactoredAuthFactory(){
		
	}
	
	public static FactoredAuthFactory instance(){
		return instance;
	}
	
	public MFAuthenticator getNewMFAAuthenticator(){
		return new MFAuthenticator();
	}
}
