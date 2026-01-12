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
package ru.dip.ui.table.ktable.model;

import java.util.List;

import org.eclipse.swt.graphics.Color;

import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.ui.IBackground;
import ru.dip.ui.table.ktable.diff.DiffModel;
import ru.dip.ui.table.table.TableSettings;


public class TableElement implements IBackground, IDipIdElement, IDipTableElement {
	
	private static final int DEFAULT_PRESENTATION_WIDTH = 200;
	
	private final IDipDocumentElement fDipDocElement;
	private final ITableNode fParent;
	private boolean fSelect;
	
	private ContentContainer fContent = new ContentContainer();
	private int fCommentMainHeight; // высота основного комментария (актуально при наличии внутренних комментариев)
	
	// linked element (для полей форм)
	private int fNumber; // номер элемента среди связанных элементов (для полей)
	private List<IDipTableElement> fLinkedElements;

	public TableElement(IDipDocumentElement dipDocElement, ITableNode node) {
		fParent = node;
		fDipDocElement = dipDocElement;
	}
	
	//===========================
	// presentation
	
	@Override
	public void prepare(int width, IDipTableModel model) {
		width = width <= 10 ? DEFAULT_PRESENTATION_WIDTH : width;
		((DipTableModel) model).reqPainter().updateTableElement(width, this);
	}
	
	//===================
	// action
	
	public void delete() {
		parent().delete(this);
	}
	
	public void up() {
		fParent.up(this);
	}
	
	public void down() {
		fParent.down(this);
	}
	
	//===================
	// entity
	
	@Override
	public boolean isVisible() {
		if (fParent != null && !fParent.children().contains(this)) {
			return false;
		}
		
		if (fParent instanceof TableNode && !(fParent instanceof ParentNode) && !fParent.expand()) {
			return false;
		}
		return true;
	}
	
	//=======================
	// linked elements
	
	public int linkedTotalHeight() {
		return getLinkedElements().stream()
				.filter(IPresentationElement.class::isInstance)
				.mapToInt(e ->  get(ContentId.PRESENTATION, ContentType.HEIGHT, Integer.class))				
				.sum();
	}
	
	//=======================
	// IDipDocumentElement
	
	@Override
	public IDipDocumentElement dipDocElement() {
		return fDipDocElement;
	}
	
	//=======================
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
		

		int fCommentHeight = getInt(ContentId.COMMENT, ContentType.HEIGHT);	
		// compare with comment
		if (model.isShowComment() && fCommentHeight > result) {
			if (isAbstractField()) {
				int linkedTotal = linkedTotalHeight();
				if (fCommentHeight > linkedTotal && isLast()) {
					result = fHeight + fCommentHeight - linkedTotal;
				}
			} else {
				result = fCommentHeight;
			}
		}
		return result;
	}

	/**
	 * Высота основного комментария
	 */
	@Override
	public int getCommentMainHeight() {
		return fCommentMainHeight;
	}
	
	@Override
	public void setCommentMainHeight(int mainCommentHeight) {
		fCommentMainHeight = mainCommentHeight;
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
	
	//========================
	// Colors
	
	@Override
	public void setBackground(Color background) {
		put(ContentId.PRESENTATION, ContentType.BACKGROUND, background);
	}
		
	@Override
	public Color background() {
		if (parent() == null) {
			if (fDipDocElement.isDisabled()) {
				return TableSettings.tableDisableColor();
			}
			return get(ContentId.PRESENTATION, ContentType.BACKGROUND, Color.class);
		}
		
		if (isDisable() && !parent().model().tableComposite().isSelect(this)) {
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
	
	//============================
	// diff element
	
	public boolean isDiff() {
		if (fParent.model().getTableModel() instanceof DiffModel) {
			return ((DiffModel) fParent.model().getTableModel()).getDiffStatus(fDipDocElement) != null;
		}
		return false;
	}
	
	//============================
	// getters & setters
	
	public void setSelection(boolean value) {
		fSelect = value;
	}
	
	public boolean isSelect() {
		return fSelect;
	}
	
	public ITableNode parent() {
		return fParent;
	}
		
	public IDipTableModel model() {
		return fParent.model();
	}
	
	//==========================
	
	@Override
	public String toString() {
		return "TABLE ELEMENT: " + fDipDocElement.name() + " " + fDipDocElement.getClass().getSimpleName(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fDipDocElement == null) ? 0 : fDipDocElement.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TableElement other = (TableElement) obj;
		if (fDipDocElement == null) {
			if (other.fDipDocElement != null)
				return false;
		} else if (!fDipDocElement.equals(other.fDipDocElement))
			return false;
		return true;
	}

	
	@Override
	public IDipTableElement self() {
		return this;
	}
	
	@Override
	public void setNumber(int number) {
		fNumber = number;
	}
	
	@Override
	public int getNumber() {
		return fNumber;
	}

	@Override
	public void setLinkedElements(List<IDipTableElement> linkedElements) {
		fLinkedElements = linkedElements;
	}
	
	@Override
	public List<IDipTableElement> getLinkedElements() {
		return fLinkedElements;
	}
	
}
