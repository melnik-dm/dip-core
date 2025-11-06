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
package ru.dip.core.unit.md;

public class MarkdownSettings {

	private static MarkdownSettings instance;
	private static MarkdownSettings findModeInstance;
	
	public static MarkdownSettings instance(boolean fontStylesEnable, boolean emptyLines, boolean listEmptyLines,
			boolean indentEnable, int indent, boolean showComment) {
		if (instance == null) {
			instance = new MarkdownSettings(fontStylesEnable, emptyLines, listEmptyLines, indentEnable, indent, showComment);
		} else {
			instance.setFontStylesEnable(fontStylesEnable);
			instance.setEmptyLines(emptyLines);
			instance.setListEmptyLines(listEmptyLines);
			instance.setIndentEnable(indentEnable);
			instance.setIndent(indent);
			instance.setShowComment(showComment);
		}
		return instance;
	}
	
	public static MarkdownSettings instanceForFastSearch() {
		if (findModeInstance == null) {
			findModeInstance = new MarkdownSettings(false, false, false, false, 0, true);
		}
		return findModeInstance;
	}
	
	private boolean fFontStylesEnable;
	private boolean fEmptyLines;
	private boolean fListEmptyLines;
	private boolean fIndentEnable;
	private boolean fShowComment;
	private int fIndent;
	
	private MarkdownSettings(boolean fontStylesEnable, boolean emptyLines, boolean listEmptyLines,
			boolean indentEnable, int indent, boolean showComment) {
		fFontStylesEnable = fontStylesEnable;
		fEmptyLines = emptyLines;
		fListEmptyLines = listEmptyLines;
		fIndentEnable = indentEnable;
		fIndent = indent;
		fShowComment = showComment;
	}

	public boolean fontStylesEnable() {
		return fFontStylesEnable;
	}

	public boolean emptyLines() {
		return fEmptyLines;
	}

	public boolean listEmptyLines() {
		return fListEmptyLines;
	}

	public boolean indentEnable() {
		return fIndentEnable;
	}

	public int indent() {
		return fIndent;
	}
	
	public boolean showComment() {
		return fShowComment;
	}
	
	public void setFontStylesEnable(boolean fontStylesEnable) {
		fFontStylesEnable = fontStylesEnable;
	}

	public void setEmptyLines(boolean emptyLines) {
		fEmptyLines = emptyLines;
	}

	public void setListEmptyLines(boolean listEmptyLines) {
		fListEmptyLines = listEmptyLines;
	}

	public void setIndentEnable(boolean indentEnable) {
		fIndentEnable = indentEnable;
	}

	public void setIndent(int indent) {
		fIndent = indent;
	}
	
	public void setShowComment(boolean showComment) {
		fShowComment = showComment;
	}

}
