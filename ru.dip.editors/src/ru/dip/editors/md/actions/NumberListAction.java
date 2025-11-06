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
package ru.dip.editors.md.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.utilities.md.MarkdownParagraphParser;
import ru.dip.core.utilities.md.MarkdownParagraphParser.MdStyledPosition;
import ru.dip.core.utilities.md.MarkdownParagraphParser.Type;
import ru.dip.editors.Messages;
import ru.dip.editors.md.MarkdownDocument;
import ru.dip.editors.md.partitions.PartitionStyles;
import ru.dip.ui.utilities.image.ImageProvider;

public class NumberListAction extends Action {
	
	public static final String ID = Messages.NumberListAction_ID;
	
	private static NumberListAction instance = new NumberListAction();
	
	public static NumberListAction instance() {
		return instance;
	}
	
	protected MarkdownDocument fMdDocument;
	
	private int fCaret;
	private Point fSelection;
	private List<ITypedRegion> fPartitions;
	private boolean fOnePartition;
	
	protected NumberListAction() {
		setText(Messages.NumberListAction_Name);
		setId(ID);	
		setChecked(true);
		setImageDescriptor(ImageProvider.NUMBER_LIST_DESCRIPTOR);
	}
	
	//========================
	// selection
	
	public void updateEmptySelection(MarkdownDocument mdDocument, int caret) {
		fCaret = caret;
		fMdDocument = mdDocument;
		String partitionType = mdDocument.partitionType(caret);
		setChecked(partitionType);
	}
	
	protected void setChecked(String partitionType) {
		if (isListItem(partitionType)) {
			setChecked(true);
			setEnabled(true);
		} else if (isCanListPartition(partitionType)) {
			setChecked(false);
			setEnabled(true);
		} else {
			setChecked(false);
			setEnabled(false);
		}
	}
	
	protected boolean isListItem(String type) {
		return PartitionStyles.NUMBER_LIST_ITEM.equals(type);
	}
	
	private boolean isCanListPartition(String type) {
		return PartitionStyles.EMPTY_LINE.equals(type) 
		|| PartitionStyles.PARAGRAPH.equals(type);
	}
	
	public void updateFullSelection(MarkdownDocument mdDocument, Point selection) {
		fMdDocument = mdDocument;
		fSelection = selection;
		fOnePartition = fMdDocument.isSelectOnePartition(selection);
		if (fOnePartition) {
			String partitionType = fMdDocument.partitionType(selection.x);
			setChecked(partitionType);			
		} else {
			fPartitions = fMdDocument.getPartitions(selection);			
			int list = 0;
			int unlist = 0;						
			for (ITypedRegion partition: fPartitions) {
				String type = partition.getType();
				if (PartitionStyles.PARAGRAPH.equals(type)) {
					list++;
				} else if (isListItem(type)) {
					unlist++;
				}
			}
			if (list > 0) {
				setChecked(false);
				setEnabled(true);
			} else if (unlist > 0) {
				setChecked(true);
				setEnabled(true);
			} else {
				setChecked(false);
				setEnabled(false);
			}
		}
	}

	//========================
	// run
	
	@Override
	public void run() {
		if (fMdDocument.hasSelection()) {
			if (isChecked()) {
				doListSelection();
			} else {
				doUnlistSelection();
			}			
		} else {
			if (isChecked()) {
				doList();
			} else {
				doUnlist();
			}		
		}
		fMdDocument.fireMdDocumentUdpated();
	}
	
	private void doListSelection() {
		if (fMdDocument.isSelectOnePartition(fSelection)) {
			try {
				ITypedRegion region = fMdDocument.document().getPartition(fSelection.x);
				doListPartition(region, false);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else {
			boolean nextPartition = false; // если дальше следует параграф, то вставляется \n
			ArrayList<String> newContents = new ArrayList<>();
			ArrayList<ITypedRegion> changedRegions = new ArrayList<>();
			
			for (int i = fPartitions.size() - 1; i >= 0; i--) { 
				ITypedRegion region =  fPartitions.get(i);
				if (region.getType().equals(PartitionStyles.PARAGRAPH)) {
					String newContent = listPartitionContent(region, nextPartition);
					changedRegions.add(0, region);
					newContents.add(0, newContent);
					nextPartition = true;
				} else {
					nextPartition = false;
				}
			}
			
			if (changedRegions.isEmpty()) {
				return;
			}
					
			int start = changedRegions.get(0).getOffset();
			ITypedRegion lastRegion = changedRegions.get(changedRegions.size() - 1);
			int end = lastRegion.getOffset() + lastRegion.getLength();
			String oldContent = fMdDocument.textWidget().getText(start, end - 1);
			
			StringBuilder builder = new StringBuilder(oldContent);
			for (int i = changedRegions.size() - 1; i >= 0; i--) {
				ITypedRegion region = changedRegions.get(i);
				int regionStart = region.getOffset() - start;				
				builder.replace(regionStart, regionStart + region.getLength(), newContents.get(i));
			}
						
			fMdDocument.textWidget().replaceTextRange(start, end - start, builder.toString());
		}
	}
	
	private void doUnlistSelection() {
		if (fMdDocument.isSelectOnePartition(fSelection)) {
			try {
				ITypedRegion region = fMdDocument.document().getPartition(fSelection.x);
				doUnlistPartition(region);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else {
			
			List<ITypedRegion> listPartitions = fPartitions.stream()
			.filter(region -> PartitionStyles.isList(region.getType()))
			.collect(Collectors.toList());
			
			if (listPartitions.isEmpty()) {
				return;
			}
			// последний пункт
			ITypedRegion lastRegion =  listPartitions.get(listPartitions.size() - 1);	
			try {			
				doUnlistPartition(lastRegion);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}	
			// остальные пункты (добавляем перенос на другую строку)
			for (int i = listPartitions.size() - 2; i >= 0; i--) { 
				ITypedRegion region =  listPartitions.get(i);	
				try {
					fMdDocument.document().replace(region.getOffset() + region.getLength(), 0, "\n"); //$NON-NLS-1$
					doUnlistPartition(region);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}								
			}
		}			
	}
	
	private void doList() {
		try {
			ITypedRegion region = fMdDocument.document().getPartition(fCaret);
			doListPartition(region, false);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}			
	}
	
	private void doUnlist() {
		try {
			ITypedRegion region = fMdDocument.document().getPartition(fCaret);
			doUnlistPartition(region);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}			
	}
	
	/**
	 * deleteEmptyLines - удалять ли пустые линии в конце Partition
	 */
	protected void doListPartition(ITypedRegion region, boolean deleteEmptyLines) {	
		if (!deleteEmptyLines) {
			fMdDocument.textWidget().replaceTextRange(region.getOffset(), 0, getMarker());
		} else {
			String text = fMdDocument.textWidget().getTextRange(region.getOffset(), region.getLength());
			text = getMarker() + text.trim();
			fMdDocument.textWidget().replaceTextRange(region.getOffset(), region.getLength(), text);
		}
	}
	
	public String listPartitionContent(ITypedRegion region, boolean deleteEmptyLines) {
		if (!deleteEmptyLines) {
			String text = fMdDocument.textWidget().getTextRange(region.getOffset(), region.getLength());
			return getMarker() + text;
		} else {
			String text = fMdDocument.textWidget().getTextRange(region.getOffset(), region.getLength());
			return getMarker() + text.trim();
		}
	}
	
	protected String getMarker() {
		return "1. "; //$NON-NLS-1$
	}
	
	protected void doUnlistPartition(ITypedRegion region) throws BadLocationException {
		String oldContent =  fMdDocument.document().get(region.getOffset(), region.getLength());	
		List<MdStyledPosition>  positions = MarkdownParagraphParser.getListItemPositions(oldContent);
		MdStyledPosition markerPosition = null;
		for (int i = 0; i < positions.size(); i++) {
			MdStyledPosition pos = positions.get(i);
			if (pos.type() == Type.LIST_MARKER) {
				markerPosition = pos;
				if (i + 1 < positions.size()) {
					pos = positions.get(i +1);
					if (pos.type() == Type.EMPTY) {
						markerPosition = pos;
					}
				}				
				break;
			}
		}
		if (markerPosition == null) {
			return;
		}
		int length = markerPosition.offset() + markerPosition.length();
		fMdDocument.document().replace(region.getOffset(), length, ""); //$NON-NLS-1$
	}

	public void setMdDocument(MarkdownDocument mdDocument) {
		fMdDocument = mdDocument;
	}

}
