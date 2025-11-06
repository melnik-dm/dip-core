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
package ru.dip.ui.table.ktable.celleditors;

import java.io.IOException;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellEditor;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.unit.UnitDescriptionPresentation;
import ru.dip.core.unit.UnitPresentation;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.ui.LayoutManager;
import ru.dip.ktable.DipTable;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.table.TableSettings;

class SimpleCellEditor extends KTableCellEditor implements ITextActionSupport {
	
	protected StyledText fText; 
	protected IDipTableElement fElement;
	protected IDipDocumentElement fDipDocElement;
	protected final DipTable fTable;
	protected final DipTableModel fModel;
	
	private String fStartContent;
	
	public SimpleCellEditor(DipTableModel model) {
		fModel = model;
		fTable = model.getTable();
	}
	
	@Override
	public void open(KTable table, int col, int row, Rectangle rect) {		
		super.open(table, col, row, rect);
		fStartContent = getEditorContent();
		if (fStartContent == null) {
			fStartContent = ""; 
		}
		Font font = getFont();
		if (font != null) {
			fText.setFont(font);
		}		
		fText.setText(fStartContent);
		fText.selectAll();
		fText.setVisible(true);
		fText.setFocus();
	}
	
	@Override
	protected Control createControl() {
		Composite composite = new Composite(fTable, SWT.BORDER);	
		composite.setLayout(LayoutManager.notIndtentLayout());		
		fText = new StyledText(composite, SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		fText.setLayoutData(new GridData(GridData.FILL_BOTH));
		setPadding();
		return composite;
	}
	
	private void setPadding() {
		fText.setMargins(TableSettings.marginLeft(), 
				TableSettings.marginTop(),
				TableSettings.marginRight(),
				TableSettings.marginBottom());
	}

	@Override
	public void setContent(Object content) {
	}
	
	protected Font getFont() {
		Object content = fModel.getContentAt(m_Col, m_Row);
		if (content instanceof IDipTableElement) {
			fElement = (IDipTableElement) content;
			fDipDocElement = fElement.dipDocElement();
			if (fDipDocElement instanceof UnitPresentation) {
				return TableSettings.presentationFont();
			} else if (fDipDocElement instanceof UnitDescriptionPresentation) {
				return TableSettings.italicPresentationFont();
			} else if (fDipDocElement instanceof DipFolder) {
				return TableSettings.boldPresentationFont();
			}
		}								
		return null;
	}
	
	protected String getEditorContent() {
		Object content = fModel.getContentAt(m_Col, m_Row);
		if (content instanceof IDipTableElement) {
			fElement = (IDipTableElement) content;
			fDipDocElement = fElement.dipDocElement();
			if (fDipDocElement instanceof UnitPresentation) {
				UnitPresentation presentation = (UnitPresentation) fDipDocElement;
				IFile file = presentation.getDipUnit().resource();
				try {
					String fileContent = FileUtilities.readFile(file);

					return fileContent;
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (fDipDocElement instanceof UnitDescriptionPresentation) {
				UnitDescriptionPresentation description = (UnitDescriptionPresentation) fDipDocElement;
				return description.getDecriptionContent();										 
			} else if (fDipDocElement instanceof DipFolder) {
				DipFolder folder = (DipFolder) fDipDocElement;	
				return folder.description();									
			} 
		} 
		return "";
	}
	
	@Override
	public void close(boolean save) {
		if (save) {
			save();
		}				
		super.close(save);
	}
	
	protected void save() {
		if (fDipDocElement instanceof UnitDescriptionPresentation ||
				fDipDocElement instanceof DipFolder) {
			String newContent = fText.getText().trim();
			if (!fStartContent.equals(newContent)) {
				fModel.tableComposite().updateDescriptionFromCellEditor(fElement, fDipDocElement, newContent);
			}
		} else if (fDipDocElement instanceof UnitPresentation) {
			String newContent = fText.getText();
			if (!fStartContent.equals(newContent)) {
				IDipUnit unit = ((UnitPresentation) fDipDocElement).getDipUnit();
				fModel.saveUnit(fStartContent, newContent, unit);
				
				//new EditUnitAction(fModel.tableComposite(), fStartContent, newContent, (UnitPresentation) fDipDocElement)
				//	.run();
			}			
		}
	}
	
	
	@Override
	public void doPaste() {
		fText.paste();
	}
	
	@Override
	public void doCopy() {
		fText.copy();
	}
	
}