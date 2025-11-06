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
package ru.dip.core.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import ru.dip.core.DipCorePlugin;

public class DipTableContainerProperties {
	
	private static QualifiedName presentationColumnWidth = new QualifiedName("_presentation_width", "-1");
	private static QualifiedName idColumnWidth = new QualifiedName("_id_width", "-1");
	private static QualifiedName commentColumnWidth = new QualifiedName("_comment_width", "-1");
	
	private static QualifiedName ratioRestoreIdWidth = new QualifiedName("_ratioRestoreIdWidth", "-1");
	private static QualifiedName ratioRestorePresentWidth = new QualifiedName("_ratioRestorePresentWidth", "-1");
	private static QualifiedName ratioRestoreCommentWidth = new QualifiedName("_ratioRestoreCommentWidth", "-1");

	private static QualifiedName ratioMaximizeIdWidth = new QualifiedName("_ratioMaximizeIdWidth", "-1");
	private static QualifiedName ratioMaximizePresentWidth = new QualifiedName("_ratioMaximizePresentWidth", "-1");
	private static QualifiedName ratioMaximizeCommentWidth = new QualifiedName("_ratioMaximizeCommentWidth", "-1");
	
	private DipTableContainer fDipTableContainer;
	
	public DipTableContainerProperties(DipTableContainer dipTableContainer) {
		fDipTableContainer = dipTableContainer;
	}

	//===============================
	// restore properties
	
	public double getRatioRestoreIdWidth() {
		return getDoublePropertyValue(ratioRestoreIdWidth);
	}
	
	public double getRatioRestorePresentationWidth() {
		return getDoublePropertyValue(ratioRestorePresentWidth);
	}
	
	public double getRatioRestoreCommentWidth() {
		return getDoublePropertyValue(ratioRestoreCommentWidth);
	}
	
	public void setRatioRestroreWidth(double idWidth, double presentationWidth, double commentWidth) {
		setDoublePropertyValue(ratioRestoreIdWidth, idWidth);
		setDoublePropertyValue(ratioRestorePresentWidth, presentationWidth);
		setDoublePropertyValue(ratioRestoreCommentWidth, commentWidth);
	}
	
	//====================================
	// maximize properties
	
	public double getRatioMaximizeIdWidth() {
		return getDoublePropertyValue(ratioMaximizeIdWidth);
	}
	
	public double getRatioMaximizePresentationWidth() {
		return getDoublePropertyValue(ratioMaximizePresentWidth);
	}
	
	public double getRatioMaximizeCommentWidth() {
		return getDoublePropertyValue(ratioMaximizeCommentWidth);
	}
	
	public void setRatioMaximizeWidth(double idWidth, double presentationWidth, double commentWidth) {
		setDoublePropertyValue(ratioMaximizeIdWidth, idWidth);
		setDoublePropertyValue(ratioMaximizePresentWidth, presentationWidth);
		setDoublePropertyValue(ratioMaximizeCommentWidth, commentWidth);
	}
	
	//============================
	// columns width
	
	public int getIDColumnWidth(){
		return getPropertyValue(idColumnWidth);
	}
	
	public void setIDColumnWidth(int id){
		setPropertyValue(idColumnWidth, id);
	}
	
	public int getPresentationColumnWidth(){
		return getPropertyValue(presentationColumnWidth);
	}
	
	public void setPresentationColumnWidth(int id){
		setPropertyValue(presentationColumnWidth, id);
	}
	
	public int getCommentColumnWidth(){
		return getPropertyValue(commentColumnWidth);
	}
	
	public void setCommentColumnWidth(int id){
		setPropertyValue(commentColumnWidth, id);
	}
	
	//=================================
	
	private int getPropertyValue(QualifiedName qualifiedName) {
		try {
			String property = fDipTableContainer.resource().getPersistentProperty(qualifiedName);
			if (property == null || property.isEmpty()){
				return -1;
			}
			return Integer.parseInt(property);
		} catch (CoreException | NumberFormatException e) {
			//e.printStackTrace();
			DipCorePlugin.logError("Get property error: " + e.getMessage());
		}
		return -1;
	}
	
	private void setPropertyValue(QualifiedName qualifiedName, int newValue) {
		String value = String.valueOf(newValue);
		try {
			fDipTableContainer.resource().setPersistentProperty(qualifiedName, value);
		} catch (CoreException e) {
			//e.printStackTrace();
			DipCorePlugin.logError("Set property error: " + e.getMessage());
		}
	}
	
	private double getDoublePropertyValue(QualifiedName qualifiedName) {
		try {
			String property = fDipTableContainer.resource().getPersistentProperty(qualifiedName);
			if (property == null || property.isEmpty()){
				return -1;
			}
			return Double.parseDouble(property);
		} catch (CoreException | NumberFormatException e) {
			//e.printStackTrace();
			DipCorePlugin.logError("Get property error: " + e.getMessage());
		}
		return -1;
	}
	
	private void setDoublePropertyValue(QualifiedName qualifiedName, double newValue) {
		String value = String.valueOf(newValue);
		try {
			fDipTableContainer.resource().setPersistentProperty(qualifiedName, value);
		} catch (CoreException e) {
			//e.printStackTrace();
			DipCorePlugin.logError("Set property error: " + e.getMessage());
		}
	}
}
