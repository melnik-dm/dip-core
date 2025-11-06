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
package ru.dip.editors.md.partitions;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import ru.dip.core.utilities.md.MarkdownParagraphParser.MdStyledPosition;
import ru.dip.core.utilities.md.MarkdownParagraphParser.Type;
import ru.dip.ui.preferences.MdPreferences;

public class PartitionStyles {
	
	// глобальные токены
	public static final String PARAGRAPH = "__paragraph"; //$NON-NLS-1$
	public static final String COMMENT = "__comment"; //$NON-NLS-1$
	public static final String EMPTY_LINE = "__emptyline"; //$NON-NLS-1$
	public static final String NUMBER_LIST_ITEM = "__number_list_item"; //$NON-NLS-1$
	public static final String GRAPHIC_LIST_ITEM = "__graphic_list_item"; //$NON-NLS-1$
	public static final String CODE = "__code";	 //$NON-NLS-1$
	
	public static IToken TOKEN_PARAGRAPH = new Token(PARAGRAPH);
	public static IToken TOKEN_COMMENT = new Token(COMMENT);
	public static IToken TOKEN_EMPTY_LINE = new Token(EMPTY_LINE);
	public static IToken TOKEN_NUMBER_LIST_ITEM = new Token(NUMBER_LIST_ITEM);
	public static IToken TOKEN_GRAPHIC_LIST_ITEM = new Token(GRAPHIC_LIST_ITEM);
	public static IToken TOKEN_CODE = new Token(CODE);
	
	// токены внутри параграфа
	public static  final IToken BOLD_TOKEN = new Token(new TextAttribute(null, null, SWT.BOLD));
	public static  final IToken ITALIC_TOKEN = new Token(new TextAttribute(null, null, SWT.ITALIC));
	public static  final IToken BOLD_ITALIC_TOKEN = new Token(new TextAttribute(null, null, SWT.BOLD | SWT.ITALIC));
	public static  final IToken TEXT_TOKEN = new Token(new TextAttribute(null));
	
	public static  IToken LINK_BOLD_TOKEN;
	public static  IToken LINK_ITALIC_TOKEN;
	public static  IToken LINK_BOLD_ITALIC_TOKEN;
	public static  IToken LINK_TEXT_TOKEN;
	public static  IToken COMMENT_BOLD_TOKEN;
	public static  IToken COMMENT_ITALIC_TOKEN;
	public static  IToken COMMENT_BOLD_ITALIC_TOKEN;
	public static  IToken COMMENT_TEXT_TOKEN;
	
	public static  IToken GLOSS_BOLD_TOKEN;
	public static  IToken GLOSS_ITALIC_TOKEN;
	public static  IToken GLOSS_BOLD_ITALIC_TOKEN;
	public static  IToken GLOSS_TEXT_TOKEN;
	
	public static  IToken VARIABLE_BOLD_TOKEN;
	public static  IToken VARIABLE_ITALIC_TOKEN;
	public static  IToken VARIABLE_BOLD_ITALIC_TOKEN;
	public static  IToken VARIABLE_TEXT_TOKEN;
	
	public static  IToken CODE_BOLD_TOKEN;
	public static  IToken CODE_ITALIC_TOKEN;
	public static  IToken CODE_BOLD_ITALIC_TOKEN;
	public static  IToken CODE_TEXT_TOKEN;
	public static  IToken LIST_MARKER_TOKEN;

	static {
		updateTokens();
	}
	
	public static void updateTokens() {
		updateLinkToken();
		updateCommentToken();	
		updateGlossToken();
		updateVariablesToken();
		updateCodeToken();
		LIST_MARKER_TOKEN = new Token(MdPreferences.listPreferences().textAttribute());
	}
	
	private static void updateLinkToken() {
		TextAttribute linkAttribute = MdPreferences.linkPreferences().textAttribute();
		Color linkColor = linkAttribute.getForeground();
		int linkStyle = linkAttribute.getStyle();	
		LINK_BOLD_TOKEN = new Token(new TextAttribute(linkColor, null, linkStyle | SWT.BOLD));
		LINK_ITALIC_TOKEN = new Token(new TextAttribute(linkColor, null, linkStyle |SWT.ITALIC));
		LINK_BOLD_ITALIC_TOKEN = new Token(new TextAttribute(linkColor, null, SWT.BOLD | SWT.ITALIC));
		LINK_TEXT_TOKEN = new Token(linkAttribute);	
	}
	
	private static void updateCommentToken() {
		TextAttribute commentAttribute = MdPreferences.commentPreferences().textAttribute();
		Color commentColor = commentAttribute.getForeground();
		int commentStyle = commentAttribute.getStyle();
		COMMENT_BOLD_TOKEN = new Token(new TextAttribute(commentColor, null, commentStyle | SWT.BOLD));
		COMMENT_ITALIC_TOKEN = new Token(new TextAttribute(commentColor, null, commentStyle | SWT.ITALIC));
		COMMENT_BOLD_ITALIC_TOKEN = new Token(new TextAttribute(commentColor, null, SWT.BOLD | SWT.ITALIC));
		COMMENT_TEXT_TOKEN = new Token(commentAttribute);
	}
	
	private static void updateGlossToken() {
		TextAttribute glossAttribute = MdPreferences.glossPreferences().textAttribute();
		Color glossColor = glossAttribute.getForeground();
		int glossStyle = glossAttribute.getStyle();
		GLOSS_BOLD_TOKEN = new Token(new TextAttribute(glossColor, null, glossStyle | SWT.BOLD));
		GLOSS_ITALIC_TOKEN = new Token(new TextAttribute(glossColor, null, glossStyle | SWT.ITALIC));
		GLOSS_BOLD_ITALIC_TOKEN = new Token(new TextAttribute(glossColor, null, SWT.BOLD | SWT.ITALIC));
		GLOSS_TEXT_TOKEN = new Token(glossAttribute);
	}
	
	private static void updateVariablesToken() {
		TextAttribute varAttribute = MdPreferences.varPreferences().textAttribute();
		Color varColor = varAttribute.getForeground();
		int varStyle = varAttribute.getStyle();
		VARIABLE_BOLD_TOKEN = new Token(new TextAttribute(varColor, null, varStyle | SWT.BOLD));
		VARIABLE_ITALIC_TOKEN = new Token(new TextAttribute(varColor, null, varStyle | SWT.ITALIC));
		VARIABLE_BOLD_ITALIC_TOKEN = new Token(new TextAttribute(varColor, null, SWT.BOLD | SWT.ITALIC));
		VARIABLE_TEXT_TOKEN = new Token(varAttribute);
	}
	
	private static void updateCodeToken() {
		TextAttribute codeAttribute = MdPreferences.codePreferences().textAttribute();
		Color glossColor = codeAttribute.getForeground();
		int glossStyle = codeAttribute.getStyle();
		CODE_BOLD_TOKEN = new Token(new TextAttribute(glossColor, null, glossStyle | SWT.BOLD));
		CODE_ITALIC_TOKEN = new Token(new TextAttribute(glossColor, null, glossStyle | SWT.ITALIC));
		CODE_BOLD_ITALIC_TOKEN = new Token(new TextAttribute(glossColor, null, SWT.BOLD | SWT.ITALIC));
		CODE_TEXT_TOKEN = new Token(codeAttribute);
	}
	
	public static IToken getToken(MdStyledPosition position) {
		if (position != null) {
			int style = position.style();
			if (position.type() == Type.LINK) {
				return getLinkToken (style);
			}			
			if (position.type() == Type.LIST_MARKER) {
				return LIST_MARKER_TOKEN;
			}
			if (position.type() == Type.GLOSSARY_WORD) {
				return getGlossToken(style);
			}
			if (position.type() == Type.VARIABLE) {
				return getVariableToken(style);
			}			
			if (position.type() == Type.COMMENT) {
				return getCommentToken(style);
			}
			if (position.type() == Type.CODE) {
				return getCodeToken(style);
			}
			
			return getTextToken(style);
		}
		return Token.EOF;		
	}
	
	private static IToken getLinkToken(int style) {
		if ((style & SWT.BOLD) == SWT.BOLD && (style & SWT.ITALIC) == SWT.ITALIC) {
			return LINK_BOLD_ITALIC_TOKEN;
		} else if((style & SWT.BOLD) == SWT.BOLD) {
			return LINK_BOLD_TOKEN;
		} else if ((style & SWT.ITALIC) == SWT.ITALIC) {
			return LINK_ITALIC_TOKEN;
		}
		return LINK_TEXT_TOKEN;
	}
	
	private static IToken getCommentToken(int style) {
		if ((style & SWT.BOLD) == SWT.BOLD && (style & SWT.ITALIC) == SWT.ITALIC) {
			return COMMENT_BOLD_ITALIC_TOKEN;
		} else if((style & SWT.BOLD) == SWT.BOLD) {
			return COMMENT_BOLD_TOKEN;
		} else if ((style & SWT.ITALIC) == SWT.ITALIC) {
			return COMMENT_ITALIC_TOKEN;
		}
		return COMMENT_TEXT_TOKEN;
	}
	
	private static IToken getGlossToken(int style) {
		if ((style & SWT.BOLD) == SWT.BOLD && (style & SWT.ITALIC) == SWT.ITALIC) {
			return GLOSS_BOLD_ITALIC_TOKEN;
		} else if((style & SWT.BOLD) == SWT.BOLD) {
			return GLOSS_BOLD_TOKEN;
		} else if ((style & SWT.ITALIC) == SWT.ITALIC) {
			return GLOSS_ITALIC_TOKEN;
		}
		return GLOSS_TEXT_TOKEN;
	}
	
	private static IToken getVariableToken(int style) {
		if ((style & SWT.BOLD) == SWT.BOLD && (style & SWT.ITALIC) == SWT.ITALIC) {
			return VARIABLE_BOLD_ITALIC_TOKEN;
		} else if((style & SWT.BOLD) == SWT.BOLD) {
			return VARIABLE_BOLD_TOKEN;
		} else if ((style & SWT.ITALIC) == SWT.ITALIC) {
			return VARIABLE_ITALIC_TOKEN;
		}
		return VARIABLE_TEXT_TOKEN;
	}
	
	private static IToken getCodeToken(int style) {
		if ((style & SWT.BOLD) == SWT.BOLD && (style & SWT.ITALIC) == SWT.ITALIC) {
			return CODE_BOLD_ITALIC_TOKEN;
		} else if((style & SWT.BOLD) == SWT.BOLD) {
			return CODE_BOLD_TOKEN;
		} else if ((style & SWT.ITALIC) == SWT.ITALIC) {
			return CODE_ITALIC_TOKEN;
		}
		return CODE_TEXT_TOKEN;
	}
	
	private static IToken getTextToken(int style) {
		if ((style & SWT.BOLD) == SWT.BOLD && (style & SWT.ITALIC) == SWT.ITALIC) {
			return BOLD_ITALIC_TOKEN;
		} else if((style & SWT.BOLD) == SWT.BOLD) {
			return BOLD_TOKEN;
		} else if ((style & SWT.ITALIC) == SWT.ITALIC) {
			return ITALIC_TOKEN;
		}
		return TEXT_TOKEN;
	}
	
	public static boolean hasStyles(String type) {
		return PARAGRAPH.equals(type)
				||isList(type);
	}
	
	public static boolean isList(String type) {
		return NUMBER_LIST_ITEM.equals(type)
		|| GRAPHIC_LIST_ITEM.equals(type);
	}

}
