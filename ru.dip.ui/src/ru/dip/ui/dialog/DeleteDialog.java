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
package ru.dip.ui.dialog;

import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.ui.Messages;

public class DeleteDialog extends Dialog {

	private static final String DeleteResourcesWizard_label_single_project = Messages.DeleteDialog_Are_you_sure_delete_project;
	private static final String DeleteResourcesWizard_label_multi_projects = Messages.DeleteDialog_Are_you_sure_delete_projects;
	private static final String DeleteResourcesWizard_label_multi = Messages.DeleteDialog_Are_you_sure_delete_resources;
	public static final String DeleteResourcesWizard_label_single = Messages.DeleteDialog_Are_you_sure_delete_res;
	private static final String DeleteResourcesWizard_label_multi_links = Messages.DeleteDialog_Are_you_sure_delete_links;
	private static final String DeleteResourcesWizard_label_single_link = Messages.DeleteDialog_Are_you_sure_delete_link;
	private static final String EXTRACT_FOLDER = Messages.DeleteDialog_ExtractContentButton;
	
	// control
	private Button fReserveButton;
	private Button fDeleteProjectButton;
	private Button fExtractContentButton;   // извлечь содержимое папки перед удалением	
	// model
	private IDipElement[] fElements;
	private boolean fDeleteProject = false;
	//private boolean fExtractContent = false;
		
	public DeleteDialog(Shell parentShell, IDipElement element) {
		super(parentShell);
		fElements = new IDipElement[] {element};
		fDeleteProject = isDeleteProject();
	}
	
	public DeleteDialog(Shell parentShell, IDipElement[] elements) {
		super(parentShell);
		fElements = elements;
		fDeleteProject = isDeleteProject();
	}
	
	private boolean isDeleteProject(){
		for (IDipElement element: fElements){
			if (element instanceof DipProject){
				return true;				
			}
		}
		return false;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.DeleteDialog_Delete_resources_title);
	}
	
	@Override
	protected Point getInitialSize() {
		Point p = super.getInitialSize();
		if (p.x < 600) {
			p.x = 600;
		}
		if (p.y < 240) {
			p.y = 240;
		}		
		return p;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.WRAP);
		composite.setLayout(new GridLayout(2 ,false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		// image
		Label imageLabel = new Label(composite, SWT.NONE);
		Image questionImage = Display.getDefault().getSystemImage(SWT.ICON_QUESTION);
		imageLabel.setImage(questionImage);
		// label
		Label textLabel = new Label(composite, SWT.WRAP);
		String textMessage = getDialogMessage();
		textLabel.setText(textMessage);
		GridData labelGD = new GridData();
		labelGD.grabExcessVerticalSpace = true;
		labelGD.grabExcessHorizontalSpace = true;
		textLabel.setLayoutData(labelGD);
		
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		
		// extract button
		if (fElements.length == 1 && fElements[0] instanceof DipFolder) {
			createExtractContentButton(composite);
		}		
		
		// reserve button
		if (!DipCorePlugin.isDisableReservation() && !containsOnlyNotReservableObjects()){
			createReserveButton(composite);
		}
		
		// delete project content button
		if (fDeleteProject){
			createDeleteProjectButton(composite);
		}		
		return composite;
	}
	
	
	private boolean containsOnlyNotReservableObjects(){
		for (IDipElement element: fElements){
			DipElementType type = element.type();
			if (type == DipElementType.RPOJECT ||
					type == DipElementType.INCLUDE_FOLDER ||
					type == DipElementType.RESERVED_UNIT ||
					type == DipElementType.RESERVED_FOLDER ||
					type == DipElementType.REPORT ||
					type == DipElementType.EXPORT_CONFIG){
			} else {
				return false;
			}
		}
		return true;
	}
	

	// точно ли паблик, зачем он тут
	public String getResourceName(IDipElement element) {
		return markLTR(element.name(), "/\\:."); //$NON-NLS-1$
	}

	private static String markLTR(String string, String delimiters) {
		return TextProcessor.process(string, delimiters);
	}
	
	private void createExtractContentButton(Composite parent) {
		fExtractContentButton = new Button(parent, SWT.CHECK);
		fExtractContentButton.setText(EXTRACT_FOLDER);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalIndent = 5;
		fExtractContentButton.setLayoutData(gd);
	}
	
	private void createReserveButton(Composite parent){
		fReserveButton  = new Button(parent, SWT.CHECK);
		fReserveButton.setText(Messages.DeleteDialog_Full_delete_label);
		fReserveButton.setSelection(true);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalIndent = 5;
		fReserveButton.setLayoutData(gd);
	}
	
	private void createDeleteProjectButton(Composite parent){
		fDeleteProjectButton = new Button(parent, SWT.CHECK);
		fDeleteProjectButton.setText(Messages.DeleteDialog_delete_project_content_on_disk_label);
		GridData gd = new GridData();
		gd.horizontalIndent = 5;
		gd.horizontalSpan = 2;
		fDeleteProjectButton.setLayoutData(gd);
	}
	
	//========================
	// get message
	
	private String getDialogMessage(){
		if (containsOnlyProjects()) {
			if (fElements.length == 1) {
				return format(DeleteResourcesWizard_label_single_project, getResourceName(fElements[0]));
			} else {
				return  format(DeleteResourcesWizard_label_multi_projects, Integer.valueOf(fElements.length));
			}
		} if (containsOnlyLinks()) { 
			if (fElements.length == 1) {
				return format(DeleteResourcesWizard_label_single_link, getResourceName(fElements[0]));
			} else {
				return  format(DeleteResourcesWizard_label_multi_links, Integer.valueOf(fElements.length));
			}
		} else {
			if (fElements.length == 1) {
				return format(DeleteResourcesWizard_label_single, getResourceName(fElements[0]));
			} else {
				return format(DeleteResourcesWizard_label_multi, Integer.valueOf(fElements.length));
			}
		}		
	}
	
	private boolean containsOnlyProjects() {
		int types = getSelectedResourceTypes();
		return types == IResource.PROJECT;
	}
	
	private int getSelectedResourceTypes() {
		int types = 0;
		for (int i = 0; i < fElements.length; i++) {
			IResource res = fElements[i].resource();
			if (res == null) {
				continue;
			}
			types |= res.getType();
		}
		return types;
	}
	
	private boolean containsOnlyLinks() {
		for (IDipElement element: fElements) {
			if (!(element instanceof IncludeFolder)) {
				return false;
			}
		}
		return true;
	}
	
	// зачем тут public static
	public static String format(String message, Object object) {
		return MessageFormat.format(message, new Object[] { object});
	}
	
	//========================
	// ok
	
	private boolean reserve = false;
	private boolean extract = false;
	private boolean deleteProjectContent = true;
	
	@Override
	protected void okPressed() {	
		if (fReserveButton != null && !fReserveButton.getSelection()){
			reserve = true;
		}
		if (fExtractContentButton != null && fExtractContentButton.getSelection()) {
			extract = true;
		}
		if (fDeleteProjectButton != null && !fDeleteProjectButton.getSelection()){
			deleteProjectContent = false;
		}
		super.okPressed();		
	}
	
	//========================
	// getters
	
	public IDipElement[] getElements() {
		return fElements;
	}
	
	public boolean isReserve() {
		return reserve;
	}
	
	public boolean isExtract() {
		return extract;
	}
	
	public boolean isDeleteProjectContent() {
		return deleteProjectContent;
	}
	
}