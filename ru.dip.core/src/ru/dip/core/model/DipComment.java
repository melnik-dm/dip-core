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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.model.interfaces.IDipComment;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.ITextComment;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.ResourcesUtilities;
import ru.dip.core.utilities.md.MdUtilities;

public class DipComment extends DipElement implements IDipComment  {

	public static final String EXTENSION = "r";
	private static final String regex = "^\\[\\d+,\\d+\\]";
	private static final Pattern pattern = Pattern.compile(regex);
	
	public static DipComment instance(IResource resource, IParent parent) {
		IDipElement element = DipRoot.getInstance().getElement(resource, parent, DipElementType.COMMENT);
		if (element == null) {
			DipComment dipComment = new DipComment(resource, parent);
			DipRoot.getInstance().putElement(dipComment);
			return dipComment;
		} else {
			return (DipComment) element;
		}
	}
		
	public static DipComment createExistsDipComment(IFile file, IParent parent){
		DipComment comment = instance(file, parent);
		comment.read();
		return comment;
	}
		
	public static DipComment createNewDipComment(IDipDocumentElement dipDocumentElement, String descriptionContent){
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		String name  = dipDocumentElement.name() + ".r";
		IDipParent parent = dipDocumentElement.parent();
		try {
			IFile file = ResourcesUtilities.createFile(parent.resource(), name, descriptionContent, shell);
			if (file.exists()){
				DipComment comment = instance(file, parent);
				comment.fDipElement = dipDocumentElement;
				comment.fCommentContent = descriptionContent;
				comment.readTextCommentsFromMdFile();
				return comment;
			}			
		} catch (CoreException e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	public static DipComment createNewDipComment(IDipDocumentElement dipDocumentElement, List<ITextComment> comments){
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		String name  = dipDocumentElement.name() + ".r";
		IDipParent parent = dipDocumentElement.parent();
		String content = getWriteContent(null, comments);
		try {
			
			IFile file = ResourcesUtilities.createFile(parent.resource(), name, content, shell);
			if (file.exists()){
				DipComment comment = instance(file, parent);
				comment.fDipElement = dipDocumentElement;
				comment.fReviewTextComments = comments;				
				return comment;
			}			
		} catch (CoreException e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	private IDipDocumentElement fDipElement;
	private String fCommentContent;
	
	 // только из файла маркдаун
	private List<ITextComment> fMdTextComments = new ArrayList<>(); 
	// только из основного
	private List<ITextComment> fReviewTextComments = new ArrayList<>();  

	private DipComment(IResource resource, IParent parent) {
		super(resource, parent);
	}
	
	public void setCorrespondingElement(){
		fDipElement = findCorrespondingElement();
		if (fDipElement != null){
			fDipElement.setDipComment(this);
		}
	}
	
	public IDipDocumentElement findCorrespondingElement(){
		if (parent() instanceof IDipParent){
			IDipParent dipParent = (IDipParent) parent();
			String elementName = getElementName();
			for (IDipElement dipElement: dipParent.getChildren()){			
				if (dipElement instanceof IDipDocumentElement && elementName.equals(dipElement.name())){
					return (IDipDocumentElement) dipElement;
				}
			}			
		}
		return null;
	}
	
	private String getElementName(){
		String fullName = name();
		return fullName.substring(0, fullName.length() - 2);
	}
		
	public void updateCommentText(String newContent){
		if (newContent.equals(fCommentContent)){
			return;
		}
		fCommentContent = newContent;
		save();
	}
	
	public void updateTextComments(List<ITextComment> textComments) {
		fReviewTextComments = textComments;
		save();
	}
	
	@Override
	public void deleteMainContent() {
		fCommentContent = null;
		save();
	}
	
	@Override
	public void delete() {
		try {
			ResourcesUtilities.deleteResource(resource(), null);
			parent().removeChild(this);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean isEmpty() {
		return (fCommentContent == null || fCommentContent.isEmpty()) 
				&& (fReviewTextComments == null || fReviewTextComments.isEmpty())
				&& (fMdTextComments == null || fMdTextComments.isEmpty());
	}
	
	@Override
	public boolean hasCommentContent() {
		return fCommentContent != null || !fCommentContent.isEmpty();
	}
	
	@Override
	public boolean hasTextComments() {
		return hasReviewTextComment() || hasMdTextComment();
	}
	
	private boolean hasReviewTextComment() {
		return fReviewTextComments != null && !fReviewTextComments.isEmpty();
	}
	
	private boolean hasMdTextComment() {
		return fMdTextComments != null && !fMdTextComments.isEmpty();
	}
	
	//==============================
	// read-write
	
	public void read() {
		fCommentContent = null;
		fReviewTextComments.clear();
		fMdTextComments.clear();
		if (resource().exists()) {
			readFromReviewFile();
		}
		readTextCommentsFromMdFile();
	}
	
	
	/**
	 * Считывает комментарии из файла .r (может быть основной комментарии и дополнительные к тексту маркдаун, в старых версиях)
	 */
	private void readFromReviewFile() {
		try {
			StringBuilder builder = new StringBuilder();
			List<String> lines = FileUtilities.readLines(resource());
			int currentOffset = -1;
			int currentLength = -1;
			
			for (String line: lines) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					if (currentOffset == -1 && currentLength == -1 && builder.length() > 0 && fCommentContent == null) {
						fCommentContent = builder.toString();
						builder = new StringBuilder();
					} else if (currentOffset >=0 && currentLength > 0 && builder.length() > 0) {
						TextComment comment = new TextComment(builder.toString(), currentOffset, currentLength);
						fReviewTextComments.add(comment);
						builder = new StringBuilder();
					}										
					String group = matcher.group();
					String[] array = group.substring(1, group.length() - 1).split(",");
					currentOffset = Integer.parseInt(array[0]);
					currentLength = Integer.parseInt(array[1]);
					builder.append(line.substring(group.length()));
				} else {
					if (builder.length() > 0) {
						builder.append("\n");
					}
					builder.append(line);
				}				
			}

			if (currentOffset == -1 && currentLength == -1 && builder.length() > 0 && fCommentContent == null) {
				fCommentContent = builder.toString();
			} else if (currentOffset >=0 && currentLength > 0 && builder.length() > 0) {
				TextComment comment = new TextComment(builder.toString(), currentOffset, currentLength);
				fReviewTextComments.add(comment);
			}				
		} catch (IOException e) {			
			e.printStackTrace();
			DipCorePlugin.logError(e, "Ошибка при чтении файла: " + resource());
		}
	}
	
	/**
	 * Считывает комментарии к тексту для маркдауна  (нужны ли там offset и длина, пока видимо да)
	 */
	public void readTextCommentsFromMdFile() {
		fMdTextComments = new ArrayList<>();
		if (isMarkdown()) {			
			fMdTextComments = MdUtilities.readTextComments((IDipUnit) fDipElement);				
		}
	}
	

	@Override
	public void save() {
		try {
			saveContent();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void saveContent() throws IOException {
		String newContent = getWriteContent();
		saveContent(newContent);
	}
	
	public void saveOnlyMainReview() throws IOException {
		String newContent = getWriteContent(fCommentContent, Collections.emptyList());
		saveContent(newContent);
	}
	
	private void saveContent(String newContent) throws IOException {
		if (newContent == null || newContent.isEmpty()) {
			delete();
			return;
		}
		
		try (PrintWriter writer = new PrintWriter(resource().getLocation().toOSString(),
				StandardCharsets.UTF_8)){
			writer.print(newContent);
		}
	}
	
	private String getWriteContent() {				
		return getWriteContent(fCommentContent, fReviewTextComments);
	}
	
	private static String getWriteContent(String mainComment, List<ITextComment> textComments) {				
		StringBuilder builder = new StringBuilder();				
		if (mainComment != null && !mainComment.isEmpty()) {
			builder.append(mainComment);			
			if (textComments.isEmpty()) {
				return builder.toString();
			}
			for (ITextComment comment: textComments) {
				builder.append("\n");
				builder.append(comment);
			}
			return builder.toString();
		} else if (textComments.isEmpty()) {
			return null;
		} else {
			for (ITextComment comment: textComments) {
				builder.append(comment);
				builder.append("\n");
			}
			builder.deleteCharAt(builder.length()-1);
			return builder.toString();
		}
	}
	
	//==============================
	// getters
	
	@Override
	public String getFullContent() {
		StringBuilder builder = new StringBuilder();
		String mainComment = getCommentContent();
		if (mainComment != null && !mainComment.isBlank()) {
			builder.append(mainComment);
		}				
		if (fReviewTextComments != null && !fReviewTextComments.isEmpty()) {
			if (builder.length() > 0) {
				builder.append("\n");
			}
			builder.append(getTextCommentsContent());
		}
		if (fMdTextComments != null && !fMdTextComments.isEmpty()) {
			if (builder.length() > 0) {
				builder.append("\n");
			}
			builder.append(getTextCommentsContent());
		}
		return builder.toString();
	}
	
	@Override
	public String getTextCommentsContent() {
		StringBuilder builder = new StringBuilder();
		fReviewTextComments.forEach(textComment -> {
			if (builder.length() > 0) {
				builder.append("\n\n");
			}				
			builder.append(textComment.getContent());				
		});	
		fMdTextComments.forEach(textComment -> {
			if (builder.length() > 0) {
				builder.append("\n\n");
			}				
			builder.append(textComment.getContent());				
		});
		
		return builder.toString();
	}
	
	private boolean isMarkdown() {
		return fDipElement instanceof IDipUnit && ((IDipUnit) fDipElement).getUnitType().isMarkdown();
	}
	
	@Override
	public String getCommentContent(){
		return fCommentContent;
	}
	
	@Override
	public List<ITextComment> getTextComments() {
		return fReviewTextComments;
	}

	@Override
	public DipElementType type() {
		return DipElementType.COMMENT;
	}
	
	@Override
	public IFile resource() {
		return (IFile) super.resource();
	}

	@Override
	public IDipDocumentElement getDipDocumentElement(){
		return fDipElement;
	}

}
