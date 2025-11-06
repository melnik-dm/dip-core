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

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellEditor;
import ru.dip.core.form.FormReader;
import ru.dip.core.unit.UnitExtension;
import ru.dip.core.unit.form.FormPresentation;
import ru.dip.ui.table.ktable.model.IDipTableModel;
import ru.dip.ui.table.table.TableSettings;

abstract class AbstractFieldCellEditor extends KTableCellEditor  {
	
	protected IDipTableModel fModel;
	private final UnitExtension fFormField;	
	private final String fStartContent;
	
	public AbstractFieldCellEditor(UnitExtension formField) {
		fFormField = formField;
		fStartContent = getStartFormContent();
	}
	
	@Override
	public void setContent(Object content) {}

	protected Font getFont() {							
		return TableSettings.presentationFont();
	}
	
	
	@Override
	public void open(KTable table, int col, int row, Rectangle rect) {		
		super.open(table, col, row, rect);
		fModel = (IDipTableModel) m_Model;
		setValue();
	}
	
	/**
	 *  Устанавливает значение из поля в редактор
	 */
	abstract void setValue();
	
	@Override
	public void close(boolean save) {
		if (save) {
			save();
		}				
		super.close(save);
	}
	
	protected void save() {
		String newContent = getNewFormContent();
		fModel.saveUnit(fStartContent, newContent, fFormField.getDipUnit());
	}
	
	private String getStartFormContent() {
		FormPresentation formPresentation = (FormPresentation) fFormField.getDipUnit()
				.getUnitPresentation().getPresentation();
		FormReader reader = formPresentation.getFormReader();				
		return reader.getContent();
	}
	
	
	private String getNewFormContent() {
		FormPresentation formPresentation = (FormPresentation) fFormField.getDipUnit()
				.getUnitPresentation().getPresentation();
		FormReader reader = formPresentation.getFormReader();
		setFieldValue(reader);
		return reader.getContent();
	}
	
	/**
	 * Сохраняет значение из редактора в поле и в FormReader
	 * В FormReader - значение устанавливается для получения нового контента форма, для записи в файл 	
	 */
	abstract void setFieldValue(FormReader reader);
	
}
