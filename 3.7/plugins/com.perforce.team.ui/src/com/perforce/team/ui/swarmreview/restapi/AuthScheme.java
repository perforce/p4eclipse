package com.perforce.team.ui.swarmreview.restapi;


public enum AuthScheme {

	Basic("Basic"),
	Bearer("Bearer"),
	Digest("Digest"),
	HOBA("HOBA"),
	Mutual("Mutual"),
	AWS4HMACSHA256("AWS4 HMAC SHA256");

	AuthScheme(String schemeName){
		this.schemeName = schemeName;
	}

	public String getSchemeName(){
		return this.schemeName;
	}

	private String schemeName;
}
