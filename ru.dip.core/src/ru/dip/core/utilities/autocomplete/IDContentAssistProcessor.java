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
package ru.dip.core.utilities.autocomplete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.DipUtilities;

public class IDContentAssistProcessor implements IContentAssistProcessor {

	private DipProject fProject;
	private IDipElement fDipElement;
	
	public IDContentAssistProcessor(IDipElement element, DipProject project) {
		fProject = project;
		fDipElement = element;
	}
	
	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {	
        String text = getTobeCompleteText(viewer.getDocument(), offset);
        // check - is link
        if (!text.contains("](")) {
        	return null;
        }
        int index = text.lastIndexOf("](");
        text = text.substring(index+2);
        
        // пустая строка
        if (text.isEmpty()) {
        	return projectProposal(viewer, offset);
        } 
        	
        IDipElement element = findElement(text);	

        // если родитель
        if (element instanceof IDipParent) {
        	IDipParent parent = (IDipParent) element;      		
        	return parentProposal(parent, text, viewer, offset);      	
        } 
        
        // если неполное имя
		if (element == null) {
			String[] array = text.split("/");
			if (array.length > 0) {
				String incomplete = array[array.length - 1];
				IDipElement previousElement = findPreviousElement(array);
				if (previousElement instanceof IDipParent) {
					return notCompleteProposal((IDipParent) previousElement, incomplete, viewer, offset);
				}
			}
		}
		return null;
        
	}
	
    private String getTobeCompleteText(IDocument document, int currentOffset){
        int count = currentOffset;
        String tobeCompleteText="";
        try{
            while(--count>=0){
                char c = ' ';
                c = document.getChar(count);
                if(isWhiteSpace(c)){
                    break;
                }
            }
            tobeCompleteText = document.get(count+1, currentOffset-(count+1));
        } catch (Exception ignore) {
        	ignore.printStackTrace();
        }
        return tobeCompleteText;
    }
    
    private boolean isWhiteSpace(char c){
        if(c=='\n'||c=='\t'||c==' '){
            return true;
        }
        return false;
    }

	private ICompletionProposal[] projectProposal(ITextViewer viewer, int offset) {
    	List<IDipDocumentElement> proposals = new ArrayList<>();
    	proposals.addAll(fProject.getDipDocChildrenList());     	
    	return proposals.stream()
    			.map(dipDocElement -> new DipElementProporsal(offset, dipDocElement, viewer))
    			.toArray(ICompletionProposal[]::new);
	}

	private ICompletionProposal[] parentProposal(IDipParent parent, String text, 
			ITextViewer viewer, int offset) {
		List<IDipDocumentElement> children = parent.getDipDocChildrenList();
		if (children.isEmpty()) {
			return null;
		}
		
		boolean needDelemiter = !text.endsWith("/");
		return children.stream()
				.map(dipDocElement -> new DipElementProporsal(offset, dipDocElement, viewer,needDelemiter))
	        	.toArray(ICompletionProposal[]::new);
	}
	
	private IDipElement findElement(String text) {
		IDipElement element = null;
		if (text.startsWith("./")) {
			element = DipUtilities.findElement(fDipElement,text);
		} else {
			element = DipUtilities.findElement(fProject,text);
		}
    	if (element == null) {
    		element = DipUtilities.findElement(text);
    	}
    	return element;
	}

	/*
	 * Массив из сегментов пути
	 * Последний элемент массива неучитывается 
	 * (предполагается - там неполное имя0)
	 */
	private IDipElement findPreviousElement(String[] array) {
		IDipElement previousElement = null;
		if (array.length == 1) {
			previousElement = fProject;
		} else {
		
			String[] previous = Arrays.copyOf(array, array.length - 1);
    	
		
    		String  previousText = String.join("/", previous);
    		previousElement = DipUtilities.findElement(fProject, previousText);
        	if (previousElement == null) {
        		previousElement = DipUtilities.findElement(previousText);
        	}	
		}
		return previousElement;
	}
	
	private ICompletionProposal[] notCompleteProposal(IDipParent previous, String incomplete,
			ITextViewer viewer, int offset) {
		
		List<IDipDocumentElement> children = previous.getDipDocChildrenList();
		List<IDipDocumentElement> result = new ArrayList<>();
		for (IDipDocumentElement dipDocElement: children) {
			String name = dipDocElement.dipName();
			if (name.startsWith(incomplete)) {
				result.add(dipDocElement);
			}	    	        				    	        				    	        			
		}	    	
		
		if (!result.isEmpty()) {		
			return result.stream()
    				.map(dipDocElement -> new FinishDipElementProporsal(offset, dipDocElement, viewer,incomplete))
    	        	.toArray(ICompletionProposal[]::new);
			
		}   
		return null;
	}
	
	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] {'/','.'};
	}
	
	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {   
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}
	
}