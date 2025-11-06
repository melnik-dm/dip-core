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
package ru.dip.core.report.model.condition;

import java.util.List;

import org.eclipse.jgit.lib.Repository;

import ru.dip.core.form.model.Field;
import ru.dip.core.model.interfaces.ISchemaContainer;
import ru.dip.core.report.model.condition.FieldName.FieldType;
import ru.dip.core.schema.Schema;
import ru.dip.core.utilities.GITUtilities;
import ru.dip.core.utilities.git.CommitInfo;

public class SimpleCondition implements ConditionPart {
	
	private FieldName fField;
	private Sign fSign;
	private Value fValue;
	
	public SimpleCondition(FieldName field, Sign sign, Value value) {
		fField = field;
		fSign = sign;
		fValue = value;
	}
	
	public String validate(List<ISchemaContainer> schemaContainers, List<Repository> repositories) {
		FieldType type = fField.type();
		if (type == null) {
			// неверное значение field
			return "Некорректное значение переменной " + fField.getValue();
		}		
		if (type == FieldType.FORM_FIELD) {
			if (schemaContainers == null || schemaContainers.isEmpty()) {
				return "SchemaContainer  не найден.";
			}
			
			String[] values = fField.getValue().split("\\.");
			
			// проверить наличие схемы
			String error = null;
			for (ISchemaContainer schemaContainer: schemaContainers) {
				Schema schema = schemaContainer.getSchema(values[0]);
				if (schema == null) {
					error =  "Отсутствует схема для расширения " + values[0];
				}
				// проверить наличие поля			
				Field schemaField = schema.getFormModel().findField(values[1]);
				if (schemaField == null) {
					error = "Форма ." + values[0] + " не содержит поле \"" + values[1] + "\"";
				} else {
					return null;
				}
			}
			return error;
		} else if (type == FieldType.VERSION) {
			if (!fSign.mayCompare()) {
				return "Некорректный знак " + fSign + " для переменной " + fField.getValue();
			}
			
			if (repositories == null || repositories.isEmpty()) {
				return "Не найдены репозитории для коммита" + fValue.getValue();
			}
			
			for (Repository repository : repositories) {
				CommitInfo commitInfo = GITUtilities.getCommitInfoByAll(repository, fValue.getValue());
				if (commitInfo != null) {
					fValue.setData(commitInfo);
					return null;
				}
			}	
			return "Не найден коммит: " + fValue.getValue();
		} else {
			// проверить знак
			if (!fSign.mayEquals()) {
				return "Некорректный знак " + fSign + " для переменной " + fField.getValue();
			}
			
			if (type == FieldType.ENABLED) {
				if (!fValue.isBooleanValue()) {
					return "Значение для переменной enabled должно быть true или false.";
				}
			}
		}		
		return null;
	}
		
	//=================================
	// getters & setters
	
	public FieldName fieldName() {
		return fField;
	}
	
	public Sign sign() {
		return fSign;
	}
	
	public Value value() {
		return fValue;
	}
	
	@Override
	public String toString() {
		return fField + " "  + fSign + " " + fValue;
	}
}
