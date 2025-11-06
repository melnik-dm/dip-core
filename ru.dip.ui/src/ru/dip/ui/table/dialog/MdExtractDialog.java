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
package ru.dip.ui.table.dialog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;

import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ui.CompositeFactory;
import ru.dip.core.utilities.ui.ControlFactory;
import ru.dip.core.utilities.ui.GridDataFactory;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.ui.Messages;

public class MdExtractDialog extends FormDialog {

	private static final String TITLE = "Extract";
	private static final String DESCRIPTION = Messages.MdSplitDialog_DialogDescription;
	private static final String TITLE_PARAGRAPH = Messages.MdSplitDialog_ParagraphColumnName;
	private static final String TITLE_FILENAME = Messages.MdSplitDialog_FileColumnName;
	// error
	private static final String DUPLICATE_NAMES_ERROR = Messages.MdSplitDialog_ErrorDuplicateNamesMsg;
	
	// control
	private Label fDescription;
	private final List<String> fParagraphs;
	private List<Button> fButtons = new ArrayList<>();
	private List<Text> fFileNames = new ArrayList<>();
	// model
	private String fFileName;
	private String fFileExtension;   // вместе с точкой
	private final IContainer fParentContainer;
	private final IDipUnit fUnit;
	//result
	private Map<Integer, String> fSelectedParagraphs = new LinkedHashMap<>();
	private List<Integer> fNotSelectedParagraphs = new ArrayList<>();
	
	public MdExtractDialog(Shell shell, List<String> paragraphs, 
			IDipUnit unit) {
		super(shell);
		fParagraphs = paragraphs;
		fUnit  = unit;
		IFile file = fUnit.resource();
		splitFileName(file.getName());
		fParentContainer = file.getParent();		
	}
	
	private void splitFileName(String name) {
		int  dotIndex = name.lastIndexOf("."); //$NON-NLS-1$
		if (dotIndex > 0 && dotIndex < name.length() - 1) {
			fFileName = name.substring(0, dotIndex);
			fFileExtension = name.substring(dotIndex, name.length());						
		} else {
			fFileName = name;
			fFileExtension = null;
		}		
	}
	
	@Override
	protected void configureShell(Shell newShell) {		
		super.configureShell(newShell);
		newShell.setText(TITLE);
	}
	
	@Override
	protected void createFormContent(IManagedForm mform) {
		Composite body = mform.getForm().getBody();
		body.setLayout(new GridLayout(2, false));
		
		Label empty = new Label(body, SWT.NONE);
		empty.setLayoutData(GridDataFactory.spanGridData(2));		
		fDescription = ControlFactory.label(body, DESCRIPTION, ColorProvider.BLACK);  
		fDescription.setLayoutData(GridDataFactory.spanGridData(2));		
	
		Composite titleParagraph = CompositeFactory.fullBorder(body); 
		ControlFactory.label(titleParagraph, TITLE_PARAGRAPH, FontManager.boldFont); 
		
		Composite titleFileName =  CompositeFactory.fullBorder(body, GridData.FILL_VERTICAL);
		Label fileNameLabel = ControlFactory.label(titleFileName, TITLE_FILENAME, FontManager.boldFont);
		fileNameLabel.setLayoutData(GridDataFactory.widthGridData(220));
	
		int counter = 1;
		for (String paragraph: fParagraphs) {
			Composite checkComposite = CompositeFactory.fullBorder(body, 2, false); 
				
			Button button = new Button(checkComposite, SWT.CHECK | SWT.CENTER);
			button.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			fButtons.add(button);			
			Label label = ControlFactory.label(checkComposite, paragraph, ColorProvider.BLACK); 
			label.setLayoutData(new GridData(GridData.FILL_BOTH));
						
			Composite fileNameComposite = CompositeFactory.fullBorder(body, GridData.FILL_VERTICAL); 
			Text fileName = new Text(fileNameComposite, SWT.BORDER);
			fileName.setLayoutData(GridDataFactory.widthGridData(200));
			fileName.setText(getFileName(counter++));
			fileName.setEnabled(false);
			fFileNames.add(fileName);
			
			button.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					validate();
					fileName.setEnabled(button.getSelection());
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {}
			});
			fileName.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					validate();					
				}
			});
		}
	}
	
	private String getFileName(int number) {
		StringBuilder builder = new StringBuilder();
		builder.append(fFileName);
		builder.append("_"); //$NON-NLS-1$
		builder.append(number);
		
		if (fFileExtension != null) {
			builder.append(fFileExtension);
		}
		return builder.toString();
	}
		
	private void validate() {
		Button ok = getButton(OK);
		ok.setEnabled(isValid());
	}
	
	private boolean isValid() {
		Set<String> names = new HashSet<>();
		for (int i = 0; i < fButtons.size(); i++) {
			if (fButtons.get(i).getSelection()) {
				String fileName = fFileNames.get(i).getText();
				if (!names.add(fileName)) {
					// содержит одинаковые именна для файлов FileName
					fDescription.setText(DUPLICATE_NAMES_ERROR + fileName);
					return false;
				}
				IStatus status = DipUtilities.canCreateFile(fParentContainer, fileName);
				if (!status.isOK()) {
					fDescription.setText(fileName + ". " + status.getMessage()); //$NON-NLS-1$
					return false;
				}									
			}
		}
		fDescription.setText(DESCRIPTION);
		return true;
	}
	
	@Override
	protected void okPressed() {
		doOk();
		super.okPressed();
	}
	
	private void doOk() {				
		for (int i = 0; i < fButtons.size(); i++) {
			if (fButtons.get(i).getSelection()) {
				fSelectedParagraphs.put(i, fFileNames.get(i).getText());
			} else {
				fNotSelectedParagraphs.add(i);
			}
		}		
	}
	
	public Map<Integer, String> getSelectedParagraphs(){
		return fSelectedParagraphs;
	}
	
	public List<Integer> getNotSeletedParagraphs(){
		return fNotSelectedParagraphs;
	}

}
