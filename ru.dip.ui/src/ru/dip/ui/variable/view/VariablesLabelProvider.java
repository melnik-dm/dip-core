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
 *******************************************************************************/package ru.dip.ui.variable.view;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TreeItem;

import ru.dip.core.model.vars.IVarContainer;
import ru.dip.core.model.vars.Variable;
import ru.dip.ui.utilities.image.ImageProvider;

public class VariablesLabelProvider extends StyledCellLabelProvider {
	
	private final VariablesComposite fComposite;
	
	public VariablesLabelProvider(VariablesComposite composite) {
		fComposite = composite;
	}
	
	@Override
	public void update(ViewerCell cell) {			
		int index = cell.getColumnIndex();
		Object obj = cell.getElement();
		if (obj instanceof Variable) {
			Variable var = (Variable) obj;
			if (index == 0) {
				cell.setImage(ImageProvider.VARIABLE);
				cell.setText(getName(var));
			} else if (index == 1) {
				cell.setText(var.getValue());
			}
		} else if (obj instanceof IVarContainer) {
			IVarContainer container = (IVarContainer) obj;
			if (index == 0) {
				cell.setImage(ImageProvider.VAR_CONTAINER);
				cell.setText(container.getRelativePath());
			}
		}
		super.update(cell);	
	}
	
	private String getName(Variable var) {
		if (fComposite.isDuplicateMode()) {
			IVarContainer container = var.parent();						
			String path = container.getRelativePath();
			if (path.isEmpty()) {
				return var.name();
			} else {
				return var.name() + " (" + path + ")";
			}										
		} else { 
			return var.name();
		}
	}
	
	@Override
	protected void erase(Event event, Object element) {
		event.detail &= ~SWT.FOREGROUND;
		event.detail &= ~SWT.HOT;
		event.detail &= ~SWT.SELECTED;
		GC gc = event.gc;
		gc.fillRectangle(event.x, event.y, event.width, event.height);
		super.erase(event, element);
	}

	@Override
	protected void measure(Event event, Object element) {
		int index = event.index;
		TreeItem item = (TreeItem) event.item;
		String text = item.getText(index);
		
		if (text != null) {
			event.gc.textExtent(text);
			TextLayout layout = new TextLayout(event.gc.getDevice());
			int width = 500;
			if (index == 0 && fComposite.getNameColumnWidth() > 20) {
				width = fComposite.getNameColumnWidth() - 20;
			} else if (index == 1) {
				width = fComposite.getValueColumnWidth();
			}

			if (width <= 0) {
				width = 40;
			}

			if (index == 0) {
				width += ImageProvider.VARIABLE.getBounds().width;
			}

			layout.setText(text);
			layout.setWidth(width);
			event.height = layout.getBounds().height;

		}
	}

	@Override
	protected void paint(Event event, Object element) {
		int index = event.index;
		TreeItem item = (TreeItem) event.item;			
		int x = event.x;
		int y = event.y;
		
		// paint image
		if (item.getData() instanceof IVarContainer) {
			x+= 16;
		}			
		if (index == 0 && item.getImage() != null) {
			event.gc.drawImage(item.getImage(), x, y);
			x += item.getImage().getBounds().width + 2;
		}
		
		// paint text
		String text = item.getText(index);
		if (text != null) {
			TextLayout layout = new TextLayout(event.gc.getDevice());
			if (index == 0) {
				layout.setWidth(fComposite.getNameColumnWidth() - 20);
			} else if (index == 1) {
				layout.setWidth(fComposite.getValueColumnWidth());
			}
			layout.setText(text);
			layout.draw(event.gc, x, y);
		}
	}
}
