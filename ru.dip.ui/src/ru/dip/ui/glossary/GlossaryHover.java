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
package ru.dip.ui.glossary;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipRoot;
import ru.dip.core.model.glossary.GlossaryField;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.utilities.ui.WSelectionListener;
import ru.dip.core.utilities.ui.hover.AbstractSelectionHover;

public class GlossaryHover extends AbstractSelectionHover {

	public static GlossaryHover instance;

	public static GlossaryHover getInstance(){
		if (instance == null){
			instance = new GlossaryHover();
		}
		return instance;
	}
	
	public static void addTextSelectionListener(StyledText sText, IFile file) {
		
		sText.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String text = sText.getSelectionText();
				String hoverText = GlossaryHover.getInstance().getText(file, text);
				GlossaryHover.getInstance().mouseHover(hoverText, sText);
				sText.setFocus();
			}
		});	
	}
	
	
	private GlossaryHover() {}
	
	@Override
	public String getText(IFile file, String text){	
		DipProject dipProject = DipRoot.getInstance().getDipProject(file.getProject());
		if (text == null || text.isEmpty()){
			return null;
		}		
		GlossaryFolder glossFolder = dipProject.getGlossaryFolder();
		if (glossFolder == null){
			return null;
		}
		GlossaryField field = glossFolder.getChild(text);
		if (field == null){
			return null;
		}
		String result =  field.getValue();
		return result.trim();
	}
	
}
