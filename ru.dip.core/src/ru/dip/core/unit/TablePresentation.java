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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.model.interfaces.IFindable;
import ru.dip.core.model.interfaces.IGlossarySupport;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.ISpellErrorPoints;
import ru.dip.core.model.interfaces.IVariablesSupport;

public abstract class TablePresentation implements IFindable, IGlossarySupport,IVariablesSupport, ISpellErrorPoints {
	
	private final IDipUnit fUnit;
	private final SpellErrorsPoints fSpellErrorPoints;
	private long fTimeModified;
	
	public TablePresentation(IDipUnit unit) {
		fUnit = unit;
		fTimeModified = fUnit.resource().getModificationStamp();
		fSpellErrorPoints = new SpellErrorsPoints();
		read();
	}
	
	protected abstract void read();
	
	public boolean checkUpdate(){
		long newValue = getResource().getModificationStamp();
		if (newValue != getTimeModified()){
			setTimeModified(newValue);
			read();
			return true;
		}		
		return false;
	}
	
	protected IFile getResource(){
		return getUnit().resource();
	}

	public abstract String getText();
	
	public abstract Image getImage();
	
	public void findErrorWords(String text) {
		fSpellErrorPoints.findErrorWords(text);
	}
	
	public List<Point> errorsPoints(){
		return fSpellErrorPoints.errorsPoints();
	}
	
	public IDipUnit getUnit(){
		return fUnit;
	}
	
	public long getTimeModified(){
		return fTimeModified;			
	}
	
	public void setTimeModified(long newValue){
		fTimeModified = newValue;
	}

	protected void dispose() {
		if (getImage() != null) {
			getImage().dispose();
		}
	}
	
}
