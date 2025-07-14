package com.perforce.team.ui.swarmreview.apimodel;

public class SwarmReview {

	private Review review;

	public Review getReview() {
		return review;
	}

	public String getDescription() {
		return getReview() != null ? getReview().getDescription() : "";
	}

}
