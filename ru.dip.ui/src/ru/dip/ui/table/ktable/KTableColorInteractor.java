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
package ru.dip.ui.table.ktable;

import java.util.HashSet;
import java.util.List;

import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IUnitExtension;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.ui.table.ktable.model.ContentId;
import ru.dip.ui.table.ktable.model.ContentType;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.TableElement;
import ru.dip.ui.table.ktable.model.TableNode;
import ru.dip.ui.table.table.TableSettings;


public class KTableColorInteractor {

	private final int FIRST_COLOR = 0;
	private final int SECOND_COLOR = 1;

	private final ColorApplicator fFolderColorApplicator = new FolderColorApplicator();
	private final ColorApplicator fFolderItemsApplicator = new FolderItemsApplicator();
	private final ColorApplicator fFolderFilesApplicator = new FolderFilesApplicator();
	private final ColorApplicator fSimpleApplicator = new SimpleItemsApplicator();
	private final ColorApplicator fNoBackgrounApplicator = new NoBackgoundApplicator();

	private final KTableComposite fTableComposite;
	private Object fExpndedItem = null; // устанавливается если Node был свёрнут или развернут

	public KTableColorInteractor(KTableComposite tableComposite) {
		fTableComposite = tableComposite;
	}

	public void expandElement(IDipTableElement element) {
		fExpndedItem = element;
		updateBackgrouColor();
	}

	public void collapseElement(IDipTableElement element) {
		fExpndedItem = element;
		updateBackgrouColor();
	}

	public void updateBackgrouColor() {
		// background
		updateBackgroundColor(fTableComposite.tableModel().getRoot());
		// ctrl + double click to folder
		if (fTableComposite.selector().isDoubleClickFolderMode()) {
			applyDoubleClickMode(fTableComposite.tableModel().getElements());
		}
		fExpndedItem = null;
	}

	// ================================
	// Backgound

	private void updateBackgroundColor(TableElement root) {
		ColorApplicator applicator = getColorApplicator();
		applicator.apply(root);
	}

	private ColorApplicator getColorApplicator() {
		int backgroundMode = TableSettings.backGroundMode();
		switch (backgroundMode) {
			case TableSettings.FOLDERS_BACKGROUND_MODE: {
				return fFolderColorApplicator;
			}
			case TableSettings.FOLDER_ITEMS_MODE: {
				return fFolderItemsApplicator;
			}
			case TableSettings.FOLDERS_FILES_MODE: {
				return fFolderFilesApplicator;
			}
			case TableSettings.FILES_BACKGROUND_MODE: {
				return fSimpleApplicator;
			}
			default: {
				return fNoBackgrounApplicator;
			}
		}
	}

	private abstract class ColorApplicator {

		abstract void apply(IDipTableElement root);

		abstract int apply(IDipTableElement item, int colorNumer);

		int applyToChildren(TableNode node, int colorNumber) {
			boolean expanded = node.expand();
			if (node.equals(fExpndedItem)) {
				expanded = !expanded;
			}
			if (expanded) {
				for (IDipTableElement childItem : node.children()) {
					colorNumber = apply(childItem, colorNumber);
				}
			}
			return colorNumber;
		}
	}

	/*
	 * Режим заливки: чередование директорий
	 */
	private class FolderColorApplicator extends ColorApplicator {

		private IDipParent fCurrentColorParent;

		@Override
		void apply(IDipTableElement root) {
			fCurrentColorParent = null;
			apply(root, FIRST_COLOR);
		}

		@Override
		int apply(IDipTableElement item, int colorNumber) {
			IDipDocumentElement resDipDocElement = item.dipResourceElement();
			IDipParent dipParent = currentDipParent(resDipDocElement);
			// need change color
			boolean nextElement = !dipParent.equals(fCurrentColorParent);
			if (nextElement) {
				colorNumber = changeColor(colorNumber);
			}
			// set color
			setColor(item, colorNumber);
			fCurrentColorParent = dipParent;
			// apply to children
			if (item instanceof TableNode) {
				colorNumber = applyToChildren((TableNode) item, colorNumber);
			}
			// special
			checkSpecialColor(item);
			return colorNumber;
		}

		private IDipParent currentDipParent(IDipDocumentElement resDipDocElement) {
			if (resDipDocElement instanceof IDipParent) {
				return (IDipParent) resDipDocElement;
			} else {
				return resDipDocElement.parent();
			}
		}

	}

	/*
	 * Режим заливки: чередование строк начиная с директории
	 */
	private class FolderItemsApplicator extends ColorApplicator {

		private IDipDocumentElement fCurrentFolderItemElement;

		@Override
		public void apply(IDipTableElement root) {
			fCurrentFolderItemElement = null;
			apply(root, FIRST_COLOR);
		}
		
		@Override
		int apply(IDipTableElement item, int colorNumber) {
			if (item instanceof TableNode) {
				return applyToNode((TableNode) item, FIRST_COLOR);
			} else {
				return applyToElement(item, colorNumber);
			}
		}

		private int applyToNode(TableNode node, int colorNumber) {
			fCurrentFolderItemElement = null;
			node.setBackground(TableSettings.tableColor1());
			colorNumber = applyToChildren(node, colorNumber);
			checkSpecialColor(node);
			fCurrentFolderItemElement = null;
			return FIRST_COLOR;
		}

		private int applyToElement(IDipTableElement element, int colorNumber) {
			IDipDocumentElement resDipDocElement = element.dipResourceElement();
			boolean nextElement = !resDipDocElement.equals(fCurrentFolderItemElement);
			if (nextElement) {
				colorNumber = changeColor(colorNumber);
			}
			setColor(element, colorNumber);
			fCurrentFolderItemElement = resDipDocElement;
			checkSpecialColor(element);
			return colorNumber;
		}
	}

	/*
	 * Режим заливки: заливка директории-строки
	 */
	private class FolderFilesApplicator extends ColorApplicator {

		@Override
		void apply(IDipTableElement root) {
			apply(root, -1);
		}

		@Override
		int apply(IDipTableElement item, int currentColor) {
			if (item instanceof TableNode) {
				item.setBackground(TableSettings.tableColor2());
				applyToChildren((TableNode) item, currentColor);
			} else {
				item.setBackground(TableSettings.tableColor1());

			}
			checkSpecialColor(item);
			return -1;
		}
	}

	/*
	 * Режим заливки - чередование строк
	 */
	private class SimpleItemsApplicator extends ColorApplicator {

		private IDipDocumentElement fCurrentColorElement;

		@Override
		void apply(IDipTableElement root) {
			fCurrentColorElement = null;
			apply(root, FIRST_COLOR);
		}

		@Override
		int apply(IDipTableElement item, int colorNumber) {
			IDipDocumentElement resReqirement = item.dipResourceElement();

			boolean nextElement = !resReqirement.equals(fCurrentColorElement);
			if (nextElement) {
				colorNumber = changeColor(colorNumber);
			}

			setColor(item, colorNumber);
			fCurrentColorElement = resReqirement;

			if (item instanceof TableNode) {
				colorNumber = applyToChildren((TableNode) item, colorNumber);
			}
			checkSpecialColor(item);
			return colorNumber;
		}

	}

	/*
	 * Режим без заливки
	 */
	private class NoBackgoundApplicator extends ColorApplicator {

		@Override
		void apply(IDipTableElement root) {
			apply(root, -1);
		}

		@Override
		int apply(IDipTableElement item, int colorNumer) {
			if (item instanceof TableNode) {
				applyToChildren((TableNode) item, -1);
			}
			checkSpecialColor(item);
			return -1;
		}
	}

	// ==============================

	private int changeColor(int colorNumber) {
		return colorNumber == SECOND_COLOR ? FIRST_COLOR : SECOND_COLOR;
	}

	private void setColor(IDipTableElement item, int colorNumber) {
		if (colorNumber == FIRST_COLOR) {
			item.setBackground(TableSettings.tableColor1());
		} else {
			item.setBackground(TableSettings.tableColor2());
		}
	}

	// ==========================
	// special color

	private boolean checkSpecialColor(IDipTableElement element) {
		if (fTableComposite.selector().isSelect(element)) {
			element.put(ContentId.PRESENTATION, ContentType.BACKGROUND, TableSettings.tableSelectionColor());
			element.put(ContentId.ID, ContentType.BACKGROUND, null);
			return true;
		}
		return false;
	}

	// ===========================
	// double click folder mode

	private void applyDoubleClickMode(List<IDipTableElement> elements) {
		Object object = fTableComposite.selector().getLastSelectObject();
		if (object instanceof TableNode) {
			if (fTableComposite.isOneListMode()) {
				applyFlatDoubleClick(elements, ((TableNode) object).dipDocElement());
			} else {
				applyDoubleClickFolderMode(elements, ((TableNode) object).dipDocElement());
			}
		}
	}

	/*
	 * Ctrl + double click folder for flat mode
	 */
	private void applyFlatDoubleClick(List<IDipTableElement> /* TreeItem[] */ items, IDipParent selectedFolder) {
		IDipParent rootFolder = getRootSelectFolder(selectedFolder);
		if (rootFolder == null) {
			return;
		}
		HashSet<IDipDocumentElement> parents = new HashSet<>();
		setDoubleClickParentColors(parents, rootFolder, selectedFolder);

		HashSet<IDipDocumentElement> children = new HashSet<>();
		setDoubleClickChildColors(children, selectedFolder);
		children.remove(selectedFolder);

		for (IDipTableElement item : items) {
			Object obj = item.dipDocElement();
			if (obj instanceof IUnitExtension) {
				obj = ((IUnitExtension) obj).getDipUnit();
			}
			if (parents.contains(obj)) {
				item.setBackground(TableSettings.tableColor2());
			} else if (children.contains(obj)) {
				item.setBackground(ColorProvider.TABLE_DOUBLE_CLICK_CHILD);
			}
		}
	}

	private void setDoubleClickParentColors(HashSet<IDipDocumentElement> set, IDipDocumentElement dipDocElement, IDipParent selectedFolder) {
		if (dipDocElement != null && dipDocElement.equals(selectedFolder)) {
			return;
		}
		set.add(dipDocElement);
		if (dipDocElement instanceof IDipParent) {
			IDipParent dipParent = (IDipParent) dipDocElement;
			for (IDipDocumentElement childReq : dipParent.getDipDocChildrenList()) {
				setDoubleClickParentColors(set, childReq, selectedFolder);
			}
		}
	}

	private void setDoubleClickChildColors(HashSet<IDipDocumentElement> set, IDipDocumentElement dipDocElement) {
		set.add(dipDocElement);
		if (dipDocElement instanceof IDipParent) {
			IDipParent dipParent = (IDipParent) dipDocElement;
			for (IDipDocumentElement childReq : dipParent.getDipDocChildrenList()) {
				setDoubleClickChildColors(set, childReq);
			}
		}
	}

	/*
	 * Ctrl + double click folder for normal mode
	 */
	private void applyDoubleClickFolderMode(List<IDipTableElement> items, IDipParent selectedFolder) {
		IDipParent rootFolder = getRootSelectFolder(selectedFolder);
		if (rootFolder == null) {
			return;
		}
		IDipTableElement rootItem = getRootItem(items, rootFolder);
		if (rootItem == null) {
			return;
		}
		IDipTableElement selectItem = setDoubleClickParentColors(rootItem, selectedFolder);
		if (selectItem == null) {
			return;
		}
		setDoubleClickChildrenColors(selectItem);
		selectItem.setBackground(TableSettings.tableSelectionColor());
	}

	private IDipParent getRootSelectFolder(IDipParent parent) {
		for (IDipDocumentElement dipDocElement : fTableComposite.model().getDipChildren()) {
			if (dipDocElement instanceof IDipParent) {
				IDipParent dipParent = (IDipParent) dipDocElement;
				if (dipDocElement.equals(parent)) {
					return dipParent;
				}
				if (parent.hasParent(dipParent)) {
					return dipParent;
				}
			}
		}
		return null;
	}

	private IDipTableElement getRootItem(List<IDipTableElement> items, IDipParent rootFolder) {
		for (IDipTableElement item : items) {
			Object obj = item.dipDocElement();
			if (obj.equals(rootFolder)) {
				return item;
			}
		}
		return null;
	}

	private IDipTableElement setDoubleClickParentColors(IDipTableElement item, IDipParent selectedFolder) {
		Object obj = item.dipDocElement();
		if (obj != null && obj.equals(selectedFolder)) {
			return item;
		}
		item.setBackground(TableSettings.tableColor2());
		IDipTableElement result = null;
		if (item instanceof TableNode) {
			for (IDipTableElement childItem : ((TableNode) item).children()) {
				IDipTableElement childResult = setDoubleClickParentColors(childItem, selectedFolder);
				if (childResult != null) {
					result = childResult;
				}
			}
		}
		return result;
	}

	private void setDoubleClickChildrenColors(IDipTableElement item) {
		item.setBackground(ColorProvider.TABLE_DOUBLE_CLICK_CHILD);
		if (item instanceof TableNode) {
			for (IDipTableElement childItem : ((TableNode) item).children()) {
				setDoubleClickChildrenColors(childItem);
			}
		}
	}

}
