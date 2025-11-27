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
package ru.dip.core.unit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipEditor;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.WorkbenchUtitlities;

public class UnitPresentationCache {
	
	private static Map<IFile, TablePresentation> fPresentationById = new HashMap<>();
	
	public static TablePresentation getPresentation(IFile file) {
		return fPresentationById.get(file);
	}
	
	public static void putPresentation(IFile file, TablePresentation presentation) {
		fPresentationById.put(file, presentation);
	}
	
	public static void applyIfExists(IFile file, Consumer<TablePresentation> consumer) {
		TablePresentation tablePresentation = fPresentationById.get(file);
		if (tablePresentation != null) {
			consumer.accept(tablePresentation);
		}
	}
	
	public static void clearHash() {
		Set<IProject> projects = WorkbenchUtitlities.getOpenedDocumentEditors()
				.stream()
				.map(IDipEditor::model)
				.map(IDipParent::dipProject)
				.map(DipProject::resource)
				.collect(Collectors.toSet());
		
		Set<IFile> filesToRemove = fPresentationById.entrySet()
				.stream()
				.filter(e -> !projects.contains(e.getKey().getProject()))
				.peek(e ->  disposePresentation(e.getValue()))
				.map(e -> e.getKey())
				.collect(Collectors.toSet());
		
		fPresentationById.keySet().removeAll(filesToRemove);
	}
	
	private static void disposePresentation(TablePresentation presentation) {
		if (presentation != null) {
			presentation.dispose();
		}
	}
}
