package com.perforce.team.ui.swarmreview.apimodel;

import java.util.List;

public class SwarmReviewList {

	private List<Review> reviews;
	private int lastSeen;

	public List<Review> getReviews() {
		return reviews;
	}

	public int lastSeen() {
		return lastSeen;
	}

	public boolean isEmpty() {
		return reviews.isEmpty();
	}

}
