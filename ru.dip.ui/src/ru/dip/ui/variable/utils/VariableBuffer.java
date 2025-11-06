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
package ru.dip.ui.variable.utils;

import java.util.stream.Stream;

import ru.dip.core.model.vars.VarContainer;
import ru.dip.core.model.vars.Variable;

public class VariableBuffer {

	private static VariableBuffer instance;
	
	public static VariableBuffer getInstance(){
		if (instance == null){
			instance = new VariableBuffer();
		}
		return instance;
	}

	private Variable[] fVariables;
	
	private VariableBuffer(){}
	
	public boolean isVariables(Object[] objs){
		if (objs.length > 0){
			if (objs[0] instanceof Variable){
				return true;
			}
			if (objs[0] instanceof VarContainer){
				return true;
			}
		}		
		return false;
	}
	
	public boolean checkVariables(Object[] objs){
		if (objs.length == 1 && objs[0] instanceof VarContainer){
			return true;
		}
		for (Object obj: objs){
			if (!(obj instanceof Variable)){
				return false;
			}
		}		
		return true;
	}
	
	public void setVariables(Object[] objects){
		if (objects.length == 1 && objects[0] instanceof VarContainer){
			VarContainer glossFolder = (VarContainer) objects[0];
			if (glossFolder.hasChildren()){
				fVariables = glossFolder.getChildren().stream().toArray(Variable[]::new);
			}
		}	
		for (Object obj: objects){
			if (obj instanceof Variable){
				fVariables = null;
			}
		}
		fVariables =  Stream.of(objects).map(e -> ((Variable) e)).toArray(Variable[]::new);		
	}
	
	public boolean isEmpty(){
		return fVariables == null || fVariables.length == 0;
	}
	
	public Variable[] getVariables(){
		return fVariables;		
	}
	
	public void setVariables(Variable[] fields){
		fVariables = fields;
	}
}
