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
package ru.dip.ui.table.ktable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.exception.DeleteDIPException;
import ru.dip.core.exception.TmpCopyException;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.unit.UnitPresentation;
import ru.dip.core.unit.md.MarkDownPresentation;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.core.utilities.tmp.MDJoinResult;
import ru.dip.core.utilities.tmp.MdExtractResult;
import ru.dip.core.utilities.tmp.TmpElement;
import ru.dip.ui.Messages;
import ru.dip.ui.table.dialog.MdExtractDialog;
import ru.dip.ui.table.dialog.MdJoinDialog;
import ru.dip.ui.table.dialog.MdSplitDialog;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.ITableNode;

public class MdActionInteractor {
	
	private static final String ERROR_SPLIT_TITLE = Messages.MdActionInteractor_ErrorTitle;
	private static final String CREATE_FILE_ERROR = Messages.MdActionInteractor_ErrorCreatingFileMsg;
	private static final String NO_PARAGRAPS_ERROR = Messages.MdActionInteractor_ErrorNoParagraphsMsg;
	private static final String ONLY_ONE_PARAGRAPH_ERROR = Messages.MdActionInteractor_ErrorOnlyOneParagraphMsg;
	private static final String DELETE_SOURCE_FILE_ERROR = Messages.MdActionInteractor_ErrorDeletingFileMsg;
	
	private static final String ERROR_JOIN_TITLE = Messages.MdActionInteractor_JoinErrorTitle;
	private static final String READ_MD_ERROR = Messages.MdActionInteractor_ErrorReadingFile;
	
	private KTableComposite fTableComposite;
	
	public MdActionInteractor(KTableComposite tableComposite) {
		fTableComposite = tableComposite;
	}
	
	private class MDSplitter {
		
		private IDipTableElement fSelectedElement;
		private IDipUnit fMdUnit;
		private List<String> fParagraphs;
		private Map<String, Integer> fSplitParagraphs;
		private ITableNode fParentNode = fTableComposite.selector().getLastSelectNode();
		private List<IDipTableElement> fNewElements; 
		
		private TmpElement fOriginalTmpElement;
			
		//=========================
		// split
		
		MdExtractResult split() throws CoreException, IOException{
			// читаем параграы
			readParagraphs();
			if (!checkParagraphs()) {
				return null;
			}
			// получаем отмеченные параграфы из диалога
			getSplitParagraphsBySplitDialog();
			if (fSplitParagraphs == null || fSplitParagraphs.isEmpty()) {
				return null;
			}			
			// для MdExtractResult
			fOriginalTmpElement = DipUtilities.tmpUnit(fMdUnit);		
			// создаем новый файлы
			fNewElements = createSplittedElements();		
			fTableComposite.editor().updater().updateParent(fParentNode.dipDocElement());
			fTableComposite.selector().setManyTableElements(fNewElements);				
			return createResult();
		}
		
		void getSplitParagraphsBySplitDialog() {
			MdSplitDialog dialog = new MdSplitDialog(getShell(),
					fParagraphs, fMdUnit);
			if (dialog.open() == Dialog.OK) {
				fSplitParagraphs  = dialog.getSelectedParagraphs();
			}
		}
	
		void readParagraphs() {
			fMdUnit = getSelectedMarkdownUnit();
			if (fMdUnit == null) {
				return;
			}
			fParagraphs = getParagraphsByUnit(fMdUnit);		
			fSelectedElement = fTableComposite.selector().getLastSelectObject();
			fParentNode = fTableComposite.selector().getLastSelectNode();
		}
		
		boolean checkParagraphs() {
			if (fParagraphs == null) {
				return false;
			}		
			if (fParagraphs.isEmpty()) {
				// добавить строки в extenal string
				MessageDialog.openInformation(getShell(), ERROR_SPLIT_TITLE, NO_PARAGRAPS_ERROR);
				return false;
			} else if (fParagraphs.size() == 1) {
				MessageDialog.openInformation(getShell(), ERROR_SPLIT_TITLE, ONLY_ONE_PARAGRAPH_ERROR);
				return false;
			}
			return true;
		}
		
		List<IDipTableElement> createSplittedElements() throws CoreException, IOException{
			List<IDipTableElement> newElements = new ArrayList<>();
			IContainer parentContainer = fMdUnit.resource().getParent();
			// текущий индех и элемет, после которого добавляем следующий файл
			int index = fMdUnit.getIndex();
			boolean needDelete = true;			
			IDipTableElement current = fTableComposite.selector().getLastSelectObject();
			// spli
			int start = 0;
			for (Entry<String, Integer> entry: fSplitParagraphs.entrySet()) {
				int length = entry.getValue();
				String content = getContent(start, length);
				start += length;
				String fileName = entry.getKey();
				if (fMdUnit.dipName().equals(fileName)) {					
					current = writeExistedFile(fMdUnit, content);
					index++;
					needDelete = false;
				} else {				
					IFile file = parentContainer.getFile(new Path(fileName));
					ResourcesUtilities.createFile(file, content, getShell());
					DipUnit newUnit = (DipUnit) fMdUnit.parent().createNewUnit(file, index++);
					if (needDelete) {
						current =  fParentNode.addNewUnit((DipUnit) newUnit, fSelectedElement, true);
					} else {
						current =  fParentNode.addNewUnit((DipUnit) newUnit, current, false);
					}
				}
				newElements.add(current);
			}		
			
			// delete source
			if (needDelete) {
				try {
					DipUtilities.deleteElement(fMdUnit, false, getShell());
					fSelectedElement.delete();
				} catch (DeleteDIPException e) {
					WorkbenchUtitlities.openError(ERROR_SPLIT_TITLE, DELETE_SOURCE_FILE_ERROR);
					e.printStackTrace();
				}
			}									
			return newElements;
		}
		
		private IDipTableElement writeExistedFile(IDipUnit sourceUnit, String content) throws IOException {
			FileUtilities.writeFile(sourceUnit.resource(), content);
			IDipTableElement result = fTableComposite.tableModel().findElement(sourceUnit);
			return result;
		}
		
		String getContent(int start, int length) {
			List<String> paragraphs = fParagraphs.subList(start, start + length);
			return String.join("\n\n", paragraphs);			
		}	
		
		MdExtractResult createResult() throws TmpCopyException {
			List<String> newNames = fNewElements.stream()
					.map(IDipTableElement::dipDocElement)
					.map(IDipDocumentElement::name)
					.collect(Collectors.toList());
			return new MdExtractResult(fOriginalTmpElement, newNames);
		}
	}
	
	public MdExtractResult split() throws CoreException, IOException {
		MDSplitter splitter = new MDSplitter();
		return splitter.split();		
	}
	
	private class MdExtracter {
		
		private IDipTableElement fSelectedElement;
		private IDipUnit fMdUnit;
		private List<String> fParagraphs;
		private Map<Integer, String> fExtractedParagraphs;
		private List<Integer> fNotSelectedParagraphs;		
		private ITableNode fParentNode = fTableComposite.selector().getLastSelectNode();
		private List<IDipTableElement> fNewElements; 
				
		//====================
		// extract
		
		MdExtractResult extract() throws TmpCopyException{
			// читаем параграы
			readParagraphs();
			if (!checkParagraphs()) {
				return null;
			}
			// получаем отмеченные параграфы из диалога
			getSelectedParagraphsByDialog();
			if (fExtractedParagraphs == null || fExtractedParagraphs.isEmpty()) {
				return null;
			}
			// создаем новый файлы
			try {
				fNewElements = createExtractedElements();
			} catch (CoreException e) {
				e.printStackTrace();
				WorkbenchUtitlities.openError(ERROR_SPLIT_TITLE, CREATE_FILE_ERROR);
				return null;
			}			
			MdExtractResult result = createResult(); 
					
			// обновляем или удаляем исходный файл
			udpateSourceUnit();
			// обновление таблицы, выделение элемента
			updateAndSelect();
			return result;
		}
		
		void readParagraphs() {
			fMdUnit = getSelectedMarkdownUnit();
			if (fMdUnit == null) {
				return;
			}
			fParagraphs = getParagraphsByUnit(fMdUnit);		
			fSelectedElement = fTableComposite.selector().getLastSelectObject();
			fParentNode = fTableComposite.selector().getLastSelectNode();
		}
		
		MdExtractResult createResult() throws TmpCopyException {
			TmpElement originalElement = DipUtilities.tmpUnit(fMdUnit);
			List<String> newNames = fNewElements.stream()
					.map(IDipTableElement::dipDocElement)
					.map(IDipDocumentElement::name)
					.collect(Collectors.toList());
			return new MdExtractResult(originalElement, newNames);
		}
		
		
		boolean checkParagraphs() {
			if (fParagraphs == null) {
				return false;
			}		
			if (fParagraphs.isEmpty()) {
				// добавить строки в extenal string
				MessageDialog.openInformation(getShell(), ERROR_SPLIT_TITLE, NO_PARAGRAPS_ERROR);
				return false;
			} else if (fParagraphs.size() == 1) {
				MessageDialog.openInformation(getShell(), ERROR_SPLIT_TITLE, ONLY_ONE_PARAGRAPH_ERROR);
				return false;
			}
			return true;
		}
		
		void getSelectedParagraphsByDialog() {
			MdExtractDialog dialog = new MdExtractDialog(getShell(),
					fParagraphs, fMdUnit);
			if (dialog.open() == Dialog.OK) {
				fExtractedParagraphs  = dialog.getSelectedParagraphs();
				fNotSelectedParagraphs = dialog.getNotSeletedParagraphs();
			}
		}
		
		List<IDipTableElement> createExtractedElements() throws CoreException {
			List<IDipTableElement> newElements = new ArrayList<>();
			IContainer parentContainer = fMdUnit.resource().getParent();
			// текущий индех и элемет, после которого добавляем следующий файл
			int index = fMdUnit.getIndex() + 1;
			IDipTableElement current = fTableComposite.selector().getLastSelectObject();			
			// create new units
			for (Entry<Integer, String> entry: fExtractedParagraphs.entrySet()) {
				IFile file = parentContainer.getFile(new Path(entry.getValue()));
				ResourcesUtilities.createFile(file, fParagraphs.get(entry.getKey()), getShell());
				DipUnit newUnit = (DipUnit) fMdUnit.parent().createNewUnit(file, index++);				
				current =  fParentNode.addNewUnit((DipUnit) newUnit, current, false);				
				newElements.add(current);
			}			
			return newElements;
		}
		
		void udpateSourceUnit() {						
			if (fNotSelectedParagraphs.isEmpty()) {
				deleteUnit();
			} else {
				String content = fNotSelectedParagraphs.stream().map(fParagraphs::get).collect(Collectors.joining("\n\n")); //$NON-NLS-1$
				try {
					FileUtilities.writeFile(fMdUnit.resource(), content);										
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		void deleteUnit() {				
			try {
				DipUtilities.deleteElement(fMdUnit, false, getShell());
				fSelectedElement.delete();			
			} catch (DeleteDIPException e) {
				WorkbenchUtitlities.openError(ERROR_SPLIT_TITLE, DELETE_SOURCE_FILE_ERROR);
				e.printStackTrace();
			} catch (TmpCopyException e) {
				e.printStackTrace();
			}
		}
				
		void updateAndSelect() {
			fTableComposite.editor().updater().updateParent(fParentNode.dipDocElement());		 
			if (fNotSelectedParagraphs.isEmpty()) {
				fTableComposite.selector().setTableElementSelection(fNewElements.get(fNewElements.size() - 1));
			} else {
				fTableComposite.selector().setTableElementSelection(fSelectedElement);
			}			
		}
		
	}
	
	public MdExtractResult extract() throws TmpCopyException {		
		MdExtracter splitter = new MdExtracter();
		return splitter.extract();	
	}
	
	private class MdJoiner {
		
		private TreeSet<IDipDocumentElement> fSources;
		private IDipParent fParent;
		private IDipTableElement fFirstElement;
		private ITableNode fParentNode;
		private int fFirstIndex; // индекс первого выбранного элемента
		
		
		MdJoiner(){
			fSources = fTableComposite.selector().getSelectedElements();		
			fParent = fSources.first().parent();		
			fFirstElement = fTableComposite.tableModel().findElement(fSources.first());
			fParentNode = fFirstElement.parent();
		}
		
		public MDJoinResult join() throws TmpCopyException, DeleteDIPException {
			String	content = readJoinedContent(fSources);
			if (content == null) {
				return null;
			}
					
			MdJoinDialog dialog = new MdJoinDialog(getShell(), fSources, fParent.resource());
			if (dialog.open() == Dialog.OK) {
				fFirstIndex = fSources.first().getIndex();
				List<TmpElement> tmpElements = deleteSources();		

				IDipTableElement result = writeResultFile(dialog, content);
				fTableComposite.editor().updater().updateParent(fParentNode.dipDocElement());
				fTableComposite.selector().setTableElementSelection(result);
				
				return new MDJoinResult(result.dipResourceElement(), tmpElements);
			}
			return null;
		}
		
		private String readJoinedContent(TreeSet<IDipDocumentElement> sources) {
			StringBuilder builder = new StringBuilder();
			for (IDipDocumentElement unit: sources) {
				IFile file = (IFile) unit.resource();
				if (builder.length() > 0) {
					builder.append("\n\n"); //$NON-NLS-1$
				}
				try {
					builder.append(FileUtilities.readFile(file));
				} catch (IOException e) {
					MessageDialog.openInformation(getShell(), ERROR_JOIN_TITLE, READ_MD_ERROR);
					e.printStackTrace();
					return null;
				}
			}
			return builder.toString();
		}
		
		private IDipTableElement writeResultFile(MdJoinDialog dialog, String content) {
			try {
				if (dialog.getSeletedSource() != null) {
					DipUnit sourceUnit = dialog.getSeletedSource();
					return writeExistedFile(sourceUnit, content);
				} else {
					String newName = dialog.getName();
					return wrtieToNewFile(newName, content);
				}
			} catch (IOException | CoreException e) {
				WorkbenchUtitlities.openError(ERROR_JOIN_TITLE, CREATE_FILE_ERROR);
				e.printStackTrace();
				return null;
			}
		}

		private IDipTableElement writeExistedFile(DipUnit sourceUnit, String content) throws IOException {
			FileUtilities.writeFile(sourceUnit.resource(), content);
			IDipTableElement result = fTableComposite.tableModel().findElement(sourceUnit);
			fSources.remove(sourceUnit);
			return result;
		}

		private IDipTableElement wrtieToNewFile(String name, String content) throws CoreException {
			IFile file = fParent.resource().getFile(new Path(name));
			ResourcesUtilities.createFile(file, content, getShell());
			DipUnit newUnit = (DipUnit) fParent.createNewUnit(file, fFirstIndex);
			return fFirstElement.parent().addNewUnit((DipUnit) newUnit, fFirstElement, true);
		}
		
		private List<TmpElement> deleteSources() throws TmpCopyException, DeleteDIPException {
			List<TmpElement> tmpElements = new ArrayList<>();			
			for (IDipDocumentElement unit: fSources) {
				IDipTableElement element = fTableComposite.tableModel().findElement(unit);
				TmpElement tmpElement = DipUtilities.deleteElement(unit, false, getShell());
				element.delete();	
				tmpElements.add(tmpElement);
			}
			return tmpElements;
		}
	}
	
	public MDJoinResult join() throws TmpCopyException, DeleteDIPException {	
		MdJoiner joiner = new MdJoiner();
		return joiner.join();
	}
	
	private IDipUnit getSelectedMarkdownUnit(){
		IDipTableElement selectedElement = fTableComposite.selector().getLastSelectObject();
		if (selectedElement == null) {
			return null;
		}
		IDipDocumentElement dipDocElement = selectedElement.dipDocElement();
		if (dipDocElement instanceof UnitPresentation) {
			UnitPresentation unitPresentation = (UnitPresentation) dipDocElement;
			TablePresentation tablePresentation = unitPresentation.getPresentation();
			if (tablePresentation instanceof MarkDownPresentation) {
				return unitPresentation.getDipUnit();
			}
		}
		return null;
	}
	
	private List<String> getParagraphsByUnit(IDipUnit unit){
		try {
			IFile file = unit.resource();
			List<String> lines = FileUtilities.readLines(file);
			return getParagraphs(lines);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private  List<String> getParagraphs(List<String> lines){		
		List<String> result = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		for (String str: lines) {
			if (str.isBlank()) {
				if (builder.length() > 0)	{
					result.add(builder.toString());
					builder.setLength(0);
				}
			} else {
				if (builder.length() > 0) {
					builder.append("\n"); //$NON-NLS-1$
				}
				builder.append(str);				
			}
		}
		if (!builder.toString().isBlank()) {
			result.add(builder.toString());
		}
		return result;
	}
	
	//======================
	// getters & setters
	
	private Shell getShell() {
		return fTableComposite.getShell();
	}
	
}
