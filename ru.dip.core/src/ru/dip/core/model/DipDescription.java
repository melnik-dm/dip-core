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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import ru.dip.core.model.interfaces.IDescriptionSupport;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.model.interfaces.IDipParent;
import ru.dip.core.model.interfaces.IParent;
import ru.dip.core.utilities.FileUtilities;
import ru.dip.core.utilities.ResourcesUtilities;

public class DipDescription extends DipElement implements IDescriptionSupport  {

	public static final String EXTENSION = "d";
	
	private IDipDocumentElement fDipElement;
	private String fDesciptionContent;
	
	public static DipDescription instance(IResource resource, IParent parent) {
		IDipElement element = DipRoot.getInstance().getElement(resource, parent, DipElementType.DESCRIPTION);
		if (element == null) {
			DipDescription dipDescription = new DipDescription(resource, parent);
			DipRoot.getInstance().putElement(dipDescription);
			return dipDescription;
		} else {
			return (DipDescription) element;
		}
	}
	
	private DipDescription(IResource resource, IParent parent) {
		super(resource, parent);
	}
	
	public static DipDescription createExistsDipDescription(IFile file, IParent parent){
		DipDescription description = DipDescription.instance(file, parent); 
		description.fDesciptionContent = description.getDescriptionText();
		return description;
	}
	
	public static DipDescription createNewDipDescription(IDipDocumentElement dipDocumentElement, String descriptionContent){
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		String name  = dipDocumentElement.name() + ".d";
		IDipParent parent = dipDocumentElement.parent();
		try {
			IFile file = ResourcesUtilities.createFile(parent.resource(), name, descriptionContent, shell);
			if (file.exists()){
				DipDescription description = DipDescription.instance(file, parent); 
				description.fDipElement = dipDocumentElement;
				description.fDesciptionContent = descriptionContent;				
				return description;
			}			
		} catch (CoreException e) {
			e.printStackTrace();
		}		
		return null;
	}
	
	public void setCorrespondingElement(){
		fDipElement = findCorrespondingElement();
		if (fDipElement != null){
			fDipElement.setDipDescription(this);
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
	
	private String getDescriptionText(){
		try {
			return FileUtilities.readFile(resource());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void updateDescriptionText(String newContent) {
		if (newContent.equals(fDesciptionContent)){
			return;
		}
		fDesciptionContent = newContent;
		try (PrintWriter writer = new PrintWriter(resource().getLocation().toOSString(), StandardCharsets.UTF_8)){
			writer.print(newContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void delete() {
		try {
			ResourcesUtilities.deleteResource(resource(), null);
			parent().removeChild(this);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public DipElementType type() {
		return DipElementType.DESCRIPTION;
	}
	
	@Override
	public IFile resource() {
		return (IFile) super.resource();
	}

	public IDipDocumentElement getDipDocElement(){
		return fDipElement;
	}
	
	public String getDescriptionContent(){
		return fDesciptionContent;
	}

}
