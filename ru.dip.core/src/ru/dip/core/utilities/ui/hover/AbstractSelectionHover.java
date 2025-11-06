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
package ru.dip.core.utilities.ui.hover;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipProjectSchemaModel;
import ru.dip.core.model.DipRoot;
import ru.dip.core.unit.UnitType;
import ru.dip.core.utilities.ArrayUtils;
import ru.dip.core.utilities.WorkbenchUtitlities;

public abstract class AbstractSelectionHover implements ISelectionListener {
	
	private Shell fHoverShell;
	private Text fHoverLabelText;
	private Control fParentControl;
	private FocusListener fParentFocusListener;


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {		
		if (selection instanceof TextSelection){
			TextSelection textSel = (TextSelection) selection;			
			IFile file = WorkbenchUtitlities.getFileFromOpenedEditor(part);
			if (file != null){
				if (DipNatureManager.hasNature(file) && checkExtension(file)){							
					String text = getText(file, textSel);
					mouseHover(text, Display.getDefault().getFocusControl());
					return;
				}
			}		
		}
		closeHover();
	}
	
	
	private boolean checkExtension(IFile file){
		String fileExtension = file.getFileExtension();
		if (fileExtension == null || fileExtension.isEmpty()){
			return false;
		}
		if (ArrayUtils.arrayContainsElement(UnitType.TXT_EXTENSIONS, fileExtension)){
			return true;
		}
		if (ArrayUtils.arrayContainsElement(UnitType.PLANTUML_EXTENSIONS, fileExtension)){
			return true;
		}
		if (UnitType.DOT_EXTENSION.equals(fileExtension)){
			return true;
		}
		if (UnitType.CSV_EXTENSION.equals(fileExtension)){
			return true;
		}
		if (ArrayUtils.arrayContainsElement(UnitType.MARKDOWN_EXTENSIONS, fileExtension)){
			return true;
		}
		
		DipProject dipProject = DipRoot.getInstance().getDipProject(file.getProject());
		DipProjectSchemaModel schemaModel = dipProject.getSchemaModel();
		if (schemaModel.containsFileExtension(fileExtension)){
			return true;
		}		
		return false;	
	}
	
	private String getText(IFile file, TextSelection selection){
		String text = selection.getText();
		if (text != null){
			text = text.trim();
		}
		return getText(file, text);
	}
	
	abstract protected String getText(IFile file, String text);

	
	public void mouseHover(String hoverMessage, Control control) {
		fParentControl = control;
		if (hoverMessage == null || hoverMessage.isEmpty()){
			closeHover();
			return;
		}
		final Display display = Display.getDefault();
		Point tipPosition = display.getCursorLocation();
		if (fHoverShell != null && fHoverShell.isVisible())
			return;		
		fHoverShell = new Shell(display, SWT.ON_TOP | SWT.TOOL | SWT.NO_FOCUS);	
		GridLayout gridLayout = new GridLayout();			
		fHoverShell.setLayout(gridLayout);
		fHoverLabelText = new Text(fHoverShell, SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		fHoverLabelText.setText(hoverMessage);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.widthHint = 500;
		fHoverLabelText.setLayoutData(gd);
		fHoverShell.pack();
		setHoverLocation(fHoverShell, tipPosition);
		fHoverShell.setVisible(true);	
		fParentFocusListener = new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				closeHover();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				
			}
		};		
		fParentControl.addFocusListener(fParentFocusListener);		
	}
	
	private void setHoverLocation(Shell shell, Point position) {
		Rectangle displayBounds = shell.getDisplay().getBounds();
		Rectangle shellBounds = shell.getBounds();
		shellBounds.x = Math.max(Math.min(position.x, displayBounds.width - shellBounds.width), 0);
		shellBounds.y = Math.max(Math.min(position.y + 16, displayBounds.height - shellBounds.height), 0);
		shell.setBounds(shellBounds);
	}

	public void closeHover() {
		if (fHoverShell != null){
			if (fParentControl != null && fParentFocusListener != null) {
				fParentControl.removeFocusListener(fParentFocusListener);
			}
			fParentControl = null;
			fHoverShell.setVisible(false);
		}
	}

	public boolean isFocus() {
		if (fHoverShell != null) {
			boolean focus =  fHoverShell.isFocusControl() || fHoverLabelText.isFocusControl();			
			return focus;
		}
		return false;
	}

}
