package com.perforce.team.ui.swarmreview.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4SubmittedFile;
import com.perforce.team.ui.swarmreview.restapi.Result;
import com.perforce.team.ui.views.SubmittedView;

public class NewSubmittedReviewDialog extends NewReviewDialog<IP4SubmittedChangelist> {

	public NewSubmittedReviewDialog(Shell parentShell) {
		super(parentShell);
	}
	
	TableViewer createShelvedFilesToBeReviewedGrid(final Composite parent) {
		
		return null;
	}
	
	@Override
	protected void okPressed() {
		try{
			checkedFiles = getSelectedItems();
			String desc = descriptionTextViewer.getTextWidget().getText().trim();
			if (desc.isEmpty()) {
				MessageDialog.openInformation(this.getShell(), "Description", "Please specify a description to continue.");
			}else{
				Result<Boolean> result = controller.requestReview(checkedFiles, desc);
				MessageDialog.openInformation(this.getShell(), "Request Review", result.getMessage());
				if(result.isSuccessful()){
					updateReviewerGrid(controller.getSelectedReviewerUsers().toArray());
					SubmittedView.getView().refresh();
					super.close();
				}else{
					cancelPressed();
				}
			}
		}catch(Exception ex){
			MessageDialog.openInformation(this.getShell(), "Request Review", ex.getMessage());
			super.okPressed();
		}
	}
	
	

	TableViewer createFilesToBeReviewedGrid(final Composite parent) {
		checkboxTableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		Table filesTable = checkboxTableViewer.getTable();
		filesTable.setHeaderVisible(true);

		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_table.heightHint = 248;
		filesTable.setLayoutData(gd_table);
		setupColumns();
		return checkboxTableViewer;
	}
	
	String getDialogTitle(){
		return "The following files will be sent for review:";
	}
	
	
	void setupColumns(){
		String[] titles = { "File Name", "Revision", "Action", "File Type", "In Folder" };
		int[] bounds = { 200, 80, 80, 80, 800 };
		
		TableViewerColumn col = createTableViewerColumn(checkboxTableViewer, titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof P4SubmittedFile) {
					return ((P4SubmittedFile) element).getFile().getName();
				}
				return null;
			}
		});

		col = createTableViewerColumn(checkboxTableViewer, titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof P4SubmittedFile) {
					return String.valueOf(((P4SubmittedFile) element).getFile().getHeadRevision());
				}
				return null;
			}
		});

		col = createTableViewerColumn(checkboxTableViewer, titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof P4SubmittedFile) {
					return ((P4SubmittedFile) element).getFile().getP4JFile().getAction().toString();
				}
				return null;
			}
		});

		col = createTableViewerColumn(checkboxTableViewer, titles[3], bounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof P4SubmittedFile) {
					return ((P4SubmittedFile) element).getFile().getP4JFile().getFileType();
				}
				return null;
			}
		});
		
		col = createTableViewerColumn(checkboxTableViewer, titles[4], bounds[4], 4);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof P4SubmittedFile) {
					return ((P4SubmittedFile) element).getFile().getP4JFile().getDepotPathString();
				}
				return null;
			}
		});
	}

	@Override
	IP4Resource[] getSelectedItems() {
		List<P4SubmittedFile>  tableItems = new ArrayList<P4SubmittedFile>();
		TableItem [] titems = checkboxTableViewer.getTable().getItems();
		if(titems != null && titems.length > 0){
			for(TableItem item : titems){
				Object data = item.getData();
				if(data instanceof P4SubmittedFile){
					tableItems.add((P4SubmittedFile) data);
				}
			}
		}
		
		return tableItems.toArray(new P4SubmittedFile[tableItems.size()]);
	}
}
