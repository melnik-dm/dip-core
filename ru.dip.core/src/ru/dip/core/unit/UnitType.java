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
package ru.dip.core.unit;

import org.eclipse.core.resources.IFile;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.ISchemaContainer;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.DipUtilities;

public enum UnitType {
	
	IMAGE, HTML_IMAGE, TEXT, UML, DOT, DIA, HTML, CSV, TABLE, FORM, UNDEFINE, MARKDOWN, SUBMARKDOWN, JSON,  
	REPROT_REF, GLOS_REF, TOC_REF, CHANGELOG, PAGEBREAK, DOCX, XLS, PDF, ZIP;

	public boolean isRefType() {
		return isImageType() || isTableDescription() || isForm() ;
	}
	
	public boolean isNumerated() {
		return isImageType() || isTableDescription();
	}
	
	public  boolean isImageType() {
		return this == IMAGE || this == UML 
				|| this == DIA || this == DOT
				|| this == HTML_IMAGE;
	}
	
	public boolean isTableDescription() {
		return this == TABLE || this == CSV || this == REPROT_REF;
	}
	
	public boolean isForm() {
		return this == FORM;
	}
	
	public boolean isMarkdown() {
		return this == MARKDOWN || this == SUBMARKDOWN;
	}
	
	public boolean isLinkSupport() {
		return this == TABLE || isTextType(); 
	}
	
	public boolean isSpellCheckingSupport() {
		return isTextType(); 
	}
	
	public boolean isTextType() {
		return this == CSV 
				|| this == TEXT 
				|| this == MARKDOWN
				|| this == SUBMARKDOWN
				|| this == JSON 
				|| this == FORM; 
	}
	
	public boolean isHtmlType() {
		return this == TABLE
				|| this == HTML
				|| this == HTML_IMAGE;
	}
	
	private static final String[] IMAGE_EXTENSIONS = {"png","gif","jpg"};
	private static final String[] HTML_IMAGE_EXTENSIONS = {"rhtml","phtml"};
	public static final String[] TXT_EXTENSIONS = {"txt", "text"};
	public static final String[] MARKDOWN_EXTENSIONS = {"md", "mdown", "mkd"};
	public static final String[] PLANTUML_EXTENSIONS = {"plantuml","pu"};
	public static final String DOT_EXTENSION = "dot";
	public static final String CSV_EXTENSION = "csv";
	public static final String HTML_EXTENSION = "html";
	public static final String JSON_EXTENSION = "json";
	public static final String DIA_EXTENSION = "dia";
	public static final String TABLE_EXTENSION = "table";
	private static final String REPORT_REF_EXTENSION = "reportref";
	private static final String CHANGELOG_EXTENSION = "changelog";
	private static final String CHANGELOG_REF_EXTENSION = "changelogref";	
	private static final String TOC_REF_EXTENSION = "tocref";
	private static final String GLOS_REF_EXTENSION = "glosref";
	private static final String PAGEBREAK_EXTENSION = "pagebreak";
	private static final String SUB_MARKDOWN_EXTENSION = "submd";
	private static final String[] DOCX_EXTENSIONS = {"docx", "odt"};
	private static final String[] XLS_EXTENSIONS = {"xlsx", "ods"};
	private static final String PDF_EXTENSION = "pdf";
	private static final String ZIP_EXTENSION = "zip";

			
	public static final String[] EXTENSION_NEW = {"txt","md", SUB_MARKDOWN_EXTENSION, "plantuml",DOT_EXTENSION,
			HTML_EXTENSION, CSV_EXTENSION,TABLE_EXTENSION, DIA_EXTENSION, 
			HTML_IMAGE_EXTENSIONS[0], HTML_IMAGE_EXTENSIONS[1], JSON_EXTENSION,
			REPORT_REF_EXTENSION, GLOS_REF_EXTENSION, TOC_REF_EXTENSION, 
			CHANGELOG_REF_EXTENSION, PAGEBREAK_EXTENSION, ""}; 
	
	
	/**
	 * Определяет тип по имени файла
	 * Если не указан schemaContainer, то не сможет определить тип FORM (для не дип-проектов)
	 */
	public static UnitType defineUnitType(String fileName, ISchemaContainer schemaContainer) {
		String fileExtension = FileUtilities.getFileExtension(fileName);
		return defineUnitType(fileExtension, fileName, schemaContainer);
	}
	
	public static UnitType defineUnitType(String fileName) {
		return defineUnitType(fileName, null);
	}
	
	public static UnitType defineUnitType(String extension, String fileName, ISchemaContainer schemaContainer){
		if (UnitType.isImage(extension)){
			return UnitType.IMAGE;
		}
		if (UnitType.isText(extension)){
			return UnitType.TEXT;
		}
		if (UnitType.isMardown(extension)){
			return UnitType.MARKDOWN;
		}
		if (UnitType.isSubMarkdown(extension)) {
			return UnitType.SUBMARKDOWN;
		}
		if (UnitType.isHtml(extension)) {
			return UnitType.HTML;
		}		
		if (UnitType.isCSV(extension)){
			return UnitType.CSV;
		}
		if (UnitType.isTable(extension)) {
			return UnitType.TABLE;
		}	
		if (UnitType.isHtmlImage(extension)) {
			return UnitType.HTML_IMAGE;
		}		
		if (UnitType.isDia(extension)) {
			return UnitType.DIA;
		}
		if (UnitType.isPlantUML(extension)){
			return UnitType.UML;
		}
		if (UnitType.isDOT(extension)){
			return UnitType.DOT;
		}
		if (UnitType.isReportRef(extension)){
			return UnitType.REPROT_REF;
		}	
		if (UnitType.isGlosRef(fileName)){
			return UnitType.GLOS_REF;
		}		
		if (UnitType.isTocRef(fileName)) {
			return UnitType.TOC_REF;
		}			
		if (UnitType.isChangeLog(fileName)) {
			return UnitType.CHANGELOG;
		}
		if (UnitType.isPagebreak(extension)) {
			return UnitType.PAGEBREAK;
		}
		if (schemaContainer != null && UnitType.isForm(schemaContainer, extension)) {
			return UnitType.FORM;
		}
		if (UnitType.isJson(extension)) {
			return UnitType.JSON;
		}
		if (UnitType.isDocx(extension)) {
			return UnitType.DOCX;
		}
		if (UnitType.isXls(extension)) {
			return UnitType.XLS;
		}
		if (UnitType.isPdf(extension)) {
			return UnitType.PDF;
		}
		if (UnitType.isZip(extension)) {
			return UnitType.ZIP;
		}
		return UnitType.UNDEFINE;
	}
	
	public static boolean isNotNameExtension(String extension) {
		String testExtension = extension;
		if (testExtension.startsWith(".")) {
			testExtension = testExtension.substring(1);
		}	
		return CHANGELOG_EXTENSION.equalsIgnoreCase(testExtension) || CHANGELOG_REF_EXTENSION.equalsIgnoreCase(testExtension)
				|| TOC_REF_EXTENSION.equalsIgnoreCase(testExtension) || GLOS_REF_EXTENSION.equalsIgnoreCase(testExtension);
	}
	
	public static boolean isImage(String extension){
		return containsExtension(extension, IMAGE_EXTENSIONS);
	}
	
	public static boolean isHtmlImage(String extension){
		return containsExtension(extension, HTML_IMAGE_EXTENSIONS);
	}
	
	public static boolean isText(String extension){
		return containsExtension(extension, TXT_EXTENSIONS);

	}
	
	public static boolean isMardown(String extension){
		return containsExtension(extension, MARKDOWN_EXTENSIONS);
	}
	
	public static boolean isSubMarkdown(String extension) {
		return SUB_MARKDOWN_EXTENSION.equalsIgnoreCase(extension);

	}
	
	public static boolean isJson(String extension) {
		return JSON_EXTENSION.equalsIgnoreCase(extension);
	}
	
	public static boolean isPlantUML(String extension){
		return containsExtension(extension, PLANTUML_EXTENSIONS);
	}
	
	public static boolean isDia(String extension) {
		return DIA_EXTENSION.equalsIgnoreCase(extension);
	}

	public static boolean isDOT(String extension){
		return DOT_EXTENSION.equalsIgnoreCase(extension);
	}
	
	public static boolean isHtml(String extension) {
		return HTML_EXTENSION.equalsIgnoreCase(extension);
	}
	
	public static boolean isCSV(String extension){
		return CSV_EXTENSION.equalsIgnoreCase(extension);
	}
	
	public static boolean isTable(String extension) {
		return TABLE_EXTENSION.equalsIgnoreCase(extension);
	}
	
	public static boolean isDocx(String extension) {
		return containsExtension(extension, DOCX_EXTENSIONS);
	}
	
	public static boolean isXls(String extension) {
		return containsExtension(extension, XLS_EXTENSIONS);
	}
	
	public static boolean isPdf(String extension) {
		return PDF_EXTENSION.equalsIgnoreCase(extension);
	}
	
	public static boolean isZip(String extension) {
		return ZIP_EXTENSION.equalsIgnoreCase(extension);
	}
	
	
	private static boolean containsExtension(String extension, String[] extensions) {
		for (String textExt: extensions){
			if (textExt.equalsIgnoreCase(extension)){
				return true;
			}
		}
		return false;
	}
	
	
		
	public static boolean isReportRef(String filename){
		return REPORT_REF_EXTENSION.equalsIgnoreCase(filename);
	}

	public static boolean isTocRef(String filename) {
		return TocRefPresentation.FILE_NAME.equalsIgnoreCase(filename);
	}
	
	public static boolean isChangeLog(String filename) {
		for (String textExt: ChangeLogPresentation.FILE_NAMES){
			if (textExt.equalsIgnoreCase(filename)){
				return true;
			}
		}
		return false;
	}
	
	public static boolean isGlosRef(String name) {
		return GlossaryPresentation.FILE_NAME.equals(name);
	}
	
	public static boolean isPagebreak(String extension) {
		return PAGEBREAK_EXTENSION.equalsIgnoreCase(extension);
	}

	public static boolean isForm(ISchemaContainer schemaContainer, String extension) {
		return schemaContainer.containsSchema(extension);
	}
	
	public static boolean isForm(IFile file) {
		String extension = file.getFileExtension();
		if (extension == null) {
			return false;
		}		
		DipProject dipProject = DipUtilities.findDipProject(file);
		if (dipProject == null) {
			return false;
		}
		return isForm(dipProject, extension);
	}
	
}
	