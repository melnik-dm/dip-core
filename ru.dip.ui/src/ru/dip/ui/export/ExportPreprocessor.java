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
import ru.dip.core.model.DipFolder;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.DipTableContainer;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.glossary.GlossaryFolder;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.report.scanner.ReportReader;
import ru.dip.core.unit.ReportRefPresentation;
import ru.dip.core.unit.TablePresentation;
import ru.dip.core.unit.TextPresentation;
import ru.dip.core.unit.UnitType;
import ru.dip.core.unit.md.SubMarkdownPresentation;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.ui.export.ExportElement.ExportElementBuilder;
import ru.dip.ui.export.error.IExportError;
import ru.dip.ui.table.ExporterHolder;

public class ExportPreprocessor implements IExportPreprocessor {
	
	protected final Path fTargetPath;
	protected final Path fPartsPath;
	protected final Path fConfigPath;
	protected final DipProject fDipProject;
	protected IProgressMonitor fMonitor;
	protected int fFilesNumber;
	
	protected List<IExportElement> fExportElements;
	private Map<String, IExportElement> fElementsById;
	
	/**
	 * @param targetPath - директория назначения
	 * @param configPath - конфигурация экспорта
	 * @param monitor
	 * @param filesNumber - количество экспортируемых файлов в проекте (для сообщений в ProgressBar)
	 */
	public ExportPreprocessor(DipProject dipProject, Path targetPath, Path configPath, IProgressMonitor monitor, int filesNumber) {
		fDipProject = dipProject;
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
	@Override
	public String export() throws Exception {
		startPreprocessor();
		return ExporterHolder.instance().getExporter().export(fDipProject, fTargetPath, fConfigPath, this, fFilesNumber);
	}	
	
	private void startPreprocessor() throws Exception {
		createExportElements();
		computeResults();						
	}
	
	protected void createExportElements() throws IOException{
		createPartFolder();				
		// createModel
		fExportElements = new ArrayList<>();		
		createFolderEntry(fDipProject, 0);
		// delete last pagebreak
		if (isLastPageBreak()) {
			removeLast();
		}	
	}
	
	protected void computeResults() throws IOException {
		prepareElementsById();
	}
	
	protected void prepareElementsById() {
		fElementsById = fExportElements.stream()
				.filter(el -> el.getType() != ExportElementType.PAGE_BREAK)
				.collect(Collectors.toMap(IExportElement::getId, el -> el));
	}

	@Override
	public boolean isCanceled() {
		if (fMonitor == null) {
			return false;
		}
		return fMonitor.isCanceled();
	}
	
	@Override
	public IProgressMonitor getProgressMonitor() {
		return fMonitor;
	}
	
	protected void createPartFolder() throws IOException {
		if (!Files.exists(fPartsPath)) {			
			Files.createDirectory(fPartsPath);
		}
	}
	
	//=======================
	// create folder
	
	protected void createFolderEntry(DipTableContainer folder, int level) {
		fMonitor.setTaskName("Preprocessing: " + folder.resource());
		if (folder.isDisabled()) {
			return;
		}
		
		if (level != 0) {
			IExportElement element = doCreateFolderEntry(folder, level);
			fExportElements.add(element);		
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
		
	protected IExportElement doCreateFolderEntry(DipTableContainer folder, int level) {
		ExportElementBuilder builder = new ExportElementBuilder()
				.buildType(ExportElementType.fromFolder(level))
				.buildId(DipUtilities.relativeProjectID(folder));
		return builder.build();
	}
	

	
	/** 
	 * Обходит детей
	 * Возвращает true - если внутри есть хотя бы одна папка
	 */
	protected boolean createChildren(DipTableContainer folder, int level, boolean eachFolder) {
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
	
	protected void createPageBreak() {
		ExportElement element = new ExportElementBuilder()
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
	
	private void removeLast() {
		if (!fExportElements.isEmpty()) {
			fExportElements.remove(fExportElements.size() - 1);
		}
	}
	
	//==============================
	// create unit
	
	protected void createUnitEntry(DipUnit unit) {
		fMonitor.setTaskName("Preprocessing: " + unit.resource());		
		if (unit.isDisabled()) {
			return;
		}
		
		UnitType unitType = unit.getUnitPresentation().getUnitType();	
		String id = DipUtilities.relativeProjectID(unit);
		ExportElementType type = ExportElementType.fromUnitType(unitType);				
		ExportElementBuilder builder = new ExportElementBuilder()
				.buildType(type)
				.buildId(id)
				.buildPath(unit.resource().getLocation().toOSString());			
		ExportElement element = builder.build();		
		// создание картинок (HTML_IMAGE вставляется как обычный html)
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
	
	protected void prepareImage(DipUnit unit, IExportElement element) {
		String fileName = element.getId().replaceAll("/", "_") + ".png";
		String fullName = fPartsPath.resolve(fileName).toString();
		convertToImage(fullName, unit);
		element.setPath(fullName);
	}
	
	protected void convertToImage(String fullName, DipUnit unit) {
		Image image = getImageFormPresentation(unit);
		ImageLoader saver = new ImageLoader();
		saver.data = new ImageData[] { image.getImageData() };
		saver.save(fullName, SWT.IMAGE_PNG);				
		/*
		if (unit.getUnitType() == UnitType.HTML_IMAGE) {
			image.dispose();
		}*/		
	}
	
	protected Image getImageFormPresentation(DipUnit unit) {
		TablePresentation presentation = unit.getUnitPresentation().getPresentation();
		// Html Image сейчас вставляется как обычный html
		/*if (presentation instanceof HtmlImagePresentation) {
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
		}*/
		return presentation.getImage();
	}
	
	
	private void prepareSubMarkdown(DipUnit unit, ExportElement element) throws IOException {
		String fileName = element.getId().replaceAll("/", "_");
		String fullName = fPartsPath.resolve(fileName).toString();				
		String content = FileUtilities.readFile(unit.resource());
		
		content = TextPresentation.prepareText(content, unit);
		
		// add number
		String number = ((SubMarkdownPresentation)unit.getUnitPresentation().getPresentation()).getNumber();
		if (number != null) {
			content = number + content;
		}
		
		FileUtilities.writeFile(Paths.get(fullName), content);
		element.setPath(fullName);
	}
	
	
	private void prepareTxtFiles(DipUnit unit, IExportElement element) throws IOException {
		String fileName = element.getId().replaceAll("/", "_");
		String fullName = fPartsPath.resolve(fileName).toString();				
		String content = FileUtilities.readFile(unit.resource());		
		String newText = TextPresentation.prepareText(content, unit);
		if (!newText.equals(content)) {
			FileUtilities.writeFile(Paths.get(fullName), newText);
			element.setPath(fullName);
		}
	}

	protected void prepareGlossary(IExportElement element) throws IOException {
		String fileName = element.getId().replaceAll("/", "_");
		String fullName = fPartsPath.resolve(fileName).toString();
		Path path = Paths.get(fullName);			
		GlossaryFolder.saveFields(path, fDipProject.getGlossaryFolder().getChildren());
		element.setPath(fullName);
	}

	
	protected void prepareReport(DipUnit unit, IExportElement element) throws IOException {		
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
	// getters & setters


	@Override
	public Path getSupportPathForElement(IDipUnit unit) {
		String id = DipUtilities.relativeProjectID(unit);
		IExportElement element = fElementsById.get(id);
		return element != null ? Paths.get(element.getPath()) : null;		
	}

	@Override
	public List<IExportError> getExportErrors() {
		return ExporterHolder.instance().getExporter().getExportErrors();
	}
	
}
