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
package ru.dip.core.utilities;

import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import ru.dip.core.annotation.CallChangeDipChildren;
import ru.dip.core.annotation.ChangeDipChildren;
import ru.dip.core.annotation.NoChangeDipChildren;
import ru.dip.core.exception.SaveTableDIPException;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.table.TableWriter;

public class DipTableUtilities {
		
	//======================
	// new folder
	
	public static boolean canNewFolderAfter(DipUnit unit){
		IDipDocumentElement nextElement = getNextElement(unit);
		return  (nextElement == null || nextElement instanceof IDipParent);
	}
	
	public static IDipParent addNewFolderBefore(IDipDocumentElement element, IFolder folder) {
		IDipParent parent = element.parent();
		List<IDipDocumentElement> children = parent.getDipDocChildrenList();
		int index = children.indexOf(element);
		return parent.createNewFolder(folder, index);
	}
	
	public static IDipParent addNewFolderAfter(IDipDocumentElement element, IFolder folder) {
		IDipParent parent = element.parent();
		List<IDipDocumentElement> children = parent.getDipDocChildrenList();
		int index = children.indexOf(element) + 1;
		return parent.createNewFolder(folder, index);
	}
	
	public static IDipParent addNewFolderStart(IDipParent parent, IFolder folder) {
		return parent.createNewFolder(folder);
	}
	
	public static IDipParent addNewFolderEnd(IDipParent parent, IFolder folder) {
		int newLastIndex = parent.getDipDocChildrenList().size();
		return parent.createNewFolder(folder, newLastIndex);
	}
	
	public static IDipParent addNewFolderByIndex(IDipParent parent, IFolder folder, int index) {
		return parent.createNewFolder(folder, index);
	}
	
	//=========================
	// include folder
	
	public static IDipParent addIncludeFolderBefore(IDipDocumentElement element, IFolder folder, String name,
			String description, boolean readOnly) {
		IDipParent parent = element.parent();
		List<IDipDocumentElement> children = parent.getDipDocChildrenList();
		int index = children.indexOf(element);
		return parent.includeFolder(folder, index, name, description, readOnly);
	}
	
	public static IDipParent addIncludeFolderAfter(IDipDocumentElement element, IFolder folder, String name, 
			String description, boolean readOnly) {
		IDipParent parent = element.parent();
		List<IDipDocumentElement> children = parent.getDipDocChildrenList();
		int index = children.indexOf(element) + 1;
		return parent.includeFolder(folder, index, name, description, readOnly);
	}
	
	public static IDipParent addIncludeFolderStart(IDipParent parent, IFolder folder, String name,
			String description, boolean readOnly) {
		return parent.includeFolder(folder, name,  description, readOnly);
	}
	
	public static IDipParent addIncludeFolderEnd(IDipParent parent, IFolder folder, String name,
			String description, boolean readOnly) {
		int newLastIndex = parent.getDipDocChildrenList().size();
		return parent.includeFolder(folder, newLastIndex, name, description, readOnly);
	}

	//=========================
	// new file
	
	public static boolean canNewFileBefore(IDipParent parent){
		IDipDocumentElement previousElement = getPreviousElement(parent);
		return (previousElement == null || previousElement instanceof DipUnit);
	}
	
	public static IDipElement addNewFileBefore(IDipDocumentElement element, IFile file) {
		IDipParent parent = element.parent();
		List<IDipDocumentElement> children = parent.getDipDocChildrenList();
		int dipIndex = children.indexOf(element);
		return parent.createNewUnit(file, dipIndex);
	}

	public static IDipElement addNewFileAfter(IDipDocumentElement element, IFile file) {
		IDipParent parent = element.parent();
		List<IDipDocumentElement> children = parent.getDipDocChildrenList();
		int dipIndex = children.indexOf(element);
		return parent.createNewUnit(file, dipIndex + 1);
	}
	
	public static IDipElement addNewFileStart(IDipParent parent, IFile file) {
		return parent.createNewUnit(file);
	}
	
	public static IDipElement addNewFileEnd(IDipParent parent, IFile file){
		int lastUnitIndex = getLastUnitIndex(parent) + 1;
		return parent.createNewUnit(file, lastUnitIndex);
	}
	
	public static IDipElement addNewFileByIndex(IDipParent parent, IFile file, int index) {
		return parent.createNewUnit(file, index);
	}

	//===============================================
	//   UP - DOWN
	
	@NoChangeDipChildren
	public static boolean canUp(IDipDocumentElement element){		
		if (element instanceof DipUnit){
			return canUp((DipUnit) element);
		} else if (element instanceof IDipParent){
			return canUp((IDipParent) element);
		}		
		return false;
	}
	
	@NoChangeDipChildren
	private static boolean canUp(DipUnit unit){
		IDipDocumentElement previous = getPreviousElement(unit);
		return previous instanceof DipUnit;	
	}
	
	@NoChangeDipChildren
	private static boolean canUp(IDipParent parent){
		IDipDocumentElement previous = getPreviousElement(parent);
		return previous instanceof IDipParent;
	}
	
	/**
	 * Можно ли двигать вверх: одного типа, один родитель, друг за другом, верхний можно двигать вверх
	 */
	@NoChangeDipChildren
	public static boolean canUp(TreeSet<IDipDocumentElement> dipDocumentElements) {
		IDipDocumentElement first = dipDocumentElements.first();		
		if (first.getClass() != dipDocumentElements.last().getClass()) {
			return false;
		}
		IDipParent parent = first.parent();
		int currentIndex = first.getIndex() - 1;
		
		for (IDipDocumentElement dipDocumentElement : dipDocumentElements) {
			if (!dipDocumentElement.parent().equals(parent)) {
				return false;
			}
			int index = dipDocumentElement.getIndex();
			if (index != currentIndex + 1) {
				return false;
			}
			currentIndex = index;			
		}												
		return canUp(first);
	}
	
	@CallChangeDipChildren
	public static void up(IDipDocumentElement element){
		List<IDipDocumentElement> children = element.parent().getDipDocChildrenList();
		int newIndex = children.indexOf(element) - 1;
		moveElement(element, newIndex, children);
	}
	
	@CallChangeDipChildren
	public static void up(TreeSet<IDipDocumentElement> dipDocumentElements) {
		IDipDocumentElement first = dipDocumentElements.first();	
		IDipDocumentElement movedElement = getPreviousElement(first);
		IDipDocumentElement last = dipDocumentElements.last();
		if (first == null || movedElement == null || last == null) {
			return;
		}
		int newIndex = last.getIndex();		
		List<IDipDocumentElement> children = first.parent().getDipDocChildrenList();
		moveElement(movedElement, newIndex, children);
	}
		
	@NoChangeDipChildren
	public static boolean canDown(IDipDocumentElement element){		
		if (element instanceof DipUnit){
			return canDown((DipUnit) element);
		} else if (element instanceof IDipParent){
			return canDown((IDipParent) element);
		}		
		return false;
	}
	
	@NoChangeDipChildren
	private static boolean canDown(DipUnit unit){
		IDipDocumentElement next = getNextElement(unit);
		return next instanceof DipUnit;	
	}
	
	@NoChangeDipChildren
	private static boolean canDown(IDipParent parent){
		IDipDocumentElement next = getNextElement(parent);
		return next instanceof IDipParent;
	}
	
	/**
	 * Можно ли двигать вниз: одного типа, один родитель, друг за другом, нижний можно двигать вниз
	 */
	@NoChangeDipChildren
	public static boolean canDown(TreeSet<IDipDocumentElement> dipDocumentElements) {
		IDipDocumentElement first = dipDocumentElements.first();		
		if (first.getClass() != dipDocumentElements.last().getClass()) {
			return false;
		}
		IDipParent parent = first.parent();
		int currentIndex = first.getIndex() - 1;
		
		for (IDipDocumentElement dipDocumentElement : dipDocumentElements) {
			if (!dipDocumentElement.parent().equals(parent)) {
				return false;
			}
			int index = dipDocumentElement.getIndex();
			if (index != currentIndex + 1) {
				return false;
			}
			currentIndex = index;			
		}												
		return canDown(dipDocumentElements.last());
	}
	
	@CallChangeDipChildren
	public static void down(IDipDocumentElement element){
		List<IDipDocumentElement> children = element.parent().getDipDocChildrenList();
		int newIndex = children.indexOf(element) + 1;
		moveElement(element, newIndex, children);
	}
	
	@CallChangeDipChildren
	public static void down(TreeSet<IDipDocumentElement> dipDocumentElements) {
		IDipDocumentElement first = dipDocumentElements.first();	
		IDipDocumentElement last = dipDocumentElements.last();
		IDipDocumentElement movedElement = getNextElement(last);
		if (first == null || movedElement == null || last == null) {
			return;
		}
		int newIndex = first.getIndex();			
		List<IDipDocumentElement> children = first.parent().getDipDocChildrenList();
		moveElement(movedElement, newIndex, children);
	}
	
	@ChangeDipChildren	
	private static void moveElement(IDipDocumentElement element, int newIndex, List<IDipDocumentElement> parentChildren) {
		parentChildren.remove(element);
		parentChildren.add(newIndex, element);
	}
	
	//=====================================
	// into folder
	
	public static boolean canIntoFolder(TreeSet<IDipDocumentElement> dipDocElements) {
		IDipParent parent = dipDocElements.first().parent();
		if (parent == null) {
			return false;
		}		
		for (IDipDocumentElement dipDocElement: dipDocElements) {
			if (!parent.equals(dipDocElement.parent())) {
				return false;
			}
		}				
		return true;
	}
	
	//========================
	// previous/next element
	
	public static  IDipDocumentElement getNextElement(IDipDocumentElement element){
		List<IDipDocumentElement> children = element.parent().getDipDocChildrenList();
		int index = children.indexOf(element);
		if (index == children.size() - 1){
			return null;
		}
		return children.get(index + 1);
	}
	
	public static IDipDocumentElement getPreviousElement(IDipDocumentElement element){
		if (element == null) {
			return null;
		}
		IDipParent parent = element.parent();
		if (parent == null) {
			return null;
		}		
		int index = parent.getDipDocChildrenList().indexOf(element);
		if (index <= 0){
			return null;
		}
		IDipDocumentElement dipDocElement = parent.getDipDocChildrenList().get(index - 1);
		return dipDocElement;
	}
	
	public static int getFirstParentIndex(IDipParent parent){
		List<IDipDocumentElement> children = parent.getDipDocChildrenList();
		for (int i = 0; i < children.size(); i++){
			IDipDocumentElement element = children.get(i);
			if (element instanceof IDipParent){
				return i;
			}
		}
		return children.size();
	}
	
	public static IDipDocumentElement getLastUnitElement(IDipParent parent){
		List<IDipDocumentElement> children = parent.getDipDocChildrenList();
		IDipDocumentElement last = null;
		for (IDipDocumentElement dipDocElement: children){
			if (dipDocElement instanceof DipUnit){
				last = dipDocElement;
			} else {
				return last;
			}
		}
		return last;
	}
	
	public static int getLastUnitIndex(IDipParent parent) {
		List<IDipDocumentElement> children = parent.getDipDocChildrenList();		
		int result = -1;
		for (int i = 0; i < children.size(); i++){
			IDipDocumentElement element = children.get(i);
			if (element instanceof IDipParent){
				return result;
			} else {
				result++;
			}
		}
		return result;
	}
	
	public static int getIndex(IDipDocumentElement element) {
		List<IDipDocumentElement> children = element.parent().getDipDocChildrenList();		
		return children.indexOf(element);
	}
	
	public static IDipParent getLastParent(IDipParent parent) {
		List<IDipDocumentElement> children = parent.getDipDocChildrenList();
		if (children.isEmpty()) {
			return null;
		}
		IDipDocumentElement last = children.get(children.size() - 1);
		if (last instanceof IDipParent) {
			return (IDipParent) last;
		}
		return null;
	}
	
	//=============================
	// numbering name
	
	public static String getNextNumber(IDipParent parent, IDipDocumentElement previous){		
		if (parent.isFileNumeration()){			
			String stepStr = parent.getFileStep();
			if (previous == null){
				return stepStr;
			}
		return getNextNumber(stepStr, previous);
		}		
		return null;
	}
	
	public static String getStartNumberInFolder(IDipParent parent) {
		if (parent.isFileNumeration()) {
			return parent.getFileStep();
		}		
		return null;		
	}
	
	public static String getEndNumberInFolder(IDipParent parent) {
		if (parent.isFileNumeration()) {
			IDipDocumentElement last = getLastUnitElement(parent);
			return getNextNumber(parent.getFileStep(), last);			
		}	
		return null;
	}
	
	public static String getNextNumber(String stepStr, IDipDocumentElement previous){
		if (previous == null){
			return stepStr;
		}
		String previousName = previous.name();
		if (previousName.length() < stepStr.length()){
			IDipDocumentElement newPrevious = getPreviousElement(previous);
			return getNextNumber(stepStr, newPrevious);
		}
		String previousNumberString = previousName.substring(0, stepStr.length());
		try {
			int previousNumber = Integer.parseInt(previousNumberString);
			int stepNumber = Integer.parseInt(stepStr);
			if (previousNumber % stepNumber == 0){
				int number = previousNumber + stepNumber;
				return appendStepPrefix(stepStr, number);
			} 		
		} catch (NumberFormatException ignore){
			// NOP
		}
		IDipDocumentElement newPrevious = getPreviousElement(previous);
		return getNextNumber(stepStr, newPrevious);		
	}
	
	public static String getFolderNextNumber(IDipParent parent, IDipDocumentElement previous){		
		if (parent.isFolderNumeration()){			
			String stepStr = parent.getFolderStep();
			if (previous == null  || !(previous instanceof IDipParent)){
				return stepStr;
			}
			return getFolderNextNumber(stepStr, previous);	
		}		
		return null;
	}
	
	public static String getFolderNextNumber(String stepStr, IDipDocumentElement previous){
		if (previous == null || !(previous instanceof IDipParent)){
			return stepStr;
		}
		String previousName = previous.name();
		if (previousName.length() < stepStr.length()){
			IDipDocumentElement newPrevious = getPreviousElement(previous);
			return getNextNumber(stepStr, newPrevious);
		}
		String previousNumberString = previousName.substring(0, stepStr.length());
		try {
			int previousNumber = Integer.parseInt(previousNumberString);
			int stepNumber = Integer.parseInt(stepStr);
			if (previousNumber % stepNumber == 0){
				int number = previousNumber + stepNumber;
				return appendStepPrefix(stepStr, number);
			} 		
		} catch (NumberFormatException ignore){
			// NOP
		}
		IDipDocumentElement newPrevious = getPreviousElement(previous);
		return getNextNumber(stepStr, newPrevious);		
	}
	
	public static String getStartFolderNumber(IDipParent parent) {
		if (parent.isFolderNumeration()) {
			return parent.getFolderStep();
		}		
		return null;		
	}
	
	public static String getEndFolderNumber(IDipParent parent) {
		if (parent.isFolderNumeration()) {			
			List<IDipDocumentElement> children = parent.getDipDocChildrenList();
			if (children.isEmpty()) {
				return null;
			}			
			IDipDocumentElement last = children.get(children.size() - 1);
			return getFolderNextNumber(parent, last);	
		}	
		return null;
	}
	
	public static String appendStepPrefix(String step, int number) {
		String numberString = String.valueOf(number);
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < step.length() - numberString.length(); i++) {
			builder.insert(0, '0');
		}
		builder.append(numberString);
		return builder.toString();
	}
	
	//================================
	// save table
	
	public static void saveModel(IDipParent parent) {
		try {
			TableWriter.saveModel(parent);
		} catch (ParserConfigurationException | IOException e) {
			new SaveTableDIPException(parent, "Ошибка сохранения таблицы Document");
			WorkbenchUtitlities.openError("Save table error", "Ошибка сохранения таблицы");
			e.printStackTrace();
		}
	}
	
}
