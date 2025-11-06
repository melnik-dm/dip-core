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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.link.Link;
import ru.dip.core.link.LinkInteractor;
import ru.dip.core.model.Appendix;
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.report.scanner.ReportReader;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.unit.HtmlImagePresentation;
import ru.dip.core.unit.ReportRefPresentation;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.unit.TextPresentation;
import ru.dip.core.unit.UnitType;
import ru.dip.core.unit.md.SubMarkdownPresentation;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.HtmlUtilities;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ui.swt.ImageUtilities;
import ru.dip.ui.export.ExportElement.ExportElementBuilder;
import ru.dip.ui.export.error.IExportError;
import ru.dip.ui.export.json.writers.JsonWriter;
import ru.dip.ui.export.json.writers.TocJsonWriter;
import ru.dip.ui.table.ExporterHolder;
import ru.dip.ui.table.ExporterHolder.IExporter;

public class ExportPreprocessor {
	
	private final Path fTargetPath;
	private final Path fPartsPath;
	private final Path fConfigPath;

	private final JsonWriter fJsonWriter;
	private final DipProject fDipProject;
	private List<ExportElement> fExportElements;
	private Map<String, ExportElement> fElementsById;
	
	private boolean fAfterToc = false; // флаг, переключается на true, когда встретится файл .toc
	private List<TocEntry> fTocEntries = new ArrayList<>();
	private ExportElement fTocElement;
	private IProgressMonitor fMonitor;
	//private List<GitTagEntry> fTagEntries = new ArrayList<>();
	private int fFilesNumber;
	
	/**
	 * @param targetPath - директория назначения
	 * @param configPath - конфигурация экспорта
	 * @param monitor
	 * @param filesNumber - количество экспортируемых файлов в проекте (для сообщений в ProgressBar)
	 */
	public ExportPreprocessor(DipProject dipProject, Path targetPath, Path configPath, IProgressMonitor monitor, int filesNumber) {
		fDipProject = dipProject;
		fJsonWriter = new JsonWriter(this);
		fTargetPath = targetPath;
		fPartsPath = fTargetPath.resolve("parts2");
		fConfigPath = configPath;
		fMonitor = monitor;
		fFilesNumber = (int) (filesNumber > 0 ? filesNumber : DipUtilities.countUnits(dipProject));
		// очистить директорию parts2  (надо написать нормальный код)
		if (Files.exists(fPartsPath)) {
			try {
				Files.list(fPartsPath).filter(Files::isRegularFile).forEach(t -> {
						try {
							Files.delete(t);
						} catch (IOException e) {
							e.printStackTrace();
						}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Возвращает путь до файла с результатом
	 */
	public String export() throws Exception {
		startPreprocessor();
		return ExporterHolder.instance().getExporter().export(fDipProject, fTargetPath, fConfigPath, this, fFilesNumber);
	}	
	
	public void startPreprocessor() throws Exception {
		createPartFolder();				
		// createModel
		fExportElements = new ArrayList<>();		
		createFolderEntry(fDipProject, 0);
		// delete last pagebreak
		if (isLastPageBreak()) {
			removeLast();
		}							
		// обновить элемент с содержанием
		prepareToc();		
		fElementsById = fExportElements.stream()
				.filter(el -> el.getType() != ExportElementType.PAGE_BREAK)
				.collect(Collectors.toMap(ExportElement::getId, el -> el));		
		// wirte Json
		fJsonWriter.writeToJson();
	}
	
	public void export(IExporter exporter) throws Exception {
		startPreprocessor();
		exporter.export(fDipProject, fTargetPath, fConfigPath, this, fFilesNumber);
	}
	
	public boolean isCanceled() {
		if (fMonitor == null) {
			return false;
		}
		return fMonitor.isCanceled();
	}
	
	public IProgressMonitor getProgressMonitor() {
		return fMonitor;
	}
	
	private void createPartFolder() throws IOException {
		if (!Files.exists(fPartsPath)) {			
			Files.createDirectory(fPartsPath);
		}
	}
	
	//=======================
	// create folder
	
	private void createFolderEntry(DipTableContainer folder, int level) {
		fMonitor.setTaskName("Preprocessing: " + folder.resource());
		if (folder.isDisabled()) {
			return;
		}
		
		if (level != 0) {
			ExportElement element = doCreateFolderEntry(folder, level);
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
		
	private ExportElement doCreateFolderEntry(DipTableContainer folder, int level) {
		ExportElementBuilder builder = new ExportElementBuilder()
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
	
	private void addToToc(ExportElement element) {
		TocEntry tocEntry = new TocEntry(element.getNumber(), element.getDescription(), 
				element.getId(), element.isAppendix());
		fTocEntries.add(tocEntry);
	}
	
	/** 
	 * Обходит детей
	 * Возвращает true - если внутри есть хотя бы одна папка
	 */
	private boolean createChildren(DipTableContainer folder, int level, boolean eachFolder) {
		boolean hasFolder = false;
		for (IDipDocumentElement dipDocElement: folder.getDipDocChildrenList()) {
			if (dipDocElement instanceof DipUnit) {
				if (((DipUnit) dipDocElement).getUnitType() == UnitType.PAGEBREAK) {
					createPageBreak();
				} else {
					createUnitEntry((DipUnit) dipDocElement);
				}
			} else if (dipDocElement instanceof DipFolder) {
				createFolderEntry((DipFolder) dipDocElement, level+1);
				hasFolder = true;
				if (eachFolder && !isLastPageBreak()) {
					createPageBreak();
				}
			}
		}
		return hasFolder;
	}
	
	private void createPageBreak() {
		ExportElement element = new ExportElementBuilder()
				.buildType(ExportElementType.PAGE_BREAK)
				.build();
		fExportElements.add(element);
	}
	
	private boolean isLastPageBreak() {
		if (fExportElements.size() == 0) {
			return true;
		}
		
		ExportElement last = fExportElements.get(fExportElements.size() - 1);
		return last.getType() == ExportElementType.PAGE_BREAK;
	}
	
	private void removeLast() {
		if (!fExportElements.isEmpty()) {
			fExportElements.remove(fExportElements.size() - 1);
		}
	}
	
	//==============================
	// create unit
	
	private void createUnitEntry(DipUnit unit) {
		fMonitor.setTaskName("Preprocessing: " + unit.resource());		
		if (unit.isDisabled()) {
			return;
		}
		
		String description = unit.description();
		UnitType unitType = unit.getUnitPresentation().getUnitType();	
		String id = DipUtilities.relativeProjectID(unit);
		ExportElementType type = ExportElementType.fromUnitType(unitType);				
		ExportElementBuilder builder = new ExportElementBuilder()
				.buildNumber(unit.getNumer())
				.buildType(type)
				.buildDescription(description)
				.buildId(id)
				.buildPath(unit.resource().getLocation().toOSString());
		if (unit.isHorizontalOrientation()) {
			builder.buildHorizontal(true);
		}				
		ExportElement element = builder.build();		
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
	
	private void prepareImage(DipUnit unit, ExportElement element) {
		String fileName = element.getId().replaceAll("/", "_") + ".png";
		String fullName = fPartsPath.resolve(fileName).toString();
		convertToImage(fullName, unit);
		element.setPath(fullName);
	}
	
	private void convertToImage(String fullName, DipUnit unit) {
		Image image = getImageFormPresentation(unit);
		ImageLoader saver = new ImageLoader();
		saver.data = new ImageData[] { image.getImageData() };
		saver.save(fullName, SWT.IMAGE_PNG);		
	}
	
	private Image getImageFormPresentation(DipUnit unit) {
		TablePresentation presentation = unit.getUnitPresentation().getPresentation();
		if (presentation instanceof HtmlImagePresentation) {
			try {
				String content = FileUtilities.readFile(unit.resource());
				content = TextPresentation.prepareText(content, unit);
				content = HtmlUtilities.setHtmlTableBorder(content);				
				return ImageUtilities.createImageFromHtml(content, unit.resource());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return presentation.getImage();
		}
	}
	
	
	private void prepareSubMarkdown(DipUnit unit, ExportElement element) throws IOException {
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
	
	
	private void prepareTxtFiles(DipUnit unit, ExportElement element) throws IOException {
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

	private void prepareGlossary(ExportElement element) throws IOException {
		String fileName = element.getId().replaceAll("/", "_");
		String fullName = fPartsPath.resolve(fileName).toString();
		Path path = Paths.get(fullName);			
		GlossaryFolder.saveFields(path, fDipProject.getGlossaryFolder().getChildren());
		element.setPath(fullName);
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
	
	private void prepareReport(DipUnit unit, ExportElement element) throws IOException {		
		//String fileName = element.getId().replaceAll("/", "_") + ".json";
		//String fullName = fPartsPath.resolve(fileName).toString();	
		ReportRefPresentation reportPresentation = (ReportRefPresentation) unit.getUnitPresentation().getPresentation();
		ReportReader reader = reportPresentation.getReportReader();	
		//ReportJsonWriter writer = new ReportJsonWriter(fDipProject);		
		//writer.writeToJson(reader, Paths.get(fullName));	
		// html presentation
		String htmlName = element.getId().replaceAll("/", "_") + ".html";
		String htmlFullName = fPartsPath.resolve(htmlName).toString();	
		HtmlReportWriter htmlWriter = new HtmlReportWriter(reader, fDipProject);
		htmlWriter.writeReportToHtml(htmlFullName);	
		element.setPath(htmlFullName);
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
	
	public List<ExportElement> getElements(){
		return fExportElements;
	}
	
	public List<TocEntry> getTocEntries(){
		return fTocEntries;
	}
	
	/*public List<GitTagEntry> getGitTagEntries(){
		return fTagEntries;
	}*/
	
	public DipProject getProject() {
		return fDipProject;
	}
		
	public Path getPartPath() {
		return fPartsPath;
	}
	
	public Map<String, ExportElement> getElementsById(){
		return fElementsById;
	}
	
	public Path getSupportPathForElement(IDipUnit unit) {
		String id = DipUtilities.relativeProjectID(unit);
		ExportElement element = fElementsById.get(id);
		return element != null ? Paths.get(element.getPath()) : null;		
	}

	public List<IExportError> getExportErrors() {
		return ExporterHolder.instance().getExporter().getExportErrors();
	}
	
}
