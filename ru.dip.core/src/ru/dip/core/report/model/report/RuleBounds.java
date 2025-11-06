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
package ru.dip.core.report.model.report;

import java.util.ArrayList;
import java.util.List;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.utilities.DipUtilities;

public class RuleBounds {

	private List<IDipParent> fRecursiveFolders = new ArrayList<>();
	private List<IDipParent> fSingleFolders = new ArrayList<>();
	
	private ReportRule fRule;
	private DipProject fDipProject;
	
	public RuleBounds(ReportRule rule, DipProject project){
		fRule = rule;
		fDipProject = project;
		getBoundsParent();
	}
		
	private void getBoundsParent(){
		String bounds = fRule.getBounds();
		if (bounds == null || bounds.isEmpty()){
			bounds = fRule.getReportEntry().getBounds(); 			
		}
		if (bounds == null || bounds.isEmpty()){
			fRecursiveFolders.add(fDipProject);
			return;
		}
		String[] dirs = bounds.split(",");
		for (String str: dirs){
			String dir = str.trim();
			String path = dir;
			if (dir.endsWith("/*")){
				path = dir.substring(0, dir.length() - 2);
				IDipElement element = DipUtilities.findElement(fDipProject, path);
				if (element instanceof IDipParent){
					fRecursiveFolders.add((IDipParent) element);
				}
			} else {
				IDipElement element = DipUtilities.findElement(fDipProject, path);
				if (element instanceof IDipParent){
					fSingleFolders.add((IDipParent) element);
				}
			}
		}
	}
	
	public List<IDipParent> getRecursiveFolders(){
		return fRecursiveFolders;
	}
	
	public List<IDipParent> getSingleFolders(){
		return fSingleFolders;
	}
}
