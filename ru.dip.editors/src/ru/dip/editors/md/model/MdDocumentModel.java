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
package ru.dip.editors.md.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.model.DipProject;
import ru.dip.editors.md.DocumentPartitioner;

public class MdDocumentModel {
	
	private IDocument fDocument;	
	private DocumentPartitioner fPartitioner;
	private Position[] fPositions; 
	private List<MDNode> fNodes = new ArrayList<>();
	
	private DipProject fProject;

	public MdDocumentModel(IDocument document, DipProject project) {
		fProject = project;
		fDocument = document;
		fPartitioner = (DocumentPartitioner) fDocument.getDocumentPartitioner();
	}
	
	public void createModel() {
		fNodes.clear();
		if (fPartitioner == null) {
			return;
		}	
		try {		
			fPositions =fPartitioner.getDocPositions();
			for (Position position: fPositions) {
				TypedPosition typedPosition = (TypedPosition) position;
				MDNode node = new MDNode(this, typedPosition);
				fNodes.add(node);								
			}					
		} catch (BadPositionCategoryException e) {
			return;
		}
	}
	
	public List<MDNode> getSelectedNodes(Point selection){
		List<MDNode> result = new ArrayList<>();
		for (MDNode node: fNodes) {
			if (node.offset() + node.length() < selection.x) {
				continue;
			}
			if (node.offset() > selection.y) {
				break;
			}
			if (node.children().isEmpty()) {
				result.add(node);
			} else {
				for (MDNode child: node.children()) {
					if (child.offset() + child.length() < selection.x) {
						continue;
					}
					if (child.offset() > selection.y) {
						break;
					}
					result.add(child);
				}
			}
		}
		return result;
	}
	
	public List<MDNode> getNotEmptySelectedNodes(Point selection){
		List<MDNode> result = new ArrayList<>();
		for (MDNode node: fNodes) {
			if (node.offset() + node.length() < selection.x) {
				continue;
			}
			if (node.offset() > selection.y) {
				break;
			}
			if (node.children().isEmpty()) {
				if (!node.isEmpty()) {				
					result.add(node);
				}
			} else {
				for (MDNode child: node.children()) {
					if (child.offset() + child.length() < selection.x) {
						continue;
					}
					if (child.offset() > selection.y) {
						break;
					}
					if (!child.isEmpty()) {				
						result.add(child);
					}
				}
			}
		}
		return result;
	}

	public MDNode findParentNode(int position) {
		for (MDNode node: fNodes) {
			if (node.hasPosition(position)) {				
				return node;
			}			
		}
		return null;
	}
	
	public MDNode findNode(Point p) {
		int current = p.x;
		MDNode node = findNode(current);
		while (node.isEmpty() && p.y > current) {
			current++;
			node = findNode(current);
		}
		return node;
	}
	
	public MDNode findNode(int position) {		
		for (MDNode node: fNodes) {
			if (node.hasPosition(position)) {				
				return node.findPosition(position);
			}			
		}
		return null;
	}
	
	public IDocument document() {
		return fDocument;
	}
	
	public DipProject dipProject() {
		return fProject;
	}
	
}
