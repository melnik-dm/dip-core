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
import org.eclipse.swt.graphics.Point;
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

public class MdSplitDialog extends FormDialog {

	private static final String TITLE = Messages.MdSplitDialog_DialogTitle;
	private static final String DESCRIPTION = Messages.MdSplitDialog_DialogDescription;
	private static final String TITLE_PARAGRAPH = Messages.MdSplitDialog_ParagraphColumnName;
	private static final String TITLE_FILENAME = Messages.MdSplitDialog_FileColumnName;
	// error
	private static final String DUPLICATE_NAMES_ERROR = Messages.MdSplitDialog_ErrorDuplicateNamesMsg;	
	// control
	private Composite fDownWrapper;
	private Composite fBody;
	private Composite fDown;
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
	private Map<String, Integer> fSelectedParagraphs = new LinkedHashMap<>();
	private List<Integer> fVerticalSpans = new ArrayList<>();
	private List<Boolean> fValues = new ArrayList<>();
	
	public MdSplitDialog(Shell shell, List<String> paragraphs, 
			IDipUnit unit) {
		super(shell);
		fParagraphs = paragraphs;
		for (int i = 0; i < fParagraphs.size(); i++) {
			fValues.add(false);
		}
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
	protected Point getInitialSize() {
		Point p = super.getInitialSize();
		if (p.y > 800) {
			p.y = 800;
		}
		return p;
	}
	
	@Override
	protected void createFormContent(IManagedForm mform) {
		fBody = mform.getForm().getBody();
		fBody.setLayout(new GridLayout());
		new Label(fBody, SWT.NONE);
		fDescription = ControlFactory.label(fBody, DESCRIPTION, ColorProvider.BLACK);  
	
		fDownWrapper = new Composite(fBody, SWT.BORDER);
		fDownWrapper.setLayout(new GridLayout());
		
		fDown = new Composite(fDownWrapper, SWT.NONE);
		fDown.setLayout(new GridLayout(2, false));
		
		createDown(fDown);
	}

	private void updateDown() {
		if (fDown != null && !fDown.isDisposed()) {
			fDown.dispose();			
		}
		fDown = new Composite(fDownWrapper, SWT.NONE);
		fDown.setLayout(new GridLayout(2, false));
		createDown(fDown);
		fDown.layout();
		fDownWrapper.layout();
		fBody.pack();
		fBody.layout();
		fBody.getParent().layout();
	}
	
	private void createDown(Composite parent) {
		fButtons.clear();
		fFileNames.clear();
		fVerticalSpans.clear();
		Composite titleParagraph = CompositeFactory.fullBorder(parent); 
		ControlFactory.label(titleParagraph, TITLE_PARAGRAPH, FontManager.boldFont); 
		
		Composite titleFileName =  CompositeFactory.fullBorder(parent, GridData.FILL_VERTICAL);
		Label fileNameLabel = ControlFactory.label(titleFileName, TITLE_FILENAME, FontManager.boldFont);
		fileNameLabel.setLayoutData(GridDataFactory.widthGridData(220));
			
		int fileNameCounter = 1;
		for (int i = 0; i < fParagraphs.size(); i++) {
			final int index = i;
			
			String paragraph = fParagraphs.get(i);
			Composite checkComposite = new Composite(parent, SWT.BORDER);
			GridLayout layout = new GridLayout(2, false);
			layout.marginTop = 5;
			layout.marginBottom = 5;
			checkComposite.setLayout(layout);		
			checkComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
				
			Button button = new Button(checkComposite, SWT.CHECK | SWT.CENTER);
			button.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			button.setSelection(fValues.get(i));
			fButtons.add(button);			
			Label label = ControlFactory.label(checkComposite, paragraph, ColorProvider.BLACK); 
			label.setLayoutData(new GridData(GridData.FILL_BOTH));
			button.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					validate();
					fValues.set(index, button.getSelection());
					updateDown();
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {}
			});
			
			
			int span = getSpanForFilename(i);			
			if (span < 0) {
				continue;
			}
			
			fVerticalSpans.add(span);		
			Composite fileNameComposite = new Composite(parent, SWT.BORDER);
			fileNameComposite.setLayout(new GridLayout());
			GridData gd = new GridData(GridData.FILL_VERTICAL);
			gd.verticalSpan = span;
			fileNameComposite.setLayoutData(gd);
					
			Text fileName = new Text(fileNameComposite, SWT.BORDER);
			fileName.setLayoutData(GridDataFactory.widthGridData(200));
			fileName.setText(getFileName(fileNameCounter++));
			fFileNames.add(fileName);					
			fileName.addModifyListener(new ModifyListener() {
				
				@Override
				public void modifyText(ModifyEvent e) {
					validate();					
				}
			});
		}
	}
	
	private int getSpanForFilename(int index) {
		boolean current = fValues.get(index);						
		if (index != 0) {
			boolean previous = fValues.get(index - 1);
			if (previous == current) {
				return -1;
			}
		}
		int currentSpan = 1;
		for (int i = index+1; i < fValues.size(); i++) {
			if (fValues.get(i) == current) {
				currentSpan++;
			} else {
				break;
			}
		}		
		return currentSpan;
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
		for (Text text:  fFileNames) {
			String fileName = text.getText();
			if (!names.add(fileName)) {
				// содержит одинаковые именна для файлов FileName
				fDescription.setText(DUPLICATE_NAMES_ERROR + fileName);
				return false;
			}
			// если сопадает с именем исходного файла, то норм
			if (fileName.equals(fUnit.dipName())) {
				continue;
			}			
			// общая проверка на создание файла
			IStatus status = DipUtilities.canCreateFile(fParentContainer, fileName);
			if (!status.isOK()) {
				fDescription.setText(fileName + ". " + status.getMessage()); //$NON-NLS-1$
				return false;
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
		for (int i = 0; i < fVerticalSpans.size(); i++) {
			fSelectedParagraphs.put(fFileNames.get(i).getText(), fVerticalSpans.get(i));
		}			
	}
	
	public Map<String,Integer> getSelectedParagraphs(){
		return fSelectedParagraphs;
	}	

}
