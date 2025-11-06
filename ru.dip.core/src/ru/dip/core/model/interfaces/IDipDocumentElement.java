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
package ru.dip.core.model.interfaces;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ru.dip.core.model.DipProject;

public interface IDipDocumentElement extends IDipElement, ICommentSupport, IDescriptionSupport, IDisable {

	// компаратор, внутри одного проекта (проект, не сравнивается)
	public static final Comparator<IDipDocumentElement> indexComparator = new Comparator<IDipDocumentElement>() {

		@Override
		public int compare(IDipDocumentElement o1, IDipDocumentElement o2) {	
			if (o1 == null) {
				return -1;
			}
			if (o2 == null) {
				return 1;
			}
			if (o1.equals(o2)) {
				return 0;							
			}
			
			List<Integer> indexes1 = getIndexes(o1);
			List<Integer> indexes2 = getIndexes(o2);
						
			int size1 = indexes1.size();
			int size2 = indexes2.size();
			
			int minSize = Math.min(size1, size2);
			for (int i = 0; i < minSize; i++) {
				int index1 = indexes1.get(i);
				int index2 = indexes2.get(i);
				if (index1 == index2) {
					continue;
				}
				return index1 - index2;
			}
			return size1 - size2;
		}
		
		List<Integer> getIndexes(IDipDocumentElement dipDocumentElement){
			List<Integer> indexes = new ArrayList<>();
			getIndexes(dipDocumentElement, indexes);
			return indexes;
		}
		
		List<Integer> getIndexes(IDipDocumentElement dipDocumentElement, List<Integer> indexes){
			indexes.add(0, dipDocumentElement.getIndex());
			IDipParent parent = dipDocumentElement.parent();
			if (!(parent instanceof DipProject)) {
				getIndexes(parent, indexes);				
			}
			return indexes;
		}
		
	};
	
	@Override
	public IDipParent parent();
	
	default int getIndex() {
		if (parent() == null) {
			return -1;
		}		
		return parent().getDipDocChildrenList().indexOf(this);	
	}	
	
	/**
	 * Сильными элементами считаются DipProject, DipFolder, DipUnit
	 */
	public IDipDocumentElement strong();
	
}
