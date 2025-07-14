package com.perforce.team.ui.swarmreview.apimodel;

import java.util.HashMap;
import java.util.Map;



public class TestDetails {

	
	private String url;
	
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	
	public String getUrl() {
		return url;
	}

	
	public void setUrl(String url) {
		this.url = url;
	}

	
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
