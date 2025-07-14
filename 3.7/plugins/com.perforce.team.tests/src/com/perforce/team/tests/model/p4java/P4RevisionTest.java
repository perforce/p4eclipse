/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.p4java.exception.AccessException;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.P4Revision;
import com.perforce.team.tests.ConnectionBasedTestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4RevisionTest extends ConnectionBasedTestCase {

	private IClient localclient;
    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        localclient = createConnection().getClient();
        addDepotFile(localclient,
                "//depot/p08.1/p4-eclipse/com.perforce.team.plugin/plugin.xml");
    }
    
    @Override
    public void tearDown() throws Exception, AccessException{
    	IChangelist changeList = createPendingChangelist(localclient);
    	deleteFile(localclient, changeList, "//depot/p08.1/p4-eclipse/com.perforce.team.plugin/plugin.xml");
    	super.tearDown();
    }

    /**
     * Basic test of p4 revision
     */
    public void testBasic() {
        IP4Connection connection = createConnection();
        IP4File file = connection
                .getFile("//depot/p08.1/p4-eclipse/com.perforce.team.plugin/plugin.xml");
        assertNotNull(file);
        IFileRevisionData[] history = file.getHistory();
        assertNotNull(history);
        assertTrue(history.length > 0);
        P4Revision revision = new P4Revision(connection, history[0]);
        assertNotNull(revision.getAuthor());
        assertNotNull(revision.getComment());
        assertNotNull(revision.getClient());
        assertNotNull(revision.getContentIdentifier());
        assertNotNull(revision.getName());
        assertNotNull(revision.getRemotePath());
        assertNotNull(revision.getType());
        assertNotNull(revision.getAction());
        assertTrue(revision.getChangelist() > 0);
        assertTrue(revision.getRevision() > 0);
        try {
            assertNull(revision.withAllProperties(new NullProgressMonitor()));
        } catch (CoreException e) {
            handle(e);
        }
        assertFalse(revision.isPropertyMissing());
        try {
            assertNotNull(revision.getStorage(new NullProgressMonitor()));
        } catch (CoreException e) {
            handle(e);
        }
    }
}
