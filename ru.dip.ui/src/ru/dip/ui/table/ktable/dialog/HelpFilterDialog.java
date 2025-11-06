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
package ru.dip.ui.table.ktable.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.utilities.ui.LayoutManager;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontManager;

public class HelpFilterDialog {

	private Point fLocation;
	private Shell fShell;
	private Shell fParent;
	private Color fBachground = new Color(Display.getDefault(), new RGB(238, 232, 170));

	public HelpFilterDialog(Shell parent) {	
		fParent =  parent;
	}

	public void createShell() {
		fShell = new Shell(fParent, SWT.ON_TOP | SWT.TOOL | SWT.NO_FOCUS);
		fShell.setLocation(fLocation);
		fShell.setLayout(new GridLayout());
		createContentArea();
		fShell.pack();	
		fShell.setBackground(fBachground);
		fShell.setVisible(true);
	}
	
	protected void createContentArea() {
		Composite composite = new Composite(fShell, SWT.NONE);
		composite.setLayout(LayoutManager.notIndtentLayout());
		composite.setLayoutData(new GridData());
		composite.setBackground(fBachground);
		Label label = new Label(composite, SWT.NONE);
		label.setText("Фильтры");
		label.setFont(FontManager.boldFont);
		
		StyledText extension = new StyledText(composite, SWT.READ_ONLY);
		extension.setText("ext == \"расширение\" (файлы с указанным расширением)");
		extension.setBackground(fBachground);
		extension.setStyleRange(new StyleRange(0, 3, ColorProvider.MAGENTA, null));
		extension.setStyleRange(new StyleRange(4, 2, null, null, SWT.BOLD));
		extension.setStyleRange(new StyleRange(7, 12, ColorProvider.BLUE, null));
		
		StyledText  path = new StyledText(composite, SWT.READ_ONLY);
		path.setText("path == \"путь_до_каталога\" (файлы из указанного каталога, путь задаётся относительно проекта)");
		path.setBackground(fBachground);
		path.setStyleRange(new StyleRange(0, 4, ColorProvider.MAGENTA, null));
		path.setStyleRange(new StyleRange(5, 2, null, null, SWT.BOLD));
		path.setStyleRange(new StyleRange(9, 18, ColorProvider.BLUE, null));
		
		StyledText  folderPath = new StyledText(composite, SWT.READ_ONLY);
		folderPath.setText("path == \"путь_до_каталога/*\" (файлы из указанного каталога и вложенных каталогов)");
		folderPath.setBackground(fBachground);
		folderPath.setStyleRange(new StyleRange(0, 4, ColorProvider.MAGENTA, null));
		folderPath.setStyleRange(new StyleRange(5, 2, null, null, SWT.BOLD));
		folderPath.setStyleRange(new StyleRange(8, 20, ColorProvider.BLUE, null));
		
		StyledText  hasText = new StyledText(composite, SWT.READ_ONLY);
		hasText.setText("text == \"какой-то текст\" (файлы которые содержат указанный текст)");
		hasText.setBackground(fBachground);
		hasText.setStyleRange(new StyleRange(0, 4, ColorProvider.MAGENTA, null));
		hasText.setStyleRange(new StyleRange(5, 2, null, null, SWT.BOLD));
		hasText.setStyleRange(new StyleRange(8, 16, ColorProvider.BLUE, null));
		
		StyledText  hasCaseText = new StyledText(composite, SWT.READ_ONLY);
		hasCaseText.setText("case_text == \"какой-то текст\" (файлы которые содержат указанный текст с учетом регистра)");
		hasCaseText.setBackground(fBachground);
		hasCaseText.setStyleRange(new StyleRange(0, 9, ColorProvider.MAGENTA, null));
		hasCaseText.setStyleRange(new StyleRange(10, 2, null, null, SWT.BOLD));
		hasCaseText.setStyleRange(new StyleRange(13, 16, ColorProvider.BLUE, null));
		
		StyledText  hasWord = new StyledText(composite, SWT.READ_ONLY);
		hasWord.setText("word == \"слово\" (файлы которые содержат указанное слово)");
		hasWord.setBackground(fBachground);
		hasWord.setStyleRange(new StyleRange(0, 4, ColorProvider.MAGENTA, null));
		hasWord.setStyleRange(new StyleRange(5, 2, null, null, SWT.BOLD));
		hasWord.setStyleRange(new StyleRange(8, 7, ColorProvider.BLUE, null));
		
		StyledText  hasCaseWord = new StyledText(composite, SWT.READ_ONLY);
		hasCaseWord.setText("case_word == \"слово\" (файлы которые содержат указанное слово с учетом регистра)");
		hasCaseWord.setBackground(fBachground);
		hasCaseWord.setStyleRange(new StyleRange(0, 9, ColorProvider.MAGENTA, null));
		hasCaseWord.setStyleRange(new StyleRange(10, 2, null, null, SWT.BOLD));
		hasCaseWord.setStyleRange(new StyleRange(13, 7, ColorProvider.BLUE, null));
		
		StyledText  disableObjs = new StyledText(composite, SWT.READ_ONLY);
		disableObjs.setText("enabled == \"true\" (false) (только неотключенные объекты)");
		disableObjs.setBackground(fBachground);
		disableObjs.setStyleRange(new StyleRange(0, 7, ColorProvider.MAGENTA, null));
		disableObjs.setStyleRange(new StyleRange(8, 2, null, null, SWT.BOLD));
		disableObjs.setStyleRange(new StyleRange(11, 6, ColorProvider.BLUE, null));
		
		StyledText  versionValue = new StyledText(composite, SWT.READ_ONLY);
		versionValue.setText("version == \"hash\" (tag, HEAD, HEAD~2) (файлы с указанной версией (последние изменения были сделаны в указанной версии))");
		versionValue.setBackground(fBachground);
		versionValue.setStyleRange(new StyleRange(0, 7, ColorProvider.MAGENTA, null));
		versionValue.setStyleRange(new StyleRange(8, 2, null, null, SWT.BOLD));
		versionValue.setStyleRange(new StyleRange(11, 6, ColorProvider.BLUE, null));
		
		StyledText  formValue = new StyledText(composite, SWT.READ_ONLY);
		formValue.setText("расширение_формы.id_поля == \"значение\" (формы с указанным расширением и значением поля)");
		formValue.setBackground(fBachground);
		formValue.setStyleRange(new StyleRange(0, 24, ColorProvider.MAGENTA, null));
		formValue.setStyleRange(new StyleRange(25, 2, null, null, SWT.BOLD));
		formValue.setStyleRange(new StyleRange(28, 10, ColorProvider.BLUE, null));
		
		new Label(composite, SWT.NONE);
		
		StyledText  usedSigns = new StyledText(composite, SWT.READ_ONLY);
		usedSigns.setText("Используемые знаки: ==, !=, >, <, >=, <=  (знаки больше-меньше используются для коммита(version)\n и полей форм типа combo, check, radio)");
		usedSigns.setBackground(fBachground);
		usedSigns.setStyleRange(new StyleRange(20, 2, null, null, SWT.BOLD));
		usedSigns.setStyleRange(new StyleRange(24, 2, null, null, SWT.BOLD));
		usedSigns.setStyleRange(new StyleRange(28, 1, null, null, SWT.BOLD));
		usedSigns.setStyleRange(new StyleRange(31, 1, null, null, SWT.BOLD));
		usedSigns.setStyleRange(new StyleRange(34, 2, null, null, SWT.BOLD));
		usedSigns.setStyleRange(new StyleRange(38, 2, null, null, SWT.BOLD));


		StyledText  canBeCombined = new StyledText(composite, SWT.READ_ONLY);
		canBeCombined.setText("Фильтры могут быть объединены при помощи знаков:  &&, and, ||, or, ()");
		canBeCombined.setBackground(fBachground);
		canBeCombined.setStyleRange(new StyleRange(50, 2, null, null, SWT.BOLD));
		canBeCombined.setStyleRange(new StyleRange(54, 3, null, null, SWT.BOLD));
		canBeCombined.setStyleRange(new StyleRange(59, 2, null, null, SWT.BOLD));
		canBeCombined.setStyleRange(new StyleRange(63, 2, null, null, SWT.BOLD));
		canBeCombined.setStyleRange(new StyleRange(67, 2, null, null, SWT.BOLD));

		
		new Label(composite, SWT.NONE);
		Label exampleLabel = new Label(composite, SWT.NONE);
		exampleLabel.setFont(FontManager.boldFont);
		exampleLabel.setText("Пример: ");
		
		StyledText  example = new StyledText(composite, SWT.READ_ONLY);
		example.setText("(ext == md) || (req.status != \"принято\" && path == section2)");
		example.setBackground(fBachground);
		example.setStyleRange(new StyleRange(0, 1, null, null, SWT.BOLD));
		example.setStyleRange(new StyleRange(1, 3, ColorProvider.MAGENTA, null));
		example.setStyleRange(new StyleRange(5, 2, null, null, SWT.BOLD));
		example.setStyleRange(new StyleRange(8, 2, ColorProvider.BLUE, null));
		example.setStyleRange(new StyleRange(10, 6, null, null, SWT.BOLD));
		example.setStyleRange(new StyleRange(16, 10, ColorProvider.MAGENTA, null));
		example.setStyleRange(new StyleRange(27, 2, null, null, SWT.BOLD));
		example.setStyleRange(new StyleRange(30, 9, ColorProvider.BLUE, null));
		example.setStyleRange(new StyleRange(40, 2, null, null, SWT.BOLD));
		example.setStyleRange(new StyleRange(43, 4, ColorProvider.MAGENTA, null));
		example.setStyleRange(new StyleRange(48, 2, null, null, SWT.BOLD));
		example.setStyleRange(new StyleRange(50, 8, ColorProvider.BLUE, null));
		example.setStyleRange(new StyleRange(59, 1, null, null, SWT.BOLD));	
	}
	
	public boolean isOpen() {
		return !fShell.isDisposed() &&fShell.isVisible();
	}
	
	public void close() {
		fShell.close();
		fBachground.dispose();
	}
	
	public void setLocation(Point location) {
		fLocation = location;
	}
}
