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

import org.eclipse.jface.action.Action;

import ru.dip.editors.Messages;
import ru.dip.ui.preferences.MdPreferences;
import ru.dip.ui.utilities.image.ImageProvider;

public class FormatBlockCodeAction extends Action {
	
	public static final String ID = "ru.dip.editors.md.format.code.blocks"; //$NON-NLS-1$

	public FormatBlockCodeAction() {
		setId(ID);
		setText(Messages.FormatBlockCodeAction_ActionName);
		setImageDescriptor(ImageProvider.FORMAT_BLOCK_CODE);
		setChecked(MdPreferences.formatBlockCode());		
	}
	
	@Override
	public void run() {
		boolean checked = isChecked();
		MdPreferences.instance().updateFormatBlockCode(checked);
	}
	
}
