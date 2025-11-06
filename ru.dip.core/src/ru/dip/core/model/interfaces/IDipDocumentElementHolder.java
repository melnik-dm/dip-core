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
package ru.dip.core.model.interfaces;

import ru.dip.core.unit.form.FieldUnity;
import ru.dip.core.unit.form.FormField;
import ru.dip.core.unit.form.FormPresentation;

public interface IDipDocumentElementHolder {

	IDipDocumentElement dipDocElement();
			
	public default boolean isDisable() {
		return dipDocElement().isDisabledInDocument();
	}
	
	public default boolean notEmptyDescription() {
		return !isEmptyDescription();
	}
	
	public default boolean isDescription() {
		return dipDocElement() instanceof IUnitDescription;
	}
	
	public default boolean isEmptyDescription() {
		if (dipDocElement() instanceof IUnitDescription) {
			IUnitDescription description = (IUnitDescription) dipDocElement();
			String desc = description.getDecriptionContent();
			if (desc == null || desc.trim().isEmpty()) {
				if (description.isNumberedDescription()) {
					return false;
				}
				return true;
			}
		}
		return false;
	}
	
	
	public default boolean isPresentation() {
		return dipDocElement() instanceof IUnitPresentation;
	}
	
	public default boolean isAbstractField() {
		return isFormField() || isFormUnityField();
	}
	
	public default boolean isFormField() {
		return dipDocElement() instanceof FormField;
	}

	public default boolean isFormUnityField() {
		return dipDocElement() instanceof FieldUnity;
	}
	
	public default boolean isTextFile() {
		if (isAbstractField()) {
			return true;
		}
		if (isPresentation()) {
			return ((IUnitPresentation) dipDocElement()).getUnitType().isTextType();
		}
		return false;
	}
	
	public default boolean isForm() {
		if (isAbstractField()) {
			return true;
		}
		if (isPresentation()) {
			return ((IUnitPresentation) dipDocElement()).getUnitType().isForm();
		}
		return false;
	}
	
	public default boolean isFormPresentation() {
		if (isPresentation()) {
			IUnitPresentation presentation = (IUnitPresentation) dipDocElement();
			return presentation.getPresentation() instanceof FormPresentation;
		}
		return false;
	}
	
	/**
	 *  Возвращает DipUnit или DipFolders
	 */
	public default IDipDocumentElement dipResourceElement() {
		if (dipDocElement() instanceof IUnitExtension) {
			return ((IUnitExtension)  dipDocElement()).getDipUnit();
		}
		return  dipDocElement();
	}
	
	/**
	 * true - для UnitPresentation и для не пустых Description
	 */
	public default boolean isHacContentElement() {
		return dipDocElement() instanceof IUnitPresentation
				|| (dipDocElement() instanceof IUnitDescription && !isEmptyDescription());
	}
	
	public default boolean isExists() {
		return dipResourceElement() != null
				&& dipResourceElement().resource() != null
				&& dipResourceElement().resource().exists();
	}
	

}
