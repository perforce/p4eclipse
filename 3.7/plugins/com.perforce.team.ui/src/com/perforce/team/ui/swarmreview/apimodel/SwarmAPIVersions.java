package com.perforce.team.ui.swarmreview.apimodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SwarmAPIVersions {
	private String[] apiVersions;
	private String version;
	private String year;
	
	public String[] getApiVersions() {
		return apiVersions;
	}
	public void setApiVersions(String[] apiVersions) {
		this.apiVersions = apiVersions;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public List<String> getAPIVersionsList(){
		List<String> versionsList = new ArrayList<String>();
		if(apiVersions != null && apiVersions.length > 0){
			versionsList.addAll(new ArrayList<String>(Arrays.asList(apiVersions)));
		}
		return versionsList;
	}
	
	public boolean isVersionSupported(String version){
		return getAPIVersionsList().contains(version);
	}
	
	public String toString(){
		return getAPIVersionsList().toString();
	}
	
}
