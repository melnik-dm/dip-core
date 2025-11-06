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
package ru.dip.ui.table.dialog.form;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;

import ru.dip.core.schema.FormShowProperties;

/** 
 * Временно хранит данные для FormProperties в диалоге
 * Если ОК - сохраняет в оригинальные Properties 
 */
public class DialogFormProperties {
	
	private FormShowProperties fProperties;
	private List<DialogFieldProperties> fFieldProperties;

	public DialogFormProperties(FormShowProperties properties) {
		fProperties = properties;
		fFieldProperties = fProperties.fieldSettings().stream().map(DialogFieldProperties::new)
				.collect(Collectors.toList());
	}

	public void saveFields(IProject project) {
		fFieldProperties.forEach(DialogFieldProperties::save);
		if (project != null) {
			fProperties.saveFields(project);
		}
	}

	public String name() {
		return fProperties.name();
	}
	
	public List<DialogFieldProperties> fieldProperteis(){
		return fFieldProperties;
	}
	
}

