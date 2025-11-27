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
package ru.dip.core.unit.factory;

import java.util.HashMap;
import java.util.Map;

import ru.dip.core.unit.ChangeLogPresentation;
import ru.dip.core.unit.CsvUnitPresentation;
import ru.dip.core.unit.DiaPresentation;
import ru.dip.core.unit.DotPresentation;
import ru.dip.core.unit.GlossaryPresentation;
import ru.dip.core.unit.HtmlImagePresentation;
import ru.dip.core.unit.HtmlUnitPresentation;
import ru.dip.core.unit.ImagePresentation;
import ru.dip.core.unit.PagebreakPresentation;
import ru.dip.core.unit.PlantUmlPresentation;
import ru.dip.core.unit.ReportRefPresentation;
import ru.dip.core.unit.TextPresentation;
import ru.dip.core.unit.TocRefPresentation;
import ru.dip.core.unit.UnitType;
import ru.dip.core.unit.form.FormPresentation;
import ru.dip.core.unit.md.MarkDownPresentation;
import ru.dip.core.unit.md.SubMarkdownPresentation;

public class PresentationFactoryProvider {

	private static final PresentationFactoryProvider instance = new PresentationFactoryProvider();

	private Map<UnitType, IPresentationFactory> fFactoryByType = new HashMap<>();

	private PresentationFactoryProvider() {
		registerDefaultFactories();
	}

	private void registerDefaultFactories() {
		fFactoryByType.put(UnitType.IMAGE, ImagePresentation::new);
		fFactoryByType.put(UnitType.TEXT, TextPresentation::new);
		fFactoryByType.put(UnitType.MARKDOWN, MarkDownPresentation::new);
		fFactoryByType.put(UnitType.SUBMARKDOWN, SubMarkdownPresentation::new);
		fFactoryByType.put(UnitType.HTML, HtmlUnitPresentation::new);
		fFactoryByType.put(UnitType.CSV, CsvUnitPresentation::new);
		fFactoryByType.put(UnitType.HTML_IMAGE, HtmlImagePresentation::new);
		fFactoryByType.put(UnitType.UML, PlantUmlPresentation::new);
		fFactoryByType.put(UnitType.DOT, DotPresentation::new);
		fFactoryByType.put(UnitType.DIA, DiaPresentation::new);
		fFactoryByType.put(UnitType.FORM, FormPresentation::new);
		fFactoryByType.put(UnitType.REPROT_REF, ReportRefPresentation::new);
		fFactoryByType.put(UnitType.TOC_REF, TocRefPresentation::new);
		fFactoryByType.put(UnitType.GLOS_REF, GlossaryPresentation::new);
		fFactoryByType.put(UnitType.CHANGELOG, ChangeLogPresentation::new);
		fFactoryByType.put(UnitType.PAGEBREAK, PagebreakPresentation::new);
		fFactoryByType.put(UnitType.JSON, TextPresentation::new);

	}

	public static IPresentationFactory getFactory(UnitType type) {
		if (type == null) {
			return null;
		}
		return instance.fFactoryByType.get(type);
	}

	public static void registerFactory(UnitType type, IPresentationFactory factory) {
		instance.fFactoryByType.put(type, factory);
	}

}
