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

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.custom.StyledText;

import ru.dip.editors.md.IMdEditor;
import ru.dip.ui.ReqUIPlugin;

public class TextTransferExecutor {

	private IMdEditor fEditor;;
	private IDocument fDocument;
	private TextViewer fViewer;	
	private StyledText fTextWidget;
	
	private int fReplaceOffset;
	private int fReplaceLength = 1;
	private String fReplaceText = "\n"; //$NON-NLS-1$
	private int fMaxLineLength;
	
	public TextTransferExecutor(IMdEditor editor) {
		fEditor = editor;
	}

	public boolean transfer(DocumentEvent event) {		
		fViewer = fEditor.getMDViewer();
		fDocument = fEditor.document();
		fTextWidget = fEditor.styledText();
		
		try {
			int line = fDocument.getLineOfOffset(event.fOffset);
			IRegion region = fDocument.getLineInformation(line);
			fMaxLineLength = ReqUIPlugin.getMarkdownMaxLine();
			if (region.getLength() > fMaxLineLength) {
				return doTransfer(region, line, event);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private boolean doTransfer(IRegion region, int line, DocumentEvent event) throws BadLocationException {
		fViewer.getRewriteTarget().beginCompoundChange();

		fReplaceOffset = getWrapOffset(region, fMaxLineLength);
		fReplaceLength = 1;
		fReplaceText = "\n"; //$NON-NLS-1$

		int caretPosition = fTextWidget.getCaretOffset();
		ITypedRegion currentPartition = fDocument.getPartition(caretPosition);

		// проверка, если выполняется Undo, тогда выходим
		String changedText = fTextWidget.getText(fReplaceOffset, fReplaceOffset);
		if (changedText.equals("\n")) { //$NON-NLS-1$
			return false;
		}

		// определяем переносимый текст
		int currentLine = line;	
		String currentLineContent = getLineContent(currentLine);

		
		int currentLineWrapOffset = getWrapOffset(currentLineContent, fMaxLineLength);
		String currentWrappingText = currentLineContent.substring(getWrapOffset(currentLineContent, fMaxLineLength) + 1)
				+ " "; //$NON-NLS-1$

		// если переносимая часть больше MaxLine (например когда удаляем переход на другую строку)
		while (currentWrappingText.length() > fMaxLineLength) {
			int offset = getWrapOffset(currentWrappingText, fMaxLineLength);
			String startCurrentLineContent = currentWrappingText.substring(0, offset);
			fReplaceText += startCurrentLineContent + "\n"; //$NON-NLS-1$
			fReplaceLength += startCurrentLineContent.length() + 1;
			currentWrappingText = currentWrappingText.substring(offset + 1);
		}

		int nextLine = currentLine + 1;
		
		// принадлежит ли следующая строчка текущему параграфу (Partition)
		boolean isOnePart = false;
		if (nextLine <= fDocument.getNumberOfLines() - 1) {
			ITypedRegion nextPartition = fDocument.getPartition(fDocument.getLineOffset(nextLine));
			if (nextPartition.equals(currentPartition)) {
				isOnePart = true;
			}
		}

		// проверяем нужно ли переносить следующие строчки
		while (isOnePart && nextLine <= fDocument.getNumberOfLines() - 1) {

			fReplaceText += currentWrappingText;
			fReplaceLength += currentWrappingText.length();

			currentLine++;
			currentLineContent = getLineContent(currentLine);
			
			// если переносимая часть больше 80 (например когда удаляем переход на другую
			// строку)
			while (currentWrappingText.length() > fMaxLineLength) {
				int offset = getWrapOffset(currentWrappingText, fMaxLineLength);
				String startCurrentLineContent = currentWrappingText.substring(0, offset);
				fReplaceText += startCurrentLineContent + "\n"; //$NON-NLS-1$
				fReplaceLength += startCurrentLineContent.length() + 1;
				currentWrappingText = currentWrappingText.substring(offset + 1);
			}

			// определям допустимую длину строки, с учетом того что уже перенесли на эту строку с предыдущей
			int maxLine = fMaxLineLength - currentWrappingText.length();
			if (maxLine < 0) {
				maxLine = fMaxLineLength;
			}

			// если есть необходимость переносим
			if (currentLineContent.length() >= maxLine) {
				currentLineWrapOffset = getWrapOffset(currentLineContent, maxLine);
				String startCurrentLineContent = currentLineContent.substring(0, currentLineWrapOffset);
				fReplaceText += startCurrentLineContent + "\n"; //$NON-NLS-1$
				fReplaceLength += startCurrentLineContent.length() + 1;
				currentWrappingText = currentLineContent.substring(getWrapOffset(currentLineContent, maxLine) + 1)
						+ " "; //$NON-NLS-1$
			} else {
				break;
			}

			nextLine++;
			ITypedRegion nextPartition = fDocument.getPartition(fDocument.getLineOffset(nextLine));
			if (!nextPartition.equals(currentPartition)) {
				isOnePart = false;
			}
		}

		doReplace(caretPosition + event.fText.length());
		return true;
	}
		
	private int getWrapOffset(IRegion region, int maxLine) throws BadLocationException {
		String lineContent = fDocument.get(region.getOffset(), region.getLength());
		int wrapOffset = getWrapOffset(lineContent, maxLine);
		int regionWrapOffset = wrapOffset + region.getOffset();
		return regionWrapOffset;
	}

	private int getWrapOffset(String content, int maxLine) throws BadLocationException {
		char[] lineChars = content.toCharArray();
		int wrapOffset = maxLine - 1;
		for (int i = maxLine - 1; i >= 0; i--) {
			if (Character.isWhitespace(lineChars[i])) {
				wrapOffset = i;
				break;
			}
		}
		return wrapOffset;
	}

	private String getLineContent(int line) throws BadLocationException {
		IRegion currentRegion = fDocument.getLineInformation(line);
		return fTextWidget.getText(currentRegion.getOffset(),
				currentRegion.getOffset() + currentRegion.getLength() - 1);
	}
	
	private void doReplace(int cursorPosition) {
		fTextWidget.getShell().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				fTextWidget.replaceTextRange(fReplaceOffset, fReplaceLength, fReplaceText);
				fViewer.getRewriteTarget().endCompoundChange();
				fTextWidget.setCaretOffset(cursorPosition);
			}
		});
	}
	
}
