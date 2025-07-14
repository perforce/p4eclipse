package com.perforce.team.tests.shelve;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.junit.Test;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.tests.ProjectBasedTestCase;

public class ShelveAndRevertTest extends ProjectBasedTestCase {
    
    private IP4Connection connection;
    private IFile fileToAdd;
    private IFile fileToRename;
    private IFile fileRenamed;
    private IFile fileToEdit;
    private IP4File p4FileToEdit;
    private IP4File p4FileToRename;
    private IP4File p4FileToAdd;
    private IP4File p4FileRenamed;
    private IP4PendingChangelist changelist;
    
    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        //##### SETTING UP SERVER CONNECTION #######
        super.setUp();
        connection = createConnection();
        IClient client = connection.getClient();
        
        //##### ADDING FILES TO THE LOCAL FS AND TO THE SERVER #######
        addFileWithContent(client, project.getFile("FileToRename"), "content");
        addFileWithContent(client, project.getFile("FileToEdit"), "content");
        openFileWithContent(client, project.getFile("FileToAdd"), "content");
        
        //##### OPENING A FILE FOR EDIT #######
        fileToEdit = project.getFile("FileToEdit");
        IP4Resource resourceToEdit = connection.getResource(fileToEdit);
        assertTrue(resourceToEdit instanceof IP4File);
        p4FileToEdit = (IP4File) resourceToEdit;
        assertFalse(p4FileToEdit.isOpened());
        p4FileToEdit.edit();
        p4FileToEdit.refresh();
        assertTrue(p4FileToEdit.isOpened());
       
      //##### RENAMING A FILE #######
        fileToRename = project.getFile("FileToRename");
        IP4Resource resourceToRename = connection.getResource(fileToRename);
        assertTrue(resourceToRename instanceof IP4File);
        p4FileToRename = (IP4File) resourceToRename;
        fileRenamed = project.getFile("FileRenamed");
        IP4Resource resourceRenamed = connection.getResource(fileRenamed);
        p4FileRenamed = (IP4File) resourceRenamed;
        assertFalse(p4FileToRename.isOpened());
        p4FileToRename.move(p4FileRenamed, true);
        p4FileToRename.refresh();  
        
      //##### OPENING A FILE FOR ADD #######
        fileToAdd = project.getFile("FileToAdd");
        IP4Resource resourceToAdd = connection.getResource(fileToAdd);
        assertTrue(resourceToAdd instanceof IP4File);
        p4FileToAdd = (IP4File) resourceToAdd;
        
        //##### CREATING A PENDING CHANGELIST #######
        IP4File[] filesForShelve = new IP4File[] { p4FileToEdit, p4FileToRename, p4FileRenamed, p4FileToAdd };
        String changelistName = "ShelveTest.testShelveWithoutRevert";
        changelist = connection.createChangelist(changelistName, filesForShelve);
        assertNotNull(changelist);
        assertTrue(changelist.getId() > 0);
        assertFalse(changelist.isShelved());
        
        //##### CHECKING PENDING CHANGELIST HAS ALL THE CHANGES IN #######
        assertTrue(Arrays.asList(changelist.members()).contains(p4FileToEdit));
        assertTrue(Arrays.asList(changelist.members()).contains(p4FileToRename));
        assertTrue(Arrays.asList(changelist.members()).contains(p4FileToAdd));
        assertTrue(Arrays.asList(changelist.members()).contains(p4FileRenamed));
        
        //##### CHECKING THE FILES IN THE CHANGELIST ARE NOT SHELVED #######
        assertEquals(0, p4FileToEdit.getShelvedVersions().length);
        assertEquals(0, p4FileToRename.getShelvedVersions().length);
        assertEquals(0, p4FileToAdd.getShelvedVersions().length);
        assertEquals(0, p4FileRenamed.getShelvedVersions().length);
        assertEquals(0, changelist.getShelvedChanges().members().length);
    }

    /**
     * Test Eclipse shelving functionality.
     */
    @Test
    public void testShelveAndRevert() {
        
        try {
            //##### TEST ######### 
            changelist.shelve(new IP4File[] { p4FileToEdit, p4FileToAdd, p4FileRenamed, p4FileToRename});
            P4Collection revertAllFile = new P4Collection(new IP4Resource[] { p4FileToEdit, p4FileRenamed, p4FileToRename});
            revertAllFile.revert(false);
            assertFalse(p4FileToEdit.isOpened());
            assertFalse(p4FileRenamed.isOpened());
            assertNotNull(revertAllFile.members());
            P4Collection revertAddFile = new P4Collection(new IP4Resource[] { p4FileToAdd});
            revertAddFile.revert(true);
            assertFalse(p4FileToAdd.isOpened());
            assertNotNull(revertAddFile.members());
            p4FileToEdit.refresh();
            p4FileToAdd.refresh();
            p4FileRenamed.refresh();
            p4FileToRename.refresh();
            
            //##### CHECKING EDIT FILE WAS SHELVED CORRECTLY #######
            IP4ShelveFile[] shelvedVersionsToEdit = p4FileToEdit.getShelvedVersions();
            assertNotNull(shelvedVersionsToEdit);
            assertEquals(1, shelvedVersionsToEdit.length);
            IP4ShelveFile shelvedFileToEdit = shelvedVersionsToEdit[0];
            assertFalse(shelvedFileToEdit.isContainer());
            assertNotNull(shelvedFileToEdit.getName());
            assertNotNull(shelvedFileToEdit.getRevision());
            assertNotNull(shelvedFileToEdit.getChangelist());
      
            //##### CHECKING RENAMED FILE WAS SHELVED CORRECTLY #######
            IP4ShelveFile[] shelvedVersionsToRename = p4FileToRename.getShelvedVersions();
            assertNotNull(shelvedVersionsToRename);
            assertEquals(1, shelvedVersionsToRename.length);
            IP4ShelveFile shelvedFileToRename = shelvedVersionsToRename[0];
            assertFalse(shelvedFileToRename.isContainer());
            assertNotNull(shelvedFileToRename.getName());
            assertNotNull(shelvedFileToRename.getRevision());
            assertNotNull(shelvedFileToRename.getChangelist());
            
            //##### CHECKING ADDED FILE WAS SHELVED CORRECTLY #######
            IP4ShelveFile[] shelvedVersionsToAdd = p4FileToAdd.getShelvedVersions();
            assertNotNull(shelvedVersionsToAdd);
            assertEquals(1, shelvedVersionsToAdd.length);
            IP4ShelveFile shelvedFileToAdd = shelvedVersionsToAdd[0];
            assertFalse(shelvedFileToAdd.isContainer());
            assertNotNull(shelvedFileToAdd.getName());
            assertNotNull(shelvedFileToAdd.getRevision());
            assertNotNull(shelvedFileToAdd.getChangelist());
            
            //###### CHECKING CHANGELIST SHELVED CORRECTLY #######
            assertTrue(changelist.isShelved());
            assertEquals(4, changelist.getShelvedChanges().members().length);
            
            //##### CHECKING FILES WERE REVERTED ON DISK ######
            File localFileToAdd = new File(fileToAdd.getLocation().toString());
            File localFileRenamed = new File(fileRenamed.getLocation().toString());
            File localFileToRename = new File(fileToRename.getLocation().toString());
            assertFalse(localFileToAdd.exists());
            assertFalse(localFileRenamed.exists());
            assertTrue(localFileToRename.exists());
            
             //##### REFRESHING THE SHELVED FILES #######
            try {
                shelvedFileToEdit.refresh();
                shelvedFileToEdit.refresh(0);
                
                shelvedFileToRename.refresh();
                shelvedFileToRename.refresh(0);
                
                shelvedFileToAdd.refresh();
                shelvedFileToAdd.refresh(0);
            } catch (Throwable e) {
                handle(e);
            }
        } finally {
            if (changelist != null) {
                changelist.revert();
                changelist.deleteShelved();
                changelist.delete();
            }
        }
    }
    
    
    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.tests.shelve";
    }
    
}

