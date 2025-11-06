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
package ru.dip.editors.formeditor.fieldpage;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ru.dip.core.form.model.CheckField;
import ru.dip.core.form.model.ComboField;
import ru.dip.core.form.model.Field;
import ru.dip.core.form.model.Item;
import ru.dip.core.form.model.RadioField;
import ru.dip.core.form.model.TextField;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.editors.Messages;
import ru.dip.editors.formeditor.model.FormModel;

public class HelpFormDialog extends Dialog {

	private FormModel fModel; 

	public HelpFormDialog(FormModel model, Shell parent) {
		super(parent);
		fModel = model;
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setText(Messages.HelpFormDialog_FormHelpTitle + fModel.getName());
		super.configureShell(newShell);
	}
	
	@Override
	protected Point getInitialSize() {
		return new Point(600, 800);
		
	}

	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
	
	@Override
	protected Control createContents(Composite parent) {
		return createDialogArea(parent);	
	}
		
	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setLayout(new FillLayout());
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.WRAP);		
	    Composite child = new Composite(sc, SWT.WRAP);
	    child.setLayout(new GridLayout());
	    //child.setLayoutData(new GridData(GridData.FILL_BOTH));
	    child.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		createContent2(child);
		sc.setContent(child);
	    sc.setExpandHorizontal(true);	   
	    sc.setExpandVertical(true);	   
	    sc.addListener( SWT.Resize, event -> {
	    	int width = sc.getClientArea().width;
	    	@SuppressWarnings("unused")
			int height = sc.getClientArea().height;
	    	fComposite.pack(true);	    	
	    	child.pack(true);
	    	sc.setMinSize(parent.computeSize( width, /*SWT.DEFAULT*/fComposite.computeSize(600, -1).y));
	    } );		    
		return parent;
	}
	
	 Composite fComposite;
	
	protected Control createContent2(Composite parent){ 
	    fComposite = new Composite(parent, SWT.BORDER | SWT.WRAP);
	    fComposite.setLayout(new GridLayout());
	    fComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	    
		String modelHint = fModel.getHint();
		
		if (modelHint != null && !modelHint.isEmpty()){
			Label modelHintLabel = new Label(fComposite, SWT.WRAP);
			modelHintLabel.setText(" " + modelHint); //$NON-NLS-1$
			modelHintLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			modelHintLabel.setForeground(ColorProvider.BLACK);
		}
		for (Field field: fModel.getFields()){
			createFieldComposite(fComposite, field);
		}	    
	    return fComposite;
	}

	private void createFieldComposite(Composite parent, Field field){
		Composite composite = new Composite(parent, SWT.WRAP | SWT.BORDER);
		GridLayout layout = new GridLayout();
		//layout.marginLeft = 5;
		//layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		Label titleLabel = new Label(composite, SWT.NONE);
		titleLabel.setText(field.getTitle() + " (" + getType(field) + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		titleLabel.setFont(FontManager.boldFont);
		titleLabel.setForeground(ColorProvider.BLACK);

		String hint = field.getHint();
		if (hint != null && !hint.isEmpty()){
			Label hintLabel = new Label(composite, SWT.WRAP);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			//gd.horizontalSpan = 2;
			hintLabel.setLayoutData(gd);
			hintLabel.setText(/*" " + */hint);
			hintLabel.setForeground(ColorProvider.BLACK);
		}
		
		if (!(field instanceof TextField)){
			Item[] items = getItems(field);
			if (items == null || items.length == 0){
				return;
			}		
			Label valuesLabel = new Label(composite, SWT.NONE);
			valuesLabel.setText(Messages.HelpFormDialog_PerhapsVlauesLabel);
			valuesLabel.setForeground(ColorProvider.SELECT);	
			GridData gd = new GridData();
			gd.verticalIndent = 4;
			//gd.horizontalSpan = 2;
			valuesLabel.setLayoutData(gd);
			for (Item item: items){
				Composite itemComposite = new Composite(composite, SWT.WRAP);
				GridLayout itemLayout = new GridLayout(3,false);
				layout.verticalSpacing = 3;
				//layout.marginHeight = 1;	
				itemComposite.setLayout(itemLayout);
				itemComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				Label imageLabel = new Label(itemComposite, SWT.NONE);
				imageLabel.setText("➢"); //$NON-NLS-1$
				imageLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
				Label itemLabel = new Label(itemComposite, SWT.NONE);
				itemLabel.setText(item.getName());
				itemLabel.setForeground(ColorProvider.BLACK);
				itemLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
				Label hintItemLabel = new Label(itemComposite, SWT.WRAP);
				hintItemLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				String hintItem = item.getHint();
				//hint = hintItem.replaceAll("\\\\n", "\n");

				if (hintItem != null && !hintItem.isEmpty()){
					hintItemLabel.setText(" - " + hintItem); //$NON-NLS-1$
					hintItemLabel.setForeground(ColorProvider.BLACK);
					hintItemLabel.setFont(FontManager.italicFont);
				}				
			}			
		}
	}
	
	private String getType(Field field){
		if (field instanceof TextField){
			return Messages.HelpFormDialog_TextFieldLabel;
		} else if (field instanceof CheckField){
			return Messages.HelpFormDialog_CheckLabel;
		} else if (field instanceof ComboField){
			return Messages.HelpFormDialog_DropListLabel;
		} else if (field instanceof RadioField){
			return Messages.HelpFormDialog_RadioLabel;
		}
		return null;
	}

	private Item[] getItems(Field field){
		if (field instanceof ComboField){
			return ((ComboField) field).getItems();
		}
		if (field instanceof RadioField){
			return ((RadioField) field).getItems(); 
		}
		if (field instanceof CheckField){
			return ((CheckField) field).getItems();
		}
		return null;
	}
	
}
