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
package ru.dip.ui.export;

import java.io.IOException;
import java.nio.file.Paths;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.ui.AbstractDialog;
import ru.dip.core.utilities.ui.CompositeFactory;
import ru.dip.core.utilities.ui.ControlFactory;
import ru.dip.core.utilities.ui.GridDataFactory;

public class ExportErrorDialog extends AbstractDialog {
	
	private static final int WIDTH = 600;
	private static final int HEIGHT = 300;		
	private static final Point INITIAL_SIZE = new Point(WIDTH, HEIGHT);
	
	private static final String DIALOG_TITLE = "Export Error";	
	private static final int SAVE_LOG_ID = 66001;
	private static final String SAVE_LOG_LABEL = "Save log";
	private static final String LOG_EXTENSION_FILTER = ".log";
	
	private final String fMessage;
	
	public ExportErrorDialog(Shell shell, String message) {
		super(shell, DIALOG_TITLE, INITIAL_SIZE);
		fMessage = message;
	}
	
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite mainComposite = CompositeFactory.full(parent, 2 ,false);		
		Label imageLabel = ControlFactory.imageLabel(mainComposite, Display.getDefault().getSystemImage(SWT.ICON_ERROR));
		GridDataFactory.applyVerticalBegining(imageLabel);		
		StyledText text = ControlFactory.textLabel(mainComposite, fMessage);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = HEIGHT;
		gd.widthHint = WIDTH;
		text.setLayoutData(gd);	
		return mainComposite;
	}	

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, SAVE_LOG_ID, SAVE_LOG_LABEL, false);
		createButton(parent, OK,"OK", true);
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == SAVE_LOG_ID) {		
			saveLogPressed();	
		} else {
			super.buttonPressed(buttonId);
		}
	}
	
	private void saveLogPressed() {
		FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
		fd.setFilterExtensions(new String[] {LOG_EXTENSION_FILTER});
		String file  = fd.open();
		if (file == null) {
			return;
		}
	
		try {
			FileUtilities.writeFile(Paths.get(file), fMessage);
			okPressed();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
}
