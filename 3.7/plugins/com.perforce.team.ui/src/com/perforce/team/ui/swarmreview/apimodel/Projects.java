package com.perforce.team.ui.swarmreview.apimodel;

import java.util.List;


public class Projects {

	private List<String> swarm = null;

	//private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	public List<String> getSwarm() {
		return swarm;
	}

	public void setSwarm(List<String> swarm) {
		this.swarm = swarm;
	}
}
