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
package ru.dip.ui.imageview.interfaces;

import java.util.List;

import org.eclipse.swt.widgets.Table;

import ru.dip.core.model.interfaces.IDipUnit;
import ru.dip.core.model.interfaces.IDipUnitProvider;
import ru.dip.ui.imageview.ImageViewSelector;
import ru.dip.ui.imageview.ImagesCacheProvider;
import ru.dip.ui.imageview.ImagesFontProvider;
import ru.dip.ui.utilities.dip.DipUnitManager;

public interface IImageView extends IDipUnitProvider {

	void createComposite();

	int getHeight();

	int getWidth();

	Table getTable();

	IImageViewPreferences getPreferences();

	DipUnitManager getDipUnitManager();

	ImageViewSelector getImageSelector();

	List<IDipUnit> getUnits();

	ImagesFontProvider getFontProvider();

	ImagesCacheProvider getImageProvider();

}
