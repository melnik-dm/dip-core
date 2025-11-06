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
package ru.dip.core.form.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedPosition;

import ru.dip.core.form.partitioner.DocumentPartitioner;
import ru.dip.core.form.partitioner.IPartitions;
import ru.dip.core.utilities.TagStringUtilities;

public class CorePositionModel {

	public static final String MAIN_TAG = "form";
	
	protected CoreFormModel fElementModel;
	protected IDocument fDocument;	
	protected DocumentPartitioner fPartitioner;
	private Position[] fPositions;  
	private List<Tag> fTags = new ArrayList<>();
	private int fCurrentPosition;
	
	public CorePositionModel() {
	}
	
	public CorePositionModel(CoreFormModel elementModel, DocumentPartitioner partitioner, IDocument document){
		fElementModel = elementModel;
		fPartitioner = partitioner;
		fDocument = document;
	}	
	
	public void createModel(){		
		fTags.clear();
		try {
			fPositions =fPartitioner.getDocPositions();
		} catch (BadPositionCategoryException e) {
			return;
		}
		for (fCurrentPosition = 0; fCurrentPosition < fPositions.length; fCurrentPosition++){
			String typePosition = ((TypedPosition)fPositions[fCurrentPosition]).getType();	
			if (IPartitions.TAG.equals(typePosition)){
				try {
					Tag tag = createFullTag();
					if (tag != null){
						fTags.add(tag);
					}
				} catch (BadLocationException e) {
					// NOP
				}
			}
		}
		if (hasMainTag()){
			fElementModel.createModel(fTags.get(0).getChildren());
		} else {
			fElementModel.createModel(fTags);
		}		
	}
	
	private Tag createFullTag() throws BadLocationException {
		Position startTagPosition = fPositions[fCurrentPosition];
		String tagDeclaration = fDocument.get(startTagPosition.offset, startTagPosition.length);
		String field = TagStringUtilities.getNameFromStartTag(tagDeclaration);			
		fCurrentPosition++;
		List<Tag> children = new ArrayList<>();
		while (fCurrentPosition < fPositions.length){
			Position endTagPosition = fPositions[fCurrentPosition];
			String type = ((TypedPosition) endTagPosition).getType();
			if(IPartitions.ENDTAG.equals(type)){
				String endTagDeclaration = fDocument.get(endTagPosition.offset, endTagPosition.length);
				String endName = TagStringUtilities.getNameFromEndTag(endTagDeclaration);
				if (field != null && field.equals(endName)){
					// создаем тег
					if (field != null){
						Tag newTag = new Tag(field, startTagPosition, endTagPosition, fDocument);
						//fTags.add(newTag);
						if (newTag != null && !children.isEmpty()){
							newTag.addChildren(children);
						}
						return newTag;
					}					
				}
			} else if (IPartitions.TAG.equals(type)){
				Tag tag = createFullTag();
				if (tag != null){
					children.add(tag);
				}
				
			}
			fCurrentPosition++;
		}	
		return null;
	}
	
	public void createTagAfterTag(Tag previousTag, String name, String value){
		if (value == null) {
			value = "";
		}
		int offset = 0; 
		if (previousTag != null){
			offset = previousTag.getEndOffset();
		} else {
			if (hasMainTag()){
				Tag mainTag = getMaingTag();
				offset = mainTag.getEndOfStartTag();
			} else {
				createMainTagWithContent(name, value);
				return;
			}
		}
		StringBuilder builder = new StringBuilder();
		if (offset != 0){
			builder.append("\n");
		}
		builder.append("<");
		builder.append(name);
		builder.append(">");
		builder.append("\n");
		builder.append(value);		
		builder.append("\n");
		builder.append("</");
		builder.append(name);
		builder.append(">");
		try {
			fDocument.replace(offset, 0, builder.toString());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		createModel();
	}
	
	
	public void createMainTag(){
		if (!hasMainTag()){
			if (fTags.isEmpty()){
				try {
					fDocument.replace(fDocument.getLength(), 0, "<form>\n</form>");
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			} else {
				try {
					Tag firstTag = fTags.get(0);
					Tag endTag = fTags.get(fTags.size() - 1);
					fDocument.replace(endTag.getEndOffset(), 0, "\n</form>");					
					fDocument.replace(firstTag.getOffset(), 0, "<form>\n");
				} catch (BadLocationException e) {
					e.printStackTrace();
				}			
			}
		}
		createModel();
	}
	
	private void createMainTagWithContent(String name, String value){
		StringBuilder builder = new StringBuilder();
		builder.append("<form>\n");
		builder.append("<");
		builder.append(name);
		builder.append(">");
		builder.append("\n");
		builder.append(value);		
		builder.append("\n");
		builder.append("</");
		builder.append(name);
		builder.append(">");
		builder.append("\n</form>");
		
		if (fTags.isEmpty()){
			try {
				fDocument.replace(fDocument.getLength(), 0, builder.toString());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean hasMainTag(){
		return !fTags.isEmpty() && MAIN_TAG.equals(fTags.get(0).getName());
	}
	
	public Tag getMaingTag(){
		return fTags.get(0);
	}
	
	public Tag findTagByOffset(int offset, int endOffset){
		if (!hasMainTag()) {
			return null;
		}
		for (Tag tag: getMaingTag().getChildren()){
			if (tag.getOffset() <= offset && tag.getEndOffset() >= endOffset){
				return tag;
			}
		}		
		return null;
	}
	
	public void setFormModel(CoreFormModel model){
		fElementModel = model;
	}
	
	public void setDocument(IDocument doucment){
		fDocument = doucment;
	}
	
	public void setDocumentPartitionet(DocumentPartitioner partitioner){
		fPartitioner = partitioner;
	}
	
}
