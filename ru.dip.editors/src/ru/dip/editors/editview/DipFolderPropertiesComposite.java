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
package ru.dip.editors.editview;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.dip.core.model.DipFolder;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.editors.Messages;
import ru.dip.ui.table.ktable.KTableComposite;

public class DipFolderPropertiesComposite extends Composite {

	private DipFolder fFolder;
	private KTableComposite fTableComposite;
	private IFolderDirtyListener fListener;
	// control
	private StyledText fDescriptionText;
	private Button fNumerationButton;

	public DipFolderPropertiesComposite(Composite parent, DipFolder folder, KTableComposite tableComposite) {
		super(parent, SWT.NONE);
		fFolder = folder;
		fTableComposite = tableComposite;
		init();
		createContent();
	}
	
	private void init() {
		GridLayoutFactory.fillDefaults().extendedMargins(10, 5, 10, 10).spacing(0, 5).numColumns(2).equalWidth(false)
		.applyTo(this);
		setBackground(ColorProvider.WHITE);
	}

	private void createContent() {
		createFolderLabel();
		createDescriptionArea();
		createNumerationButton();
		createApplyButton();
	}
	
	private void createFolderLabel() {
		Label folderName = new Label(this, SWT.NONE);
		folderName.setText(fFolder.name() + Messages.DipFolderPropertiesComposite_0);
		folderName.setFont(FontManager.bold14Font);
		GridDataFactory.generate(folderName, 4, 1);
	}
	
	private void createDescriptionArea() {
		Label descriptionLabel = new Label(this, SWT.NONE);
		descriptionLabel.setText(Messages.DipFolderPropertiesComposite_DescriptionLabel);
		descriptionLabel.setFont(FontManager.boldTwelweFont);
		GridDataFactory.create(GridData.VERTICAL_ALIGN_BEGINNING).applyTo(descriptionLabel);

		fDescriptionText = new StyledText(this, SWT.BORDER | SWT.WRAP);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.minimumHeight = 50;
		fDescriptionText.setLayoutData(data);
		String description = fFolder.description();
		if (description != null) {
			fDescriptionText.setText(description);
		}
		fDescriptionText.setEditable(true);
		fDescriptionText.setMargins(5, 3, 3, 5);
		fDescriptionText.setFont(FontManager.font12);
		fDescriptionText.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (fListener != null) {
					fListener.setFolderDirty(true);
				}
			}
		});
	}
	
	private void createNumerationButton() {
		fNumerationButton = new Button(this, SWT.CHECK);
		fNumerationButton.setText(Messages.DipFolderPropertiesComposite_NumerationButton);
		GridDataFactory.generate(fNumerationButton, 4, 1);
		fNumerationButton.setSelection(fFolder.isActiveNumeration());
		fNumerationButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fListener != null) {
					fListener.setFolderDirty(true);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});

	}
	
	private void createApplyButton() {
		Button button = new Button(this, SWT.PUSH);
		button.setText(Messages.DipFolderPropertiesComposite_ApplyButton);
		GridDataFactory.create(GridData.HORIZONTAL_ALIGN_END).span(4, 1).applyTo(button);
		button.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				save();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
			
		});		
	}

	public void setDirtyListener(IFolderDirtyListener listener) {
		fListener = listener;
	}
	
	public void save() {
		String newDescription = fDescriptionText.getText().trim();
		if (newDescription == null || newDescription.isEmpty()){
			fFolder.removeDescription();
		} else {
			fFolder.updateDescription(newDescription);
		}
		fFolder.setActiveNumeration(fNumerationButton.getSelection());
		if (fListener != null) {
			fListener.setFolderDirty(false);
		}
		//fTableComposite.viewer().refresh();
		fTableComposite.asyncRefreshTree();
		fTableComposite.editor().doSave(null);
	}

}
