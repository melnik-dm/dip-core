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
package ru.dip.ui.action.hyperlink;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.PlatformUI;

import ru.dip.core.utilities.WorkbenchUtitlities;

public class HyperlinkDetector extends AbstractHyperlinkDetector {

	public HyperlinkDetector() {
		
	}

	@Override
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
		int offset = region.getOffset();
		IDocument document =textViewer.getDocument();		
		try {
			IRegion line = document.getLineInformationOfOffset(offset);
			String strLine = document.get(line.getOffset(), line.getLength());
			int lineRelOffset = region.getOffset() - line.getOffset();
			
			int start = -1;
			for (int i = lineRelOffset; i > 0; i--){
				if (i >= strLine.length()) {
					i --;
					continue;
				}
				
				if (')' == strLine.charAt(i)){
					return null;
				}
				if ('(' == strLine.charAt(i) && ']' == strLine.charAt(i - 1)){
					start = i + 1;					
					break;
				}
			}
			if (start < 0){
				return null;
			}
						
			int end = strLine.length();
			for (int i = lineRelOffset; i < strLine.length(); i++){
				if (')' == strLine.charAt(i)){
					end = i;
					break;
				}
			}
			
			int regionStart = start + line.getOffset();
			int regionLength = end - start;		
			String result = document.get(regionStart, regionLength);			
			IFile file = WorkbenchUtitlities.getFileFromOpenedEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart());
			Region reg = new Region(regionStart,  regionLength) ;
			if (file != null){	
				return new IHyperlink[]{new ReqLink(reg, result, file)};
			}					
		} catch (BadLocationException e) {
			e.printStackTrace();
		}	
		return null;
	}
	
	

}
