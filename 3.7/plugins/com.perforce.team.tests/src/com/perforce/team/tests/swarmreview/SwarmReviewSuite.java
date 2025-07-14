/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.swarmreview;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ RequestNewSwarmReviewTest.class, JsonStringTranslationTest.class, SwarmConnectionTest.class
	})
public class SwarmReviewSuite {

}
