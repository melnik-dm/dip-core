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
package ru.dip.ui.markview;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IMarkable;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;

public class MarkViewModel {
	
	// компаратор, сравнивает по номерам меток
	private static final Comparator<IMarkable> markableComparator = new Comparator<IMarkable>() {
		
		@Override
		public int compare(IMarkable o1, IMarkable o2) {
			for (int markNumber = 0; markNumber < IMarkable.MARKS_SIZE; markNumber++) {
				if (o1.isMark(markNumber) && !o2.isMark(markNumber)) {
					return -1;
				}
				if (!o1.isMark(markNumber) && o2.isMark(markNumber)) {
					return 1;
				}
			}						
			return 0;
		}		
	};
			
	private List<IMarkable> fAllMarkable;
	
	public MarkViewModel(DipProject dipProject) {
		getMarksFromProject(dipProject);
	}
	
	public void getMarksFromProject(DipProject dipProject){		
		fAllMarkable = marksFromParent(dipProject);
		fAllMarkable.sort(markableComparator);		
	}
		
	private List<IMarkable> marksFromParent(IDipParent parent) {
		 List<IMarkable> result = parent.getDipDocChildrenList().stream()
			.filter(IMarkable.class::isInstance)						
			.map(IMarkable.class::cast)
			.filter(this::hasAnyMark)
			.collect(Collectors.toList());
		 for (IDipDocumentElement dipDocElement: parent.getDipDocChildrenList()) {
			 if (dipDocElement instanceof IDipParent) {
				 result.addAll(marksFromParent((IDipParent) dipDocElement));
			 }			
		 }
		return result;
	}
	
	private boolean hasAnyMark(IMarkable markable) {
		for (int i = 0; i < IMarkable.MARKS_SIZE; i++) {
			if (markable.isMark(i)) {
				return true;
			}
		}	
		return false;		
	}
		
	/**
	 * Возвращает только те объекты у которых есть метки указанные в filter
	 */
	public List<IMarkable> getMarkables(List<Integer> filter){
		if (filter.size() == IMarkable.MARKS_SIZE) {
			return getAllMarkable();
		}
		
		return fAllMarkable.stream()
			.filter(m -> hasMark(m, filter))
			.collect(Collectors.toList());
	}
	
	private boolean hasMark(IMarkable markable, List<Integer> markNumbers) {
		return markNumbers.stream().anyMatch(markable::isMark);		
	}
	
	private List<IMarkable> getAllMarkable(){
		return fAllMarkable;
	}
	
}
