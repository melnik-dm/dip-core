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

import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;

import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ui.ControlFactory;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.ui.Messages;
import ru.dip.ui.utilities.ui.EntryTextComposite;

public class MdJoinDialog extends FormDialog {

	private static final String TITLE = Messages.MdJoinDialog_DialogTitle;
	private static final String DESCRIPTION = Messages.MdJoinDialog_InputNewName;
	private static final String FILE_NAME_LAVEL = Messages.MdJoinDialog_FileNameLabel;
	private static final String SOURCE_FILES_LABEL = Messages.MdJoinDialog_SourcesFileLabel;
	
	// model
	private final Collection<IDipDocumentElement> fSourceUnits;
	private final IContainer fParentContainer;
	private String fName;
	private DipUnit fSelectedSource;  // если имя совпала с одним из исходных файлов
	// control
	private Label fDescription;
	private EntryTextComposite fNameEntry;

	
	public MdJoinDialog(Shell shell, Collection<IDipDocumentElement> units, IContainer parent) {
		super(shell);
		fSourceUnits = units;
		fParentContainer = parent;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(TITLE);
	}
	
	@Override
	protected Point getInitialSize() {
		Point size =  super.getInitialSize();
		if (size.x < 600) {
			size.x = 600;
		}
		if (size.y < 500) {
			size.y = 500;
		}		
		return size;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		Composite body = mform.getForm().getBody();
		body.setLayout(new GridLayout());	
		fDescription = ControlFactory.label(body, DESCRIPTION, ColorProvider.BLACK);  
		fDescription.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));	
		new Label(body, SWT.NONE);

		fNameEntry = new EntryTextComposite(body, FILE_NAME_LAVEL);
		fNameEntry.setValue("new_name.md"); //$NON-NLS-1$
		fNameEntry.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				validate();				
			}
		});
		
		new Label(body, SWT.NONE);
		ControlFactory.label(body, SOURCE_FILES_LABEL, FontManager.boldFont);
		
		Text files = new Text(body, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY);
		files.setLayoutData(new GridData(GridData.FILL_BOTH)); 
		
		String filesContent = fSourceUnits.stream()
				.map(IDipDocumentElement::dipName)
				.collect(Collectors.joining("\n")); //$NON-NLS-1$
		files.setText(filesContent);				
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control control =  super.createButtonBar(parent);
		validate();
		return control;
	}
	
	private void validate() {
		Button ok = getButton(OK);
		ok.setEnabled(isValid());
	}
	
	private boolean isValid() {
		String fileName = fNameEntry.getValue();
		fSelectedSource = null;
		
		for (IDipDocumentElement source: fSourceUnits) {
			if (fileName.equals(source.dipName())
					&& source.parent().resource().equals(fParentContainer)){
				fSelectedSource = (DipUnit) source;
				return true;				
			}
		}
		
		IStatus status = DipUtilities.canCreateFile(fParentContainer, fileName);
		if (!status.isOK()) {
			fDescription.setText(status.getMessage());
			return false;
		} else {			
			fDescription.setText(DESCRIPTION);
			return true;
		}		
	}
	
	@Override
	protected void okPressed() {
		fName = fNameEntry.getValue();
		super.okPressed();
	}
	
	public String getName() {
		return fName;
	}
	
	public DipUnit getSeletedSource() {
		return fSelectedSource;
	}
	
}
