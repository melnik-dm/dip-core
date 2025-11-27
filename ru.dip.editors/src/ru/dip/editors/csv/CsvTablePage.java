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
package ru.dip.editors.csv;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.csv.model.CsvModel;
import ru.dip.core.model.DipUnit;
import ru.dip.core.model.interfaces.IDipElement;
import ru.dip.core.unit.TextPresentation;
import ru.dip.core.utilities.DipUtilities;
import ru.dip.core.utilities.ui.image.ImageUtilities;
import ru.dip.editors.Messages;
import ru.dip.ui.table.table.TableSettings;

public class CsvTablePage extends FormPage {

	private CsvMultiEditor fMultiEditor;
	private Composite fBody;
	private Composite fImageComposite;
	private ScrolledForm fForm ;
	
	public CsvTablePage(CsvMultiEditor editor) {
		super(editor, Messages.CsvTablePage_TablePageName, Messages.CsvTablePage_TablePageName);
		fMultiEditor = editor;
	}
	
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		fForm = managedForm.getForm();
		fBody = fForm.getBody();	
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		fBody.setLayout(layout);
		fBody.setLayoutData(new GridData(GridData.FILL_BOTH));
		updateContent();	
	}
		
	public void updateContent(){		
		if (fImageComposite != null){
			fImageComposite.dispose();
		}
		fImageComposite = new Composite(fBody, SWT.NONE);
		fImageComposite.setLayout(new GridLayout());
		fImageComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		int width = fForm.getBounds().width;		
		Label label = new Label(fImageComposite, SWT.NONE);
		Image image = getHtmlImage(width);
		if (image != null){
			label.setImage(image);
		}
		fImageComposite.layout();
		fBody.layout();		
		
		fImageComposite.addControlListener(new ControlListener() {

			@Override
			public void controlResized(ControlEvent e) {
				updateContent();
			}
			
			@Override
			public void controlMoved(ControlEvent e) {
				
			}
		});
	}

	private String getHtml(int width) {
		String text = fMultiEditor.getText();		
		text = prepareText(text);
		CsvModel csvModel = new CsvModel();
		csvModel.readModel(text);
		return csvModel.getHtmlText(width, TableSettings.isCsvColumnWidthByContent());
	}
	
	private String prepareText(String original){		
		IDipElement element = DipUtilities.findElement(fMultiEditor.getFile());
		if (element instanceof DipUnit) {
			DipUnit unit = (DipUnit) element;
			return TextPresentation.prepareText(original, unit);

		}
		return original;
	}
	
	public void update(){
		CsvTextEditor textEditor = fMultiEditor.getCsvTextEditor();
		if (textEditor.isTableDirty()){
			updateContent();
			textEditor.setTableDirty(false);
		}	
	}
	
	public Image getHtmlImage(int width) {
		String html = getHtml(width);
		if (html != null){			
			try {
				Image image = ImageUtilities.createImageFromHtml(html, fMultiEditor.getFile());
				return image;
			} catch (Exception e) {
				DipCorePlugin.logError(e, Messages.CsvTablePage_CreateHtmlImageErrorMessage);
				e.printStackTrace();
			}
		}
		return null;
	}
	
}
