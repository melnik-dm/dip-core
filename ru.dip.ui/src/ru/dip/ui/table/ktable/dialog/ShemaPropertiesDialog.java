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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IManagedForm;

import ru.dip.core.model.DipProject;
import ru.dip.core.utilities.ui.LayoutManager;
import ru.dip.core.utilities.ui.swt.ColorProvider;
import ru.dip.core.utilities.ui.swt.FontManager;
import ru.dip.ui.table.dialog.form.DialogFieldProperties;
import ru.dip.ui.table.dialog.form.DialogFormProperties;
import ru.dip.ui.table.dialog.form.FieldPropertiesControl;

public class ShemaPropertiesDialog extends FormDialog {

	private List<DialogFormProperties> fFormProperties = new ArrayList<>();
	private DipProject fProject;
	private Composite fDetailsComposite;
	private Composite fComposite;
	
	public ShemaPropertiesDialog(Shell parentShell) {
		super(parentShell);
	}
	

	public ShemaPropertiesDialog(Shell parentShell, DipProject project) {
		super(parentShell);
		fProject = project;
		fFormProperties = fProject.getSchemaModel().getFormShowProperties().stream().map(DialogFormProperties::new)
				.collect(Collectors.toList());
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Schema Display Settings");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 600);
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		fComposite = mform.getForm().getBody();
		fComposite.setLayout(new GridLayout(2, false));
		fComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		fComposite.setBackground(ColorProvider.WHITE);

		Label label = new Label(fComposite, SWT.NONE);
		label.setText("Схема");
		label.setFont(FontManager.boldTwelweFont);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		label.setForeground(ColorProvider.BLACK);

		Label label2 = new Label(fComposite, SWT.NONE);
		label2.setText("Показывать поля: ");
		label2.setForeground(ColorProvider.BLACK);
		label2.setFont(FontManager.boldTwelweFont);
		label2.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

		createSchemasViewer(fComposite);
		updateDetailsComposite(fComposite, null);
		fComposite.layout();
	}

	protected TreeViewer createSchemasViewer(Composite parent) {
		Tree tree = new Tree(parent, SWT.BORDER);
		tree.setLayout(new GridLayout());
		tree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		TreeViewer viewer = new TreeViewer(tree);
		viewer.setContentProvider(getContentProvider());
		viewer.setLabelProvider(getLabelProvider());
		viewer.addSelectionChangedListener(getSelectionChangeListener());
		viewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
		viewer.setInput(getInput());
		return viewer;
	}
	
	protected ITreeContentProvider getContentProvider() {
		return new ITreeContentProvider() {

			@Override
			public boolean hasChildren(Object element) {
				return false;
			}

			@Override
			public Object getParent(Object element) {
				return null;
			}

			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof DipProject) {
					return fFormProperties.toArray();
				}
				return null;
			}

			@Override
			public Object[] getChildren(Object parentElement) {
				return null;
			}
		};
	}
	
	protected LabelProvider getLabelProvider() {
		return new LabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof DialogFormProperties) {
					return ((DialogFormProperties) element).name();
				}

				return super.getText(element);
			}
		};
	}
	
	protected ISelectionChangedListener getSelectionChangeListener() {
		return new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = event.getStructuredSelection();
				if (selection.size() == 1) {
					Object obj = selection.getFirstElement();
					if (obj instanceof DialogFormProperties) {

						updateDetailsComposite(fComposite, (DialogFormProperties) obj);
						fComposite.layout();
					}
				}

			}
		};
	}
	
	protected Object getInput() {
		return fProject;
	}
	
	private void updateDetailsComposite(Composite parent, DialogFormProperties setting) {
		if (fDetailsComposite != null && !fDetailsComposite.isDisposed()) {
			fDetailsComposite.dispose();
		}
		fDetailsComposite = new Composite(parent, SWT.BORDER);
		fDetailsComposite.setLayout(LayoutManager.notIndtentLayout(3, false));
		fDetailsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		fDetailsComposite.setBackground(ColorProvider.WHITE);						
		if (setting != null) {
			
			Composite composite = new Composite(fDetailsComposite, SWT.BORDER);
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			Label label = new Label(composite, SWT.CENTER);
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			label.setText("Имя поля");
			label.setFont(FontManager.boldFont);
			
			composite = new Composite(fDetailsComposite, SWT.BORDER);
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(composite, SWT.CENTER);
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			label.setText("Поле");
			label.setFont(FontManager.boldFont);
			
			composite = new Composite(fDetailsComposite, SWT.BORDER);
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			label = new Label(composite, SWT.CENTER);
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			label.setText("Заголовок");
			label.setFont(FontManager.boldFont);
									
			for (DialogFieldProperties fieldSettings : setting.fieldProperteis()) {
				new FieldPropertiesControl(fDetailsComposite, fieldSettings);
			}
		}
		fDetailsComposite.layout();
	}

	@Override
	protected void okPressed() {
		fFormProperties.forEach(settings -> settings.saveFields(fProject.getProject()));
		super.okPressed();
	}
}
