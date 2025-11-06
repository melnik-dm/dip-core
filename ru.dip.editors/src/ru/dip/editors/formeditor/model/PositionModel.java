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
package ru.dip.editors.formeditor.model;

import ru.dip.core.form.model.CorePositionModel;
import ru.dip.core.form.partitioner.DocumentPartitioner;
import ru.dip.editors.formeditor.xml.FormTextEditor;

public class PositionModel extends CorePositionModel {
		
	public PositionModel(FormTextEditor editor) {		
		setFormModel(editor.getElementModel());
		setDocument(editor.getDocumnet());
		setDocumentPartitionet((DocumentPartitioner) fDocument.getDocumentPartitioner());
	}
	
}
