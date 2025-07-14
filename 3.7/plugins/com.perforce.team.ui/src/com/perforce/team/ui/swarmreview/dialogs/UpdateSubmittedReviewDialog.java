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

import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4SubmittedFile;
import com.perforce.team.ui.swarmreview.restapi.Result;
import com.perforce.team.ui.views.SubmittedView;

public class UpdateSubmittedReviewDialog extends UpdateReviewDialog<IP4SubmittedChangelist> {

	public UpdateSubmittedReviewDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	TableViewer createFilesToBeReviewedGrid(final Composite parent) {
		checkboxTableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		Table filesTable = checkboxTableViewer.getTable();
		//filesTable.setLinesVisible(true);
		filesTable.setHeaderVisible(true);

		GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd_table.heightHint = 248;
		filesTable.setLayoutData(gd_table);
		setupColumns();
		return checkboxTableViewer;
	}
	
	TableViewer createShelvedFilesToBeReviewedGrid(final Composite parent) {
		
		shelfTableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		Table shelvedTable = shelfTableViewer.getTable();
		shelvedTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		shelvedTable.setHeaderVisible(true);
		//setupColumns(false);
		
		
		return checkboxTableViewer;
	}
	
	@Override
	protected void okPressed() {
		try{
			Object[] checkedFiles = getSelectedItems();
			if (checkedFiles == null || checkedFiles.length == 0) {
				MessageDialog.openInformation(this.getShell(), "Review", "No files selected for review");
			} else {
				Result<Boolean> result = controller.requestReview(checkedFiles, null);
				if(result.isSuccessful()){
					SubmittedView.getView().refresh();
				}
				MessageDialog.openError(this.getShell(), "Progress", result.getMessage());
				super.okPressed();
			}
		}catch(Exception ex){
			MessageDialog.openInformation(this.getShell(), "Update Review", ex.getMessage());
			super.okPressed();
		}
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
	Object[] getSelectedItems() {
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
