/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.perforce.team.ui.dialogs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A buffer for a workspace resource.
 */
public class BufferedResourceNode extends ResourceNode {

    private boolean fDirty = false;
    private IFile fDeleteFile;

    /**
     * Creates a <code>ResourceNode</code> for the given resource.
     * 
     * @param resource
     *            the resource
     */
    public BufferedResourceNode(IResource resource) {
        super(resource);
    }

    @Override
    protected IStructureComparator createChild(IResource child) {
        return new BufferedResourceNode(child);
    }

    @Override
    public void setContent(byte[] contents) {
        fDirty = true;
        super.setContent(contents);
    }

    /**
     * Commits buffered contents to resource.
     */
    public void commit(IProgressMonitor pm) throws CoreException {
        if (fDirty) {

            if (fDeleteFile != null) {
                fDeleteFile.delete(true, true, pm);
                return;
            }

            IResource resource = getResource();
            if (resource instanceof IFile) {
                ByteArrayInputStream is = new ByteArrayInputStream(getContent());
                try {
                    IFile file = (IFile) resource;
                    if (file.exists())
                        file.setContents(is, false, true, pm);
                    else
                        file.create(is, false, pm);
                    fDirty = false;
                } finally {
                    if (is != null)
                        try {
                            is.close();
                        } catch (IOException ex) {
                        }
                }
            }
        }
    }

    @Override
    public ITypedElement replace(ITypedElement child, ITypedElement other) {

        if (child == null) { // add resource
            // create a node without a resource behind it!
            IResource resource = getResource();
            if (resource instanceof IFolder) {
                IFolder folder = (IFolder) resource;
                IFile file = folder.getFile(other.getName());
                child = new BufferedResourceNode(file);
            }
        }

        if (other == null && child!=null) { // delete resource
            IResource resource = getResource();
            if (resource instanceof IFolder) {
                IFolder folder = (IFolder) resource;
                IFile file = folder.getFile(child.getName());
                if (file != null && file.exists()) {
                    fDeleteFile = file;
                    fDirty = true;
                }
            }
            return null;
        }

        if (other instanceof IStreamContentAccessor
                && child instanceof IEditableContent) {
            IEditableContent dst = (IEditableContent) child;

            try {
                InputStream is = ((IStreamContentAccessor) other).getContents();
                byte[] bytes = readBytes(is);
                if (bytes != null)
                    dst.setContent(bytes);
            } catch (CoreException ex) {
            }
        }
        return child;
    }

    public static byte[] readBytes(InputStream in) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            while (true) {
                int c = in.read();
                if (c == -1)
                    break;
                bos.write(c);
            }

        } catch (IOException ex) {
            return null;

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException x) {
                }
            }
            try {
                bos.close();
            } catch (IOException x) {
            }
        }

        return bos.toByteArray();
    }
    

    @Override
    public boolean equals(Object obj) {
    	// override to prevent coverity from complaining.
    	return obj instanceof BufferedResourceNode && super.equals(obj);
    }
    
    @Override
    public int hashCode() {
    	// override to prevent coverity from complaining: FB.EQ_COMPARETO_USE_OBJECT_EQUALS
    	return super.hashCode();
    }

}
