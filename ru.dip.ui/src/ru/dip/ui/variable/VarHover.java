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
package ru.dip.ui.variable;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipRoot;
import ru.dip.core.model.vars.Variable;
import ru.dip.core.utilities.ui.WSelectionListener;
import ru.dip.core.utilities.ui.hover.AbstractSelectionHover;

public class VarHover extends AbstractSelectionHover {

	public static VarHover instance;

	public static VarHover getInstance(){
		if (instance == null){
			instance = new VarHover();
		}
		return instance;
	}
	
	public static void addTextSelectionListener(StyledText sText, IFile file) {
		
		sText.addSelectionListener(new WSelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {								
				String text = sText.getSelectionText();
				String hoverText = VarHover.getInstance().getText(file, text);
				VarHover.getInstance().mouseHover(hoverText, sText);
				sText.setFocus();
			}		
		});	
	}


	private VarHover() {}
	
	@Override
	protected  String getText(IFile file, String text){	
		DipProject dipProject = DipRoot.getInstance().getDipProject(file.getProject());
		if (text == null || text.isEmpty()){
			return null;
		}			
		Variable variable = dipProject.getVariablesContainer().getChild(text);
		if (variable == null){
			return null;
		}
		return variable.getValue().trim();
	}
		
}
