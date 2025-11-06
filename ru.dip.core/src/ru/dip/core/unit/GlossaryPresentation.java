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

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;

import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.INotTextPresentation;

public class GlossaryPresentation extends TablePresentation implements INotTextPresentation  {

	public static final String FILE_NAME = ".glosref";
	private static final String GLOSSARY = "Глоссарий";
	private static final String NOT_GLOSSARY = "Glossary not found";
	
	private GlossaryFolder fGlosaryFolder;
	private long fGlossaryTimeModified;
	
	public GlossaryPresentation(IDipUnit unit) {
		super(unit);
		fGlossaryTimeModified = getGlossaryTimeModified();
	}
	
	private long getGlossaryTimeModified(){
		if (fGlosaryFolder != null){
			IFile file = fGlosaryFolder.getGlossaryFile();
			if (file != null && file.exists()){
				return file.getModificationStamp();
			}			
		}
		return -1;
	}

	@Override
	public boolean checkUpdate(){	
		fGlosaryFolder = getUnit().dipProject().getGlossaryFolder();	
		long newValue = getResource().getModificationStamp();
		long newReportValue = getGlossaryTimeModified();
		if (newValue != getTimeModified() || fGlossaryTimeModified != newReportValue){
			setTimeModified(newValue);
			fGlossaryTimeModified = newReportValue;
			read();
			return true;
		}		
		return false;
	}	
	
	@Override
	protected void read() {
		fGlosaryFolder = getUnit().dipProject().getGlossaryFolder();
	}

	@Override
	public String getText() {		
		return GLOSSARY;
	}
		
	public String getFixedText() {
		if (fGlosaryFolder == null){
			return NOT_GLOSSARY;
		}
		return GLOSSARY;
	}

	@Override
	public Image getImage() {
		return null;
	}
	
}
