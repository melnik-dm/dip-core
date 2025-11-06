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
package ru.dip.ui.export;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.utilities.ui.CompositeBuilder;
import ru.dip.core.utilities.ui.ControlFactory;

public class FailJavaExportResultDialog extends Dialog {

	protected FailJavaExportResultDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = CompositeBuilder.instance(parent).full().build();
		ControlFactory.label(composite, "Экспорт на Java не выполнен.");	
		// Ошибка при экспорте на Java
		// Текст ошибки
		
		// продолжить экспорт на Python
		// выход

		return super.createDialogArea(parent);
	}

}
