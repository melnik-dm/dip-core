/******************************************************************************* * 
 * Copyright (c) 2025 Denis Melnik.
 * Copyright (c) 2025 Ruslan Sabirov.
 * Copyright (c) 2025 Andrei Motorin.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package ru.dip.ui.action.duplicate;

import java.io.IOException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.ide.undo.MoveResourcesOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.exception.DeleteDIPException;
import ru.dip.core.exception.RenameDIPException;
import ru.dip.core.exception.SaveTableDIPException;
import ru.dip.core.exception.TmpCopyException;
import ru.dip.core.link.LinkInteractor;
import ru.dip.core.model.DipContainer;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.ui.CompositeFactory;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.Messages;
import ru.dip.ui.dialog.DeleteDialog;

public class DuplicateDialog extends Dialog {
	
	private WorkbenchLabelProvider fWorkbenchLabelProvider = new WorkbenchLabelProvider();	
	protected DipContainer fContainer;
	protected IResource fFirst;
	protected IResource fSecond;
	
	public DuplicateDialog(Shell parentShell, DipContainer container, IResource first,
			IResource second) {
		super(parentShell);
		fContainer = container;
		fFirst = first;
		fSecond = second;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.DuplicateDialog_Shell_title);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = createMainComposite(parent);				
		createLabelComposite(composite);	
		Composite objectsComopsite = createObjectComposite(composite);	
		if (fFirst != null) {
			duplicateObjectComposite(objectsComopsite, fFirst);
		}
		duplicateObjectComposite(objectsComopsite, fSecond);	
		return composite;
	}
	
	private Composite createMainComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginLeft = 10;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));	
		return composite;
	}

	private void createLabelComposite(Composite composite) {
		createMessageLabel(composite);	
		new Label(composite, SWT.NONE);	
		Composite objectsComopsite = new Composite(composite, SWT.NONE);
		GridLayout layout =  new GridLayout(3, false);
		layout.horizontalSpacing = 30;
		objectsComopsite.setLayout(layout);
	}
	
	protected void createMessageLabel(Composite composite) {
		String message = Messages.DuplicateDialog_Directory_label; 
		if (fContainer instanceof DipProject) {
			message = Messages.DuplicateDialog_Project_label;
		}
		
		int offset = message.length();		
		String id =  DipUtilities.fullIDWithoutRevision(fContainer);
		int length = id.length();
		message += id + Messages.DuplicateDialog_Message
				+ Messages.DuplicateDialog_Message1
				+ Messages.DuplicateDialog_Message2;
		
		StyledText messageLabel = new StyledText(composite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);		
		messageLabel.setText(message);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 640;
		messageLabel.setLayoutData(gd);
		StyleRange range = new StyleRange(offset, length, null, null, SWT.BOLD);
		messageLabel.setStyleRange(range);
	}
	
	private Composite createObjectComposite(Composite composite) {
		Composite objectsComopsite = new Composite(composite, SWT.NONE);
		GridLayout layout =  new GridLayout(3, false);
		layout.horizontalSpacing = 30;
		objectsComopsite.setLayout(layout);
		return objectsComopsite;
	}
	
	private void duplicateObjectComposite(Composite composite, IResource resource) {
		createObjectLabel(composite, resource);
		createRenameHyeperlink(composite, resource);				
		createDeleteHyperlink(composite, resource);
	}
	
	private void createObjectLabel(Composite composite, IResource resource) {
		Composite labelComposite = CompositeFactory.noIndent(composite, 2, false);	
		Label imageLabel = new Label(labelComposite, SWT.NONE);
		imageLabel.setImage(fWorkbenchLabelProvider.getImage(resource));
		Label label = new Label(labelComposite, SWT.NONE);
		String resMessage2 = resource.getName();
		if (resource instanceof IContainer) {
			try {
				if (((IContainer) resource).members().length == 0){
					resMessage2 += Messages.DuplicateDialog_EmptyLabel;
				}
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}
		
		label.setText(resMessage2);
	}
	
	private void createRenameHyeperlink(Composite composite, IResource resource) {
		Hyperlink renameLink = new Hyperlink(composite, SWT.NONE);
		renameLink.setText(Messages.DuplicateDialog_Rename_link);
		renameLink.setForeground(ColorProvider.BLUE);
		renameLink.addHyperlinkListener(new IHyperlinkListener() {
			
			@Override
			public void linkExited(HyperlinkEvent e) {}
			
			@Override
			public void linkEntered(HyperlinkEvent e) {}
			
			@Override
			public void linkActivated(HyperlinkEvent e) {
				RenameResourceDialog dialog = new RenameResourceDialog(getShell(), resource);
				if (dialog.open() == OK) {
					String newName = dialog.getNewName();
					rename(resource, newName);	
					okPressed();
				}
			}
		});
	}
	
	private void createDeleteHyperlink(Composite composite, IResource resource) {
		Hyperlink deleteLink = new Hyperlink(composite, SWT.NONE);
		deleteLink.setText(Messages.DuplicateDialog_Delete_link);
		deleteLink.setForeground(ColorProvider.MAGENTA);
		deleteLink.addHyperlinkListener(new IHyperlinkListener() {
			
			@Override
			public void linkExited(HyperlinkEvent e) {}
			
			@Override
			public void linkEntered(HyperlinkEvent e) {}
			
			@Override
			public void linkActivated(HyperlinkEvent e) {
				String message =  TagStringUtilities.format(DeleteDialog.DeleteResourcesWizard_label_single, 
						resource);
				boolean confirm = MessageDialog.openQuestion(getShell(), Messages.DuplicateDialog_Delete_question_title, message);
				if (confirm) {
					delete(resource);
					okPressed();
				}					
			}
		});
	}
	
	private void rename(IResource resource, String newName) {
		IDipElement element = DipUtilities.findElement(resource);
		if (element != null) {		
			renameElement(element, newName);
		} else {
			renameResource(resource, newName);
		}
	}
	
	private void renameElement(IDipElement element, String newName) {
		try {
			DipUtilities.renameElement(element, newName, false, getShell());
		} catch (RenameDIPException e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), Messages.DuplicateDialog_Rename_error_dialog_title, e.getMessage());
		} catch (SaveTableDIPException e) {
			e.printStackTrace();
			MessageDialog.openError(getShell(), Messages.DuplicateDialog_Save_table_error_dialog_title, e.getMessage());
		}
	}
	
	private void renameResource(IResource resource, String newName) {	
		IPath newPath = resource.getFullPath().removeLastSegments(1).append(newName);
		MoveResourcesOperation mp = new MoveResourcesOperation(resource, newPath, "Move resource"); //$NON-NLS-1$
		try {
			mp.execute(null, WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
		} catch (ExecutionException e) {
			DipCorePlugin.logError("Rename Error " + e.getMessage()); //$NON-NLS-1$
			e.printStackTrace();
		}
	}
	
	private void delete(IResource resource) {
		IDipElement element = DipUtilities.findElement(resource);
		if (element != null) {
			deleteElement(element);
		} else {
			deleteResource(resource);
		}	
	}
	
	private void deleteElement(IDipElement element) {
		try {
			DipProject project = element.dipProject();
			DipUtilities.deleteElements(new IDipElement[] {element}, false, false, getShell());
			if (project != null) {
				LinkInteractor.instance().checkLinksAfterDelete(project);	
			}
		} catch (DeleteDIPException | TmpCopyException e) {
			e.printStackTrace();
		}
	}
	
	private void deleteResource(IResource resource) {
		try {
			resource.delete(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		button.setEnabled(false);
	}
	
	public void forceRename() {
		String name = fSecond.getName();
		String extension = null;
		if (fSecond instanceof IFile) {
			extension = fSecond.getFileExtension();
		}
		if (extension != null) {
			name = fSecond.getName().substring(0, name.length() - extension.length() - 1);
		} 		
		int n = 1;
		while (true) {
			String newName = name + "_" + String.valueOf(n); //$NON-NLS-1$
			if (extension != null) {
				newName += "." + extension; //$NON-NLS-1$
			}
			try {
				if (ResourcesUtilities.contains(fContainer.resource(), newName)) {
					n++;
				} else {
					rename(fSecond, newName);
					MessageDialog.openInformation(getShell(), Messages.DuplicateDialog_Rename_message_title, 
							Messages.DuplicateDialog_Rename_message1 + fSecond.getName() + Messages.DuplicateDialog_Rename_message2 + newName);
					break;
				}
			} catch (CoreException | IOException e) {
				e.printStackTrace();
			}	
		}
	}

}
