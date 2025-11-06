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
package ru.dip.core.form.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;

import ru.dip.core.utilities.ui.swt.ColorProvider;

public class Item {
	
	private String fName;
	private String fForegroundColor;
	private String fBackgroundColor;
	private String fHint;
	private Integer[] fSeq;
	
	public Item(String name, String foreground, String background, String hint){
		fName = name;
		fForegroundColor = foreground;
		fBackgroundColor = background;
		fHint = hint;
	}
	
	public String getName(){
		return fName;
	}
	
	public Color getBackgroundColor(){
		if (fBackgroundColor != null){
			return ColorProvider.getColor(fBackgroundColor);
		}		
		return null;		
	}
	
	public Color getForegroundColor(){
		if (fForegroundColor != null){
			return ColorProvider.getColor(fForegroundColor);
		}		
		return null;				
	}
	
	public String getHint(){
		return fHint;
	}

	public void setSeq(String seq) {
		if (!seq.isEmpty()) {
			List<Integer> seqNumbers = new ArrayList<>();			
			String[] strNumbers = seq.split(",");
			for (String str: strNumbers) {
				try {
					int n = Integer.parseInt(str);
					seqNumbers.add(n);

				} catch (NumberFormatException ignore) {
					// NOP					
				}
			}	
			fSeq = new Integer[seqNumbers.size()];
			seqNumbers.toArray(fSeq);
		} else {		
			fSeq = new Integer[] {};
		}
	}
	
	public boolean isIllegalSeq() {
		return  fSeq != null && fSeq.length == 1 && fSeq[0] == -1;
	}
	
	public Integer[] getSeq() {
		return fSeq;
	}
}
