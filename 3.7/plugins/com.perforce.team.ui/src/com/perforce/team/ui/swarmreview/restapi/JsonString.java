package com.perforce.team.ui.swarmreview.restapi;

public class JsonString {

	private String jasonString = "";


	public JsonString(String jsonString){
		this.jasonString = jsonString;
	}

	public String getJasonString(){
		return this.jasonString;
	}

	@Override
	public String toString(){
		return jasonString;
	}
	
	
}
