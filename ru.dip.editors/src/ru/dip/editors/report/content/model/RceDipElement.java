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
package ru.dip.editors.report.content.model;

import java.util.List;

import org.eclipse.swt.graphics.Color;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.editors.report.content.ReportContentModel;
import ru.dip.ui.table.ktable.model.ContentContainer;
import ru.dip.ui.table.ktable.model.ContentId;
import ru.dip.ui.table.ktable.model.ContentType;
import ru.dip.ui.table.ktable.model.IDipIdElement;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.IDipTableModel;
import ru.dip.ui.table.ktable.model.IPresentationElement;
import ru.dip.ui.table.ktable.model.ITableNode;
import ru.dip.ui.table.table.TableSettings;

public class RceDipElement implements IDipTableElement, IDipIdElement, IPresentationElement {

	private static final int DEFAULT_PRESENTATION_WIDTH = 200;

	private final ContentContainer fContent = new ContentContainer();
	private final IDipElement fDipElement;
	private final ITableNode fParent;
	private int fNumber = 0;
	private List<IDipTableElement> fLinkedElements;
	
	public RceDipElement(IDipElement element, ITableNode parent) {
		fDipElement = element;
		fParent = parent;
	}

	@Override
	public void prepare(int width, IDipTableModel dipTableModel) {
		int fWidth = width <= 10 ? DEFAULT_PRESENTATION_WIDTH : width;
		((ReportContentModel) dipTableModel).reqPainter().updateTableElement(fWidth, this);
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public boolean isDiff() {
		return false;
	}
	
	//======================
	// height
		
	@Override
	public int height(IDipTableModel model) {
		int fHeight = getInt(ContentId.PRESENTATION, ContentType.HEIGHT);
		if (fHeight == 0 && isAbstractField()) {
			return 0;
		}
		int result = fHeight;		
		int fIdHeight = getInt(ContentId.ID, ContentType.HEIGHT);

		// compare with id
		if (model.isShowId() && fIdHeight > result) {
			if (isAbstractField()) {
				int linkedTotal = linkedTotalHeight();
				if (fIdHeight > linkedTotal && isLast()) {
					result = fHeight + fIdHeight - linkedTotal;
				}
			} else {
				result = fIdHeight;
			}
		}
		return result;
	}
	
	public int linkedTotalHeight() {
		return getLinkedElements().stream()
				.filter(IPresentationElement.class::isInstance)
				.mapToInt(e ->  get(ContentId.PRESENTATION, ContentType.HEIGHT, Integer.class))				
				.sum();
	}
	
	@Override
	public int getCommentMainHeight() {
		return 0;
	}
	
	@Override
	public void setCommentMainHeight(int mainCommentHeight) {}

	//====================
	// background
	
	@Override
	public Color background() {		
		if (isDisable()) {
			return TableSettings.tableDisableColor();
		}	
		return get(ContentId.PRESENTATION, ContentType.BACKGROUND, Color.class);
	}
	
	@Override
	public Color idBackground() {
		Color idBackgound = get(ContentId.ID, ContentType.BACKGROUND, Color.class);
		if (idBackgound != null) {
			return idBackgound;
		}		
		return  get(ContentId.PRESENTATION, ContentType.BACKGROUND, Color.class);
	}
	
	@Override
	public void setBackground(Color background) {
		put(ContentId.PRESENTATION, ContentType.BACKGROUND, background);		
	}
	
	//=======================
	// content

	@Override
	public void put(ContentId id, ContentType type, Object obj) {
		fContent.put(id, type, obj);				
	}
	
	@Override
	public <T> T get(ContentId id, ContentType type, Class<T> className){
		return fContent.get(id, type, className);
	}
	
	@Override
	public int getInt(ContentId id, ContentType type){
		return fContent.getInt(id, type);
	}

	//=============================
	// getters
		
	@Override
	public IDipTableElement self() {
		return this;
	}

	@Override
	public IDipDocumentElement dipDocElement() {
		return (IDipDocumentElement) fDipElement;
	}

	@Override
	public int getNumber() {
		return fNumber;
	}

	@Override
	public void setNumber(int number) {
		fNumber  = number;
	}

	@Override
	public void setLinkedElements(List<IDipTableElement> linkedElements) {
		fLinkedElements = linkedElements;
	}
	
	@Override
	public List<IDipTableElement> getLinkedElements() {
		return fLinkedElements;
	}

	@Override
	public ITableNode parent() {
		return fParent;
	}
	
}
