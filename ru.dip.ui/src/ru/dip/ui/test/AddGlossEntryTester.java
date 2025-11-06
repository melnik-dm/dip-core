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
package ru.dip.ui.test;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipRoot;
import ru.dip.core.model.glossary.GlossaryField;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.utilities.WorkbenchUtitlities;

public class AddGlossEntryTester extends PropertyTester {


	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof TextSelection){								
			String text = ((TextSelection) receiver).getText();		
			if (text == null || text.isEmpty() || text.length() > GlossaryField.NAME_MAX_LENGTH){
				return false;
			}
			IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
			IFile file = WorkbenchUtitlities.getFileFromOpenedEditor(part);
			if (file == null){
				return false;
			}
			if (!DipNatureManager.hasNature(file)){
				return false;
			}
			DipProject project = DipRoot.getInstance().getDipProject(file.getProject());
			if (project == null){
				return false;
			}
			GlossaryFolder glossFolder = project.getGlossaryFolder();
			if (glossFolder == null){
				return false;
			}			
			GlossaryField field = glossFolder.getChild(text);
			if (field == null){
				return true;
			}
		}
		return false;
	}

}
