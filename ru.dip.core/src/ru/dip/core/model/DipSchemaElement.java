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
package ru.dip.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import ru.dip.core.model.interfaces.IDipElement;

public class DipSchemaElement extends DipElement {

	public static DipSchemaElement instance(IResource resource, DipSchemaFolder parent) {
		IDipElement element = DipRoot.getInstance().getElement(resource, parent, DipElementType.SCHEMA);
		if (element == null) {
			DipSchemaElement formSchema = new DipSchemaElement(resource, parent);
			DipRoot.getInstance().putElement(formSchema);
			return formSchema;
		} else {
			return (DipSchemaElement) element;
		}
	}
	
	private DipSchemaElement(IResource resource, DipSchemaFolder parent) {
		super(resource, parent);
	}

	@Override
	public DipElementType type() {
		return DipElementType.SCHEMA;
	}
	
	@Override
	public DipSchemaFolder parent() {
		return (DipSchemaFolder) super.parent();
	}
	
	@Override
	public IFile resource() {
		return (IFile) super.resource();
	}
	
	public String getFormExtension() {
		String fileName = resource().getName();
		if (fileName.endsWith(".xml")) {
			return fileName.substring(0, fileName.length() - 4);
		} else {
			return fileName;
		}
	}

}
