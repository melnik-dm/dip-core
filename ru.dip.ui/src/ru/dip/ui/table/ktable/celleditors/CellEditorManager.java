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

import java.util.List;

import org.eclipse.swt.graphics.Rectangle;

import de.kupzog.ktable.KTableCellEditor;
import ru.dip.core.form.model.ComboField;
import ru.dip.core.form.model.Field;
import ru.dip.core.form.model.FormFieldType;
import ru.dip.core.form.model.ItemsField;
import ru.dip.core.form.model.TextField;
import ru.dip.core.unit.form.FieldUnity;
import ru.dip.core.unit.form.FormField;
import ru.dip.ktable.DipTable;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.IDipTableElement;

public final class CellEditorManager {
	
	private final DipTableModel fDipTableModel;
	private final DipTable fDipTable;
	
	public CellEditorManager(DipTable table, DipTableModel dipTableModel) {
		fDipTable = table;
		fDipTableModel = dipTableModel;
	}
	
	public void openCellEditor(int column, int row) {
		IDipTableElement element = (IDipTableElement) fDipTableModel.getContentAt(column, row);
		if (element.isFormUnityField()) {
			openFieldUnityEditor(element, (FieldUnity)  element.dipDocElement(), column, row);
		} else if (element.isFormField()) {
			openFieldEditor(element, (FormField) element.dipDocElement(), column, row);
		} else {
			Rectangle rect = fDipTable.getCellRect(column, row);
			SimpleCellEditor editor = new SimpleCellEditor(fDipTableModel);
			editor.open(fDipTable, column, row, rect);
			fDipTable.setCellEditor(editor);
		}
	}
	
	//========================
	// field editor
	
	private void openFieldEditor(IDipTableElement element, FormField formField, int col, int row) {
		Field field = formField.getField();
		switch (field.getType()) {
			case COMBO:{
				openComboFieldEditor((ComboField) field, col, row, formField, element);					
				break;
			}
			case CHECK:
			case RADIO:{
				openButtonCellEditor((ItemsField) field, col, row, formField, element);
				break;
			}
			case TEXT:{
				openTextFieldEditor((TextField) field, col, row, formField, element);
				break;
			}
		}
	}
	
	private void openFieldUnityEditor(IDipTableElement element, FieldUnity unity, int col, int row) {	
		int editorHeight = unity.getFormFields().stream()
			.map(FormField::getField)
			.filter(ItemsField.class::isInstance)
			.map(ItemsField.class::cast)
			.mapToInt(this::getFieldHeight)
			.sum() 
			+ unity.getFormFields().size() * 4;  // + небольшие отступы
			
		FieldUnityCellEditor fieldUnityEditor = new FieldUnityCellEditor(unity);	
		openCellEditor(fieldUnityEditor, col, row, editorHeight);		
		addDisposeListener(fieldUnityEditor, col, row, element);
	}
	
	private void openComboFieldEditor(ComboField comboField, int col, int row, FormField formField, IDipTableElement element) {		
		FieldComboCellEditor comboCellEditor = new FieldComboCellEditor(comboField, formField);
		int editorHeight = getFieldHeight(comboField);			
		openCellEditor(comboCellEditor, col, row, editorHeight);
		addDisposeListener(comboCellEditor, col, row, element);
	}
	
	private void openButtonCellEditor(ItemsField itemField, int col, int row, FormField formField, IDipTableElement element) {
		FieldButtonCellEditor buttonEditor = new FieldButtonCellEditor(itemField, formField);		
		int editorHeight = getFieldHeight(itemField);
		openCellEditor(buttonEditor, col, row, editorHeight);
		addDisposeListener(buttonEditor, col, row, element);
	}
	
	private void openTextFieldEditor(TextField textField, int col, int row, FormField formField, IDipTableElement element) {
		FieldTextCellEditor textEditor = new FieldTextCellEditor(textField, formField);
		int editorHeight = 100;
		openCellEditor(textEditor, col, row, editorHeight);
		addDisposeListener(textEditor, col, row, element);
	}
	
	private int getFieldHeight(ItemsField itemField) {
		if (itemField.getType() == FormFieldType.COMBO) {
			return 45;
		}
		
		int length = 4;
		if (itemField.getLength() != null && itemField.getLength() > 0) {
			length = itemField.getLength();
		}		
		return itemField.getItems().length / length * 35;
	}
	
	private void openCellEditor(KTableCellEditor editor, int col, int row, int editorHeight) {
		fDipTableModel.setEditRow(row);
		fDipTableModel.setEditRowHeight(editorHeight);
		fDipTable.updateCell(col, row);
		
		Rectangle rect = fDipTable.getCellRect(col, row);
		editor.open(fDipTable, col, row, rect);			
		fDipTable.setCellEditor(editor);
	}
	
	private void addDisposeListener(KTableCellEditor editor, int col, int row, IDipTableElement element) {
		editor.getControl().addDisposeListener(e -> {
			fDipTableModel.setEditedCellInfo(new CellInfo(element, col, row));
		});
	}

	public void updateEditedCell() {
		if (fDipTableModel.getEditedCellInfo() != null) {
			fDipTableModel.setEditRow(-1);
			CellInfo info = (CellInfo) fDipTableModel.getEditedCellInfo();
			fDipTableModel.updateElements(List.of(info.getTableElement()));
			fDipTableModel.getTable().updateCell(info.getColumn(), info.getRow());
			fDipTableModel.setEditedCellInfo(null);
			fDipTable.redraw();
		}
	}
	
	//=====================
	// open comment editor
	
	public void openCommentCellEditor(int column, int row) {
		Rectangle rect = fDipTable.getCellRect(column, row);
		CommentCellEditor editor = new CommentCellEditor(fDipTableModel);
		editor.open(fDipTable, column, row, rect);
		fDipTable.setCellEditor(editor);
	}
	
}
