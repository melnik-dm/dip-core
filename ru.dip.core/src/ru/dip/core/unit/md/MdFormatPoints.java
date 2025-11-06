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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Point;

public class MdFormatPoints {
	
	private List<Point> fBoldPoints = new ArrayList<>(); 
	private List<Point> fItalicPoints = new ArrayList<>();
	private List<Point> fBoldItalicPoints = new ArrayList<>();
	private List<Point> fCodePoints = new ArrayList<>();
	private List<Point> fFencedPoints = new ArrayList<>();
	private List<Point> fCommentPoints = new ArrayList<>();
	
	public void clear() {
		fBoldPoints.clear();; 
		fItalicPoints.clear();
		fBoldItalicPoints.clear();
		fCodePoints.clear();
		fFencedPoints.clear();
		fCommentPoints.clear();		
	}
	
	public List<Point> codePoints(){
		return fCodePoints;
	}
	
	public List<Point> fencedCodePoints(){
		return fFencedPoints;
	}
	
	public List<Point> commentPoints(){
		return fCommentPoints;
	}
	
	public List<Point> boldPoints(){
		return fBoldPoints;
	}
	
	public List<Point> italicPoints(){
		return fItalicPoints;
	}
	
	public List<Point> boldItalicPoints(){
		return fBoldItalicPoints;
	}

}
