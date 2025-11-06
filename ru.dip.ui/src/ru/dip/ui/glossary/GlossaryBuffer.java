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

import java.util.stream.Stream;

import ru.dip.core.model.glossary.GlossaryField;
import ru.dip.core.model.glossary.GlossaryFolder;

public class GlossaryBuffer {

	private static GlossaryBuffer instance;
	
	public static GlossaryBuffer getInstance(){
		if (instance == null){
			instance = new GlossaryBuffer();
		}
		return instance;
	}

	private GlossaryField[] fFields;
	
	private GlossaryBuffer(){}
	
	public boolean isGlossaryFields(Object[] objs){
		if (objs.length > 0){
			if (objs[0] instanceof GlossaryField){
				return true;
			}
			if (objs[0] instanceof GlossaryFolder){
				return true;
			}
		}		
		return false;
	}
	
	public boolean checkGlossaryFields(Object[] objs){
		if (objs.length == 1 && objs[0] instanceof GlossaryFolder){
			return true;
		}
		for (Object obj: objs){
			if (!(obj instanceof GlossaryField)){
				return false;
			}
		}		
		return true;
	}
	
	public void setGlossaryFields(Object[] objects){
		if (objects.length == 1 && objects[0] instanceof GlossaryFolder){
			GlossaryFolder glossFolder = (GlossaryFolder) objects[0];
			if (glossFolder.hasChildren()){
				fFields = glossFolder.getChildren().stream().toArray(GlossaryField[]::new);
			}
		}	
		for (Object obj: objects){
			if (obj instanceof GlossaryField){
				fFields = null;
			}
		}
		fFields =  Stream.of(objects).map(e -> ((GlossaryField) e)).toArray(GlossaryField[]::new);		
	}
	
	public boolean isEmpty(){
		return fFields == null || fFields.length == 0;
	}
	
	public GlossaryField[] getFields(){
		return fFields;		
	}
	
	public void setGlossaryFields(GlossaryField[] fields){
		fFields = fields;
	}
}
