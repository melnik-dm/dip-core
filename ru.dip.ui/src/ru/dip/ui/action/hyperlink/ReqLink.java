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
package ru.dip.ui.action.hyperlink;

import static ru.dip.core.utilities.DnfoUtils.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ru.dip.core.link.LinkInteractor;
import ru.dip.core.manager.DipNatureManager;
import ru.dip.core.model.IncludeFolder;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipRoot;
import ru.dip.core.model.DnfoTable;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.WorkbenchUtitlities;
import ru.dip.ui.table.editor.DipTableEditor;
import ru.dip.ui.table.editor.TableEditorInput;
import ru.dip.ui.table.table.TableSettings;

public class ReqLink implements IHyperlink {

	private IRegion fRegion;
	private String fLink;
	private IFile fFile;
	
	public ReqLink(IRegion region, String link, IFile file) {
		fRegion = region;
		fLink = link;
		fFile = file;
	}
	
	@Override
	public IRegion getHyperlinkRegion() {
		return fRegion;
	}

	@Override
	public String getTypeLabel() {
		return "null";
	}

	@Override
	public String getHyperlinkText() {
		return "null";
	}

	@Override
	public void open() {
		IDipElement element = LinkInteractor.findElement(fFile, fLink);
		if (element == null) {
			return;
		}
		if (element instanceof DipFolder) {
			element = ((DipTableContainer) element).getTable();
		}
		if (element instanceof DipUnit && element.isReadOnly()) {
			WorkbenchUtitlities.openReadOnlyErrorMessage((IDipDocumentElement) element);
		} else {
			openFile((IFile)element.resource());
		}
	}
	
	public static String changeHtmlLink(String str, IFile file) {
		String regex = "href=\"[^\"]+\"";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		ArrayList<Point> regions = new ArrayList<>();
		while (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			regions.add(new Point(start, end));
		}
		StringBuilder builder = new StringBuilder();
		int offset = 0;
		for (Point p: regions){
			builder.append(str.substring(offset, p.x));		
			String oldLink = str.substring(p.x + 6, p.y - 1);					
			String newLink = updateLink(oldLink, file.getProject());
			builder.append("href=\"");
			builder.append(newLink);
			builder.append("\"");
			offset = p.y;
		}
		builder.append(str.substring(offset));	
		return builder.toString();
	}
	
	private static String updateLink(String oldLink, IProject project){
		Path projectPath = Paths.get(project.getLocationURI());
		Path oldPath = Paths.get(oldLink);
		if (oldPath.isAbsolute()){
			return oldLink;
		}
		Path resolvePath = projectPath.resolve(oldPath);
		if (Files.exists(resolvePath)){
			if (Files.isDirectory(resolvePath)){
				return resolvePath.resolve(DNFO_FILENAME).toString();
			}			
			return resolvePath.toString();
		}
		
		// ссылки на include-объекты
		String first = oldPath.getName(0).toString();
		DipProject dipProject = DipRoot.getInstance().findDipProject(project);
		if (dipProject != null) {
			IncludeFolder folder = dipProject.getIncludeFolder(first);
			if (folder != null) {
			Path folderPath = Paths.get(folder.resource().getLocationURI()).getParent();
			Path folderResolvePath = folderPath.resolve(oldPath);
			if (Files.exists(folderResolvePath)){
				if (Files.isDirectory(folderResolvePath)){
					return folderResolvePath.resolve(DNFO_FILENAME).toString();
				}			
				return folderResolvePath.toString();
			}
			}
		}		
		return oldLink;
	}
	
	public static void openFile(String location){		
		IFile file = ResourcesUtilities.findFile(location);
		openFile(file);	
	}
	
	public static void openFile(IFile file) {
		if (file != null && file.exists()){
			if (file.getName().equals(DNFO_FILENAME)){
				openTable(file);
			} else {			
				WorkbenchUtitlities.openFile(file);
			}
		}
	}
	
	private static void openTable(IFile file){
		IProject project = file.getProject();
		if (!DipNatureManager.hasNature(project)){
			return;
		}
		DipProject dipProject = DipRoot.getInstance().getDipProject(project);
		IDipElement element = DipUtilities.findDipElementInProject(file.getParent(), dipProject);
		if (element instanceof DipTableContainer){
			openLinkTable((DipTableContainer) element);
		}	
	}
	
	public static void openLinkTable(DipTableContainer tableContainer) {
		if (TableSettings.isOpenLinkFolderSection()){							
			DnfoTable table = tableContainer.getTable();
			if (table != null){
				openTable(table);
			}
		} else {
			openElementInTable(tableContainer);
		}
	}
	
	public static IEditorPart openTable(DnfoTable table){
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorInput editorInput = new TableEditorInput(table);
		try {
			IEditorPart part = page.openEditor(editorInput, DipTableEditor.EDITOR_ID);
			part.setFocus();
			return part;
		} catch (PartInitException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void openElementInTable(IDipDocumentElement dipDocElement){
		DipProject project = dipDocElement.dipProject();
		DnfoTable table = project.getTable();
		IEditorPart part = openTable(table);
		if (part != null && part instanceof DipTableEditor){
			DipTableEditor tableEditor = (DipTableEditor) part;
			tableEditor.select(dipDocElement);
		}
	}
	
}
