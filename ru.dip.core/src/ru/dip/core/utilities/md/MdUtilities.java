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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import ru.dip.core.model.TextComment;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.ITextComment;
import ru.dip.core.utilities.FileUtilities;

public class MdUtilities {
	
	public static final String COMMENT_TAG_NAME = "comment";
	public static final String COMMENT_ARGUMENT = "description";
	
	public static final String COMMENT_START_TAG = "<" + COMMENT_TAG_NAME +" " + COMMENT_ARGUMENT + "=";
	public static final String COMMENT_END_TAG = "</" + COMMENT_TAG_NAME + ">";
	
	public static final String regex = COMMENT_START_TAG + "\\\"[^>]*\\\">";
	public static final String endRegex = COMMENT_END_TAG;
	public static final Pattern comment_tag_pattern = Pattern.compile(regex);
	public static final Pattern end_comment_tag_pattern = Pattern.compile(endRegex);
	

	/**
	 * Определяет нужно ли вставлять звездочки либо символы подчеркивания (при
	 * применении стилей)
	 */
	public static boolean isAsteriskEmphasis(IDocument document, int offsetStart, int offsetEnd) {

		try {
			int length = document.getLength();
			if (offsetStart > 0 && offsetStart < length -1) {
				char ch = document.getChar(offsetStart - 1);
				if (isLetterChar(ch)) {
					return true;
				}
			}
			if (offsetEnd > 0 && offsetEnd < length -1) {
				char ch = document.getChar(offsetEnd);
				if (isLetterChar(ch)) {
					return true;
				}
			}			
			return false;
		} catch (BadLocationException e) {
			e.printStackTrace();
			return true;
		}
	}

	private static boolean isLetterChar(int c) {
		return Character.isLetter(c);
	}
	
	/**
	 * Ищет комментарии в файле маркдаун (IDipUnit должен быть markdown)
	 */
	public static List<ITextComment> readTextComments(IDipUnit unit){
		List<ITextComment> textComments = new ArrayList<>();
		try {
			String content = FileUtilities.readFile((IFile) unit.resource());
			TextComment comment = findComment(content);
			while (comment != null) {			
				textComments.add(comment);
				StringBuilder builder = new StringBuilder();
				builder.append(content, 0, comment.getOffset());
				int startMainText = comment.getContent().length() + 6 + COMMENT_TAG_NAME.length()
						+ COMMENT_ARGUMENT.length() + comment.getOffset();

				builder.append(content, startMainText, startMainText + comment.getLength());
				builder.append(content, startMainText + comment.getLength() + COMMENT_END_TAG.length(),
						content.length());
				content = builder.toString();			
				comment = findComment(content);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return textComments;
	}
	
	/**
	 * Возвращает первый найденный комметарий в тексте
	 */
	private static TextComment findComment(String content) {
		Matcher matcher = MdUtilities.comment_tag_pattern.matcher(content);
		if (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();

			Matcher endMatcher = MdUtilities.end_comment_tag_pattern.matcher(content);
			if (endMatcher.find(end)) {
				int endStart = endMatcher.start();

				String description = content
						.substring(start + 4 + COMMENT_TAG_NAME.length() + COMMENT_ARGUMENT.length(), end - 2);
				int offset = start;
				int length = endStart - end;
				return new TextComment(description, offset, length);
			}
		} 
		return null;
	}
	
	/**
	 * Сохраняет новые комментарии
	 */
	public static void saveTextCommentsToMdUnit(IDipUnit unit, List<ITextComment> comments) throws IOException {
		String originalContent = FileUtilities.readFile(unit.resource());
		String withoutComments = removeComments(originalContent);
		String withNewComments = saveCommentsContent(withoutComments, comments);
		FileUtilities.writeFile(unit.resource(), withNewComments);	
	}
	
	/**
	 * Удаляет комментарии из текста
	 */
	public static String removeComments(String content) {
		ITextComment annotation = findComment(content);
		while (annotation != null) {
			StringBuilder builder = new StringBuilder();
			builder.append(content, 0, annotation.getOffset());			
			int startMainText = annotation.getOffset() +  annotation.getContent().length() + 6 + COMMENT_TAG_NAME.length() + COMMENT_ARGUMENT.length();			
			builder.append(content,startMainText, startMainText + annotation.getLength());
			builder.append(content, startMainText + annotation.getLength() + COMMENT_END_TAG.length(), content.length());			
			content = builder.toString();			
			annotation = findComment(content);
		}
		return content;
	}
	
	/**
	 * Добавляет комментарии в текст
	 */
	private static String saveCommentsContent(String contentWithoutComments, List<ITextComment> comments) {					
		StringBuilder builder = new StringBuilder();
		int index = 0;
				
		for (ITextComment comment: comments) {
			builder.append(contentWithoutComments, index, comment.getOffset());
			index = comment.getOffset();
			builder.append(COMMENT_START_TAG);
			builder.append("\"");
			builder.append(comment.getContent());
			builder.append("\">");
			builder.append(contentWithoutComments, comment.getOffset(), comment.getEndOffset());
			builder.append(COMMENT_END_TAG);
			index = comment.getEndOffset();							
		}
		builder.append(contentWithoutComments, index, contentWithoutComments.length());
		return builder.toString();
	}
}
