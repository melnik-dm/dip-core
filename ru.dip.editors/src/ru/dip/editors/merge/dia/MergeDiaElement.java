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
package ru.dip.editors.merge.dia;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.dip.core.exception.DiaConvertCommandException;
import ru.dip.core.utilities.DiaUtilities;

public class MergeDiaElement {
	
	private ITypedElement fElement;
	private File fPngFile;

	public MergeDiaElement(CompareEditorInput compareContainer,
			ITypedElement element) {
		fElement = element;
		read();
	}
	
	private void read() {
		try (InputStream inputStream = getInputStream()) {
			if (inputStream == null) {
				return;
			}		
			File file = File.createTempFile("dia_tmp", "");			
			Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			fPngFile = DiaUtilities.toPng(file.toPath().toString(), "dia_merge");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DiaConvertCommandException e) {
			e.printStackTrace();
		}
	}
		
	//================================
	// read form
	
	private InputStream getInputStream() {
		if (fElement instanceof IStreamContentAccessor) {
			IStreamContentAccessor streamAccessor = (IStreamContentAccessor) fElement;
			try {
				InputStream inputStream = streamAccessor.getContents();
				return inputStream;
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
		
	//================================
	// controls
		
	public void createControls(Composite composite) {
		ScrolledComposite scroll = new ScrolledComposite(composite,  SWT.H_SCROLL | SWT.V_SCROLL);
		scroll.setExpandHorizontal(true);
		scroll.setExpandVertical(true);
				
		Label imageLabel = new Label(scroll, SWT.BORDER);
		Image image = new Image(null, fPngFile.toPath().toString());
		imageLabel.setImage(image);
		
		scroll.setMinSize(image.getBounds().width +10 , image.getBounds().height + 10);		
		scroll.setContent(imageLabel);
	}
				
}
