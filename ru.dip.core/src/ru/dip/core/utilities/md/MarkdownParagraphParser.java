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
package ru.dip.core.utilities.md;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.commonmark.node.Code;
import org.commonmark.node.Emphasis;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.HtmlInline;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.TagStringUtilities;
import ru.dip.core.utilities.md.parser.MdParser;
import ru.dip.core.utilities.text.Terms;

public class MarkdownParagraphParser {

	public static enum Type {
		TEXT,
		LINK,
		COMMENT,CODE,
		BOLD_START, BOLD_END, 
		ITALIC_START, ITALIC_END, 
		EMPTY, 
		LIST_MARKER,
		GLOSSARY_WORD,
		VARIABLE;
		
		public boolean isStringType() {
			return this == TEXT /*|| this == COMMENT*/;
		}
		
	}
	
	public static class MdStyledPosition {
		
		private int fStyle;
		private int fOffset;
		private int fLength;
		private Type fType;
		
		public MdStyledPosition(Type type, int style, Object node) {
			fStyle = style;
			fType = type;
		}
		
		public MdStyledPosition buildOffset(int offset, int length) {
			fOffset = offset;
			fLength = length;
			return this;
		}
		
		public int offset() {
			return fOffset;
		}
		
		public int length() {
			return fLength;
		}
		
		public Point position() {
			return new Point(fOffset, fLength);
		}
		
		public Type type() {
			return fType;
		}
		
		public int style() {
			return fStyle;
		}		
	
		@Override
		public String toString() {
			String result = fType.toString() + " "  + fOffset + "  " + fLength;
			return result;
		}	

	}
	
	/**
	 * Добавляет позиции глоссария и переменных
	 */
	public static List<MdStyledPosition>  addGlossaryWordPosition(DipProject project, String docContent, List<MdStyledPosition> positions, int offset) {
		if (project == null) {
			return null;
		}
		
		List<MdStyledPosition> updatePositions = new ArrayList<>();
		
		for (MdStyledPosition position: positions) {
			if (position.type().isStringType()) {
				List<MdStyledPosition> newPositions = new ArrayList<>();
				String posContent = docContent.substring(offset + position.offset(), offset +  position.length() + position.offset());				
				
				List<Point> glossaryWords = project.getGlossaryFolder().findKeyWords(posContent);				
				List<Point> vars = Terms.findVars(posContent);
					
				List<Point> keyWordsPositions = new ArrayList<Point>();
				keyWordsPositions.addAll(glossaryWords);
				keyWordsPositions.addAll(vars);
				
				sortGlossaryPosition(keyWordsPositions);
				deleteIntersectionPosition(keyWordsPositions);
							
				int start = 0;
				for (Point p: keyWordsPositions) {
					int keyWordOffset = p.x;
					int keyWordLength = p.y;
					int end = p.x + p.y;

					// add TEXT position
					if (keyWordOffset - start > 0) {
						MdStyledPosition text = new MdStyledPosition(position.type(), position.style(), null);
						text.buildOffset(position.offset() + start, keyWordOffset - start);
						newPositions.add(text);
					}
					// add GLOSSARY (VARIABL) position					
					Type type = vars.contains(p) ? Type.VARIABLE : Type.GLOSSARY_WORD;					
					MdStyledPosition keyWord = new MdStyledPosition(type, position.style(), null);
					keyWord.buildOffset(keyWordOffset + position.offset(), keyWordLength);
					newPositions.add(keyWord);
					start = end;
				}
				// add TEXT position
				if (position.length() > start) {
					MdStyledPosition text = new MdStyledPosition(position.type(), position.style(), null);
					text.buildOffset(position.offset() + start, position.length() - start);
					newPositions.add(text);
				}
				updatePositions.addAll(newPositions);				
			} else {
				updatePositions.add(position);
			}								
		}
		return updatePositions;
	}
	
	private static void sortGlossaryPosition(List<Point> keyWordsPositions) {
		keyWordsPositions.sort(new Comparator<Point>() {

			@Override
			public int compare(Point o1, Point o2) {
				return o1.x - o2.x;
			}
		});
	}
	
	private static void deleteIntersectionPosition(List<Point> keyWordsPositions) {
		int size = keyWordsPositions.size();
		for (int i = 1; i < size; i++) {
			Point prev = keyWordsPositions.get(i-1);
			Point current = keyWordsPositions.get(i);
			if (prev.x + prev.y > current.x) {
				keyWordsPositions.remove(current);
				i--;
				size--;
				continue;
			}
		}
	}
	
	/**
	 * Возвращает список позиций для параграфа (когда не нужен экземпляр парсера)
	 */
	public static List<MdStyledPosition> getParagraphPositions(String content){
		MarkdownParagraphParser parser = new MarkdownParagraphParser();		
		parser.parseParagraph(TagStringUtilities.replaceUnbreakableSpaces(content));
		return parser.getPositions();	
	}
		
	/**
	 * Возвращает список позиций для списка (когда не нужен экземпляр парсера)
	 */
	public static List<MdStyledPosition> getListItemPositions(String content){
		List<MdStyledPosition> result = new ArrayList<>();
		// пробелы вначале
		int start = TagStringUtilities.startSpacesChars(content.toCharArray());
		MdStyledPosition startPosition = new MdStyledPosition(Type.EMPTY, SWT.NONE, null)
				.buildOffset(0, start);		
		content = TagStringUtilities.trim(content);
		int marker = getListMarker(content);
		if (marker > 0) {				
			content = content.substring(marker);
		}
		// маркер списка
		MdStyledPosition markerPosition = new MdStyledPosition(Type.LIST_MARKER, SWT.NONE, null)
				.buildOffset(start, marker);
		
		MarkdownParagraphParser parser = new MarkdownParagraphParser(start + marker);
		parser.parseParagraph(content);
		result = parser.getPositions();
		result.add(0, markerPosition);
		result.add(0, startPosition);
		return result;
	}
	
	/**
	 * Возвращает позицию маркера для маркирвоанного списка (* или -)
	 * Возвращает позицию символа обозначения списка ( `1)` или `1.` - вернет поззицию точки или скобки)
	 * Возвращает ноль - если не список
	 */
	private static int getListMarker(String content) {
		// если продолжение старого списка
		if (content.startsWith("-.") || content.startsWith("-)")) {
			return 2;
		}
		
		if (content.startsWith("*") || content.startsWith("-")) {
			return 1;
		}

		char[] chars = content.toCharArray();
		int result = 0;
		for (int i = 0; i < chars.length; i++) {
			if (!Character.isDigit(chars[i])) {
				break;
			} else {
				result++;
			}
		}
		if (result < chars.length  && (chars[result] == '.' || chars[result] == ')')) {
			return result + 1;
		}						
		return 0;
	}
	
	private List<MdStyledPosition> fPositions = new ArrayList<>();
	private List<Integer> fIndents = new ArrayList<>(); // список отступов в начале и конце строк		
	private int fOffset = 0;
	private String fContent;
	
	public MarkdownParagraphParser() {
	}
	
	public MarkdownParagraphParser(int offset) {
		fOffset = offset;
	}

	//===============================
	// parsing
	
	public void parseParagraph(String content) {
		fPositions = new ArrayList<>();
		fIndents = new ArrayList<>();
		fContent = content;
		getIndents(content);
		addSingleEmptyPosition();
		parse(content);
		addSingleEmptyPosition();
	}
	
	private void parse(String content) {
		MdParser parser = MdParser.instance();
		Node document = parser.parse(TagStringUtilities.trim(content), null);
		Node node = document.getFirstChild();
		if (node instanceof Paragraph) {
			Paragraph paragraph = (Paragraph) node;
			Node child = paragraph.getFirstChild();	
			while (child != null) {
				handleNode(child, SWT.NONE);		
				child = child.getNext();
			}								
		}
	}
	
	public void parseListItem(String content) {
		fPositions = new ArrayList<>();
		fIndents = new ArrayList<>();
		// пробелы вначале
		int start = TagStringUtilities.startSpacesChars(content.toCharArray());
		MdStyledPosition startPosition = new MdStyledPosition(Type.EMPTY, SWT.NONE, null)
				.buildOffset(0, start);		
		content = content.substring(start);
		int marker = getListMarker(content);
		if (marker > 0) {				
			content = content.substring(marker);
		}
		// маркер списка
		MdStyledPosition markerPosition = new MdStyledPosition(Type.LIST_MARKER, SWT.NONE, null)
				.buildOffset(start, marker);
		
		fOffset = start + marker;
		parseParagraph(content);
		fPositions.add(0, markerPosition);
		fPositions.add(0, startPosition);
	}
	
	private void getIndents(String content) {
		int start = TagStringUtilities.startSpacesChars(content.toCharArray());
		int end = TagStringUtilities.endSpaces(content.toCharArray());		
		String[] lines = TagStringUtilities.trim(content).split("\n");
		for (String line: lines) {
			char[] chars = line.toCharArray();
			fIndents.add(TagStringUtilities.startSpacesChars(chars));
			fIndents.add(TagStringUtilities.endSpaces(chars));			
		}		
		fIndents.set(0, start);
		fIndents.set(fIndents.size() - 1, end);
	}

	private void handleNode(Node node, int style) {
		if (node instanceof Text) {
			handleText((Text) node, style);
		} else if (node instanceof StrongEmphasis) {
			handleBoldToken((StrongEmphasis) node, style);			
		} else if (node instanceof Emphasis) {
			handleItalicToken((Emphasis) node, style);
		} else if (node instanceof HardLineBreak || node instanceof SoftLineBreak) {
			addEmptyPosition(style);
		} else if (node instanceof Link) {
			handleLink((Link) node, style);
		} else if (node instanceof HtmlInline) { 
			handleComment((HtmlInline) node, style);
		} else if (node instanceof Code) {
			handleCode((Code) node, style);
		} else {
			//System.out.println("ELSE " + node);
		}
	}

	private void handleCode(Code code, int style) {
		int length = code.getLiteral().length() + 2;
		if (fContent.substring(fOffset).startsWith("```")) {
			length += 4;
		}		
		createToken(Type.CODE, style,length, code);		
	}

	private void handleText(Text text, int style) {
		int length = text.getLiteral().length();									
		createToken(Type.TEXT, style, length, text);
	}
	
	private void handleBoldToken(StrongEmphasis emphasis, int style) {
		int boldStyle = style | SWT.BOLD;
		int openLength = emphasis.getOpeningDelimiter().length();		
		createToken(Type.BOLD_START, boldStyle, openLength, emphasis);
		Node boldchild = emphasis.getFirstChild();
		while (boldchild != null) {
			handleNode(boldchild, boldStyle);
			boldchild = boldchild.getNext();
		}
		int closeLength = emphasis.getOpeningDelimiter().length();
		createToken(Type.BOLD_END, boldStyle, closeLength, emphasis);	
	}
	
	private void handleItalicToken(Emphasis emphasis, int style) {
		int italicStyle = style | SWT.ITALIC;
		int openLength = emphasis.getOpeningDelimiter().length();		
		createToken(Type.ITALIC_START, italicStyle, openLength, emphasis);
		Node italicChild = emphasis.getFirstChild();
		while (italicChild != null) {
			handleNode(italicChild, italicStyle);
			italicChild = italicChild.getNext();

		}
		int closeLength = emphasis.getOpeningDelimiter().length();
		createToken(Type.ITALIC_END, italicStyle, closeLength, emphasis);	
	}
	
	private void handleLink(Link link, int style) {
		int titleLink = 0;
		if (link.getFirstChild() instanceof Text) {
			Text text = (Text) link.getFirstChild();
			titleLink = text.getLiteral().length();
		}
		int destination = 0;
		if (link.getDestination() != null) {
			destination = link.getDestination().length();
		}
		int length = 4 + titleLink + destination;
		createToken(Type.LINK, style, length, link);		
	}
	
	private void handleComment(HtmlInline inline, int style) {		
		int length = inline.getLiteral().length();
		createToken(Type.COMMENT, style, length, inline);
	}
	
	private void createToken(Type type, int style, int length, Node node) {
		MdStyledPosition position = new MdStyledPosition(type, style, node)
				.buildOffset(fOffset, length);
		fPositions.add(position);
		fOffset += length;
	}
	
	private void addSingleEmptyPosition() {
		int length = fIndents.get(0);
		fIndents.remove(0);
		MdStyledPosition position = new MdStyledPosition(Type.EMPTY, SWT.NONE, null)
				.buildOffset(fOffset, length);
		fPositions.add(position);
		fOffset += length;
	}
	
	private void addEmptyPosition(int style) {
		int length = fIndents.get(0) + fIndents.get(1) + 1;
		fIndents.remove(0);
		fIndents.remove(0);
		MdStyledPosition position = new MdStyledPosition(Type.EMPTY, style, null)
				.buildOffset(fOffset, length);
		fPositions.add(position);
		fOffset += length;
	}

	//=====================================
	
	public int findPosition(int offset) {
		for (int i = 0; i < fPositions.size(); i++) {
			MdStyledPosition position = fPositions.get(i);
			if (position.offset() <= offset &&
			position.length() + position.offset() > offset) {
				return i;
			}
		}
		MdStyledPosition lastPosition = fPositions.get(fPositions.size() - 1);
		if (offset == lastPosition.length() + lastPosition.offset()) {
			return fPositions.size() - 1;
		}	
		return -1;
	}
	
	/**
	 * Возвращает позицию по смещению, но в отличие от findPosition, position.offset, должно быть строго больше
	 * Т.е. если курсор стоит перед позицией, эта позиция не включается.
	 */
	public int findLastPosition(int offset) {
		for (int i = 0; i < fPositions.size(); i++) {
			MdStyledPosition position = fPositions.get(i);			
			if (position.offset() < offset &&
			position.length() + position.offset() >= offset) {
				return i;
			}
		}				
		MdStyledPosition lastPosition = fPositions.get(fPositions.size() - 1);
		if (offset == lastPosition.length() + lastPosition.offset()) {
			return fPositions.size() - 1;
		}			
		return -1;
	}
	
	//=====================================
	// bold
			
	public boolean isBold(int indexPosition, int offset) {
		MdStyledPosition position = fPositions.get(indexPosition);	
		Type type = position.type();
		if (type ==  Type.BOLD_START) {
			return offset != position.offset();			
		} else if (type == Type.BOLD_END) {
			return true;
		} else {
			for (int i = indexPosition + 1; i < fPositions.size(); i++){
				if (fPositions.get(i).type() == Type.BOLD_END) {
					break;
				}
				if (fPositions.get(i).type() == Type.BOLD_START) {
					return false;
				} 
			}	
			for (int i = indexPosition - 1; i >= 0; i--){
				if (fPositions.get(i).type() == Type.BOLD_START) {
					return true;
				}
				if (fPositions.get(i).type() == Type.BOLD_END) {
					return false;
				}
			}
		}						
		return false;
	}
	
	public MdStyledPosition previousBoldStartPosition(int currentPosition) {
		for (int i = currentPosition; i >= 0; i--){
			if (fPositions.get(i).type() == Type.BOLD_START) {
				return fPositions.get(i);
			}
		}
		return null;
	}
	
	public MdStyledPosition nextBoldEndPosition(int currentPosition) {
		for (int i = currentPosition; i < fPositions.size(); i++){
			if (fPositions.get(i).type() == Type.BOLD_END) {
				return fPositions.get(i);
			}
		}	
		return null;
	}

	//=====================================
	// italic
	
	public boolean isItalic(int indexPosition, int offset) {
		MdStyledPosition position = fPositions.get(indexPosition);	
		Type type = position.type();
		if (type ==  Type.ITALIC_START) {
			return offset != position.offset();			
		} else if (type == Type.ITALIC_END) {
			return true;
		} else {
			for (int i = indexPosition + 1; i < fPositions.size(); i++){
				if (fPositions.get(i).type() == Type.ITALIC_END) {
					break;
				}
				if (fPositions.get(i).type() == Type.ITALIC_START) {
					return false;
				} 
			}	
			for (int i = indexPosition - 1; i >= 0; i--){
				if (fPositions.get(i).type() == Type.ITALIC_START) {
					return true;
				}
				if (fPositions.get(i).type() == Type.ITALIC_END) {
					return false;
				}
			}
		}						
		return false;
	}
	
	public MdStyledPosition previousItalicStartPosition(int currentPosition) {
		for (int i = currentPosition; i >= 0; i--){
			if (fPositions.get(i).type() == Type.ITALIC_START) {
				return fPositions.get(i);
			}
		}
		return null;
	}
	
	public MdStyledPosition nextItalicEndPosition(int currentPosition) {
		for (int i = currentPosition; i < fPositions.size(); i++){
			if (fPositions.get(i).type() == Type.ITALIC_END) {
				return fPositions.get(i);
			}
		}	
		return null;
	}
	
	//========================
	// getters
	
	public List<MdStyledPosition> getPositions(){
		return fPositions;
	}
	
}
