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
package ru.dip.ui.table.editor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import ru.dip.core.exception.NotFoundTableNodeException;
import ru.dip.core.link.LinkInteractor;
import ru.dip.core.model.DipElementType;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.table.TableWriter;
import ru.dip.core.unit.form.FormPresentation;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.table.ktable.KTableComposite;
import ru.dip.ui.table.ktable.model.DipTableModel;
import ru.dip.ui.table.ktable.model.IDipTableElement;
import ru.dip.ui.table.ktable.model.ITableNode;
import ru.dip.ui.table.ktable.model.TableNode;
import ru.dip.ui.table.table.TableModel;

public class DipEditorUpdater {
	
	private DipTableEditor fEditor;
	
	public DipEditorUpdater(DipTableEditor editor) {
		fEditor = editor;
	}
	
	/**
	 * Обновление после переименования файла (папки)
	 * Переименовываем только идентификаторы
	 */
	public void updateAfterRename(IDipParent objectParent, String objName, boolean select) {
		// additional update (difs)
		additionalModelUpdate(objectParent);		
		IDipDocumentElement renamedElement = (IDipDocumentElement) objectParent.getChild(objName);						
		tableModel().updateIdentificators(getElements(List.of(renamedElement)));		
	}
	
	/**
	 *  Обновление, если изменился только порядок элементов в папке
	 */
	public void updateFolderOrder(ITableNode parent) {
		saveParent(parent.dipDocElement());
		updateNumeration();
		updatePresentationForAllDescriptionsInNode(parent);
		additionalModelUpdate(parent.dipDocElement());
		computeTableTreeContent();	
	}
	
	/**
	 * Обновление файла (при изменении в редакторе)
	 */
	public void updateFilePresentation(IFile file) {
		IDipElement dipElement = DipUtilities.findElement(file);
		if (dipElement instanceof IDipDocumentElement) {
			IDipTableElement element = tableModel().findElement((IDipDocumentElement) dipElement);
			if (element != null) {
				if (element.isFormField()) {
					IDipUnit unit = (IDipUnit) dipElement;
					((FormPresentation)unit.getUnitPresentation().getPresentation()).updateFieldsFromFile();
					tableModel().updateElements(element.getLinkedElements());
				} else {
					tableModel().updateElements(List.of(element));
				}
			}
		}
	}
	
	/**
	 * Обновить объект (файл или папка) внутри папки
	 * Используется при создании нового файла или папки
	 * 
	 * @param objectParent - папка внутрь которой добавляется новый объект
	 * @param objName - имя добавленного объекта (файл, папка)
	 * @throws NotFoundTableNodeException 
	 * 
	 * В данной реализации обновляет всю папку:
	 * updateChildren(nodes) - обновляет список файлов в папке
	 * далее обновляются все компоненты, можно переписать, чтобы добавлялся только новый объект
	 * 
	 */
	public void updateNewObject(IDipParent objectParent, String objName, boolean select) throws NotFoundTableNodeException  {
		saveParent(objectParent);
		// additional update (difs)
		additionalModelUpdate(objectParent);
		updateNumeration();
		// find node model
		Optional<TableNode> nodeOpt = tableModel().findNodeByName(objectParent);
		if (nodeOpt.isEmpty()) {
			// в Section не найдет узел если ищем родителя для каталога верхнего уровня
			if (fEditor.model().isTable(objectParent) || fEditor.model().isParentHeader(objectParent)) {
				nodeOpt = Optional.of(tableModel().getRoot());
			} else {
				throw new NotFoundTableNodeException("Не найден родительский узел " + objectParent.name());
			}
		}
		// update node
		updateChildren(nodeOpt.get());
		computeTableTreeContent();		
		//updateNodePresentationWithChildren(nodes);
		updateNodePresentationWithChildren(nodeOpt.get());		
		// select element		
		if (select) {
			IDipTableElement element = nodeOpt.get().find(objName);
			if (element != null) {
				table().selectElement(element);
			}
		}		
	}
	
	public void updatePresentationForNewObject(IDipParent objectParent) throws NotFoundTableNodeException  {		
		// additional update (difs)
		additionalModelUpdate(objectParent);
		// find node model
		Optional<TableNode> nodeOpt = tableModel().findNodeByName(objectParent);
		if (nodeOpt.isEmpty()) {
			// в Section не найдет узел если ищем родителя для каталога верхнего уровня
			if (fEditor.model().isTable(objectParent) || fEditor.model().isParentHeader(objectParent)) {
				nodeOpt = Optional.of(tableModel().getRoot());
			} else {
				throw new NotFoundTableNodeException("Не найден родительский узел " + objectParent.name());
			}
		}
		// update node
		updateChildren(nodeOpt.get());
		computeTableTreeContent();	
		// update presentation
		updateNodePresentationWithChildren(nodeOpt.get());		
	}
	
	public void updateAfterDelete(IDipDocumentElement[] dipDocElements) {		
		// dip-model level
		saveParents(getDipParents(dipDocElements));				
		additionalModelUpdate(dipDocElements);	
		updateNumeration();				
		// dip-table model
		List<TableNode> nodes = getNodes(dipDocElements);
		updateChildren(nodes);
		computeTableTreeContent();
		// presentaion
		updateNodeWithCursorBusy(nodes);
		// other
		updateLinks();
		// clean selection
		fEditor.kTable().deselect();
	}
	
	public void updateNewObjects(List<IDipDocumentElement> objects) {
		// dipModel
		saveParents(getDipParents(objects.stream().toArray(IDipDocumentElement[]::new)));
		additionalModelUpdate(objects);
		updateNumeration();
		// dip-table model
		List<TableNode> nodes =getNodes(objects);
		updateChildren(nodes);
		computeTableTreeContent();
		// presentaion
		updateNodeWithCursorBusy(nodes);
		// other
		WorkbenchUtitlities.updateProjectExplorer();
		updateLinks();
	}
	
	/**
	 * Обновляем родительский элемент, например посkе извлечения (delete_extract)
	 */
	public void updateParent(IDipParent fTargetParent) {
		saveParent(fTargetParent);
		additionalModelUpdate(fTargetParent);
		updateNumeration();
		// dip-table model
		List<TableNode> nodes =  List.of(tableModel().findNode(fTargetParent));
		updateChildren(nodes);
		computeTableTreeContent();
		// presentaion
		updateNodeWithCursorBusy(nodes);
		// other
		WorkbenchUtitlities.updateProjectExplorer();
		updateLinks();
	}
	
	/**
	 * Обновить папку и ее родительску папку 
	 * Использование: отмена удаления с извлечением (delete_extract)
	 */
	public void updateFolderWithParent(IDipParent dipParent) {
		IDipParent parent = dipParent.parent();
		saveEditor();
		additionalModelUpdate(parent);
		updateNumeration();
		// dip-table model
		List<TableNode> nodes =  getNodes(parent, dipParent);
		updateChildren(nodes);
		computeTableTreeContent();
		// presentaion
		updateNodeWithCursorBusy(nodes);
		// other
		WorkbenchUtitlities.updateProjectExplorer();
		updateLinks();
	}
	
	/**
	 * Простое обновление одного элемента, при изменении-удалении Description или Комментария
	 */
	public void updateUnitElement(IDipTableElement endElement, boolean select) {
		//saveEditor();					
		saveParent(endElement.dipDocElement().parent());
		computeTableTreeContent();
		
		updateOneElement(endElement);
		additionalModelUpdate(endElement.dipDocElement());
		
		if (select) {
			table().selector().setSelection(endElement.dipResourceElement());
		}
	}

	/**
	 * Обновить две папки (при пермещении)
	 */
	public void updateTwoFolder(ITableNode node1, ITableNode node2) {
		//saveEditor();
		saveParents(List.of(node1.dipDocElement(), node2.dipDocElement()));
		
		updateNumeration();		
		additionalModelUpdate(node1.dipDocElement(), node2.dipDocElement());
		computeTableTreeContent();
		
		updatePresentationForAllDescriptions();
		updatePresentationForAllNodes();
		updateLinks();
	}
	
	/**
	 * Сохранить изменения, и дополнительные обновления (diff)
	 * Когда например обновилось dnfo (pagebreak, настройки, orientation)
	 * Т.е. таблица внешне остается без изменений, но есть изменения в dnfo 
	 */
	public void saveWithAdditionalUpdate(IDipParent parent) {
		saveEditor();
		additionalModelUpdate(parent);
		Optional<TableNode> optNode = tableModel().findNodeByName(parent);
		if (optNode.isPresent()) {
			tableModel().updateElements(List.of(optNode.get()));
		}	
	}
	
	/**
	 * Проверить additional для parent
	 * Обновить отсальные nodes
	 * 
	 * Используется при включени/отключении нумерации для папки
	 */
	public void updateNodes(IDipParent parent) {
		saveEditor();
		updateNumeration();		
		additionalModelUpdate(parent.parent());
		computeTableTreeContent();
		updatePresentationForAllNodes();		
	}

	/**
	 *  После включения/выключения автонумерации
	 */
	public void updateAfterEnableAutoNumbering(IDipParent parent) {		
		saveEditor();
		// если выбрана табличная директория или одна из родительских - надо сохранять на уровень выше
		if ((model().isTable(parent) || model().isChild(parent))
				&& parent.type() != DipElementType.RPOJECT) {
			try {
				TableWriter.saveModel(parent.parent());
			} catch (ParserConfigurationException | IOException e) {
				e.printStackTrace();
			}
		}	
		additionalModelUpdate(parent);
	}
	
	private void updateNodeWithCursorBusy(Collection<TableNode> nodes) {
		if (nodes.isEmpty()) {
			return;
		}
		if (nodes.size() < 3) {
			updateNodePresentationWithChildren(nodes);
		}
		
		IWorkbench wb = PlatformUI.getWorkbench();
		IProgressService ps = wb.getProgressService();
		try {
			ps.busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor pm) {
					Display.getDefault().asyncExec(() -> {
						updateColors();
						updatePresentationForAllDescriptions();
						updatePresentationForAllNodes();
						
						nodes.forEach(node -> { 
								tableModel().updateElements(List.of(node));
								tableModel().updateElementsWithChild(node.children());
						});
						table().refreshTable();
					});
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void updateNodePresentationWithChildren(TableNode node) {
		updateColors();
		updatePresentationForAllDescriptions();
		updatePresentationForAllNodes();
		
		tableModel().updateElements(List.of(node));
		tableModel().updateElementsWithChild(node.children());		
	}
	
	private void updateNodePresentationWithChildren(Collection<TableNode> nodes) {
		updateColors();
		updatePresentationForAllDescriptions();
		updatePresentationForAllNodes();
		
		nodes.forEach(node -> { 
			tableModel().updateElements(List.of(node));
			tableModel().updateElementsWithChild(node.children());
		});
	}
	
	
	private void updateOneElement(IDipTableElement element) {	
		Display.getDefault().asyncExec(() -> {
			for (IDipTableElement el: element.allLinkedElements()) {
				tableModel().updateElements(List.of(el));				
			}
		});
		
	}
	
	
	/**
	 *  Обновление текстовый элементов (например для комманды Highlight Glossary)
	 */
	public void updateTextElements() {
		List<IDipTableElement> elements = tableModel().getElements().stream()
				.filter(IDipTableElement::isTextFile).collect(Collectors.toList());
		tableModel().updateElements(elements);
		table().refreshTable();
	}
	
	/**
	 *  Обновление форм (например при изменении настроек для отображении формы
	 *  
	 *  computeElements - если нужно обновить список элементов,
	 *  если например какие-то fields перестали отображаться
	 */
	public void updateFormElements(boolean computeElements) {
		if (computeElements) {
			tableModel().getRoot().computeChildren();
			tableModel().computeElements();
			updateColors();	
		}
		
		List<IDipTableElement> elements = tableModel().getElements().stream()
				.filter(IDipTableElement::isForm).collect(Collectors.toList());
		tableModel().updateElements(elements);
		table().refreshTable();
	}
	
	/**
	 * Обновить идентификаторы
	 */
	public void updateIdentificators() {
		tableModel().updateIdentificators(tableModel().getElements());	
	}
	
	//======================
	// уровень ReqModel / TableModel 
	
	private void saveEditor() {
		fEditor.doSave(null);
	}
	
	private void saveParent(IDipParent dipParent) {
		try {
			TableWriter.saveModel(dipParent);
		} catch (ParserConfigurationException | IOException e) {
			e.printStackTrace();
		}

		Job job = Job.create("", new ICoreRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						ResourcesUtilities.updateContainer(dipParent.resource());
						WorkbenchUtitlities.updateProjectExplorer();
					}
				});

			}
		});
		job.schedule();
		fEditor.updateListeners();
	}
	
	private void saveParents(Collection<IDipParent> dipParents) {
		Job job = Job.create("", new ICoreRunnable() {

			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				
				dipParents.forEach(t -> {
					try {
						TableWriter.saveModel(t);
					} catch (ParserConfigurationException | IOException e) {
						e.printStackTrace();
					}
				});
				
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						dipParents.stream()
							.map(IDipParent::resource)
							.forEach(ResourcesUtilities::updateContainer);
						WorkbenchUtitlities.updateProjectExplorer();
					}
				});

			}
		});
		job.schedule();
		fEditor.updateListeners();
	}
	
	
	
	/*
	 *  Допонительные обновления (diff) для папки или файла 
	 */
	public void additionalModelUpdate(IDipDocumentElement dipDocElement) {
		model().additionalUpdate(dipDocElement);
	}
	
	/*
	 * @Children - могут быть дочерние элементы в разных папках,
	 * получаем родителей и их обновляем
	 */
	private void additionalModelUpdate(IDipDocumentElement... children) {
		Set<IResource> parentPaths = getParentResource(children);
		model().additionalUpdate(parentPaths);
	}
	
	private void additionalModelUpdate(List<IDipDocumentElement> children) {
		Set<IResource> parentPaths = getParentResource(children);
		model().additionalUpdate(parentPaths);
	}
	
	private void updateNumeration() {
		table().dipProject().updateNumeration();	
	}
	
	//======================
	// уровень DipTableModel, TableElement, TableNode
	
	private void updateColors() {
		table().updateBackgrouColor();
	}
	
	private void updateChildren(TableNode node) {
		node.computeChildren();
	}
	
	private void updateChildren(List<TableNode> nodes) {
		nodes.forEach(TableNode::computeChildren);
	}
	
	/*
	 * Получает набор элементов для всей модели
	 */
	private void computeTableTreeContent() {
		Display.getDefault().syncExec(() -> {
			tableModel().computeElements();
		});
	}
	
	private void updatePresentationForAllDescriptions() {
		tableModel().updateDescriptions();
	}
	
	private void updatePresentationForAllDescriptionsInNode(ITableNode node) {
		tableModel().updateDescriptionsInNode(node);
	}
	
	private void updatePresentationForAllNodes() {
		tableModel().updateNodes();
	}
	
	/**
	 * Выполняет Prepare для переданных элементов
	 */
	public void updateElements(List<IDipDocumentElement> dipDocElements) {
		tableModel().updateElements(getElements(dipDocElements));
	}

	//======================
	// дополнительные обновления
	
	private void updateLinks() {
		LinkInteractor.instance().checkLinksAfterDelete(fEditor.getDipProject());
	}
	
	//=======================
	// getters & setters

	private Set<IDipParent> getDipParents(IDipDocumentElement[] dipDocElements) {
		return Stream.of(dipDocElements)
				.map(dipDocElement -> dipDocElement.parent())
				.collect(Collectors.toSet());
	}

	private Set<IResource> getParentResource(IDipDocumentElement[] dipDocElements) {
		return getParentResource(Stream.of(dipDocElements));
	}

	private Set<IResource> getParentResource(List<IDipDocumentElement> dipDocElements) {
		return getParentResource(dipDocElements.stream());
	}

	private Set<IResource> getParentResource(Stream<IDipDocumentElement> dipDocElementsStream) {
		return dipDocElementsStream
				.map(dipDocElement -> dipDocElement.parent().resource())
				.distinct()
				.collect(Collectors.toSet());
	}

	private List<TableNode> getNodes(IDipDocumentElement... children) {
		return getNodes(Stream.of(children));
	}

	private List<TableNode> getNodes(Collection<IDipDocumentElement> children) {
		return getNodes(children.stream());
	}

	private List<TableNode> getNodes(Stream<IDipDocumentElement> childrenStream) {
		return childrenStream
				.map(IDipDocumentElement::parent)
				.distinct()
				.map(tableModel()::findNodeByName)
				.map(Optional::get).collect(Collectors.toList());
	}

	private List<IDipTableElement> getElements(List<IDipDocumentElement> dipDocElements) {
		return dipDocElements.stream()
				.map(tableModel()::findElementByName)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	private TableModel model() {
		return fEditor.model();
	}

	private KTableComposite table() {
		return fEditor.kTable();
	}

	private DipTableModel tableModel() {
		return table().tableModel();
	}

	//=================================
	// статические методы (вызов при операциях из ProjectExploter
	
	// обновить в редакторах
	public static void updateNewRes(IResource res) {
		IDipElement element = DipUtilities.findElement(res);
		if (element instanceof IDipDocumentElement) {
			updateNewRes((IDipDocumentElement) element);
		}
	}
	
	private static void updateNewRes(IDipDocumentElement element) {
		IDipDocumentElement dde = (IDipDocumentElement) element;
		DipEditorRegister.instance.findEditors(dde).forEach(editor -> {
			try {
				editor.updater().updatePresentationForNewObject(dde.parent());
			} catch (NotFoundTableNodeException e) {
				e.printStackTrace();
			}
		});
	}

	public static void updateNewResInFolder(IDipParent dipParent) {
		DipEditorRegister.instance.findEditors(dipParent).forEach(editor -> {
			try {
				editor.updater().updatePresentationForNewObject(dipParent);
			} catch (NotFoundTableNodeException e) {
				e.printStackTrace();
			}
		});
	}

}
