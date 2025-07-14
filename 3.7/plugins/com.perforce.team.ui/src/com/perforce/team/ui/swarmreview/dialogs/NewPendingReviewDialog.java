package com.perforce.team.ui.swarmreview.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.ui.swarmreview.restapi.Result;
import com.perforce.team.ui.views.PendingView;

public class NewPendingReviewDialog extends NewReviewDialog<IP4PendingChangelist> {

	public NewPendingReviewDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);
		((CheckboxTableViewer )checkboxTableViewer).setAllChecked(true);
		return control;
	}
	
	TableViewer createFilesToBeReviewedGrid(final Composite parent) {
		checkboxTableViewer = CheckboxTableViewer.newCheckList(parent,SWT.BORDER | SWT.FULL_SELECTION);
		Table filesTable = checkboxTableViewer.getTable();
		filesTable.setHeaderVisible(true);

		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_table.heightHint = 348;
		filesTable.setLayoutData(gd_table);
		setupColumns();
		((CheckboxTableViewer )checkboxTableViewer).setAllChecked(true);

		return checkboxTableViewer;
	}
	
	TableViewer createShelvedFilesToBeReviewedGrid(final Composite parent) {
		
		shelfTableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		Table shelvedTable = shelfTableViewer.getTable();
		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_table.heightHint = 351;
		shelvedTable.setLayoutData(gd_table);
		shelvedTable.setHeaderVisible(true);
		setupShelvedColumns();
		
		return shelfTableViewer;
	}
	
	public int open(){
		int openResult = -1;
		Object[] files = controller.getReviewFiles();
		Object[] shelffiles = controller.getShelvedReviewFiles();
		if(files.length == 0 && shelffiles.length == 0){
			MessageDialog.openInformation(NewPendingReviewDialog.this.getShell(), "Add review", "No files to review");
		}else{
			Display.getDefault().asyncExec(new Runnable() {
				 public void run() {
					 if(controller.updateChangelist()){
		        		checkboxTableViewer.setInput(controller.getFilesInSelectedChangelist());
		        		checkboxTableViewer.refresh();
			        }
				 }
			});
	        openResult = super.open();
		}
		return openResult;
	}

	void setupColumns(){
		String[] titles = { "Select", "File", "Kind", "Pending Action", "Shelved" };
		int[] bounds = { 30, 800, 100, 100, 100 };
		
		TableViewerColumn col = createTableViewerColumn(checkboxTableViewer, titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return null;
			}
		});

		col = createTableViewerColumn(checkboxTableViewer, titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IP4File) {
					return ((IP4File) element).getP4JFile().getDepotPathString();
				}
				if (element instanceof IP4ShelveFile) {
					return ((IP4ShelveFile) element).getFile().getP4JFile().getDepotPathString();
				}
				return null;
			}
		});
		
		col = createTableViewerColumn(checkboxTableViewer, titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IP4File) {
					return ((IP4File) element).getP4JFile().getFileType();
				}
				if (element instanceof IP4ShelveFile) {
					return ((IP4ShelveFile) element).getFile().getP4JFile().getFileType();
				}
				return null;
			}
		});

		
		col = createTableViewerColumn(checkboxTableViewer, titles[3], bounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IP4File) {
					return ((IP4File) element).getP4JFile().getAction().toString();
				}
				if (element instanceof IP4ShelveFile) {
					return ((IP4ShelveFile) element).getFile().getP4JFile().getAction().toString();
				}
				return null;
			}
		});

		
		col = createTableViewerColumn(checkboxTableViewer, titles[4], bounds[4], 4);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof P4File) {
					return Boolean.FALSE.toString();
				}
				if (element instanceof IP4ShelveFile) {
					return "true";
				}
				return null;
			}
		});
	}
	
	void setupShelvedColumns() {
		
		String[] titlesWithNoCheck = {  "File", "Kind", "Pending Action", "Shelved" };
		Integer[] chosenLabelBounds = { 800, 100, 100, 100 };
		
		
		TableViewerColumn col = createTableViewerColumn(shelfTableViewer, titlesWithNoCheck[0], chosenLabelBounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof P4File) {
					return ((IP4File) element).getP4JFile().getDepotPathString();
				}
				if (element instanceof IP4ShelveFile) {
					return ((IP4ShelveFile) element).getFile().getP4JFile().getDepotPathString();
				}

				return null;
			}
		});
		
		// second column is for the last name
		col = createTableViewerColumn(shelfTableViewer, titlesWithNoCheck[1], chosenLabelBounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof P4File) {
					return ((IP4File) element).getP4JFile().getFileType();
				}
				if (element instanceof IP4ShelveFile) {
					return ((IP4ShelveFile) element).getFile().getP4JFile().getFileType();
				}
				
				return null;
			}
		});

		col = createTableViewerColumn(shelfTableViewer, titlesWithNoCheck[2], chosenLabelBounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof P4File) {
					return ((IP4File) element).getP4JFile().getAction().toString();
				}
				if (element instanceof IP4ShelveFile) {
					return ((IP4ShelveFile) element).getFile().getP4JFile().getAction().toString();
				}
				return null;
			}
		});

		col = createTableViewerColumn(shelfTableViewer, titlesWithNoCheck[3], chosenLabelBounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof P4File) {
					return Boolean.toString(((IP4File) element).getChangelist().isShelved());
				}
				if (element instanceof IP4ShelveFile) {
					return "true";
				}
				return null;
			}
		});

	}
	
	@Override
	protected void okPressed() {
		try{
			checkedFiles = getSelectedItems();
			String desc = descriptionTextViewer.getTextWidget().getText().trim();
		    if (checkedFiles == null || checkedFiles.length == 0) {
				MessageDialog.openInformation(this.getShell(), "Reviews", "No files selected for review");
			} else if (desc.isEmpty()) {
				MessageDialog.openInformation(this.getShell(), "Description", "Please specify a description to continue.");
			}else{
				Result<Boolean> result = controller.requestReview(checkedFiles, desc);
				MessageDialog.openInformation(this.getShell(), "Request Review", result.getMessage());
				if(result.isSuccessful()){
					updateReviewerGrid(controller.getSelectedReviewerUsers().toArray());
					PendingView.getView().refresh();
					super.okPressed();
				}else{
					cancelPressed();
				}
			}
		}catch(Exception ex){
			MessageDialog.openInformation(this.getShell(), "Request Review", ex.getMessage());
			super.okPressed();
		}
	}

	@Override
	IP4Resource[] getSelectedItems() {
		Object [] checked = ((CheckboxTableViewer)checkboxTableViewer).getCheckedElements();
		List<Object> items = new ArrayList<Object>(Arrays.asList(checked));
		if(shelfTableViewer != null){
			items.addAll(Arrays.asList(shelfTableViewer.getInput()));
		}
		List<IP4Resource> selectedFiles = new ArrayList<IP4Resource>();
		for (int i = 0; i < checked.length; i++) {
            if (checked[i] instanceof IP4Resource) {
                selectedFiles.add((IP4Resource) checked[i]);
            }
        }
		return selectedFiles.toArray(new IP4Resource[0]);
	}


	@Override
	String getDialogTitle() {
		return "The following files will be shelved for review:";
	}
}
