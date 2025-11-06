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
package ru.dip.core.schema;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import ru.dip.core.form.model.Field;

public class FormShowProperties {
	
	private Schema fSchema;
	private List<FieldShowProperties> fFieldProperties = new ArrayList<>();
	
	public FormShowProperties(Schema schema){
		fSchema = schema;
	}
	
	public void createFieldProperties() {
		for (Field field: fSchema.getFormModel().getFields()) {
			FieldShowProperties fieldSetting = new FieldShowProperties(field, true, true);
			fFieldProperties.add(fieldSetting);
		}
	}
	
	public void loadFieldsProperties(IProject project) {
		QualifiedName qualName = createQualifiedName();
		try {
			String value = project.getPersistentProperty(qualName);
			if (value != null) {
				String[] lines = value.split("\n");
				for (int i = 0; i + 2 < lines.length; i += 3) {
					String name = lines[i];
					FieldShowProperties fieldSettings = findFieldProperties(name);
					if (fieldSettings != null) {
						boolean enable = Boolean.parseBoolean(lines[i+1]);
						fieldSettings.setEnable(enable);
						boolean showTitle = Boolean.parseBoolean(lines[i+2]);
						fieldSettings.setShowTitle(showTitle);
					}	
				}								
			}			
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public void saveFields(IProject project) {
		QualifiedName qualName = createQualifiedName();
		try {
			String content = toPreferencesString();
			project.setPersistentProperty(qualName, content);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public QualifiedName createQualifiedName() {
		return new QualifiedName("Schema:" + fSchema.getName(), "0");
	}
			
	public String toPreferencesString() {
		StringBuilder builder = new StringBuilder();
		for (FieldShowProperties fieldSettings: fFieldProperties) {
			builder.append(fieldSettings.field().getName());
			builder.append("\n");
			builder.append(fieldSettings.isEnable());
			builder.append("\n");
			builder.append(fieldSettings.isShowTitle());
			builder.append("\n");
		}
		return builder.toString();
	}
	
	public FieldShowProperties findFieldProperties(String name) {
		for (FieldShowProperties settings: fFieldProperties) {
			if(name.equals(settings.field().getName())) {
				return settings;
			}
		}
		return null;
	}
	
	public String name() {
		return fSchema.getName();
	}
	
	public List<FieldShowProperties> fieldSettings(){
		return fFieldProperties;
	}
	
}
