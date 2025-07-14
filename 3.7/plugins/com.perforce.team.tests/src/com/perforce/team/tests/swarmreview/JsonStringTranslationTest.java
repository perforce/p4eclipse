package com.perforce.team.tests.swarmreview;

import java.util.List;

import org.junit.Assert;

import com.perforce.team.ui.swarmreview.apimodel.Review;
import com.perforce.team.ui.swarmreview.apimodel.SwarmReview;
import com.perforce.team.ui.swarmreview.apimodel.SwarmReviewList;
import com.perforce.team.ui.swarmreview.util.SwarmReviewUtil;

import junit.framework.TestCase;

public class JsonStringTranslationTest extends TestCase {

	public void testJsonStringToSwarmReviewTranslation() {
		String str = "{" + "review: {id: 12204,author: bruno,changes: [10667],commits: [10667],commitStatus: [],"
				+ "created: 1399325913,"
				+ "deployDetails: [],deployStatus: null,description: 'Adding .jar that should have been included in r10145\n',"
				+ "participants: {alex_qc: [],bruno: {vote: 1,required: true}, vera: []}, " + "pending: false,"
				+ "projects: { swarm: [main] }," + "state: archived,stateLabel: Archived," + "testDetails: {"
				+ "url: 'http://jenkins.example.com/job/project_ci/123/'" + "  },"
				+ "testStatus: null,type: default,updated: 1399325913" + "}" + "}";

		SwarmReview sw = SwarmReviewUtil.generateSwarmReviewFromJSON(str);
		Assert.assertNotNull(sw);
		Review review = sw.getReview();
		Assert.assertNotNull(review);
		Assert.assertEquals("Adding .jar that should have been included in r10145\n", review.getDescription());

	}

	public void testJsonStringToSwarmReviewListTranslation() {
		String str = "{lastSeen: 12206," + "reviews:[" + "{" + "id: 12206, author: swarm,changes: [12205],"
				+ "comments: 0,commits: [],"
				+ "commitStatus: [], created: 1402507043, deployDetails: [],deployStatus: null,"
				+ "created: 1402507043, deployDetails: [],deployStatus: null,"
				+ "description: 'Review Description\n', participants: {},"
				+ "pending: true,  projects: [],  state: needsReview, stateLabel: 'Needs Reviewhghg',"
				+ "testDetails: [], testStatus: null, type: default,  updated: 1402518492"

				+ "}" + "]," + "totalCount:1}";

		SwarmReviewList sw = SwarmReviewUtil.generateSwarmReviewListFromJSON(str);
		Assert.assertNotNull(sw);
		List<Review> reviews = sw.getReviews();
		Assert.assertNotNull(reviews);
		Assert.assertEquals(1, reviews.size());
		

		str = "{" + "lastSeen: 33,"
				+ "reviews: [{id: 33,author: super,changes: [32,34,35,36, 112,121,122,123,124, 128,129,130,131,132,133],"
				+ "comments: [0,0 ],commits: [],commitStatus: [],created: 1504790227,deployDetails:[],deployStatus:null,"
				+ " description:'TestingbyTP#',groups:[],participants:{super:[]},pending:true,"
				+ "projects:{sampledata-project:[main]},"
				+ "state :needsReview,stateLabel:'NeedsReview' ,'':[],testStatus:null,"
				+ "updated: 1505829050,updateDate: '2017-09-19T14:50:50+01:00'" + "}" + "]," + "totalCount: 1" + "}";

		sw = SwarmReviewUtil.generateSwarmReviewListFromJSON(str);
		Assert.assertNotNull(sw);
		reviews = sw.getReviews();
		Assert.assertNotNull(reviews);
		Assert.assertEquals(1, reviews.size());
	}
}
