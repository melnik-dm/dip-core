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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import ru.dip.core.model.interfaces.IMarkable;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.ITextComment;
import ru.dip.core.model.properties.QualifiedNames;
import ru.dip.core.unit.UnitDescriptionPresentation;
import ru.dip.core.unit.UnitExtension;
import ru.dip.core.unit.UnitPresentation;
import ru.dip.core.unit.UnitType;
import ru.dip.core.unit.form.FormPresentation;

public class DipUnit extends DipElement implements IDipUnit {

	private UnitDescriptionPresentation fDipUnitDescription;
	private UnitPresentation fUnitPresentation;
	private String fNumber = null;  // for table and images
	private boolean fHorizontalOrientation = false;
	private Boolean[] fMarks;
	
	public static DipUnit instance(IResource resource, IParent parent) {
		IDipElement element = DipRoot.getInstance().getElement(resource, parent, DipElementType.UNIT);
		if (element == null || element.parent() != parent) {
			DipUnit unit = new DipUnit(resource, parent);
			DipRoot.getInstance().putElement(unit);
			return unit;
		} else {
			return (DipUnit) element;
		}
	}
	
	protected DipUnit(IResource resource, IParent parent) {
		super(resource, parent);
		fDipUnitDescription = new UnitDescriptionPresentation(this);
		fUnitPresentation = new UnitPresentation(this);
		fMarks = IMarkable.markNumberSteam()
				.mapToObj(markNumber -> QualifiedNames.isMark(resource, markNumber))
				.toArray(Boolean[]::new);
	}

	@Override
	public DipElementType type() {
		return DipElementType.UNIT;
	}
	
	public UnitType getUnitType() {
		return fUnitPresentation.getUnitType();
	}
	
	@Override
	public IFile resource() {
		return (IFile) super.resource();
	}
	
	@Override
	public IDipParent parent() {
		return (IDipParent) super.parent();
	}

	@Override
	public UnitPresentation getUnitPresentation(){
		return fUnitPresentation;
	}
	
	@Override
	public List<UnitExtension> getUnionExtensions() {
		if (fUnitPresentation.getUnitType().isForm()) {
			return getFormExtensions();								
		}
		return 	List.of(getUnitPresentation(), getUnitDescription());	
	}
	
	private List<UnitExtension> getFormExtensions(){
		FormPresentation formPresentation = (FormPresentation) fUnitPresentation.getPresentation();
		List<UnitExtension> extensions = new ArrayList<>();
		extensions.addAll(formPresentation.getFormFields());
		extensions.add(getUnitDescription());						
		return extensions;	
	}
	
	//=======================
	// Comment
	
	@Override
	public void updateDipComment(String commentContent){
		DipComment dipComment = (DipComment) comment();
		if (dipComment == null){
			if (commentContent != null && !commentContent.trim().isEmpty()){
				setDipComment(DipComment.createNewDipComment(this, commentContent));
			} 			
		} else {
			if (commentContent != null && !commentContent.trim().isEmpty()){
				dipComment.updateCommentText(commentContent);
			} else {
				dipComment.deleteMainContent();
				if (dipComment.isEmpty()) {				
					deleteDipComment();
				}
			}
		}
	}
	
	@Override
	public void deleteDipComment(){
		DipComment dipComment = (DipComment) comment();
		if (dipComment != null){
			dipComment.delete();
			setDipComment(null);
		}
	}
	
	@Override
	public void updateTextAnnotations(List<ITextComment> textComments) {
		DipComment dipComment = (DipComment) comment();
		if (dipComment == null){
			if (textComments != null && !textComments.isEmpty()){
				setDipComment(DipComment.createNewDipComment(this, textComments));
			} 			
		} else {
			dipComment.updateTextComments(textComments);
		}
	}
	
		
	//========================
	// Description
	
	public UnitDescriptionPresentation getUnitDescription(){
		return fDipUnitDescription;
	}
	
	@Override
	public void updateDescription(String descriptionContent){
		DipDescription description = dipDescription();
		if (description == null){
			setDipDescription(DipDescription.createNewDipDescription(this, descriptionContent));
		} else {
			description.updateDescriptionText(descriptionContent);
		}
	}
	
	@Override
	public String description() {
		DipDescription dipDescription = dipDescription();
		if (dipDescription != null){
			String description = dipDescription.getDescriptionContent();
			if (description != null && !description.isEmpty()){
				return description;
			}
		}		
		return super.description();
	}
	
	@Override
	public void removeDescription(){
		DipDescription description = dipDescription();
		if (description != null) {
			description.delete();
		}
		setDipDescription(null);		
	}

	/**
	 * Возвращает если есть не пустое описание,
	 * либо является таблицей или рисунком
	 */
	public boolean hasDescription() {
		String desc = description();
		if (desc != null && !desc.isEmpty()) {
			return true;
		}
		if (fNumber != null && getUnitType().isNumerated()){
			return true;
		}		
		return false;
	}
	
	//=========================
	// numbering (table/images)
	
	@Override
	public String getNumer() {
		if (fNumber == null) {
			dipProject().updateNumeration();
		}
		return fNumber;
	}
	
	public void setNumber(String number) {
		fNumber = number;
	}
	
	//===========================
	// orientation
	
	public boolean isHorizontalOrientation() {
		return fHorizontalOrientation;
	}
	
	public void setHorizontalOrientation(boolean value) {
		fHorizontalOrientation = value;
	}

	@Override
	public IDipDocumentElement strong() {
		return this;
	}

	@Override
	public boolean isMark(int number) {
		return fMarks[number];
	}

	@Override
	public void setMark(int number, boolean value) {
		QualifiedNames.setMark(resource(), number, value);
		fMarks[number] = value;
	}

	@Override
	public void dispose() {
		if (fDipUnitDescription != null) {
			fDipUnitDescription.dispose();
		}
		if (fUnitPresentation != null) {
			fUnitPresentation.dispose();
		}
	}

}
