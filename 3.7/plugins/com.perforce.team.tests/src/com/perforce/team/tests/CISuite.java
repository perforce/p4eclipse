/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.junit.runners.model.Statement;

import com.perforce.team.tests.actions.p4java.ActionSuite;
import com.perforce.team.tests.changeset.ChangeSetSuite;
import com.perforce.team.tests.client.ClientSuite;
import com.perforce.team.tests.core.CoreSuite;
import com.perforce.team.tests.dialogs.DialogSuite;
import com.perforce.team.tests.diff.DiffSuite;
import com.perforce.team.tests.extensions.ExtensionsSuite;
import com.perforce.team.tests.folder.FolderSuite;
import com.perforce.team.tests.map.MapSuite;
import com.perforce.team.tests.model.p4java.ModelSuite;
import com.perforce.team.tests.mylyn.MylynSuite;
import com.perforce.team.tests.p4api.ApiSuite;
import com.perforce.team.tests.patch.PatchSuite;
import com.perforce.team.tests.preferences.PreferencesSuite;
import com.perforce.team.tests.project.ProjectSuite;
import com.perforce.team.tests.refactor.RefactorSuite;
import com.perforce.team.tests.search.SearchSuite;
import com.perforce.team.tests.shelve.ShelveSuite;
import com.perforce.team.tests.swarmreview.LocalTestUtil;
import com.perforce.team.tests.swarmreview.SwarmReviewSuite;
import com.perforce.team.tests.synchronize.SynchronizeSuite;
import com.perforce.team.tests.timelapse.TimeLapseSuite;
import com.perforce.team.tests.views.branches.BranchesSuite;
import com.perforce.team.tests.views.console.ConsoleSuite;
import com.perforce.team.tests.views.depots.DepotSuite;
import com.perforce.team.tests.views.history.HistorySuite;
import com.perforce.team.tests.views.jobs.JobSuite;
import com.perforce.team.tests.views.labels.LabelSuite;
import com.perforce.team.tests.views.pending.PendingSuite;
import com.perforce.team.tests.views.submitted.SubmittedSuite;
import com.perforce.team.tests.wizards.WizardSuite;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
@RunWith(Suite.class)
@SuiteClasses({
		 ActionSuite.class, ApiSuite.class, DialogSuite.class,
		 BranchesSuite.class,
		 ChangeSetSuite.class,
		 ClientSuite.class, ConsoleSuite.class,
		 CoreSuite.class,
		 DepotSuite.class, DiffSuite.class,
		 ExtensionsSuite.class, FolderSuite.class, HistorySuite.class,
		 JobSuite.class, LabelSuite.class, MapSuite.class,
		 ModelSuite.class, MylynSuite.class, PendingSuite.class,
		 PreferencesSuite.class, PatchSuite.class, ProjectSuite.class,
		 RefactorSuite.class,
		 SearchSuite.class, ShelveSuite.class,
		 SubmittedSuite.class,
		 SynchronizeSuite.class, TimeLapseSuite.class, WizardSuite.class,
		 SwarmReviewSuite.class
})
public class CISuite extends TestCase {
	
	private static LocalTestUtil localP4d = LocalTestUtil.newInstance();
	@ClassRule
	public static TearDownSuite tearDownSuite = new TearDownSuite();
	

	/**
	 * Suite
	 * 
	 * @return - Test
	 */
	public static Test suite() {
		return new CISuite();
	}

	private static class TearDownSuite implements TestRule {

		public void cleanup() {
			while (Files.exists(Paths.get("resource/tmp"))) {
				try {
					FileUtils.forceDelete(new File("resource/tmp"));
				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {

				}
			}
		}

		@Override
		public Statement apply(Statement arg0, Description arg1) {
			return new TestStatement(arg0);
		}

		public class TestStatement extends Statement {

			private final Statement statement;

			public TestStatement(Statement statement) {
				this.statement = statement;
			}

			@Override
			public void evaluate() throws Throwable {
				statement.evaluate();
				Thread.sleep(100);
				Runnable runnable = new Runnable() {
					public void run() {
						cleanup();
					}
				};
				new Thread(runnable).start();
			}
		}
	}

}
