/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.refactor;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Folder;
import com.perforce.team.core.p4java.P4JavaSysFileCommandsHelper;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.PerforceTestsPlugin;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.p4java.actions.AddAction;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.ImportProjectAction;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.MoveProjectOperation;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class MoveDeleteNew2Test3 extends ProjectBasedTestCase {

    private IProject otherProject = null;
    private final Object lock = new Object();

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        Utils.getUIStore().setValue(
                IPerforceUIConstants.PREF_REFACTOR_USE_MOVE, true);
        // Reconnect to each so that p4jserver is updated
        for (IP4Connection connection : P4Workspace.getWorkspace()
                .getConnections()) {
            connection.refreshServer();
            connection.connect();
        }

        IClient client = createConnection().getClient();

        for (int i = 0; i < 5; i++) {
            addFile(client,
                    project.getFile(new Path("images/test" + i + ".gif")));
        }
        addFile(client, project.getFile(new Path("META-INF/MANIFEST.MF")));
        addFile(client, project.getFile("p4eclipse.properties"));
        addFile(client, project.getFile("plugin.xml"));
        addFile(client, project.getFile(new Path("src/com/perforce/test.txt")));
        addDepotFile(client,
                "//depot/p08.1/p4-eclipse/com.perforce.team.ui/src/com/perforce/test.txt");
    }
    
    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#tearDown()
     */
    @Override
    public void tearDown() throws Exception {
        try {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_REFACTOR_USE_MOVE, false);
            if (otherProject != null) {
                try {
                    otherProject.refreshLocal(IResource.DEPTH_INFINITE, null);
                    otherProject.accept(new IResourceVisitor() {

                        public boolean visit(IResource resource)
                                throws CoreException {
                            ResourceAttributes attrs = resource
                                    .getResourceAttributes();
                            if (attrs != null) {
                                attrs.setReadOnly(false);
                                try {
                                    resource.setResourceAttributes(attrs);
                                } catch (CoreException e) {
                                }
                            }
                            return true;
                        }
                    });
                    otherProject.delete(true, true, null);
                } catch (CoreException e) {
                    handle(e);
                }
                assertFalse(otherProject.exists());
            }
        } finally {
            super.tearDown();
        }
    }

    /**
     * Test renaming the same file twice, would fail with 'classic move'
     */
    public void testDoubleRename() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFile fromFile = project.getFile("p4eclipse.properties");
        assertTrue(fromFile.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(fromFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        IPath fromLocation = fromFile.getFullPath();
        assertNotNull(fromLocation);

        IFile toFile = project.getFile("test_rename"
                + System.currentTimeMillis() + ".properties");
        assertFalse(toFile.exists());

        try {
            fromFile.move(toFile.getFullPath(), true, null);
        } catch (CoreException e) {
            assertFalse(true);
        }

        check(FileAction.MOVE_DELETE, p4File);

        assertTrue(toFile.exists());

        IP4Resource resource2 = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource2);
        assertTrue(resource2 instanceof IP4File);
        IP4File p4File2 = (IP4File) resource2;

        check(FileAction.MOVE_ADD, p4File2);

        assertFalse(fromFile.exists());

        IFile toFile2 = project.getFile("test2_rename"
                + System.currentTimeMillis() + ".properties");
        assertFalse(toFile2.exists());

        try {
            toFile.move(toFile2.getFullPath(), true, null);
        } catch (CoreException e) {
            e.printStackTrace();
            assertFalse("Core exception thrown moving back", true);
        }
        assertFalse(toFile.exists());
        assertTrue(toFile2.exists());

        IP4Resource resource3 = P4Workspace.getWorkspace().getResource(toFile2);
        assertNotNull(resource3);
        assertTrue(resource3 instanceof IP4File);
        IP4File p4File3 = (IP4File) resource3;

        check(FileAction.MOVE_ADD, p4File3);
        assertFalse(p4File2.isOpened());
        check(FileAction.MOVE_DELETE, p4File);
    }

    

    private IProject createOtherProject(String path) {
        IProject project = null;
        ImportProjectAction checkout = new ImportProjectAction();
        IP4Connection connection = new P4Connection(parameters);
        connection.setOffline(false);
        connection.login(parameters.getPassword());
        connection.connect();
        assertFalse(connection.isOffline());
        assertTrue(connection.isConnected());
        IP4Folder folder = new P4Folder(connection, null, path);
        assertNotNull(folder.getClient());
        assertNotNull(folder.getRemotePath());
        folder.updateLocation();
        assertNotNull(folder.getLocalPath());
        StructuredSelection selection = new StructuredSelection(folder);
        Action wrapAction = new Action() {
        };
        wrapAction.setEnabled(false);
        checkout.selectionChanged(wrapAction, selection);
        assertTrue(wrapAction.isEnabled());

        checkout.runAction(new NullProgressMonitor(), false);

        String name = folder.getName();

        P4Collection collection = new P4Collection(new IP4Resource[] { folder });
        collection.forceSync(new NullProgressMonitor());

        project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
        assertNotNull(project);
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e1) {
            assertFalse(true);
        }
        assertTrue(project.exists());
        assertTrue(project.isAccessible());
        assertTrue(project.isOpen());
        this.otherProject = project;
        try {
            addFile(this.otherProject.getFile(new Path("META-INF/MANIFEST.MF")));
        } catch (Exception e) {
            handle(e);
        }
        return project;
    }

    private void check(FileAction action, IP4File file) {
        assertSame(action, file.getAction());
        assertTrue(file.isOpened());
        List<IP4Resource> pendinglist = Arrays.asList(
        		file.getConnection().getPendingChangelist(0).members());
        if(action==FileAction.MOVE_DELETE || action==FileAction.MOVE_ADD){
	        if(!pendinglist.contains(file)){
	        	// see P4Resource.hashCode() to why. Simply, the localPath is used for hash code, which result moved file not in the list.
	        	// The following code will check the MOVE_* files in the pending change list.
	        	boolean match=false;
	        	for(IP4Resource pending: pendinglist){
	        		if(pending.getRemotePath().equals(file.getMovedFile())){
	        			match=true;
	        			break;
	        		}
	        	}
	        	assertTrue(match);
	        }
        }else{
        	assertTrue(pendinglist.contains(file)); 
        }
        switch (action) {
        case DELETE:
        case MOVE_DELETE:
            assertTrue(file.openedForDelete());
            break;
        case BRANCH:
        case ADD:
        case MOVE_ADD:
            assertTrue(file.openedForAdd());
            break;
        case INTEGRATE:
        case EDIT:
            assertTrue(file.openedForEdit());
        default:
            break;
        }
    }

    private int check(FileAction action, IP4File[] files) {
        int counted = 0;
        for (IP4File file : files) {
            check(action, file);
            counted++;
        }
        return counted;
    }

    private int check(FileAction action, IP4Folder folder) {
        int counted = 0;
        for (IP4Resource resource : folder.members()) {
            if (resource instanceof IP4Folder) {
                counted += check(action, (IP4Folder) resource);
            } else {
                assertTrue(resource instanceof IP4File);
                check(action, (IP4File) resource);
                counted++;
            }
        }
        return counted;
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.core";
    }

}
