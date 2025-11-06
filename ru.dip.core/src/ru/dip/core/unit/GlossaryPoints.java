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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Point;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IGlossaryPoints;

public class GlossaryPoints implements IGlossaryPoints  {
	
	private List<Point> fGlossaryPoints = new ArrayList<>();
	private final DipProject fProject;
	
	public GlossaryPoints(DipProject project){
		fProject = project;
	}
	
	public void findGlossaryWords(String text){
		fGlossaryPoints = fProject.getGlossaryFolder().findKeyWords(text);
	}
	
	public List<Point> glossaryPoints(){
		return fGlossaryPoints;
	}

}
