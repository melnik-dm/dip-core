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
package ru.dip.ui.export;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.link.Link;
import ru.dip.core.link.LinkInteractor;
import ru.dip.core.model.Appendix;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.DipUnit;
import ru.dip.core.unit.TextPresentation;
import ru.dip.core.unit.UnitType;
import ru.dip.core.unit.md.SubMarkdownPresentation;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.ui.export.FullExportElement.FullExportElementBuilder;
import ru.dip.ui.export.json.writers.JsonWriter;
import ru.dip.ui.export.json.writers.TocJsonWriter;

public class FullExportPreprocessor extends ExportPreprocessor  implements IExportPreprocessor {

	private final JsonWriter fJsonWriter;

	private boolean fAfterToc = false; // флаг, переключается на true, когда встретится файл .toc
	private List<TocEntry> fTocEntries = new ArrayList<>();
	private FullExportElement fTocElement;
	
	/**
	 * @param targetPath - директория назначения
	 * @param configPath - конфигурация экспорта
	 * @param monitor
	 * @param filesNumber - количество экспортируемых файлов в проекте (для сообщений в ProgressBar)
	 */
	public FullExportPreprocessor(DipProject dipProject, Path targetPath, Path configPath, IProgressMonitor monitor, int filesNumber) {
		super(dipProject, targetPath, configPath, monitor, filesNumber);
		fJsonWriter = new JsonWriter(this);
	}
	
	@Override
	protected void computeResults() throws IOException {
		// обновить элемент с содержанием
		prepareToc();
		
		// подготовить мапу
		prepareElementsById();
	
		// wirte Json
		fJsonWriter.writeToJson();
	}
	
	
	
	
	//=======================
	// create folder
	
	@Override
	protected void createFolderEntry(DipTableContainer folder, int level) {
		fMonitor.setTaskName("Preprocessing: " + folder.resource());
		if (folder.isDisabled()) {
			return;
		}
		
		if (level != 0) {
			FullExportElement element = doCreateFolderEntry(folder, level);
			fExportElements.add(element);		
			if (fAfterToc) {
				addToToc(element);
			}
		}
		// pagebreak
		boolean hasFolder = false;			
		String pageBreak = folder.getPageBreak();
		boolean eachFolder = "EACH_FOLDER".equals(pageBreak);
		boolean lastFolder = "LAST_FOLDER".equals(pageBreak);
					
		hasFolder = createChildren(folder, level, eachFolder);
		
		if (lastFolder && hasFolder && !isLastPageBreak()) {
			createPageBreak();
		}
	}
		
	@Override
	protected FullExportElement doCreateFolderEntry(DipTableContainer folder, int level) {
		FullExportElementBuilder builder = new FullExportElementBuilder()
				.buildType(ExportElementType.fromFolder(level))
				.buildDescription(folder.description())
				.buildId(DipUtilities.relativeProjectID(folder))
				//.buildNumeration(folder.isActiveNumeration())
				.buildPagebreak(folder.getPageBreak())
				.buildAppendix(folder instanceof Appendix);
		
		if (folder.isActiveNumeration()) {
			builder.buildNumber(folder.number());
		}	
		return builder.build();
	}
	
	private void addToToc(FullExportElement element) {
		TocEntry tocEntry = new TocEntry(element.getNumber(), element.getDescription(), 
				element.getId(), element.isAppendix());
		fTocEntries.add(tocEntry);
	}
	
	@Override
	protected void createPageBreak() {
		FullExportElement element = new FullExportElementBuilder()
				.buildType(ExportElementType.PAGE_BREAK)
				.build();
		fExportElements.add(element);
	}
	
	private boolean isLastPageBreak() {
		if (fExportElements.size() == 0) {
			return true;
		}
		
		IExportElement last = fExportElements.get(fExportElements.size() - 1);
		return last.getType() == ExportElementType.PAGE_BREAK;
	}
	
	
	//==============================
	// create unit
	
	@Override
	protected void createUnitEntry(DipUnit unit) {		
		fMonitor.setTaskName("Preprocessing: " + unit.resource());		
		if (unit.isDisabled()) {
			return;
		}
		
		String description = unit.description();
		UnitType unitType = unit.getUnitPresentation().getUnitType();	
		String id = DipUtilities.relativeProjectID(unit);
		ExportElementType type = ExportElementType.fromUnitType(unitType);				
		FullExportElementBuilder builder = new FullExportElementBuilder()
				.buildNumber(unit.getNumer())
				.buildType(type)
				.buildDescription(description)
				.buildId(id)
				.buildPath(unit.resource().getLocation().toOSString());
		if (unit.isHorizontalOrientation()) {
			builder.buildHorizontal(true);
		}			
		FullExportElement element = builder.build();		
		// создание картинок
		if (unitType == UnitType.UML || /*unitType == UnitType.HTML_IMAGE ||*/ unitType == UnitType.DIA || unitType == UnitType.DOT) {
			prepareImage(unit, element);
		}
		
		if (unitType == UnitType.SUBMARKDOWN){
			try {
				prepareSubMarkdown(unit, element);
			} catch (IOException e) {
				DipCorePlugin.logError(e, "prepare submd error " + e.getMessage());		
				e.printStackTrace();
			}
		} else if (unitType.isLinkSupport()) {
			// переименование ссылок
			try {
				prepareTxtFiles(unit, element);
			} catch (IOException e) {
				DipCorePlugin.logError(e, "prepare text files " + e.getMessage());		
				//WorkbenchUtitlities.openError("Prepare file", "Ошибка при подготовке файла " + unit);
				e.printStackTrace();
			}
		}
		// глоссарий
		if (unitType == UnitType.GLOS_REF) {
			try {
				prepareGlossary(element);
			} catch (IOException e) {
				//WorkbenchUtitlities.openError("Prepare file", "Ошибка при подготовке файла " + unit);
				e.printStackTrace();
			}
		}
		// содержание
		if (unitType == UnitType.TOC_REF) {
			fAfterToc = true;
			fTocElement = element;
		}
		// отчет
		if (unitType == UnitType.REPROT_REF) {
			try {
				prepareReport(unit, element);
			} catch (IOException e) {
				//WorkbenchUtitlities.openError("Prepare file", "Ошибка при подготовке файла " + unit);
				e.printStackTrace();
			}
		}
		
		
		// changelog
		/*if (unitType == UnitType.CHANGELOG) {
			try {
				prepareChangeLog(element);
			} catch (IOException e) {
				WorkbenchUtitlities.openError("Prepare file", "Ошибка при подготовке файла " + unit);
				e.printStackTrace();
			}
		}*/
		
		// создание таблиц в dox
		/*if (unitType == UnitType.TABLE || unitType == UnitType.HTML) {
			converToHtml(id, element);
		}*/
		fExportElements.add(element);
	}

	//===============================
	// prepare
	
	private void prepareSubMarkdown(DipUnit unit, FullExportElement element) throws IOException {
		String fileName = element.getId().replaceAll("/", "_");
		String fullName = fPartsPath.resolve(fileName).toString();				
		String content = FileUtilities.readFile(unit.resource());
		List<Link> links = LinkInteractor.findAllLinks(unit, content);
		if (!links.isEmpty()) {
			element.setLinks(links);
		}
		content = TextPresentation.prepareText(content, unit);
		
		// add number
		String number = ((SubMarkdownPresentation)unit.getUnitPresentation().getPresentation()).getNumber();
		if (number != null) {
			content = number + content;
		}
		
		FileUtilities.writeFile(Paths.get(fullName), content);
		element.setPath(fullName);
	}
	
	
	private void prepareTxtFiles(DipUnit unit, FullExportElement element) throws IOException {
		String fileName = element.getId().replaceAll("/", "_");
		String fullName = fPartsPath.resolve(fileName).toString();				
		String content = FileUtilities.readFile(unit.resource());		
		String newText = TextPresentation.prepareText(content, unit);
		if (!newText.equals(content)) {
			FileUtilities.writeFile(Paths.get(fullName), newText);
			element.setPath(fullName);
		}		
		List<Link> links = LinkInteractor.findAllLinks(unit, content);
		if (!links.isEmpty()) {
			element.setLinks(links);
		}
	}

	private void prepareToc() {
		// номер   // текст // link_id
		fDipProject.getDipDocChildrenList();
		if (fTocElement != null) {
			String fileName = fTocElement.getId().replaceAll("/", "_") + ".json";
			String fullName = fPartsPath.resolve(fileName).toString();
			
			TocJsonWriter writer = new TocJsonWriter();
			try {
				writer.writeToJson(this, Paths.get(fullName));
				fTocElement.setPath(fullName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//===========================
	// пока не используется
	
	/*private void prepareChangeLog(ExportElement element) throws IOException {
		String fileName = element.getId().replaceAll("/", "_") + ".json";
		String fullName = fPartsPath.resolve(fileName).toString();
		getChangeLog();
		ChangeLogWriter writer = new ChangeLogWriter();
		writer.writeToJson(this, Paths.get(fullName));
		element.setPath(fullName);		
	}
	
	private void getChangeLog() throws IOException {
		if (!fTagEntries.isEmpty()) {
			// если файл .chagelogref - встречается 2 раза
			return;
		}
		
		Repository repo = GITUtilities.findRepo(fDipProject.getProject());
		try (RevWalk revWalk = new RevWalk(repo)) {
			if (repo != null) {
				Map<String, String> result = GITUtilities.getTags(repo.getDirectory().getAbsolutePath());
				for (Entry<String, String> entry : result.entrySet()) {
					Ref ref = repo.findRef(entry.getValue());
					RevTag revTag = revWalk.parseTag(ref.getObjectId());
					if (revTag != null) {
						GitTagEntry  tagEntry = new GitTagEntry(entry.getValue(), revTag.getShortMessage());
						fTagEntries.add(tagEntry);
					} else {
						throw new IOException("Git. Ошибка при чтении тега. " + entry.getValue());
					}
				}
			}
		}
	}*/
		
	/*private void converToHtml(String id, ExportElement element) {
		String fileName = id.replaceAll("/", "_") + ".docx";
		String fullName = partPath.resolve(fileName).toString();		
		fExporter.convertHtml(fullName, element);
	}*/
	
	/*private void createGlossaryTable(String id) {
		String fileName = id.replaceAll("/", "_") + ".docx";
		String fullName = partPath.resolve(fileName).toString();	
		fExporter.createGlossary(fullName, fDipProject);
	}*/

	//===========================
	// getters & setters
	
	public List<? extends IExportElement> getElements(){
		return fExportElements;
	}
	
	public List<TocEntry> getTocEntries(){
		return fTocEntries;
	}
	
	public DipProject getProject() {
		return fDipProject;
	}
		
	public Path getPartPath() {
		return fPartsPath;
	}

}
