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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ru.dip.ui.Messages;

public class DefaultSchemaMessage  extends Dialog {

	private static String fTitle = Messages.DefaultSchemaMessage_Shell_title;
	
	private IFile fFile;
	
	public DefaultSchemaMessage(Shell parentShell, IFile file) {
		super(parentShell);
		fFile = file;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(fTitle);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(580, 200);
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,	true);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout comLayout = new GridLayout(2, false);
		comLayout.horizontalSpacing = 10;
		composite.setLayout(comLayout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
				
		Label imageLabel = new Label(composite, SWT.NONE);
		imageLabel.setImage(getWarningImage());

		Composite messageComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 0;
		messageComposite.setLayout(layout);
				
		Label label1 = new Label(messageComposite, SWT.NONE);
		label1.setText(Messages.DefaultSchemaMessage_Message);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		label1.setLayoutData(gd);
		
		new Label(messageComposite, SWT.NONE);
		new Label(messageComposite, SWT.NONE);
		
		Hyperlink hyperlink = new Hyperlink(messageComposite, SWT.UNDERLINE_LINK);
		hyperlink.setText(Messages.DefaultSchemaMessage_HyperLink_set);
		hyperlink.setUnderlined(true);
		hyperlink.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		hyperlink.addHyperlinkListener(new IHyperlinkListener() {
			
			@Override
			public void linkExited(HyperlinkEvent e) {
				
			}
			
			@Override
			public void linkEntered(HyperlinkEvent e) {
				
			}
			
			@Override
			public void linkActivated(HyperlinkEvent e) {
				close();
				openProperties(fFile.getProject());
				
			}
		});
		
		Label label2 = new Label(messageComposite, SWT.NONE);
		label2.setText(Messages.DefaultSchemaMessage_Shema_for_file_label + fFile.getFileExtension() + Messages.DefaultSchemaMessage_In_project_properties_label);
		
		Label label3 = new Label(messageComposite, SWT.NONE);
		label3.setText(Messages.DefaultSchemaMessage_Need_open_again);
		gd = new GridData();
		gd.horizontalSpan = 2;
		label3.setLayoutData(gd);
		return composite;
	}
	
	private void openProperties(IProject project) {
		  String ID = "ru.dip.project.properties"; //$NON-NLS-1$
		  Shell shell = getShell();
		  PreferencesUtil.createPropertyDialogOn(shell, project, ID, null, null, 0).open();
	}
	
	@Override
	protected void okPressed() {
		super.okPressed();
	}
	
	private Image getWarningImage() {
		Shell shell = getShell();
		final Display display;
		if (shell == null || shell.isDisposed()) {
			shell = getParentShell();
		}
		if (shell == null || shell.isDisposed()) {
			display = Display.getDefault();
			Assert.isNotNull(display,
					"The dialog should be created in UI thread"); //$NON-NLS-1$
		} else {
			display = shell.getDisplay();
		}

		final Image[] image = new Image[1];
		display.syncExec(() -> image[0] = display.getSystemImage(SWT.ICON_WARNING));
		return image[0];
	}
}
