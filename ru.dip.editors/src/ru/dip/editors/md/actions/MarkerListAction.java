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
package ru.dip.editors.md.actions;

import ru.dip.editors.Messages;
import ru.dip.editors.md.partitions.PartitionStyles;
import ru.dip.ui.utilities.image.ImageProvider;

public class MarkerListAction extends NumberListAction {
	
	public static final String ID = Messages.MarkerListAction_ID;
	
	private static MarkerListAction instance = new MarkerListAction();
	
	public static MarkerListAction instance() {
		return instance;
	}
	
	private MarkerListAction() {
		setText(Messages.MarkerListAction_Name);
		setId(ID);	
		setChecked(true);
		setImageDescriptor(ImageProvider.MARKER_LIST_DESCRIPTOR);
	}
	
	@Override
	protected boolean isListItem(String type) {
		return PartitionStyles.GRAPHIC_LIST_ITEM.equals(type);
	}
	
	@Override
	protected String getMarker() {
		return "- "; //$NON-NLS-1$
	}

}
