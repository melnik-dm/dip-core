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
package ru.dip.core.manager;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import ru.dip.core.model.DipComment;
import ru.dip.core.model.DipDescription;
import ru.dip.core.model.DipElementType;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.interfaces.IDipComment;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDisable;
import ru.dip.core.table.TableEntry;
import ru.dip.core.table.TableReader;
import ru.dip.core.utilities.ResourcesUtilities;

/**
 * Класс создает список DipDocumentElement (те элементы, которые отображаются в таблицеы)
 */
public class DipTableContainerComputer {

	/**
	 * Модель основанная только на ресурсах (без учета данных из файла .dnfof)
	 */
	private static class DipFolderResourceModel {

		private final List<DipUnit> fileList = new ArrayList<>();
		private final List<DipFolder> folderList = new ArrayList<>();
		private final List<DipComment> commentList = new ArrayList<>();
		private final List<DipDescription> descriptionList = new ArrayList<>();
		private IDipComment fDipComment;
	}

	private final DipTableContainer fDipContainer;
	private DipFolderResourceModel fResourceModel;
	private TableReader modelReader;
	private List<IDipDocumentElement> fResultDipElements = new ArrayList<IDipDocumentElement>();

	public DipTableContainerComputer(DipTableContainer dipContainer) {
		fDipContainer = dipContainer;
	}

	public List<IDipDocumentElement> computeDipChildren(boolean withBrokenFolders) {
		fResourceModel = computeResourceModel(fDipContainer);

		if (fResourceModel.fDipComment != null) {
			fDipContainer.setDipComment(fResourceModel.fDipComment);
		}

		readDnfo();
		setProperties();

		addDnfoFilesToModel();
		addRemainingFilesToModel();

		addDnfoFoldersToModel();
		computeRemainingFolders(withBrokenFolders);

		addComments();
		addDescriptions();

		return fResultDipElements;
	}

	private DipFolderResourceModel computeResourceModel(DipTableContainer container) {
		DipFolderResourceModel resourceModel = new DipFolderResourceModel();
		for (IDipElement element : container.getChildren()) {
			DipElementType type = element.type();
			if (type == DipElementType.UNIT) {
				resourceModel.fileList.add((DipUnit) element);
			} else if (type == DipElementType.FOLDER) {
				resourceModel.folderList.add((DipFolder) element);
			} else if (type == DipElementType.INCLUDE_FOLDER) {
				resourceModel.folderList.add((IncludeFolder) element);
			} else if (type == DipElementType.COMMENT) {
				resourceModel.commentList.add((DipComment) element);
			} else if (type == DipElementType.DESCRIPTION) {
				resourceModel.descriptionList.add((DipDescription) element);
			} else if (type == DipElementType.FOLDER_COMMENT) {
				resourceModel.fDipComment = ((IDipComment) element);
			}
		}
		return resourceModel;
	}

	private void readDnfo() {
		modelReader = new TableReader();
		modelReader.readModel(fDipContainer.getTable().resource());
	}

	private void setProperties() {
		fDipContainer.setDescription(modelReader.getDescription());

		String fileStepNumeration = modelReader.getFileStepNumeration();
		if (fileStepNumeration != null && !fileStepNumeration.isEmpty()) {
			fDipContainer.setFileStep(fileStepNumeration);
		}
		String folderStepNumeration = modelReader.getFolderStepNumeration();
		if (folderStepNumeration != null && !folderStepNumeration.isEmpty()) {
			fDipContainer.setFolderStep(folderStepNumeration);
		}

		fDipContainer.setPageBreak(modelReader.getPageBreak());
	}

	/**
	 * Добавление файлов, которые указаны в DNFO
	 */
	private void addDnfoFilesToModel() {
		List<TableEntry> filesEntries = modelReader.getFiles();
		for (TableEntry entry : filesEntries) {
			String resName = entry.getName();
			DipUnit child = findElement(fResourceModel.fileList, resName);
			if (child != null) {
				child.setDescription(entry.getDescription());
				boolean disable = entry.isDisable() || child.name().startsWith(IDisable.DISABLE_MARKER);
				child.setDisabled(disable);
				child.setHorizontalOrientation(entry.isHorizontal());
				fResourceModel.fileList.remove(child);
				fResultDipElements.add(child);
			}
		}
	}

	/**
	 * Добавление файлов, которых нет в DNFO
	 */
	private void addRemainingFilesToModel() {
		for (DipUnit child : fResourceModel.fileList) {
			fResultDipElements.add(child);
		}
	}

	/**
	 * Добавление папок, которые указаны в DNFO
	 */
	private void addDnfoFoldersToModel() {
		List<TableEntry> folderEntries = modelReader.getFolders();
		for (TableEntry entry : folderEntries) {
			DipFolder child = prepareDipFolder(entry);

			if (child != null) {
				child.setActiveNumeration(entry.isShowNumeration());
				boolean disable = entry.isDisable() || child.dipName().startsWith(IDisable.DISABLE_MARKER);
				child.setDisabled(disable);
				fResourceModel.folderList.remove(child);
				fResultDipElements.add(child);
			}
		}
	}

	private DipFolder prepareDipFolder(TableEntry entry) {
		DipFolder child = null;
		if (entry.getLink() != null && !entry.getLink().isEmpty()) {
			child = prepareIncludeFolder(entry);
		} else {
			String resName = entry.getName();
			child = findElement(fResourceModel.folderList, resName);
		}
		return child;
	}

	private DipFolder prepareIncludeFolder(TableEntry entry) {
		// find Include folder in ResourceModel
		String resName = entry.getName();
		if (resName == null || resName.isEmpty()) {
			resName = Paths.get(entry.getLink()).getFileName().toString();
		}
		DipFolder child = findElement(fResourceModel.folderList, resName);
		// проверка соответсвия ссылок
		checkLinks(entry, child);
		// если нет ссылки в ProjectExploter
		if (child == null) {
			child = createIncludeFolderLink(entry);
		}
		// свойства
		child.setReadOnly(entry.isReadOnly());
		if (child instanceof IncludeFolder) {
			((IncludeFolder) child).setLinkName(entry.getName());
			((IncludeFolder) child).setLinkDescription(entry.getDescription());
		}
		return child;
	}

	/**
	 * Проверка соответсвия ссылок в ProjectExplorer и dnfo Если отличается, меняет
	 * ссылку
	 */
	private void checkLinks(TableEntry entry, DipFolder child) {
		if (child != null) {
			java.nio.file.Path pathFromDfno = java.nio.file.Path
					.of(fDipContainer.resource().getLocation().toOSString(), entry.getLink()).normalize();

			java.nio.file.Path pathFromLinkResource = java.nio.file.Path.of(child.resource().getLocation().toOSString())
					.normalize();
			if (!pathFromDfno.equals(pathFromLinkResource)) {
				try {
					ResourcesUtilities.deleteResource(child.resource(), null);
					child.resource().createLink(new Path(pathFromDfno.toString()), IResource.DEPTH_ONE, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Создает IncludeFolder Используется при отсутсвии ссылки в ProjectExplorer
	 */
	private DipFolder createIncludeFolderLink(TableEntry entry) {
		java.nio.file.Path thisPath = Paths.get(fDipContainer.resource().getLocationURI());
		java.nio.file.Path linksPath = Paths.get(entry.getLink());
		java.nio.file.Path resolve = thisPath.resolve(linksPath);
		java.nio.file.Path relative = resolve.normalize();
		IFolder folder = null;
		try {
			folder = IncludeFolder.createLinkFolder(entry.getName(), relative.toString(), fDipContainer.resource());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if (folder == null) {
			return IncludeFolder.createBrokenLinkFolder(relative.toString(), fDipContainer);
		} else {
			return IncludeFolder.instance(folder, fDipContainer);
		}
	}

	/**
	 * Обработка папок, которых нет в dfno Либо добавляем при withBrokenFolders =
	 * true Либо удаляем из childre
	 */
	private void computeRemainingFolders(boolean withBrokenFolders) {
		if (withBrokenFolders) {
			addBrokenFolders();
		} else {
			removeBrokenFolders();
		}
	}

	private void addBrokenFolders() {
		fResultDipElements.addAll(fResourceModel.folderList);
	}

	private void removeBrokenFolders() {
		fDipContainer.getChildren().removeAll(fResourceModel.folderList);
	}

	private void addComments() {
		// add comments
		for (DipComment comment : fResourceModel.commentList) {
			comment.setCorrespondingElement();
		}
		// add description
	}

	private void addDescriptions() {
		for (DipDescription description : fResourceModel.descriptionList) {
			description.setCorrespondingElement();
		}
	}

	private static <T extends IDipElement> T findElement(List<T> elements, String name) {
		for (T element : elements) {
			if (element.name().equals(name)) {
				return element;
			}
		}
		return null;
	}

}
