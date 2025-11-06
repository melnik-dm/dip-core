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
package ru.dip.core.model.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import ru.dip.core.model.interfaces.IMarkable;

public class QualifiedNames {
	
	private static QualifiedName oneListProperty = new QualifiedName("one_list_tqble", "0");
	private static QualifiedName idShowMode = new QualifiedName("id_show_mode_table", "1");
	private static QualifiedName fixedContent = new QualifiedName("fixed_content", "0");
	private static QualifiedName reviewMode = new QualifiedName("review_mode", "0");
	private static QualifiedName numeration = new QualifiedName("numeration", "1");
	private static QualifiedName formNumeration = new QualifiedName("formNumeration", "1");
	private static QualifiedName dtPropertyName = new QualifiedName("ru.dip.core", "_dt_project");
	private static QualifiedName exportDocx = new QualifiedName("export_docx", "1");
	private static QualifiedName exportOut = new QualifiedName("export_out", "export_out");
	private static QualifiedName exportConfig = new QualifiedName("export_config", "export_config");
	private static QualifiedName projectRepo = new QualifiedName("project_repo", "_project_repo");
	private static QualifiedName mdcomment = new QualifiedName("md_comment", "1");	
	private static QualifiedName strictMdComment = new QualifiedName("strict_md_comment", "1");	
	private static QualifiedName highlightGloss = new QualifiedName("hoghlight_Gloss", "0");
	private static QualifiedName enableFormShowProperties = new QualifiedName("enable_form_show_properties", "0");
	private static QualifiedName checkSpelling = new QualifiedName("checkSpelling", "1");
	private static QualifiedName hideDisableObj = new QualifiedName("hideDisableObj", "0");
	private static QualifiedName showFormVersion = new QualifiedName("showFormVersion", "0");
	private static QualifiedName reportRefRelativePath = new QualifiedName("reportRefRelativePath", "1");

	// метки 
	private static QualifiedName[] marks =  IMarkable.markNumberSteam()
			.mapToObj(markNumber -> new QualifiedName("mark_" + markNumber, "0"))
			.toArray(QualifiedName[]::new);
	
	
	
	public static boolean isOneListMode(IResource resource){
		try {
			String property = resource.getPersistentProperty(oneListProperty);
			return "1".equals(property);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void setOneListMode(IResource resource, boolean mode){
		String value = mode ? "1" : "0";
		try {
			resource.setPersistentProperty(oneListProperty, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static int getIDShowMode(IResource resource){
		try {
			String property = resource.getPersistentProperty(idShowMode);
			if (property == null || property.isEmpty()){
				return 1;
			}
			return Integer.parseInt(property);
		} catch (CoreException | NumberFormatException e) {
			e.printStackTrace();
		}
		return 1;
	}
	
	public static void setIDShowMode(IResource resource, int id){
		String value = String.valueOf(id);
		try {
			resource.setPersistentProperty(idShowMode, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static int getReviewMode(IResource resource){
		try {
			String property = resource.getPersistentProperty(reviewMode);
			if (property == null || property.isEmpty()){
				return 1;
			}
			return Integer.parseInt(property);
		} catch (CoreException | NumberFormatException e) {
			e.printStackTrace();
		}
		return 1;
	}
	
	public static void setReviewMode(IResource resource, int id){
		String value = String.valueOf(id);
		try {
			resource.setPersistentProperty(reviewMode, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isFixedContentMode(IResource resource){
		try {
			String property = resource.getPersistentProperty(fixedContent);
			return "1".equals(property);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void setFixedContentMode(IResource resource, boolean mode){
		String value = mode ? "1" : "0";
		try {
			resource.setPersistentProperty(fixedContent, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static void setDTPAthProperty(IResource resource, String path){
		try {
			resource.setPersistentProperty(dtPropertyName, path);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static String getDTPathProperty(IResource resource){
		try {
			return resource.getPersistentProperty(dtPropertyName);
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean isExportDocx(IResource resource){
		try {
			String property = resource.getPersistentProperty(exportDocx);
			return property == null ||property.isEmpty()  || "1".equals(property);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void setExportDocx(IResource resource, boolean docx){
		String value = docx ? "1" : "0";
		try {
			resource.setPersistentProperty(exportDocx, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static String getExportOut(IResource resource){
		try {
			return resource.getPersistentProperty(exportOut);
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void setExportOut(IResource resource, String path){
		try {
			resource.setPersistentProperty(exportOut, path);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static String getExportConfig(IResource resource){
		try {
			return resource.getPersistentProperty(exportConfig);
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void setExportConfig(IResource resource, String path){
		try {
			resource.setPersistentProperty(exportConfig, path);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static String getProjectRepo(IResource resource) {
		try {
			return resource.getPersistentProperty(projectRepo);
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void setProjectRepo(IResource resource, String name) {
		try {
			resource.setPersistentProperty(projectRepo, name);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
		
	public static boolean isFormNumeration(IResource resource){
		try {
			String property = resource.getPersistentProperty(formNumeration);
			return property == null ||property.isEmpty()  || "1".equals(property);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void setFormNumeration(IResource resource, boolean newValue){
		String value = newValue ? "1" : "0";
		try {
			resource.setPersistentProperty(formNumeration, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isMdComment(IResource resource) {
		try {
			String property = resource.getPersistentProperty(mdcomment);
			return property == null ||property.isEmpty()  || "1".equals(property);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void setMDComment(IResource resource, boolean newValue){
		String value = newValue ? "1" : "0";
		try {
			resource.setPersistentProperty(mdcomment, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isStrictMdComment(IResource resource) {
		try {
			String property = resource.getPersistentProperty(strictMdComment);
			return property == null ||property.isEmpty()  || "1".equals(property);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void setStrictMDComment(IResource resource, boolean newValue){
		String value = newValue ? "1" : "0";
		try {
			resource.setPersistentProperty(strictMdComment, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isNumeration(IResource resource){
		try {
			String property = resource.getPersistentProperty(numeration);
			return property == null ||property.isEmpty()  || "1".equals(property);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void setNumeration(IResource resource, boolean newValue){
		String value = newValue ? "1" : "0";
		try {
			resource.setPersistentProperty(numeration, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isHighlightGlossMode(IResource resource){
		try {
			String property = resource.getPersistentProperty(highlightGloss);
			return "1".equals(property);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void setHighlightGlossMode(IResource resource, boolean mode){
		String value = mode ? "1" : "0";
		try {
			resource.setPersistentProperty(highlightGloss, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isFormShowPreferenciesEnable(IResource resource) {
		try {
			String property = resource.getPersistentProperty(enableFormShowProperties);
			return "1".equals(property);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void setFormFilterPreferenciesEnable(IResource resource, boolean mode){
		String value = mode ? "1" : "0";
		try {
			resource.setPersistentProperty(enableFormShowProperties, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	} 
	
	public static boolean isCheckSpellingEnable(IResource resource) {
		try {
			String property = resource.getPersistentProperty(checkSpelling);
			return "1".equals(property);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void setCheckSpellingEnable(IResource resource, boolean mode) {
		String value = mode ? "1" : "0";
		try {
			resource.setPersistentProperty(checkSpelling, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isHideDisableObjsEnable(IResource resource) {
		try {
			String property = resource.getPersistentProperty(hideDisableObj);
			return "1".equals(property);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void setHideDisableObjsEnable(IResource resource, boolean mode) {
		String value = mode ? "1" : "0";
		try {
			resource.setPersistentProperty(hideDisableObj, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isShowFormVersion(IResource resource) {
		try {
			String property = resource.getPersistentProperty(showFormVersion);
			return "1".equals(property);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void setShowFormVersion(IResource resource, boolean mode) {
		String value = mode ? "1" : "0";
		try {
			resource.setPersistentProperty(showFormVersion, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isMark(IResource resource, int markNumber) {
		try {
			String property = resource.getPersistentProperty(marks[markNumber]);
			return "1".equals(property);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void setMark(IResource resource, int markNumber, boolean mode) {
		String value = mode ? "1" : "0";
		try {
			resource.setPersistentProperty(marks[markNumber], value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isReportRefRelativePath(IResource resource) {
		try {
			String property = resource.getPersistentProperty(reportRefRelativePath);
			return property == null ||property.isEmpty()  || "1".equals(property);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void setReportRefRelativePath(IResource resource, boolean mode) {
		String value = mode ? "1" : "0";
		try {
			resource.setPersistentProperty(reportRefRelativePath, value);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
}
