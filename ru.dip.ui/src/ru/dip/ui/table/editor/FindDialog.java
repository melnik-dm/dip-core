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
package ru.dip.ui.table.editor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.widgets.WidgetFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import ru.dip.core.model.finder.IFinder;
import ru.dip.core.model.interfaces.IFindSupport;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.ui.Messages;

public class FindDialog extends Dialog {
	
	private final int PREVIOUS = 86;
	private final int NEXT = 87;
	private final int FIND = 88;

	// controls
	private Text fText;
	private Label fResultPointsLabel;
	private Label fResultObjectsLabel;
	private Button fCaseSensitive;
	private Button fFindInId;
	private Button fSwitchPosition;
	private Button fPreviousButton;
	private Button fNextButton;
	// model
	private IFindSupport fEditor;
	private IFindSupport fOldEditor;
	private IFinder fFinder; 
	private String fStartText;
	
	public FindDialog(IFindSupport editor, Shell parentShell) {
		super(parentShell);
		fEditor = editor;
		fStartText = fEditor.getLastSearchedText();
		setBlockOnOpen(false);
		setShellStyle(SWT.MODELESS | SWT.TITLE | SWT.CLOSE);		
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.FindDialog_Title);			
	}
	
	@Override
	protected Point getInitialSize() {
		Point p =  super.getInitialSize();
		if (p.x < 300) {
			p.x = 300;
		}
		return p;
	}
		
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite textComposite = new Composite(composite, SWT.NONE);
		textComposite.setLayout(new GridLayout(2, false));
		textComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label findLabel = new Label(textComposite, SWT.NONE);
		findLabel.setText(Messages.FindDialog_FindLabel);
		
		fText = new Text(textComposite, SWT.BORDER);
		fText.setLayoutData(new GridData(GridData.FILL_BOTH));
		fText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.KEYPAD_CR || e.keyCode == 13) {
					doFindWithBusyIndication();
				}					
			}
		});
		if (fStartText != null) {
			fText.setText(fStartText);
			fText.setSelection(0,  fStartText.length());
		}
		
		fCaseSensitive = new Button(composite, SWT.CHECK);
		fCaseSensitive.setText(Messages.FindDialog_CaseSensitiveButton);
		
		fFindInId = new Button(composite, SWT.CHECK);
		fFindInId.setText(Messages.FindDialog_FindInId);
				
		fSwitchPosition = new Button(composite, SWT.CHECK);
		fSwitchPosition.setText(Messages.FindDialog_SwitchingPositions);
		
		new Label(composite, SWT.NONE);
		
		fResultPointsLabel = new Label(composite, SWT.NONE);
		fResultPointsLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		fResultObjectsLabel = new Label(composite, SWT.NONE);
		fResultObjectsLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		new Label(composite, SWT.NONE);
		
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(2,true));
		buttonComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		return composite;
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 0; // this is incremented by createButton
		layout.makeColumnsEqualWidth = true;
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END
				| GridData.VERTICAL_ALIGN_CENTER);

		Composite composite = WidgetFactory.composite(SWT.NONE).layout(layout).layoutData(data).font(parent.getFont())
				.create(parent);

		createButtonsForButtonBar(composite);
		layout.numColumns = 2;
		return composite;		
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		fPreviousButton =  createButton(parent, PREVIOUS, Messages.FindDialog_PrieviousButton, false);
		fPreviousButton.setEnabled(false);
		fNextButton = createButton(parent, NEXT, Messages.FindDialog_NextButton, false);
		fNextButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, Messages.FindDialog_CloseButton, false);		
		createButton(parent, FIND, Messages.FindDialog_FindButton, false);						
	}		
	
	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case FIND:{
			doFindWithBusyIndication();
			break;
		}
		case PREVIOUS:{
			doPrevious();
			break;
		}
		case NEXT:{
			doNext();
			break;
		}
		case CANCEL:{
			super.buttonPressed(buttonId);
			return;
		}		
		}		
		super.buttonPressed(buttonId);
	}
	
	public boolean isOpen() {
		Shell shell = getShell();
		if (shell != null) {
			return shell.isVisible();
		}
		return false;
	}
	
	@Override
	public boolean close() {
		Display.getCurrent().asyncExec(this::clean);
		return super.close();
	}
	
	private void doFindWithBusyIndication() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IProgressService ps = wb.getProgressService();
		try {
			ps.busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor pm) {
					Display.getDefault().asyncExec(() -> {
						doFind();
					});
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void doFind() {		
		if (fOldEditor != null) {
			fOldEditor.cleanFind();
		}
		fOldEditor = fEditor;
		String searchText = fText.getText();
		if (searchText.trim().isEmpty()) {
			fEditor.setLastSearchedText(null);
			return;
		}
		
		
		fFinder = fEditor.find(searchText, fCaseSensitive.getSelection(), fFindInId.getSelection());				
		if (fFinder.hasNext()) {						
			fEditor.setFindMode(true);
			List<IDipDocumentElement> findedElements = fFinder.getElements();
			fEditor.updateFindedElements(findedElements);
			selectFirst();
		 } 				
		setMessage();
		setEnableButton();
		fEditor.setLastSearchedText(searchText);
	}
	
	private void setMessage() {
		if (fFinder.hasNext()) {
			String text = Messages.FindDialog_FindMatchesLabel + fFinder.size();
			fResultPointsLabel.setText(text);
			text = Messages.FindDialog_FindObjectsLabel + fFinder.unitSize();
			fResultObjectsLabel.setText(text);
		} else {
			fResultPointsLabel.setText(Messages.FindDialog_NoMatchFound);
			fResultObjectsLabel.setText("");
		}
	}
	
	private void setEnableButton() {
		if (fFinder.size() > 1) {
			 setEnbleButtons(true);
		} else {
			setEnbleButtons(false);
		}
	}
	
	public void setEnbleButtons(boolean value) {
		 fPreviousButton.setEnabled(value);
		 fNextButton.setEnabled(value);
	}
					
	
	private void selectFirst() {
		fFinder.hasNext();
		if (!fSwitchPosition.getSelection()) {
			fFinder.cleanCurrentPosition();
		}
		
		IDipDocumentElement dipDocElement = fFinder.getCurrent();
		fEditor.selectNext(dipDocElement);
	}
		
	private void doNext() {
		if (fFinder != null && fFinder.hasNext()) {
			IDipDocumentElement dipDocElement = fFinder.next(fSwitchPosition.getSelection());
			fEditor.selectNext(dipDocElement);				
		}
	}
		
	private void doPrevious() {
		if (fFinder != null && fFinder.hasNext()) {
			IDipDocumentElement dipDocElement = fFinder.previous(fSwitchPosition.getSelection());
			fEditor.selectNext(dipDocElement);				
		}
	}
			
	private void clean() {
		if (fOldEditor != null && fOldEditor != fEditor) {
			fOldEditor.cleanFind();
		}			
		fEditor.cleanFind();
	}

	public void setNewEditor(IEditorPart targetEditor) {
		if (targetEditor instanceof IFindSupport) {
			fEditor = (IFindSupport) targetEditor;
			if (isOpen()) {
				setEnbleButtons(fEditor == fOldEditor);
			}
		} else {
			fEditor = null;
			if (isOpen()) {
				setEnbleButtons(false);
			}
		}
	}
}


