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
package ru.dip.core.report.model.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;

import ru.dip.core.form.partitioner.DocumentPartitioner;
import ru.dip.core.form.partitioner.IPartitions;
import ru.dip.core.utilities.xml.EmptyTag;
import ru.dip.core.utilities.xml.EndTag;
import ru.dip.core.utilities.xml.FullTag;
import ru.dip.core.utilities.xml.Tag;
import ru.dip.core.utilities.xml.XmlStringUtilities;
import ru.dip.core.utilities.xml.XmlTagUtilities;

public class RulesModel {
	
	private Report fReport;
	private DocumentPartitioner fPartitioner;
	private IDocument fDocument;
	private Position[] fPositions;  
	
	protected List<Tag> fRoots = new ArrayList<>();   // корневой тег, лист сделан на случай ошибки, если юудет несколько корневых тегов
	protected HashMap<ProjectionAnnotation, Position> fAnnotations = new HashMap<ProjectionAnnotation, Position>(); 
	private int fCurrentPosition;

	
	public RulesModel(IDocument document) {
		fDocument = document;
		fPartitioner = (DocumentPartitioner) fDocument.getDocumentPartitioner();
	}
	
	/**
	 * Метод создает модель 
	 */	
	public void createModel(){	
		createTagModel();
		createReportModel();
	}
	
	private void createTagModel(){
		fRoots = new ArrayList<>();   
		fAnnotations = new HashMap<ProjectionAnnotation, Position>();

		try {
			fPositions = fPartitioner.getDocPositions();
		} catch (BadPositionCategoryException e) {
			return;
		}
				
		// сама модель
		for (fCurrentPosition = 0; fCurrentPosition < fPositions.length; fCurrentPosition++){			
			String typePosition = ((TypedPosition)fPositions[fCurrentPosition]).getType();	
			// если тег, создаем и клаедм в лист и идем дальше
			if (IPartitions.TAG.equals(typePosition)){
				FullTag tag = createFullTag((TypedPosition)fPositions[fCurrentPosition],null);
				if (tag!=null)
					fRoots.add(tag);
				continue;
			}			
			// если пустой тег - создаем и кладем в лист и идем дальше
			if (IPartitions.EMPTYTAG.equals(typePosition)){
				EmptyTag tag = createEmptyTag((TypedPosition)fPositions[fCurrentPosition],null);
				if (tag!=null)
					fRoots.add(tag);
				continue;
			}					
			// если неполный тег - создаем и кладем в лист и идем дальше
			if (IPartitions.INCOMPLETETAG.equals(typePosition)){
				continue;
			}						
			// если неполный тег - создаем и кладем в лист и идем дальше
			if (IPartitions.ENDTAG.equals(typePosition)){
				continue;
			}				
		}	
	}
	
	/**
	 *   метод создает FullTag, добавляет вложенные теги, пока не найдет конечный тег   
	 */
	private FullTag createFullTag(TypedPosition position, Tag parent){		
		try {			
			String content = fDocument.get(position.offset, position.length);
			String name = XmlStringUtilities.getName(content,false);
			if (name == null){
				fCurrentPosition++;
				return null;
			}
							   
			FullTag tag = new FullTag(name, (FullTag) parent);			
			fCurrentPosition++;
			
			// перебираем вложенные теги, пока не дойдем до енд тега
			for (;fCurrentPosition < fPositions.length; fCurrentPosition++){			
				String typePosition = ((TypedPosition)fPositions[fCurrentPosition]).getType();	
				// если пустой тег - создаем и кладем в лист children и идем дальше
				if (IPartitions.EMPTYTAG.equals(typePosition)){
					EmptyTag emptyTag = createEmptyTag((TypedPosition)fPositions[fCurrentPosition],tag);
					if (emptyTag!=null)
						tag.getChildren().add(emptyTag);
					continue;
				}															
				// если тег, создаем и клаедм в лист children и идем дальше
				if (IPartitions.TAG.equals(typePosition)){
					FullTag fullTag = createFullTag((TypedPosition)fPositions[fCurrentPosition], tag);
					if (fullTag!=null)
						tag.getChildren().add(fullTag);
					continue;
				}
				
				// если энд тег, создаем и клаедм в лист children и идем дальше
				if (IPartitions.ENDTAG.equals(typePosition)){
					try {
						String endContent = fDocument.get(fPositions[fCurrentPosition].offset, fPositions[fCurrentPosition].length);
						String endName = XmlStringUtilities.getName(endContent,true);
						
						if(endName == null){
							continue;						
						}
						// если эндтег совпадает с начлаьным (все ОК)	
						// если  иначе, то это будет ошибка в документе, надо бы обработать
						if (name.equals(endName)){  
							new EndTag(tag,name);												
							// кладем в карту с анотациями						 		
							Position positionAnnotation = new Position(position.getOffset(),fPositions[fCurrentPosition].getOffset() + fPositions[fCurrentPosition].getLength()	- position.getOffset());
							fAnnotations.put(new ProjectionAnnotation(), positionAnnotation);
							tag.setPositionDeclarationTag(position);
							tag.setAttributs(XmlTagUtilities.getAttributs(content));
							tag.setPosition(positionAnnotation);
							tag.setDocument(fDocument);
							tag.setPositionEndTag(fPositions[fCurrentPosition]);
							//tag.fPositionEndTag = fPositions[fCurrentPosition];
							return tag;
						}						
						// в случае если не было начального тега (в родительском элементе)
						if (parent == null) {
							continue;
						}
						// если получили конечный тег родительского документа
						// (ошибка в документе пропускаем этот элемент)
						if (endName.equals(parent.getName())) {
							fCurrentPosition--;
							return null;
						}

						fCurrentPosition++;
						return null;

					} catch (BadLocationException e) {
						e.printStackTrace();
					}
				}	
			}				
			return null;	// если не получили конечный тег																																
		} catch (BadLocationException e) {
				e.printStackTrace();
		}
		return null;
	}

	private EmptyTag createEmptyTag(TypedPosition position, Tag parent) {
		String content;
		try {
			content = fDocument.get(position.offset, position.length);
			String name = XmlStringUtilities.getName(content, false);
			if (name != null) {
				EmptyTag empTag = new EmptyTag(name, (FullTag) parent);
				empTag.setAttributs(XmlTagUtilities.getAttributs(content));
				empTag.setPosition(position);
				empTag.setDocument(fDocument);
				return empTag;
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	private void createReportModel(){
		if (fRoots.isEmpty()){
			return;
		}
		Tag reportTag = fRoots.get(0);
		fReport = new Report();
		fReport.createModel(reportTag);
	}
	
	public List<ReportEntry> getEntries(){
		if (fReport == null){
			return Collections.emptyList();
		}
		return fReport.getEntries();
	}
	
	public String getDescription(){
		if (fReport != null){
			return fReport.getDescription();
		}
		return null;
	}
	
}
