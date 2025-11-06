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

import ru.dip.core.model.interfaces.IDipElement;

public class DipProjectProperties {
	
	private final IDipElement fDipElement;
	
	public DipProjectProperties(IDipElement dipElement) {
		fDipElement = dipElement;
	}
	
	public boolean isOneListMode(){
		return QualifiedNames.isOneListMode(resource());
	}
	
	public void setOneListMode(boolean mode){
		QualifiedNames.setOneListMode(resource(), mode);
	}
	
	public int getIDShowMode(){
		return QualifiedNames.getIDShowMode(resource());
	}
	
	public void setIDShowMode(int id){
		QualifiedNames.setIDShowMode(resource(), id);
	}
	
	public int getReviewMode(){
		return QualifiedNames.getReviewMode(resource());
	}
	
	public void setReviewMode(int id){
		QualifiedNames.setReviewMode(resource(), id);
	}
	
	public boolean isFixedContentMode(){
		return QualifiedNames.isFixedContentMode(resource());
	}
	
	public void setFixedContentMode(boolean mode){
		QualifiedNames.setFixedContentMode(resource(), mode);
	}
	
	public boolean isExportDocx(){
		return QualifiedNames.isExportDocx(resource());
	}
		
	public String getExportOut(){
		return QualifiedNames.getExportOut(resource());
	}
	
	public void setExportOut(String path){
		QualifiedNames.setExportOut(resource(), path);
	}
	
	public String getExportConfig(){
		return QualifiedNames.getExportConfig(resource());
	}
	
	public void setExportConfig(String path){
		QualifiedNames.setExportConfig(resource(), path);
	}
	
	public String getProjectRepo() {
		return QualifiedNames.getProjectRepo(resource());
	}
	
	public void setProjectRepo(String name) {
		QualifiedNames.setProjectRepo(resource(), name);
	}
		
	public boolean isFormNumeration(){
		return QualifiedNames.isFormNumeration(resource());
	}
	
	public void setFormNumeration(boolean newValue){
		QualifiedNames.setFormNumeration(resource(), newValue);
	}
	
	public boolean isMdComment() {
		return QualifiedNames.isMdComment(resource());
	}
	
	public void setMDComment(boolean newValue){
		QualifiedNames.setMDComment(resource(), newValue);
	}
	
	public boolean isStrictMdComment() {
		return QualifiedNames.isStrictMdComment(resource());

	}
	
	public void setStrictMDComment(boolean newValue){
		QualifiedNames.setStrictMDComment(resource(), newValue);
	}
	
	public boolean isNumeration(){
		return QualifiedNames.isNumeration(resource());
	}
	
	public void setNumeration(boolean newValue){
		QualifiedNames.setNumeration(resource(), newValue);
	}
	
	public boolean isHighlightGlossMode(){
		return QualifiedNames.isHighlightGlossMode(resource());
	}
	
	public void setHighlightGlossMode(boolean mode){
		QualifiedNames.setHighlightGlossMode(resource(), mode);
	}
	
	public boolean isFormShowPreferenciesEnable() {
		return QualifiedNames.isFormShowPreferenciesEnable(resource());
	}
	
	public void setFormFilterPreferenciesEnable(boolean mode){
		QualifiedNames.setFormFilterPreferenciesEnable(resource(), mode);
	} 
	
	public boolean isCheckSpellingEnable() {
		return QualifiedNames.isCheckSpellingEnable(resource());
	}
	
	public void setCheckSpellingEnable(boolean mode) {
		QualifiedNames.setCheckSpellingEnable(resource(), mode);
	}
	
	public boolean isHideDisableObjsEnable() {
		return QualifiedNames.isHideDisableObjsEnable(resource());
	}
	
	public void setHideDisableObjsEnable(boolean mode) {
		QualifiedNames.setHideDisableObjsEnable(resource(), mode);
	}
	
	public boolean isShowFormVersion() {
		return QualifiedNames.isShowFormVersion(resource());

	}
	
	public void setShowFormVersion(boolean mode) {
		QualifiedNames.setShowFormVersion(resource(), mode);
	}
	
	public boolean isReportRefRelativePath() {
		return QualifiedNames.isReportRefRelativePath(resource());
	}
	
	public void setReportRefRelativePath(boolean mode) {
		QualifiedNames.setReportRefRelativePath(resource(), mode);
	}
	
	private IResource resource() {
		return fDipElement.resource();
	}

}
