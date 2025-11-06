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
package ru.dip.ui.table.ktable.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipTableUtilities;
import ru.dip.core.utilities.ReservedUtilities;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.utilities.image.ImageProvider;

public class ApplyNumberingDialog extends FormDialog {

	public static class RenamingElement {

		private IDipDocumentElement fDipDocElement;
		private String fOldName;
		private String fNewName;
		private boolean fEnable;
		private TableItem fItem;

		RenamingElement(IDipDocumentElement dipDocElement, TableItem item, String newName) {
			fDipDocElement = dipDocElement;
			fItem = item;
			fNewName = newName;
			fOldName = dipDocElement.name();
			fEnable = true;
		}
		
		public boolean isEnable() {
			return fEnable;
		}
		
		public String getNewName() {
			return fNewName;
		}
		
		public String getOldName() {
			return fOldName;
		}
		
		public IDipDocumentElement getDipDocElement() {
			return fDipDocElement;
		}
		
	}

	// model
	private WorkbenchLabelProvider fWorkbenchLabelProvider = new WorkbenchLabelProvider();
	private IDipParent fParent;
	private List<RenamingElement> fRenamingElements = new ArrayList<>();
	private List<String> fAllExtension = new ArrayList<>();
	private List<String> fCurrentExtension = new ArrayList<>();	
	// controls
	private String[] titles = { "Старое имя", "", "", "", "Новое имя", "", "Примечание" };
	private Button fFileNumeration;
	private Text fFileStepText;
	private Button fFolderNumeration;
	private Text fFolderStepText;
	private Button fSaveText;
	private Button fUpdate;
	private Table fTable;
	private Label fMessage;
	private Hyperlink fExtensionsLink;
	
	// step counts
	private boolean fFileNumbering;
	private int fFileStep = 0;
	private int fFileNumber = 0;
	private String fFileStepValue;
	private boolean fodlerNumbering;
	private int folderStep = 0;
	private int fFolderNumber = 0;
	private String fFolderStepValue;

	public ApplyNumberingDialog(Shell parentShell, IDipParent parent) {
		super(parentShell);
		fParent = parent;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Auto-renaming");
	}

	@Override
	protected Point getInitialSize() {
		Point p = super.getInitialSize();
		p.x = 600;
		if (p.y > 800){
			p.y = 800;
		}
		return p;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control control =  super.createContents(parent);
		setTableValues();
		checkCanOkPressed();
		return control;
	}
	
	@Override
	protected void createFormContent(IManagedForm mform) {
		Composite composite = mform.getForm().getBody();
		GridLayout layout = new GridLayout();
		layout.marginTop = 8;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createSettingsComposite(composite);
		setSettings();
		createChildrenComposite(composite);
	}

	private void createSettingsComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(4, false));
		composite.setBackground(ColorProvider.WHITE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fFileNumeration = new Button(composite, SWT.CHECK);
		fFileNumeration.setText("Нумерация файлов");
		fFileNumeration.setForeground(ColorProvider.BLACK);
		Label label = new Label(composite, SWT.NONE);
		label.setText(" Шаг:");
		label.setForeground(ColorProvider.BLACK);
		fFileStepText = new Text(composite, SWT.BORDER);
		fFileStepText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label emptyLabel = new Label(composite, SWT.NONE);
		emptyLabel.setText("      ");
		fFolderNumeration = new Button(composite, SWT.CHECK);
		fFolderNumeration.setText("Нумерация каталогов   ");
		fFolderNumeration.setForeground(ColorProvider.BLACK);
		Label label2 = new Label(composite, SWT.NONE);
		label2.setForeground(ColorProvider.BLACK);
		label2.setText(" Шаг:");
		fFolderStepText = new Text(composite, SWT.BORDER);
		fFolderStepText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label emptyLabel2 = new Label(composite, SWT.NONE);
		emptyLabel2.setText("");
		fSaveText = new Button(composite, SWT.CHECK);
		fSaveText.setText("Сохранить текст");
		fSaveText.setForeground(ColorProvider.BLACK);
		new Label(composite, SWT.NONE);
		new Label(composite, SWT.NONE);
		Label l = new Label(composite, SWT.NONE);
		l.setText("\n");

		Label extLabel = new Label(composite, SWT.NONE);
		extLabel.setText(" Тип файлов: ");
		extLabel.setForeground(ColorProvider.BLACK);
		fExtensionsLink  = new Hyperlink(composite, SWT.NONE);
		fExtensionsLink.setForeground(ColorProvider.BLUE);
		fExtensionsLink.setText("");
		fExtensionsLink.addHyperlinkListener(new IHyperlinkListener() {
			
			@Override
			public void linkExited(HyperlinkEvent e) {
				
			}
			
			@Override
			public void linkEntered(HyperlinkEvent e) {
				
			}
			
			@Override
			public void linkActivated(HyperlinkEvent e) {
				SelectExtensionDialog dialog = new SelectExtensionDialog(getShell(), fAllExtension, fCurrentExtension);
				if (dialog.open() == OK){
					fCurrentExtension = dialog.getExtensions();
					fExtensionsLink.setText(fCurrentExtension.toString());
				}
			}
		});
		GridData linkData = new GridData();
		linkData.horizontalSpan = 3;
		fExtensionsLink.setLayoutData(linkData);

		fUpdate = new Button(parent, SWT.PUSH);
		fUpdate.setText("Update");
		fUpdate.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		fUpdate.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				fMessage.setText("");
				getButton(OK).setEnabled(true);
				updateTableValues();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
	}

	private void setSettings() {
		
		fAllExtension = getExtensions();
		fCurrentExtension = fAllExtension;
		if (fParent.isFileNumeration()) {
			fFileNumeration.setSelection(true);
			fFileStepText.setText(fParent.getFileStep());
		}
		fExtensionsLink.setText(getExtensions().toString());
		if (fParent.isFolderNumeration()) {
			fFolderNumeration.setSelection(true);
			fFolderStepText.setText(fParent.getFolderStep());
		}	
	}

	private List<String> getExtensions() {
		ArrayList<String> result = new ArrayList<>();
		for (IDipDocumentElement req : fParent.getDipDocChildrenList()) {
			if (req instanceof IDipDocumentElement) {
				IResource res = ((IDipDocumentElement) req).resource();
				if (res instanceof IFile) {
					String extension = ((IFile) res).getFileExtension();
					if (extension != null && !extension.isEmpty() && !result.contains(extension)) {
						result.add(extension);
					}
				}
			}
		}
		result.sort(null);
		return result;
	}

	private void createChildrenComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.BORDER);		
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setBackground(ColorProvider.WHITE);
		createTableComposite(composite);
		fMessage = new Label(composite, SWT.NONE);
		fMessage.setText("\n");
		fMessage.setForeground(ColorProvider.BLACK);
		fMessage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void createTableComposite(Composite parent) {
		fTable = new Table(parent, SWT.FULL_SELECTION);		
		fTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(fTable, SWT.NONE);
			column.setText(titles[i]);
		}
		//packTable();
	}

	private void setTableValues() {
		updateSteps();
		for (IDipDocumentElement dipDocElement : fParent.getDipDocChildrenList()) {
			TableItem item = new TableItem(fTable, SWT.NONE);
			String newName = getNewName(dipDocElement);
			setItemValue(item, dipDocElement, newName);
			RenamingElement element = new RenamingElement(dipDocElement, item, newName);
			fRenamingElements.add(element);
		}
		packTable();
	}

	private void updateTableValues() {
		updateSteps();
		for (RenamingElement element : fRenamingElements) {			
			TableItem item = element.fItem;
			IDipDocumentElement dipDocElement = element.fDipDocElement;
			if (dipDocElement.resource() instanceof IFile){
				String extension = ((IFile)dipDocElement.resource()).getFileExtension();
				if (extension == null || extension.isEmpty() || !fCurrentExtension.contains(extension)){
					element.fNewName = "";
					setItemValue(item, dipDocElement, "");
				} else {
					String newName = getNewName(dipDocElement);
					element.fNewName = newName;
					setItemValue(item, dipDocElement, newName);
				}				
			} else {
				String newName = getNewName(dipDocElement);
				element.fNewName = newName;
				setItemValue(item, dipDocElement, newName);
			}
		}
		packTable();
		checkCanOkPressed();
	}

	private void updateSteps() {
		fFileNumbering = fFileNumeration.getSelection();
		fFileStep = 0;
		fFileNumber = 0;
		fFileStepValue = fFileStepText.getText().trim();
		if (fFileNumbering) {
			try {
				fFileStep = Integer.parseInt(fFileStepText.getText().trim());
			} catch (NumberFormatException e) {
				fMessage.setText("Некорректное значение шага для файлов");
				getButton(OK).setEnabled(false);
			}
		}
		fodlerNumbering = fFolderNumeration.getSelection();
		folderStep = 0;
		fFolderNumber = 0;
		fFolderStepValue = fFolderStepText.getText().trim();
		if (fodlerNumbering) {
			try {
				folderStep = Integer.parseInt(fFolderStepValue);
			} catch (NumberFormatException e) {
				fMessage.setText("Некорректное значение шага для каталогов");
				getButton(OK).setEnabled(false);
			}
		}
	}

	private String getNewName(IDipDocumentElement dipDocElement) {
		String newName = "";
		if (dipDocElement instanceof IDipParent) {
			if (fodlerNumbering) {
				fFolderNumber += folderStep;
				newName = DipTableUtilities.appendStepPrefix(fFolderStepValue, fFolderNumber);
			} else {
				return newName;
			}
		} else {
			if (fFileNumbering) {
				fFileNumber += fFileStep;
				newName = DipTableUtilities.appendStepPrefix(fFileStepValue, fFileNumber);
			} else {
				return newName;
			}
		}
		return getNewStepName(dipDocElement, newName);
	}

	private String getNewStepName(IDipDocumentElement dipDocElement, String newNumber) {
		String fileExtension = dipDocElement.resource().getFileExtension();
		if (fSaveText.getSelection()) {
			String oldName = dipDocElement.name();
			String textName = getTextName(oldName);
			return newNumber + textName;
		} else if (fileExtension != null && !fileExtension.isEmpty()) {
			return newNumber + "." + fileExtension;
		}
		return newNumber;
	}

	private String getTextName(String name) {
		char[] chars = name.toCharArray();
		int digitCount = 0;
		for (int i = 0; i < name.length(); i++) {
			char ch = chars[i];
			if (Character.isDigit(ch)) {
				digitCount++;
			} else {
				break;
			}
		}
		if (digitCount == name.length()) {
			return "";
		} else {
			return name.substring(digitCount);
		}
	}

	private void setItemValue(TableItem item, IDipDocumentElement dipDocElement, String newName) {
		Image resImage = fWorkbenchLabelProvider.getImage(dipDocElement);
		item.setText(0, dipDocElement.name());
		item.setImage(0, resImage);
		item.setText(1, "        ");
		item.setImage(2, ImageProvider.FORWARD);
		item.setText(3, "       ");
		item.setText(4, newName);
		item.setText(5, "      ");
		item.setText(6, "           ");
		item.setForeground(6, ColorProvider.BLACK);
		if (dipDocElement instanceof DipUnit && fFileNumbering) {
			IFile file = fParent.resource().getFile(new Path(newName));
			if (ReservedUtilities.hasReservedFile(file)) {
				// RESERVE ERROR
				item.setText(6, "reserved");
				item.setForeground(6, ColorProvider.RED);
				getButton(OK).setEnabled(false);
			}
		}
	}

	private void packTable() {
		for (int i = 0; i < titles.length; i++) {
			fTable.getColumn(i).pack();
		}
	}

	
	private void checkCanOkPressed() {
		getButton(OK).setEnabled(hasChanges());
	}
	
	private boolean hasChanges() {
		for (RenamingElement element: fRenamingElements) {
			if (!element.fNewName.equals(element.fDipDocElement.name())){
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void okPressed() {
		//doOkPressed();
		fFileStepValue = fFileStepText.getText();
		fFolderStepValue = fFolderStepText.getText();	
		fRenamingElements = fRenamingElements.stream()
		.filter(RenamingElement::isEnable)
		.filter(element -> !element.getNewName().isEmpty())
		.collect(Collectors.toList());
		super.okPressed();
	}
	
	public List<RenamingElement> getRenamingElements(){ 
		return fRenamingElements;
	}
	
	public String getFileStep() {
		return fFileStepValue;
	}
	
	public String getFolderStep() {
		return fFolderStepValue;
	}

}
